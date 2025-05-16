package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.miempresa.totalhealth.auth.UserProfile // Se utiliza la definición consolidada de UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Define los estados posibles para la UI de detalle del perfil de usuario
sealed class UserProfileDetailUiState {
    object Idle : UserProfileDetailUiState() // Estado inicial o cuando no hay operación en curso
    object Loading : UserProfileDetailUiState() // Estado mientras se cargan los datos
    data class Success(val userProfile: UserProfile) : UserProfileDetailUiState() // Estado cuando los datos se cargan con éxito
    data class Error(val message: String) : UserProfileDetailUiState() // Estado cuando ocurre un error
}

class TrainerUserDetailViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore // Instancia de Firestore

    // Flujo mutable privado para el estado de la UI, expuesto como StateFlow inmutable
    private val _uiState = MutableStateFlow<UserProfileDetailUiState>(UserProfileDetailUiState.Idle)
    val uiState: StateFlow<UserProfileDetailUiState> = _uiState.asStateFlow()

    /**
     * Obtiene el perfil de un usuario específico desde Firestore basado en su UID.
     * @param userId El ID único del usuario a obtener.
     */
    fun fetchUserProfile(userId: String) {
        // Verifica si el userId es válido
        if (userId.isBlank()) {
            _uiState.value = UserProfileDetailUiState.Error("ID de usuario inválido.")
            Log.w("TrainerUserDetailVM", "fetchUserProfile llamado con userId vacío.")
            return
        }

        Log.d("TrainerUserDetailVM", "Fetching profile for userId: $userId")
        _uiState.value = UserProfileDetailUiState.Loading // Actualiza el estado a Cargando

        viewModelScope.launch { // Inicia una corrutina en el ámbito del ViewModel
            try {
                // Obtiene el documento del usuario desde la colección "users"
                val documentSnapshot = db.collection("users").document(userId).get().await()

                if (documentSnapshot.exists()) { // Si el documento existe
                    // Convierte el documento a un objeto UserProfile
                    val userProfile = documentSnapshot.toObject(UserProfile::class.java)
                    if (userProfile != null) {
                        _uiState.value = UserProfileDetailUiState.Success(userProfile) // Éxito
                        Log.i("TrainerUserDetailVM", "Successfully fetched profile for $userId")
                    } else {
                        _uiState.value = UserProfileDetailUiState.Error("No se pudieron convertir los datos del perfil.")
                        Log.e("TrainerUserDetailVM", "Failed to convert Firestore document to UserProfile for $userId")
                    }
                } else {
                    _uiState.value = UserProfileDetailUiState.Error("Perfil de usuario no encontrado.")
                    Log.w("TrainerUserDetailVM", "No profile document found for $userId")
                }
            } catch (e: Exception) { // Captura cualquier excepción durante la obtención de datos
                _uiState.value = UserProfileDetailUiState.Error("Error al cargar el perfil: ${e.localizedMessage}")
                Log.e("TrainerUserDetailVM", "Error fetching profile for $userId: ${e.message}", e)
            }
        }
    }
}
