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

// Estados para la UI del registro diario
sealed class DailyLogUiState {
    object Idle : DailyLogUiState() // Estado inicial o después de una operación exitosa
    object Loading : DailyLogUiState() // Cargando datos
    data class Success(val log: DailyLog?) : DailyLogUiState() // Datos cargados (log puede ser null si no hay entrada para esa fecha)
    data class Error(val message: String) : DailyLogUiState() // Error durante la carga o guardado
}

class DailyLogViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _dailyLogUiState = MutableStateFlow<DailyLogUiState>(DailyLogUiState.Idle)
    val dailyLogUiState: StateFlow<DailyLogUiState> = _dailyLogUiState.asStateFlow()

    // Formateador de fecha para crear IDs de documento consistentes (ej: "2024-05-16")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Almacena el DailyLog actual que se está editando o mostrando
    // Esto podría ser útil si la UI necesita modificar partes del log antes de guardarlo todo.
    // O, la UI podría construir el objeto DailyLog completo y pasarlo a saveDailyLog.
    // Por ahora, lo mantenemos simple y la UI construirá el objeto.

    /**
     * Genera un ID de documento para un DailyLog basado en el UID del usuario y la fecha.
     * @param userId El UID del usuario.
     * @param date La fecha para el log.
     * @return Un String con el formato "userId_yyyy-MM-dd".
     */
    private fun getDocumentIdForDate(userId: String, date: Date): String {
        return "${userId}_${dateFormat.format(date)}"
    }

    /**
     * Carga el DailyLog para el usuario actual y una fecha específica.
     * Si no existe un log para esa fecha, el estado Success contendrá un log null.
     * @param date La fecha para la cual cargar el log.
     */
    fun loadDailyLogForDate(date: Date) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _dailyLogUiState.value = DailyLogUiState.Error("Usuario no autenticado.")
            return
        }
        val userId = currentUser.uid
        val documentId = getDocumentIdForDate(userId, date)

        _dailyLogUiState.value = DailyLogUiState.Loading
        Log.d("DailyLogViewModel", "Cargando DailyLog para $documentId")
        viewModelScope.launch {
            try {
                val documentSnapshot = db.collection("daily_logs").document(documentId).get().await()
                if (documentSnapshot.exists()) {
                    val dailyLog = documentSnapshot.toObject(DailyLog::class.java)
                    _dailyLogUiState.value = DailyLogUiState.Success(dailyLog)
                    Log.d("DailyLogViewModel", "DailyLog cargado: $dailyLog")
                } else {
                    // No existe un log para esta fecha, es un caso válido (ej. el usuario no ha registrado nada aún)
                    // Devolvemos Success con un log null para que la UI pueda mostrar un estado vacío o de creación.
                    _dailyLogUiState.value = DailyLogUiState.Success(null)
                    Log.d("DailyLogViewModel", "No existe DailyLog para $documentId. Se puede crear uno nuevo.")
                }
            } catch (e: Exception) {
                Log.e("DailyLogViewModel", "Error al cargar DailyLog para $documentId", e)
                _dailyLogUiState.value = DailyLogUiState.Error("Error al cargar el registro diario: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Guarda o actualiza un DailyLog completo en Firestore.
     * Utiliza el UID del usuario actual y la fecha del log para determinar el ID del documento.
     * @param dailyLog El objeto DailyLog a guardar.
     * @param onSuccess Callback opcional que se ejecuta si el guardado es exitoso.
     * @param onError Callback opcional que se ejecuta si ocurre un error.
     */
    fun saveDailyLog(
        dailyLog: DailyLog,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Usuario no autenticado.")
            _dailyLogUiState.value = DailyLogUiState.Error("Usuario no autenticado.") // También actualiza el estado global si es relevante
            return
        }

        // Asegurarse de que el userId en el log es el del usuario actual
        val logToSave = dailyLog.copy(userId = currentUser.uid)
        val documentId = getDocumentIdForDate(logToSave.userId, logToSave.date)

        _dailyLogUiState.value = DailyLogUiState.Loading // Indicar que se está guardando
        Log.d("DailyLogViewModel", "Guardando DailyLog para $documentId: $logToSave")
        viewModelScope.launch {
            try {
                // Usar set con SetOptions.merge() para crear el documento si no existe,
                // o actualizarlo/fusionarlo si ya existe.
                db.collection("daily_logs").document(documentId).set(logToSave, SetOptions.merge()).await()
                _dailyLogUiState.value = DailyLogUiState.Success(logToSave) // Actualizar estado con el log guardado
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

    /**
     * Resetea el estado de la UI a Idle. Útil después de mostrar un mensaje de error o éxito.
     */
    fun resetUiState() {
        _dailyLogUiState.value = DailyLogUiState.Idle
    }

    /**
     * Obtiene la fecha de hoy con la hora a medianoche (00:00:00) para consistencia.
     */
    fun getTodayDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
