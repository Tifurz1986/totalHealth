package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.miempresa.totalhealth.auth.UserProfile // Asegúrate que esta es la ruta correcta
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

    fun fetchUserProfile(documentId: String?) {
        if (documentId.isNullOrBlank()) {
            Log.w(TAG, "fetchUserProfile called with null or blank documentId.")
            _uiState.value = UserProfileDetailUiState.Error("ID del documento inválido.")
            return
        }

        _uiState.value = UserProfileDetailUiState.Loading

        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(documentId).get().await()
                if (doc.exists()) {
                    val userProfile = doc.toObject(UserProfile::class.java)
                    userProfile?.let {
                        _uiState.value = UserProfileDetailUiState.Success(it)
                        Log.d(TAG, "Usuario cargado correctamente: $documentId")
                    } ?: run {
                        _uiState.value = UserProfileDetailUiState.Error("Error al convertir el usuario.")
                    }
                } else {
                    _uiState.value = UserProfileDetailUiState.Error("No se encontró el documento.")
                }
            } catch (e: Exception) {
                _uiState.value = UserProfileDetailUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Error al cargar usuario por ID: ${e.message}")
            }
        }
    }
}