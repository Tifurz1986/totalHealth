package com.miempresa.totalhealth.dailylog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.miempresa.totalhealth.dailylog.EmotionEntry

// Estados para la UI del registro diario
sealed class DailyLogUiState {
    object Idle : DailyLogUiState()
    object Loading : DailyLogUiState()
    data class Success(val log: DailyLog?) : DailyLogUiState()
    data class Error(val message: String) : DailyLogUiState()
}

class DailyLogViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _dailyLogUiState = MutableStateFlow<DailyLogUiState>(DailyLogUiState.Idle)
    val dailyLogUiState: StateFlow<DailyLogUiState> = _dailyLogUiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getDocumentIdForDate(userId: String, date: Date): String {
        return "${userId}_${dateFormat.format(date)}"
    }

    fun loadDailyLogForDate(date: Date) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _dailyLogUiState.value = DailyLogUiState.Error("Usuario no autenticado.")
            return
        }
        val userId = currentUser.uid
        // Normalizar la fecha a medianoche para consistencia en el ID
        val normalizedDate = normalizeDate(date)
        val documentId = getDocumentIdForDate(userId, normalizedDate)

        _dailyLogUiState.value = DailyLogUiState.Loading
        Log.d("DailyLogViewModel", "Cargando DailyLog para $documentId")
        viewModelScope.launch {
            try {
                val documentSnapshot = db.collection("daily_logs").document(documentId).get().await()
                if (documentSnapshot.exists()) {
                    val dailyLog = documentSnapshot.toObject(DailyLog::class.java)?.copy(id = documentSnapshot.id)
                    _dailyLogUiState.value = DailyLogUiState.Success(dailyLog)
                    Log.d("DailyLogViewModel", "DailyLog cargado: $dailyLog")
                } else {
                    _dailyLogUiState.value = DailyLogUiState.Success(null)
                    Log.d("DailyLogViewModel", "No existe DailyLog para $documentId.")
                }
            } catch (e: Exception) {
                Log.e("DailyLogViewModel", "Error al cargar DailyLog para $documentId", e)
                _dailyLogUiState.value = DailyLogUiState.Error("Error al cargar el registro diario: ${e.localizedMessage}")
            }
        }
    }

    fun saveDailyLog(
        dailyLog: DailyLog,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Usuario no autenticado.")
            _dailyLogUiState.value = DailyLogUiState.Error("Usuario no autenticado.")
            return
        }

        // Normalizar la fecha a medianoche para consistencia en el ID y la fecha guardada
        val normalizedDate = normalizeDate(dailyLog.date)
        val documentId = getDocumentIdForDate(currentUser.uid, normalizedDate)

        val logToSave = dailyLog.copy(
            userId = currentUser.uid,
            id = documentId, // Asegurar que el ID del log sea el ID del documento
            date = normalizedDate // Guardar la fecha normalizada
        )

        _dailyLogUiState.value = DailyLogUiState.Loading
        Log.d("DailyLogViewModel", "Guardando DailyLog para $documentId: $logToSave")
        viewModelScope.launch {
            try {
                db.collection("daily_logs").document(documentId).set(logToSave, SetOptions.merge()).await()
                _dailyLogUiState.value = DailyLogUiState.Success(logToSave)
                Log.d("DailyLogViewModel", "DailyLog guardado exitosamente para $documentId")
                onSuccess()
            } catch (e: Exception) {
                Log.e("DailyLogViewModel", "Error al guardar DailyLog para $documentId", e)
                val errorMessage = "Error al guardar el registro: ${e.localizedMessage}"
                _dailyLogUiState.value = DailyLogUiState.Error(errorMessage)
                onError(errorMessage)
            }
        }
    }

    fun resetUiState() {
        _dailyLogUiState.value = DailyLogUiState.Idle
    }

    private fun normalizeDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getTodayDate(): Date {
        return normalizeDate(Date()) // Devuelve la fecha de hoy normalizada
    }
    fun getUserEmotionEntries(userId: String): Flow<List<EmotionEntry>> = flow {
        val result = db.collection("emotions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()

        Log.d("DailyLogViewModel", "Documentos recuperados: ${result.documents.size}")

        val emotions = result.documents.mapNotNull { doc ->
            val mood = doc.getString("emotion") ?: ""
            val moodIntensity = null // No tienes intensidad, pon null
            val triggers = doc.getString("subemotion") ?: ""
            val journalEntry = ""
            val timestamp = doc.getLong("timestamp")?.let { Date(it) } ?: Date()
            EmotionEntry(
                mood = mood,
                moodIntensity = moodIntensity,
                triggers = triggers,
                journalEntry = journalEntry,
                timestamp = timestamp
            )
        }

        Log.d("DailyLogViewModel", "Emociones encontradas para userId $userId: ${emotions.size}")
        emit(emotions)
    }
}