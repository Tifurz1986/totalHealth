package com.miempresa.totalhealth.trainer.history.dailylog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject // Asegúrate que es esta y no otra
import com.miempresa.totalhealth.dailylog.DailyLog // Importa la clase DailyLog corregida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDailyLogHistoryViewModel(private val userId: String) : ViewModel() {

    private val _dailyLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val dailyLogs: StateFlow<List<DailyLog>> = _dailyLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    // Definir constantes fuera del try para que sean accesibles en el catch
    private val collectionName = "daily_logs"
    private val userIdField = "userId"
    private val dateField = "date" // Campo 'date' en tu DailyLog.kt


    init {
        loadDailyLogs()
    }

    private fun loadDailyLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("UserDailyLogHistoryVM", "Fetching daily logs for user: $userId, from collection: $collectionName, ordering by $dateField DESC")

                // Aquí ya se usa userId recibido por parámetro
                val snapshot = db.collection(collectionName)
                    .whereEqualTo(userIdField, userId)
                    .orderBy(dateField, Query.Direction.DESCENDING)
                    .get()
                    .await()

                Log.d("UserDailyLogHistoryVM", "Snapshot received, ${snapshot.size()} documents.")

                _dailyLogs.value = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Mapear a DailyLog y asegurarse que el ID del documento se asigna al campo 'id' del objeto
                        val log = doc.toObject<DailyLog>()?.copy(id = doc.id)
                        if (log == null) {
                            Log.e("UserDailyLogHistoryVM", "Failed to convert document ${doc.id} to DailyLog. Object is null.")
                        }
                        log
                    } catch (e: Exception) {
                        Log.e("UserDailyLogHistoryVM", "Error converting document ${doc.id} to DailyLog", e)
                        null // Devuelve null si la conversión falla para este documento
                    }
                }
                Log.d("UserDailyLogHistoryVM", "Daily logs loaded: ${_dailyLogs.value.size}")

            } catch (e: Exception) {
                Log.e("UserDailyLogHistoryVM", "Error loading daily logs for user $userId: ${e.message}", e)
                if (e.message?.contains("FAILED_PRECONDITION") == true && e.message?.contains("index") == true) {
                    // Usar las constantes definidas fuera del try
                    _error.value = "Error: Se requiere un índice en Firestore para la colección '$collectionName' con los campos consultados. Revisa el Logcat para detalles y crea el índice sugerido."
                    Log.w("UserDailyLogHistoryVM", "Firestore index missing for $collectionName. Query: $userIdField == $userId, orderBy $dateField DESC. Firestore error: ${e.message}")
                } else {
                    _error.value = "Error al cargar registros diarios: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Borra un registro diario (DailyLog) dado su ID de documento en Firestore.
     * @param dailyLogId El ID del documento DailyLog a borrar.
     * @param onResult Callback que recibe true si el borrado fue exitoso, false si falló.
     */
    fun deleteDailyLog(dailyLogId: String, onResult: (Boolean) -> Unit = {}) {
        db.collection(collectionName).document(dailyLogId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Actualiza las notas (u otros campos que necesites) de un registro diario.
     * @param dailyLogId El ID del documento DailyLog a editar.
     * @param newNotes El texto actualizado para el campo de notas.
     * @param onResult Callback que recibe true si la actualización fue exitosa, false si falló.
     */
    fun updateDailyLog(dailyLogId: String, newNotes: String, onResult: (Boolean) -> Unit = {}) {
        db.collection(collectionName).document(dailyLogId)
            .update("notes", newNotes)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val userId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserDailyLogHistoryViewModel::class.java)) {
                return UserDailyLogHistoryViewModel(userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}