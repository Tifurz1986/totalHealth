package com.miempresa.totalhealth.progress

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

// Estados para la UI de Progreso (guardado de entradas manuales)
sealed class ProgressUiState {
    object Idle : ProgressUiState()
    object Loading : ProgressUiState()
    object Success : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

// Nuevos estados para la UI de las valoraciones del coach
sealed class CoachRatingsUiState {
    object Idle : CoachRatingsUiState()
    object Loading : CoachRatingsUiState()
    data class Success(val progressRatings: UserProgressRatings?) : CoachRatingsUiState() // Puede ser null si no hay datos
    data class Error(val message: String) : CoachRatingsUiState()
}

class ProgressViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // --- Estados y lógica para el registro de progreso manual (existente) ---
    private val _addProgressUiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Idle)
    val addProgressUiState: StateFlow<ProgressUiState> = _addProgressUiState

    private val _physicalLevel = mutableFloatStateOf(5f)
    val physicalLevel: State<Float> = _physicalLevel

    private val _mentalLevel = mutableFloatStateOf(5f)
    val mentalLevel: State<Float> = _mentalLevel

    private val _nutritionLevel = mutableFloatStateOf(5f)
    val nutritionLevel: State<Float> = _nutritionLevel

    private val _notes = mutableStateOf("")
    val notes: State<String> = _notes

    private val _entryDateMillis = mutableStateOf(System.currentTimeMillis())
    val entryDateMillis: State<Long> = _entryDateMillis

    // --- NUEVO: Estados y lógica para las valoraciones del coach ---
    private val _coachRatingsUiState = MutableStateFlow<CoachRatingsUiState>(CoachRatingsUiState.Idle)
    val coachRatingsUiState: StateFlow<CoachRatingsUiState> = _coachRatingsUiState.asStateFlow()

    init {
        // Cargar las valoraciones del coach al iniciar el ViewModel (ej. para el período actual)
        loadCoachRatingsForCurrentPeriod()
    }

    fun onPhysicalLevelChange(level: Float) { _physicalLevel.floatValue = level.coerceIn(0f, 10f) }
    fun onMentalLevelChange(level: Float) { _mentalLevel.floatValue = level.coerceIn(0f, 10f) }
    fun onNutritionLevelChange(level: Float) { _nutritionLevel.floatValue = level.coerceIn(0f, 10f) }
    fun onNotesChange(newNotes: String) { _notes.value = newNotes }
    fun onEntryDateSelected(millis: Long) { _entryDateMillis.value = millis }

    fun addProgressEntry() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
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
                val newEntry = ProgressEntry(
                    userId = userId,
                    physical = _physicalLevel.floatValue.toInt(),
                    mental = _mentalLevel.floatValue.toInt(),
                    nutrition = _nutritionLevel.floatValue.toInt(),
                    notes = _notes.value.takeIf { it.isNotBlank() },
                    entryDate = entryDateForDb
                    // createdAt y updatedAt se manejan con @ServerTimestamp en el modelo ProgressEntry
                )
                db.collection("users").document(userId)
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

    // --- NUEVAS FUNCIONES para las valoraciones del coach ---
    fun loadCoachRatingsForCurrentPeriod() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _coachRatingsUiState.value = CoachRatingsUiState.Error("Usuario no autenticado para cargar valoraciones.")
            return
        }
        val userId = currentUser.uid
        // Determinar el ID del período actual (ej. "YYYY-WW" para semana o "YYYY-MM-DD" para día)
        // Por simplicidad, usaremos un ID de período fijo para este ejemplo.
        // En una app real, calcularías esto basado en la fecha actual.
        val currentPeriodId = "2025-CURRENT" // Reemplazar con lógica real de período

        _coachRatingsUiState.value = CoachRatingsUiState.Loading
        Log.d("ProgressViewModel", "Cargando valoraciones del coach para userID: $userId, periodo: $currentPeriodId")
        viewModelScope.launch {
            try {
                // SIMULACIÓN DE DATOS (ya que no hay interfaz de coach para introducirlos)
                // En una app real, aquí harías una consulta a Firestore:
                // val documentSnapshot = db.collection("users").document(userId)
                //    .collection("coach_ratings").document(currentPeriodId).get().await()
                // if (documentSnapshot.exists()) {
                //     val ratings = documentSnapshot.toObject(UserProgressRatings::class.java)
                //     _coachRatingsUiState.value = CoachRatingsUiState.Success(ratings)
                // } else {
                //     _coachRatingsUiState.value = CoachRatingsUiState.Success(null) // No hay valoraciones para este período
                // }

                // Datos de ejemplo:
                kotlinx.coroutines.delay(1000) // Simular carga
                val exampleRatings = UserProgressRatings(
                    userId = userId,
                    periodId = currentPeriodId,
                    ratings = listOf(
                        ProgressCategoryRating("nutrition", "Nutrición", 4.5f, "Muy buen seguimiento de las pautas, sigue así con la hidratación."),
                        ProgressCategoryRating("exercise", "Ejercicio Físico", 3.0f, "Constancia en los entrenamientos de fuerza, mejorar la frecuencia del cardio."),
                        ProgressCategoryRating("mental_wellbeing", "Bienestar Mental", 4.0f, "Se nota una actitud más positiva. Sigue con las técnicas de mindfulness."),
                        ProgressCategoryRating("goal_adherence", "Cumplimiento de Objetivos", 3.5f, "Buen progreso general, enfócate en el objetivo X esta semana.")
                    ),
                    overallAverageRating = (4.5f + 3.0f + 4.0f + 3.5f) / 4,
                    generalCoachFeedback = "¡Excelente trabajo esta semana, Carlos! Sigue esforzándote y verás grandes resultados. No olvides descansar lo suficiente.",
                    lastUpdated = Date()
                )
                _coachRatingsUiState.value = CoachRatingsUiState.Success(exampleRatings)
                Log.d("ProgressViewModel", "Valoraciones de coach (ejemplo) cargadas: $exampleRatings")

            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error al cargar valoraciones del coach", e)
                _coachRatingsUiState.value = CoachRatingsUiState.Error("Error al cargar valoraciones del coach: ${e.localizedMessage}")
            }
        }
    }

    fun resetCoachRatingsUiState() {
        _coachRatingsUiState.value = CoachRatingsUiState.Idle
    }
}
