package com.miempresa.totalhealth.auth // Asegúrate que este sea el paquete

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // Importante para exponer como StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// Estados de la UI para autenticación y rol
sealed class AuthAndRoleUiState {
    object Idle : AuthAndRoleUiState()
    object AuthLoading : AuthAndRoleUiState()
    object RoleLoading : AuthAndRoleUiState()
    data class Authenticated(val user: FirebaseUser, val role: UserRole) : AuthAndRoleUiState()
    data class Error(val message: String) : AuthAndRoleUiState()
}

enum class UserRole {
    ADMIN, // Entrenador
    USER,  // Usuario normal
    UNKNOWN,
    LOADING_ROLE
}

data class UserProfile(
    val email: String? = null,
    val role: String = "USER", // Rol por defecto
    val createdAt: Date? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _authAndRoleUiState = MutableStateFlow<AuthAndRoleUiState>(AuthAndRoleUiState.Idle)
    val authAndRoleUiState: StateFlow<AuthAndRoleUiState> = _authAndRoleUiState.asStateFlow()

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        if (_authAndRoleUiState.value is AuthAndRoleUiState.Error) {
            _authAndRoleUiState.value = AuthAndRoleUiState.Idle
        }
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        if (_authAndRoleUiState.value is AuthAndRoleUiState.Error) {
            _authAndRoleUiState.value = AuthAndRoleUiState.Idle
        }
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun registerUser() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _authAndRoleUiState.value =
                AuthAndRoleUiState.Error("El email y la contraseña no pueden estar vacíos.")
            return
        }

        _authAndRoleUiState.value = AuthAndRoleUiState.AuthLoading
        Log.d("AuthViewModel", "Attempting to register user: ${email.value}")
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email.value.trim(), password.value.trim()).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    Log.d("AuthViewModel", "Firebase Auth SUCCEEDED for ${firebaseUser.email}. Attempting Firestore write.")
                    val userProfile = UserProfile(
                        email = firebaseUser.email,
                        role = "USER",
                        createdAt = Date()
                    )
                    try { // Bloque try-catch específico para la operación de Firestore
                        db.collection("users").document(firebaseUser.uid).set(userProfile).await()
                        Log.d("AuthViewModel", "Firestore write SUCCEEDED for user ${firebaseUser.uid}.")
                        _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER)
                        Log.d("AuthViewModel", "State set to Authenticated for ${firebaseUser.email}")
                    } catch (firestoreEx: Exception) {
                        Log.e("AuthViewModel", "Firestore write FAILED for user ${firebaseUser.uid}: ${firestoreEx.message}", firestoreEx)
                        // Aunque el auth funcionó, el perfil no se guardó. Considerar esto un error.
                        // Podrías querer borrar el usuario de Firebase Auth aquí o manejarlo de otra forma.
                        _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al guardar el perfil de usuario: ${firestoreEx.localizedMessage}")
                    }
                } else {
                    Log.e("AuthViewModel", "Firebase Auth FAILED (user is null) after createUserWithEmailAndPassword.")
                    _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al crear el usuario (resultado nulo de Firebase).")
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                Log.w("AuthViewModel", "Registration failed: Weak password.", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("La contraseña es demasiado débil (mínimo 6 caracteres).")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.w("AuthViewModel", "Registration failed: Invalid email format.", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("El formato del correo electrónico no es válido.")
            } catch (e: FirebaseAuthUserCollisionException) {
                Log.w("AuthViewModel", "Registration failed: Email already in use.", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Este correo electrónico ya está registrado.")
            } catch (e: Exception) { // Captura general para otros errores de Firebase Auth o inesperados
                Log.e("AuthViewModel", "Generic error during registration: ${e.message}", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error desconocido durante el registro: ${e.localizedMessage}")
            }
        }
    }

    fun loginUser() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _authAndRoleUiState.value =
                AuthAndRoleUiState.Error("El email y la contraseña no pueden estar vacíos.")
            return
        }

        _authAndRoleUiState.value = AuthAndRoleUiState.AuthLoading
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email.value.trim(), password.value.trim()).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    fetchUserRoleAndSetState(firebaseUser)
                } else {
                    _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al iniciar sesión (usuario nulo).")
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Credenciales inválidas. Verifica tu email y contraseña.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during login: ${e.message}", e)
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("Error desconocido durante el inicio de sesión: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchUserRoleAndSetState(firebaseUser: FirebaseUser) {
        _authAndRoleUiState.value = AuthAndRoleUiState.RoleLoading
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                var role = UserRole.USER
                if (userDoc.exists()) {
                    val roleString = userDoc.getString("role")
                    role = when (roleString?.uppercase()) {
                        "ADMIN", "ENTRENADOR" -> UserRole.ADMIN
                        "USER", "USUARIO" -> UserRole.USER
                        else -> {
                            Log.w("AuthViewModel", "Rol desconocido '$roleString' para ${firebaseUser.uid}. Asignando USER.")
                            UserRole.USER
                        }
                    }
                    Log.d("AuthViewModel", "User role fetched: $roleString, mapped to: $role")
                } else {
                    Log.w("AuthViewModel", "User document not found for ${firebaseUser.uid} during role fetch. Defaulting role to USER.")
                    role = UserRole.USER
                }
                _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, role)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user role for ${firebaseUser.uid}: ${e.message}", e)
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER)
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                auth.signOut()
                clearFields()
                _authAndRoleUiState.value = AuthAndRoleUiState.Idle
                Log.d("AuthViewModel", "User logged out. State set to Idle.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout: ${e.message}", e)
                clearFields()
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("Error al cerrar sesión: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        Log.d("AuthViewModel", "Resetting state from ${_authAndRoleUiState.value} to Idle.")
        _authAndRoleUiState.value = AuthAndRoleUiState.Idle
    }


    fun clearFields() {
        _email.value = ""
        _password.value = ""
        _passwordVisible.value = false
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
