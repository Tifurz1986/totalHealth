package com.miempresa.totalhealth.auth

import android.net.Uri
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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

// Estados de la UI para autenticación y rol (sin cambios)
sealed class AuthAndRoleUiState {
    object Idle : AuthAndRoleUiState()
    object AuthLoading : AuthAndRoleUiState()
    object RoleLoading : AuthAndRoleUiState()
    data class Authenticated(val user: FirebaseUser, val role: UserRole) : AuthAndRoleUiState()
    data class Error(val message: String) : AuthAndRoleUiState()
}

enum class UserRole {
    ADMIN,
    USER,
    UNKNOWN,
    LOADING_ROLE
}

// Modelo de datos para el perfil del usuario ACTUALIZADO con más campos
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    var name: String = "",
    var surname: String = "",
    var profilePictureUrl: String? = null,
    var age: Int? = null,            // NUEVO
    var sex: String = "",            // NUEVO
    var height: Int? = null,         // NUEVO (en cm)
    var weight: Double? = null,      // NUEVO (en kg)
    var activityLevel: String = "",  // NUEVO (Ej: Sedentario, Ligero, Moderado, Activo, Muy Activo)
    var healthGoals: String = "",    // NUEVO (Objetivos de salud del usuario)
    val role: String = "USER",
    val createdAt: Date? = null
)

// Estado para la UI de la carga/actualización del perfil (sin cambios)
sealed class UserProfileUiState {
    object Idle : UserProfileUiState()
    object Loading : UserProfileUiState()
    data class Success(val profile: UserProfile) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}


class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val storage = Firebase.storage

    private val _authAndRoleUiState = MutableStateFlow<AuthAndRoleUiState>(AuthAndRoleUiState.Idle)
    val authAndRoleUiState: StateFlow<AuthAndRoleUiState> = _authAndRoleUiState.asStateFlow()

    private val _userProfileUiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Idle)
    val userProfileUiState: StateFlow<UserProfileUiState> = _userProfileUiState.asStateFlow()

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    init {
        auth.currentUser?.let {
            // No llamar a loadUserProfile aquí directamente para evitar doble carga si fetchUserRoleAndSetState ya lo hace.
            // fetchUserRoleAndSetState se encargará de cargar el perfil completo.
            if (_authAndRoleUiState.value is AuthAndRoleUiState.Idle) { // Solo si no se ha determinado el estado de auth aún
                fetchUserRoleAndSetState(it)
            } else if (_userProfileUiState.value is UserProfileUiState.Idle && it.uid.isNotBlank()){
                // Si el estado de auth ya está determinado pero el perfil no se ha cargado
                loadUserProfile(it.uid)
            }
        }
    }

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
            _authAndRoleUiState.value = AuthAndRoleUiState.Error("El email y la contraseña no pueden estar vacíos.")
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
                    val userProfile = UserProfile( // Inicializar con nuevos campos
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = "",
                        surname = "",
                        profilePictureUrl = null,
                        age = null, // Inicializar a null o un valor por defecto
                        sex = "",
                        height = null,
                        weight = null,
                        activityLevel = "",
                        healthGoals = "",
                        role = "USER",
                        createdAt = Date()
                    )
                    try {
                        db.collection("users").document(firebaseUser.uid).set(userProfile).await()
                        Log.d("AuthViewModel", "Firestore write SUCCEEDED for user ${firebaseUser.uid}.")
                        _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER)
                        // También inicializar el estado del perfil localmente después del registro
                        _userProfileUiState.value = UserProfileUiState.Success(userProfile)
                        Log.d("AuthViewModel", "State set to Authenticated and UserProfile loaded for ${firebaseUser.email}")
                    } catch (firestoreEx: Exception) {
                        Log.e("AuthViewModel", "Firestore write FAILED for user ${firebaseUser.uid}: ${firestoreEx.message}", firestoreEx)
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
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Generic error during registration: ${e.message}", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error desconocido durante el registro: ${e.localizedMessage}")
            }
        }
    }

    fun loginUser() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _authAndRoleUiState.value = AuthAndRoleUiState.Error("El email y la contraseña no pueden estar vacíos.")
            return
        }
        _authAndRoleUiState.value = AuthAndRoleUiState.AuthLoading
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email.value.trim(), password.value.trim()).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    fetchUserRoleAndSetState(firebaseUser) // Esto cargará el rol y el perfil completo
                } else {
                    _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al iniciar sesión (usuario nulo).")
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Credenciales inválidas. Verifica tu email y contraseña.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during login: ${e.message}", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error desconocido durante el inicio de sesión: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchUserRoleAndSetState(firebaseUser: FirebaseUser) {
        _authAndRoleUiState.value = AuthAndRoleUiState.RoleLoading
        _userProfileUiState.value = UserProfileUiState.Loading
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                var role = UserRole.USER
                var userProfile: UserProfile? = null

                if (userDoc.exists()) {
                    userProfile = userDoc.toObject(UserProfile::class.java)
                    // Asegurar que los campos nuevos tengan valores por defecto si son null desde Firestore
                    userProfile = userProfile?.copy(
                        name = userProfile.name ?: "",
                        surname = userProfile.surname ?: "",
                        sex = userProfile.sex ?: "",
                        activityLevel = userProfile.activityLevel ?: "",
                        healthGoals = userProfile.healthGoals ?: ""
                        // age, height, weight ya son nullable
                    )

                    val roleString = userProfile?.role ?: "USER"
                    role = when (roleString.uppercase()) {
                        "ADMIN", "ENTRENADOR" -> UserRole.ADMIN
                        "USER", "USUARIO" -> UserRole.USER
                        else -> UserRole.USER
                    }
                    Log.d("AuthViewModel", "User role from profile: $roleString, mapped to: $role")
                    if (userProfile != null) {
                        _userProfileUiState.value = UserProfileUiState.Success(userProfile)
                        Log.d("AuthViewModel", "User profile loaded from Firestore: ${userProfile.email}")
                    } else {
                        _userProfileUiState.value = UserProfileUiState.Error("Error al convertir datos del perfil.")
                        Log.e("AuthViewModel", "Failed to convert Firestore document to UserProfile for UID: ${firebaseUser.uid}")
                    }
                } else {
                    Log.w("AuthViewModel", "User document not found for ${firebaseUser.uid}. Creating basic profile.")
                    val basicProfile = UserProfile(uid = firebaseUser.uid, email = firebaseUser.email ?: "", role = "USER", createdAt = Date())
                    db.collection("users").document(firebaseUser.uid).set(basicProfile).await()
                    _userProfileUiState.value = UserProfileUiState.Success(basicProfile)
                    userProfile = basicProfile
                }
                _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, role)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user role/profile: ${e.message}", e)
                _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER)
                _userProfileUiState.value = UserProfileUiState.Error("Error al cargar el perfil: ${e.localizedMessage}")
            }
        }
    }

    fun loadUserProfile(uid: String, existingProfileData: UserProfile? = null) {
        if (uid.isBlank()) {
            _userProfileUiState.value = UserProfileUiState.Error("UID de usuario inválido.")
            return
        }
        _userProfileUiState.value = UserProfileUiState.Loading
        viewModelScope.launch {
            try {
                if (existingProfileData != null) {
                    _userProfileUiState.value = UserProfileUiState.Success(existingProfileData)
                    Log.d("AuthViewModel", "User profile pre-loaded: ${existingProfileData.email}")
                    return@launch
                }

                val documentSnapshot = db.collection("users").document(uid).get().await()
                if (documentSnapshot.exists()) {
                    var userProfile = documentSnapshot.toObject(UserProfile::class.java)
                    // Asegurar valores por defecto para campos de texto si son null desde Firestore
                    userProfile = userProfile?.copy(
                        name = userProfile.name ?: "",
                        surname = userProfile.surname ?: "",
                        sex = userProfile.sex ?: "",
                        activityLevel = userProfile.activityLevel ?: "",
                        healthGoals = userProfile.healthGoals ?: ""
                    )
                    if (userProfile != null) {
                        _userProfileUiState.value = UserProfileUiState.Success(userProfile)
                        Log.d("AuthViewModel", "User profile loaded from Firestore: ${userProfile.email}")
                    } else {
                        _userProfileUiState.value = UserProfileUiState.Error("Error al convertir datos del perfil.")
                        Log.e("AuthViewModel", "Failed to convert Firestore document to UserProfile for UID: $uid")
                    }
                } else {
                    _userProfileUiState.value = UserProfileUiState.Error("Perfil de usuario no encontrado. Intenta de nuevo más tarde.")
                    Log.w("AuthViewModel", "No profile document found for UID: $uid during explicit load.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user profile for UID $uid: ${e.message}", e)
                _userProfileUiState.value = UserProfileUiState.Error("Error al cargar el perfil: ${e.localizedMessage}")
            }
        }
    }

    // FUNCIÓN ACTUALIZADA para incluir los nuevos campos del perfil
    fun updateUserProfile(
        name: String,
        surname: String,
        age: Int?,
        sex: String,
        height: Int?,
        weight: Double?,
        activityLevel: String,
        healthGoals: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Usuario no autenticado.")
            return
        }
        // Aquí podrías añadir más validaciones si es necesario
        // if (name.isBlank() && surname.isBlank()) { ... }

        _userProfileUiState.value = UserProfileUiState.Loading
        viewModelScope.launch {
            try {
                val userProfileUpdates = hashMapOf<String, Any?>(
                    "name" to name.trim(),
                    "surname" to surname.trim(),
                    "age" to age,
                    "sex" to sex.trim(),
                    "height" to height,
                    "weight" to weight,
                    "activityLevel" to activityLevel.trim(),
                    "healthGoals" to healthGoals.trim()
                )
                // Eliminar campos nulos del mapa si no quieres guardar `null` explícitamente
                // o si Firestore maneja mejor los campos ausentes que los `null`.
                // Por ahora, se guardarán los nulos si los valores Int? o Double? son null.

                db.collection("users").document(currentUser.uid)
                    .set(userProfileUpdates, SetOptions.merge())
                    .await()
                Log.d("AuthViewModel", "User profile updated successfully for UID: ${currentUser.uid}")
                loadUserProfile(currentUser.uid)
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating user profile for UID ${currentUser.uid}: ${e.message}", e)
                _userProfileUiState.value = UserProfileUiState.Error("Error al actualizar el perfil: ${e.localizedMessage}")
                onError("Error al actualizar el perfil: ${e.localizedMessage}")
            }
        }
    }

    fun uploadAndSaveProfilePictureUrl(imageUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Usuario no autenticado para subir foto.")
            return
        }
        _userProfileUiState.value = UserProfileUiState.Loading
        Log.d("AuthViewModel", "Starting profile picture upload for UID: ${currentUser.uid}")

        val fileName = "${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}/$fileName")

        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Uploading image to: ${storageRef.path}")
                val uploadTask = storageRef.putFile(imageUri).await()
                Log.d("AuthViewModel", "Image uploaded to Storage, getting download URL...")

                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
                Log.d("AuthViewModel", "Download URL obtained: $downloadUrl")

                db.collection("users").document(currentUser.uid)
                    .update("profilePictureUrl", downloadUrl)
                    .await()
                Log.d("AuthViewModel", "Profile picture URL updated in Firestore.")

                loadUserProfile(currentUser.uid)
                onSuccess()

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during profile picture upload/update for UID ${currentUser.uid}: ${e.message}", e)
                loadUserProfile(currentUser.uid)
                onError("Error al subir la foto de perfil: ${e.localizedMessage}")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                auth.signOut()
                clearFields()
                _authAndRoleUiState.value = AuthAndRoleUiState.Idle
                _userProfileUiState.value = UserProfileUiState.Idle
                Log.d("AuthViewModel", "User logged out. State set to Idle.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout: ${e.message}", e)
                clearFields()
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al cerrar sesión: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        Log.d("AuthViewModel", "Resetting auth state from ${_authAndRoleUiState.value} to Idle.")
        _authAndRoleUiState.value = AuthAndRoleUiState.Idle
    }

    fun resetUserProfileState() {
        Log.d("AuthViewModel", "Resetting user profile state to Idle.")
        _userProfileUiState.value = UserProfileUiState.Idle
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
