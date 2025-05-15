package auth // Asegúrate que este sea el paquete

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
    // Puedes añadir más campos de tu tabla User aquí
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    // Esta es la propiedad que tu App.kt intentará usar
    private val _authAndRoleUiState = MutableStateFlow<AuthAndRoleUiState>(AuthAndRoleUiState.Idle)
    val authAndRoleUiState: StateFlow<AuthAndRoleUiState> = _authAndRoleUiState

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        if (_authAndRoleUiState.value is AuthAndRoleUiState.Error) {
            resetState()
        }
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        if (_authAndRoleUiState.value is AuthAndRoleUiState.Error) {
            resetState()
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
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email.value.trim(), password.value.trim()).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userProfile = UserProfile(
                        email = firebaseUser.email,
                        role = "USER",
                        createdAt = Date()
                    )
                    db.collection("users").document(firebaseUser.uid).set(userProfile).await()
                    Log.d("AuthViewModel", "User profile created in Firestore for ${firebaseUser.uid}")
                    // Después de registrar y crear perfil, el estado podría ser Authenticated con rol USER
                    _authAndRoleUiState.value =
                        AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER)
                } else {
                    _authAndRoleUiState.value =
                        AuthAndRoleUiState.Error("Error al crear el usuario.")
                }
                clearFields()
            } catch (e: FirebaseAuthWeakPasswordException) {
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("La contraseña es débil (mínimo 6 caracteres).")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("El formato del email no es válido.")
            } catch (e: FirebaseAuthUserCollisionException) {
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("Este email ya está registrado.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during registration: ${e.message}", e)
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("Error desconocido registro: ${e.localizedMessage}")
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
                    _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al iniciar sesión.")
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Credenciales inválidas.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during login: ${e.message}", e)
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("Error desconocido login: ${e.localizedMessage}")
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
                        else -> UserRole.USER
                    }
                    Log.d("AuthViewModel", "User role fetched: $roleString, mapped to: $role")
                } else {
                    Log.d("AuthViewModel", "User document not found for ${firebaseUser.uid}, defaulting role to USER.")
                }
                _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, role)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user role: ${e.message}", e)
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER) // Fallback
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _authAndRoleUiState.value = AuthAndRoleUiState.Idle
                clearFields()
            } catch (e: Exception) {
                _authAndRoleUiState.value =
                    AuthAndRoleUiState.Error("Error al cerrar sesión: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        if (_authAndRoleUiState.value is AuthAndRoleUiState.Error ||
            _authAndRoleUiState.value is AuthAndRoleUiState.Authenticated
        ) {
            _authAndRoleUiState.value = AuthAndRoleUiState.Idle
        }
    }

    fun clearFields() {
        _email.value = ""
        _password.value = ""
        _passwordVisible.value = false
    }

    fun getCurrentUser() = auth.currentUser
}
