package com.miempresa.totalhealth.progress

import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date


sealed class ProgressUiState {
    object Idle : ProgressUiState()
    object Loading : ProgressUiState()
    object Success : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

sealed class CoachRatingsUiState {
    object Idle : CoachRatingsUiState()
    object Loading : CoachRatingsUiState()
    data class Success(val progressRatings: UserProgressRatings?) : CoachRatingsUiState()
    data class Error(val message: String) : CoachRatingsUiState()
}

class ProgressViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    private val _addProgressUiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Idle)
    val addProgressUiState: StateFlow<ProgressUiState> = _addProgressUiState.asStateFlow()
    private val _physicalLevel = mutableFloatStateOf(5f)
    val physicalLevel: androidx.compose.runtime.State<Float> = _physicalLevel
    private val _mentalLevel = mutableFloatStateOf(5f)
    val mentalLevel: androidx.compose.runtime.State<Float> = _mentalLevel
    private val _nutritionLevel = mutableFloatStateOf(5f)
    val nutritionLevel: androidx.compose.runtime.State<Float> = _nutritionLevel
    private val _notes = mutableStateOf("")
    val notes: androidx.compose.runtime.State<String> = _notes
    private val _entryDateMillis = mutableStateOf(System.currentTimeMillis())
    val entryDateMillis: androidx.compose.runtime.State<Long> = _entryDateMillis

    private val _coachRatingsUiState = MutableStateFlow<CoachRatingsUiState>(CoachRatingsUiState.Idle)
    val coachRatingsUiState: StateFlow<CoachRatingsUiState> = _coachRatingsUiState.asStateFlow()

    fun onPhysicalLevelChange(level: Float) { _physicalLevel.floatValue = level.coerceIn(0f, 10f) }
    fun onMentalLevelChange(level: Float) { _mentalLevel.floatValue = level.coerceIn(0f, 10f) }
    fun onNutritionLevelChange(level: Float) { _nutritionLevel.floatValue = level.coerceIn(0f, 10f) }
    fun onNotesChange(newNotes: String) { _notes.value = newNotes }
    fun onEntryDateSelected(millis: Long) { _entryDateMillis.value = millis }

    fun addProgressEntry() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _addProgressUiState.value = ProgressUiState.Error("Usuario no autenticado.")
            return
        }
        _addProgressUiState.value = ProgressUiState.Loading
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = _entryDateMillis.value
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val entryDateForDb = calendar.time
                val newEntry = ProgressEntry( // Asumiendo ProgressEntry.kt está definido
                    userId = currentUserId,
                    physical = _physicalLevel.floatValue.toInt(),
                    mental = _mentalLevel.floatValue.toInt(),
                    nutrition = _nutritionLevel.floatValue.toInt(),
                    notes = _notes.value.takeIf { it.isNotBlank() },
                    entryDate = entryDateForDb
                )
                db.collection("users").document(currentUserId)
                    .collection("progress_entries")
                    .add(newEntry).await()
                _addProgressUiState.value = ProgressUiState.Success
                Log.d("ProgressViewModel", "Progress entry added successfully for date: $entryDateForDb")
                clearFields()
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error adding progress entry", e)
                _addProgressUiState.value = ProgressUiState.Error("Error al guardar el progreso: ${e.message}")
            }
        }
    }

    fun clearFields() {
        _physicalLevel.floatValue = 5f
        _mentalLevel.floatValue = 5f
        _nutritionLevel.floatValue = 5f
        _notes.value = ""
        _entryDateMillis.value = System.currentTimeMillis()
    }

    fun resetAddProgressUiState() {
        _addProgressUiState.value = ProgressUiState.Idle
    }

    fun loadLatestCoachRatingsForUser(userId: String) {
        if (userId.isBlank()) {
            _coachRatingsUiState.value = CoachRatingsUiState.Error("ID de usuario no válido.")
            Log.w("ProgressViewModel", "loadLatestCoachRatingsForUser llamado con UID vacío.")
            return
        }

        _coachRatingsUiState.value = CoachRatingsUiState.Loading
        Log.d("ProgressViewModel", "Cargando ÚLTIMAS valoraciones del coach para userID: $userId (Usando Gold Palette)")
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("users").document(userId)
                    .collection("coach_ratings")
                    .orderBy("lastUpdated", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val ratings = documentSnapshot.toObject<UserProgressRatings>()
                    if (ratings != null) {
                        _coachRatingsUiState.value = CoachRatingsUiState.Success(ratings)
                        Log.d("ProgressViewModel", "Última valoración del coach cargada para $userId: $ratings")
                    } else {
                        _coachRatingsUiState.value = CoachRatingsUiState.Error("Error al parsear datos de valoración para $userId.")
                        Log.e("ProgressViewModel", "Error al convertir UserProgressRatings para $userId. Doc existe pero conversión falló.")
                    }
                } else {
                    _coachRatingsUiState.value = CoachRatingsUiState.Success(null)
                    Log.d("ProgressViewModel", "No se encontraron valoraciones del coach para $userId.")
                }
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error al cargar últimas valoraciones para $userId", e)
                _coachRatingsUiState.value = CoachRatingsUiState.Error("Error al cargar valoraciones: ${e.localizedMessage}")
            }
        }
    }

    fun saveCoachRatingsForUser(
        userId: String, periodId: String, overallRating: Float,
        generalFeedback: String?, categoryRatings: List<ProgressCategoryRating>
    ) {
        viewModelScope.launch {
            Log.d("ProgressViewModel", "Intentando guardar valoraciones para Usuario ID: $userId, Periodo: $periodId")
            try {
                val userProgressRatings = UserProgressRatings(
                    userId = userId, periodId = periodId, ratings = categoryRatings,
                    overallAverageRating = overallRating, generalCoachFeedback = generalFeedback,
                    lastUpdated = Date()
                )
                db.collection("users").document(userId)
                    .collection("coach_ratings").document(periodId)
                    .set(userProgressRatings).await()
                Log.i("ProgressViewModel", "Valoraciones guardadas exitosamente para: $userId, periodo: $periodId")
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error al guardar valoraciones para: $userId", e)
            }
        }
    }

    fun resetCoachRatingsUiState() {
        _coachRatingsUiState.value = CoachRatingsUiState.Idle
    }
}