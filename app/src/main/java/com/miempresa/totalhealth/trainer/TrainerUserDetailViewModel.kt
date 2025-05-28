package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.miempresa.totalhealth.auth.UserProfile // Asegúrate que esta es la ruta correcta
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estados de la UI para el detalle del perfil del usuario
sealed class UserProfileDetailUiState {
    object Idle : UserProfileDetailUiState() // Estado inicial o si no hay userId
    object Loading : UserProfileDetailUiState()
    data class Success(val userProfile: UserProfile) : UserProfileDetailUiState()
    data class Error(val message: String) : UserProfileDetailUiState()
}

class TrainerUserDetailViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val TAG = "TrainerUserDetailVM" // Para logs

    private val _uiState = MutableStateFlow<UserProfileDetailUiState>(UserProfileDetailUiState.Idle)
    val uiState: StateFlow<UserProfileDetailUiState> = _uiState.asStateFlow()

    val trainerId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun fetchUserProfile(userId: String?) {
        if (userId.isNullOrBlank()) {
            Log.w(TAG, "fetchUserProfile called with null or blank userId.")
            _uiState.value = UserProfileDetailUiState.Error("ID de usuario inválido.")
            return
        }

        _uiState.value = UserProfileDetailUiState.Loading
        Log.d(TAG, "Fetching profile by field query for UID: $userId")

        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("uid", userId) // <<--- CAMBIO PRINCIPAL: Buscar por el campo "uid"
                    .limit(1) // Esperamos como máximo un resultado
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0] // Tomamos el primer documento encontrado
                    Log.d(TAG, "Document found with ID: ${documentSnapshot.id} for UID query: $userId")

                    // Asegurarse de que el documento no sea nulo y convertirlo
                    var userProfile = documentSnapshot.toObject(UserProfile::class.java)

                    // Es buena práctica asegurar valores no nulos para campos String si la data class los espera así,
                    // aunque UserProfile.kt ya tiene valores por defecto para Strings.
                    // Esto es más relevante si tuvieras String? y quisieras "" por defecto en la UI.
                    userProfile = userProfile?.copy(
                        name = userProfile.name ?: "",
                        surname = userProfile.surname ?: "",
                        email = userProfile.email ?: "", // Aunque email no debería ser null
                        sex = userProfile.sex ?: "",
                        activityLevel = userProfile.activityLevel ?: "",
                        healthGoals = userProfile.healthGoals ?: "",
                        role = userProfile.role.ifEmpty { "USER" } // Asegurar un rol por defecto
                        // No es necesario tocar profilePictureUrl, age, height, weight, createdAt, uid aquí,
                        // ya que UserProfile.kt maneja sus valores por defecto/nulabilidad.
                    )

                    if (userProfile != null) {
                        Log.d(TAG, "Successfully fetched and converted profile. Emitting Success state: $userProfile")
                        _uiState.value = UserProfileDetailUiState.Success(userProfile)
                    } else {
                        Log.e(TAG, "Failed to convert Firestore document to UserProfile for UID: $userId (document ID: ${documentSnapshot.id})")
                        _uiState.value = UserProfileDetailUiState.Error("Error al procesar datos del perfil para UID: $userId")
                    }
                } else {
                    Log.w(TAG, "No profile document found with 'uid' field matching: $userId")
                    _uiState.value = UserProfileDetailUiState.Error("Perfil de usuario no encontrado con UID: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION while fetching profile by field query for UID $userId: ${e.message}", e)
                _uiState.value = UserProfileDetailUiState.Error("Error al cargar el perfil: ${e.localizedMessage}")
            }
        }
    }
}