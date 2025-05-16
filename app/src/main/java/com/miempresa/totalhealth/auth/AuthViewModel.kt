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

// Estados de la UI para autenticación y rol
sealed class AuthAndRoleUiState {
    object Idle : AuthAndRoleUiState()
    object AuthLoading : AuthAndRoleUiState()
    object RoleLoading : AuthAndRoleUiState()
    // Modificado: user puede ser null (para el entrenador) y se añade userProfile
    data class Authenticated(val user: FirebaseUser?, val role: UserRole, val userProfile: UserProfile?) : AuthAndRoleUiState()
    data class Error(val message: String) : AuthAndRoleUiState()
}

enum class UserRole {
    ADMIN,
    USER,
    TRAINER, // <-- NUEVO ROL AÑADIDO
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
    var age: Int? = null,
    var sex: String = "",
    var height: Int? = null,
    var weight: Double? = null,
    var activityLevel: String = "",
    var healthGoals: String = "",
    val role: String = "USER", // Este campo 'role' en UserProfile es el string de Firestore
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

    // Credenciales para el entrenador (super usuario)
    private val TRAINER_EMAIL_CREDENTIAL = "entrenador"
    private val TRAINER_PASSWORD_CREDENTIAL = "123456"


    init {
        auth.currentUser?.let {
            if (_authAndRoleUiState.value is AuthAndRoleUiState.Idle) {
                fetchUserRoleAndSetState(it)
            } else if (_userProfileUiState.value is UserProfileUiState.Idle && it.uid.isNotBlank()){
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
                    val userProfile = UserProfile(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = "", // Se podrían pedir en el registro
                        surname = "", // Se podrían pedir en el registro
                        profilePictureUrl = null,
                        age = null,
                        sex = "",
                        height = null,
                        weight = null,
                        activityLevel = "",
                        healthGoals = "",
                        role = "USER", // Rol por defecto en Firestore
                        createdAt = Date()
                    )
                    try {
                        db.collection("users").document(firebaseUser.uid).set(userProfile).await()
                        Log.d("AuthViewModel", "Firestore write SUCCEEDED for user ${firebaseUser.uid}.")
                        // Pasar el UserProfile al estado Authenticated
                        _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER, userProfile)
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
        val currentEmail = email.value.trim()
        val currentPassword = password.value.trim()

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _authAndRoleUiState.value = AuthAndRoleUiState.Error("El email y la contraseña no pueden estar vacíos.")
            return
        }

        // Lógica especial para el inicio de sesión del entrenador
        if (currentEmail == TRAINER_EMAIL_CREDENTIAL && currentPassword == TRAINER_PASSWORD_CREDENTIAL) {
            Log.d("AuthViewModel", "Trainer login attempt successful.")
            val trainerProfile = UserProfile(
                uid = "trainer_local_id", // ID local, no de Firebase
                email = currentEmail,
                name = "Entrenador", // Nombre para el entrenador
                surname = "Principal",
                role = "TRAINER" // Rol string para el perfil, UserRole enum para el estado
            )
            _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(
                user = null, // El entrenador no tiene un FirebaseUser real en este flujo
                role = UserRole.TRAINER,
                userProfile = trainerProfile
            )
            _userProfileUiState.value = UserProfileUiState.Success(trainerProfile) // Actualizar también el perfil del entrenador
            clearFields() // Limpiar campos después del login exitoso
            return // Salimos para no continuar con la autenticación de Firebase
        }

        // Lógica de inicio de sesión normal con Firebase
        _authAndRoleUiState.value = AuthAndRoleUiState.AuthLoading
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(currentEmail, currentPassword).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    fetchUserRoleAndSetState(firebaseUser) // Esto cargará el rol y el perfil completo
                    clearFields() // Limpiar campos después del login exitoso
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
                var mappedRole = UserRole.USER // Rol por defecto
                var userProfileData: UserProfile? = null

                if (userDoc.exists()) {
                    userProfileData = userDoc.toObject(UserProfile::class.java)
                    userProfileData = userProfileData?.copy( // Asegurar valores por defecto
                        name = userProfileData.name ?: "",
                        surname = userProfileData.surname ?: "",
                        sex = userProfileData.sex ?: "",
                        activityLevel = userProfileData.activityLevel ?: "",
                        healthGoals = userProfileData.healthGoals ?: ""
                    )

                    val roleString = userProfileData?.role ?: "USER"
                    mappedRole = when (roleString.uppercase()) {
                        "ADMIN" -> UserRole.ADMIN
                        "ENTRENADOR" -> UserRole.TRAINER // <-- MAPEAR "ENTRENADOR" A UserRole.TRAINER
                        "USER", "USUARIO" -> UserRole.USER
                        else -> UserRole.USER // Por defecto USER si el string no coincide
                    }
                    Log.d("AuthViewModel", "User role from profile: $roleString, mapped to: $mappedRole")

                    if (userProfileData != null) {
                        _userProfileUiState.value = UserProfileUiState.Success(userProfileData)
                        Log.d("AuthViewModel", "User profile loaded from Firestore: ${userProfileData.email}")
                    } else {
                        _userProfileUiState.value = UserProfileUiState.Error("Error al convertir datos del perfil.")
                        Log.e("AuthViewModel", "Failed to convert Firestore document to UserProfile for UID: ${firebaseUser.uid}")
                    }
                } else {
                    Log.w("AuthViewModel", "User document not found for ${firebaseUser.uid}. Creating basic profile.")
                    userProfileData = UserProfile(uid = firebaseUser.uid, email = firebaseUser.email ?: "", role = "USER", createdAt = Date())
                    db.collection("users").document(firebaseUser.uid).set(userProfileData).await()
                    _userProfileUiState.value = UserProfileUiState.Success(userProfileData)
                    mappedRole = UserRole.USER // Rol por defecto para nuevo perfil
                }
                // Pasar el UserProfile al estado Authenticated
                _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, mappedRole, userProfileData)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user role/profile: ${e.message}", e)
                // En caso de error al obtener el perfil, se autentica como USER con un perfil básico
                val basicProfileOnError = UserProfile(uid = firebaseUser.uid, email = firebaseUser.email ?: "", role = "USER", createdAt = Date())
                _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER, basicProfileOnError)
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
            // Verificar si el estado actual es el del entrenador
            val currentAuthState = _authAndRoleUiState.value
            if (currentAuthState is AuthAndRoleUiState.Authenticated && currentAuthState.role == UserRole.TRAINER) {
                // El entrenador no debería usar esta función para actualizar un perfil de Firebase
                onError("Función no aplicable para el perfil de entrenador local.")
                return
            }
            onError("Usuario no autenticado.")
            return
        }

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

                db.collection("users").document(currentUser.uid)
                    .set(userProfileUpdates, SetOptions.merge())
                    .await()
                Log.d("AuthViewModel", "User profile updated successfully for UID: ${currentUser.uid}")
                loadUserProfile(currentUser.uid) // Recargar el perfil para actualizar la UI
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating user profile for UID ${currentUser.uid}: ${e.message}", e)
                // Revertir el estado de carga si es necesario o cargar el perfil anterior
                val previousProfileState = _userProfileUiState.value
                if (previousProfileState is UserProfileUiState.Success) {
                    loadUserProfile(currentUser.uid, previousProfileState.profile) // Recargar con datos anteriores si falla
                } else {
                    loadUserProfile(currentUser.uid) // Intentar recargar
                }
                onError("Error al actualizar el perfil: ${e.localizedMessage}")
            }
        }
    }

    fun uploadAndSaveProfilePictureUrl(imageUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val currentAuthState = _authAndRoleUiState.value
            if (currentAuthState is AuthAndRoleUiState.Authenticated && currentAuthState.role == UserRole.TRAINER) {
                onError("Función no aplicable para el perfil de entrenador local.")
                return
            }
            onError("Usuario no autenticado para subir foto.")
            return
        }

        _userProfileUiState.value = UserProfileUiState.Loading // O un estado específico de carga de imagen
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

                loadUserProfile(currentUser.uid) // Recargar para obtener la nueva URL en el perfil
                onSuccess()

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during profile picture upload/update for UID ${currentUser.uid}: ${e.message}", e)
                // Revertir el estado de carga si es necesario o cargar el perfil anterior
                val previousProfileState = _userProfileUiState.value
                if (previousProfileState is UserProfileUiState.Success) {
                    loadUserProfile(currentUser.uid, previousProfileState.profile)
                } else {
                    loadUserProfile(currentUser.uid)
                }
                onError("Error al subir la foto de perfil: ${e.localizedMessage}")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                val currentAuthState = _authAndRoleUiState.value
                if (currentAuthState is AuthAndRoleUiState.Authenticated && currentAuthState.role == UserRole.TRAINER) {
                    // Para el entrenador, solo limpiar estados locales
                    clearFields()
                    _authAndRoleUiState.value = AuthAndRoleUiState.Idle
                    _userProfileUiState.value = UserProfileUiState.Idle
                    Log.d("AuthViewModel", "Trainer logged out. State set to Idle.")
                } else {
                    // Para usuarios de Firebase, cerrar sesión de Firebase
                    auth.signOut()
                    clearFields()
                    _authAndRoleUiState.value = AuthAndRoleUiState.Idle
                    _userProfileUiState.value = UserProfileUiState.Idle
                    Log.d("AuthViewModel", "Firebase user logged out. State set to Idle.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout: ${e.message}", e)
                clearFields() // Asegurar limpieza de campos incluso en error
                _authAndRoleUiState.value = AuthAndRoleUiState.Error("Error al cerrar sesión: ${e.localizedMessage}")
                _userProfileUiState.value = UserProfileUiState.Idle // Resetear perfil también
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
        // Si el estado es Entrenador, no hay FirebaseUser real
        val currentAuthState = _authAndRoleUiState.value
        if (currentAuthState is AuthAndRoleUiState.Authenticated && currentAuthState.role == UserRole.TRAINER) {
            return null
        }
        return auth.currentUser
    }
}
