package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.miempresa.totalhealth.auth.UserProfile // Asegúrate que esta es la ruta correcta a tu UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UserListUiState {
    object Loading : UserListUiState()
    data class Success(val users: List<UserProfile>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}

class TrainerViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore

    private val _userListUiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val userListUiState: StateFlow<UserListUiState> = _userListUiState.asStateFlow()

    init {
        fetchAllUsers()
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _userListUiState.value = UserListUiState.Loading
            try {
                val result = db.collection("users")
                    .get()
                    .await()

                val users = result.documents.mapNotNull { document ->
                    // Intentar convertir el documento a UserProfile.
                    // Se asume que UserProfile tiene un constructor sin argumentos para Firestore.
                    document.toObject(UserProfile::class.java)
                }
                _userListUiState.value = UserListUiState.Success(users)
                Log.d("TrainerViewModel", "Fetched ${users.size} users.")

            } catch (e: Exception) {
                Log.e("TrainerViewModel", "Error fetching users", e)
                _userListUiState.value = UserListUiState.Error("Error al cargar usuarios: ${e.localizedMessage}")
            }
        }
    }
}