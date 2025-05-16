package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.miempresa.totalhealth.auth.UserProfile // Asegúrate que la importación sea correcta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TrainerScreenUiState(
    val isLoadingUsers: Boolean = true,
    val users: List<UserProfile> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val feedbackSentMessage: String? = null
)

class TrainerViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore

    private val _isLoadingUsers = MutableStateFlow(true)
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _feedbackSentMessage = MutableStateFlow<String?>(null)


    val filteredUsers: StateFlow<List<UserProfile>> =
        combine(_allUsers, _searchQuery) { users, query ->
            if (query.isBlank()) {
                users
            } else {
                users.filter {
                    (it.name.contains(query, ignoreCase = true)) ||
                            (it.surname.contains(query, ignoreCase = true)) ||
                            (it.email.contains(query, ignoreCase = true))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<TrainerScreenUiState> =
        combine(
            _isLoadingUsers,
            filteredUsers,
            _searchQuery,
            _errorMessage,
            _feedbackSentMessage
        ) { isLoading, usersList, query, error, feedbackMsg ->
            TrainerScreenUiState(
                isLoadingUsers = isLoading,
                users = usersList,
                searchQuery = query,
                errorMessage = error,
                feedbackSentMessage = feedbackMsg
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrainerScreenUiState()
        )


    init {
        fetchAllUsers()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            _errorMessage.value = null
            try {
                val result = db.collection("users")
                    .get()
                    .await()

                val userList = result.documents.mapNotNull { document ->
                    // Intenta convertir el documento a UserProfile
                    val userProfile = document.toObject(UserProfile::class.java)
                    // Asegúrate de que el uid no sea nulo o vacío después de la conversión
                    if (userProfile?.uid?.isNotBlank() == true) {
                        userProfile
                    } else {
                        Log.w("TrainerViewModel", "Usuario descartado debido a UID vacío o nulo. Document ID: ${document.id}")
                        null // Descarta este usuario si el uid es inválido
                    }
                }.filter { userProfile ->
                    // Filtrar por rol DESPUÉS de asegurar que tenemos un UID válido
                    val isValidUserRole = userProfile.role.uppercase() == "USER"
                    if (!isValidUserRole) {
                        Log.d("TrainerViewModel", "Usuario ${userProfile.uid} filtrado por rol: ${userProfile.role}")
                    }
                    isValidUserRole
                }

                _allUsers.value = userList
                Log.d("TrainerViewModel", "Fetched ${userList.size} users after filtering UID and role.")
            } catch (e: Exception) {
                Log.e("TrainerViewModel", "Error fetching users: ${e.message}", e)
                _errorMessage.value = "Error al cargar usuarios: ${e.localizedMessage}"
                _allUsers.value = emptyList()
            } finally {
                _isLoadingUsers.value = false
            }
        }
    }

    fun sendFeedbackToUser(userId: String, feedbackText: String) {
        viewModelScope.launch {
            Log.d("TrainerViewModel", "Feedback para el usuario $userId: $feedbackText (Simulación)")
            _feedbackSentMessage.value = "Feedback enviado (simulado) a usuario con ID: $userId"
            kotlinx.coroutines.delay(3000)
            _feedbackSentMessage.value = null
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearFeedbackMessage() {
        _feedbackSentMessage.value = null
    }
}
