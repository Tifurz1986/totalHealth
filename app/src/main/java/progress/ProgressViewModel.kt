package com.miempresa.totalhealth.progress // ¡ASEGÚRATE DE QUE ESTE PAQUETE SEA CORRECTO!

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
// Quita la importación de FieldValue si no la usas directamente para los timestamps
// import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

// Estados para la UI de Progreso
sealed class ProgressUiState {
    object Idle : ProgressUiState()
    object Loading : ProgressUiState()
    object Success : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

class ProgressViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

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


    fun onPhysicalLevelChange(level: Float) {
        _physicalLevel.floatValue = level.coerceIn(0f, 10f)
    }

    fun onMentalLevelChange(level: Float) {
        _mentalLevel.floatValue = level.coerceIn(0f, 10f)
    }

    fun onNutritionLevelChange(level: Float) {
        _nutritionLevel.floatValue = level.coerceIn(0f, 10f)
    }

    fun onNotesChange(newNotes: String) {
        _notes.value = newNotes
    }

    fun onEntryDateSelected(millis: Long) {
        _entryDateMillis.value = millis
    }


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
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val entryDateForDb = calendar.time

                val newEntry = ProgressEntry(
                    userId = userId,
                    physical = _physicalLevel.floatValue.toInt(),
                    mental = _mentalLevel.floatValue.toInt(),
                    nutrition = _nutritionLevel.floatValue.toInt(),
                    notes = _notes.value.takeIf { it.isNotBlank() },
                    entryDate = entryDateForDb,
                    createdAt = null, // Firestore usará @ServerTimestamp
                    updatedAt = null  // Firestore usará @ServerTimestamp
                )

                db.collection("users").document(userId)
                    .collection("progress_entries")
                    .add(newEntry)
                    .await()

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
}
