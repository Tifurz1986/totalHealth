package com.miempresa.totalhealth.trainer.history.journal

// Importaciones de AndroidX y Kotlin Coroutines
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

// Importaciones de Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject // Para convertir documentos de Firestore a objetos

// Importación de tu data class
import com.miempresa.totalhealth.journal.ImprovementJournalEntry

// Importaciones de KotlinX Flows
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserImprovementJournalHistoryViewModel(private val userId: String) : ViewModel() {

    private val _journalEntries = MutableStateFlow<List<ImprovementJournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<ImprovementJournalEntry>> = _journalEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // --- AJUSTA ESTOS VALORES SI ES NECESARIO ---
                // Nombre de la colección en Firestore para las entradas del diario.
                val collectionName = "improvement_journal_entries"
                // Nombre del campo en tus documentos que almacena el ID del usuario.
                val userIdField = "userId"
                // Nombre del campo en tus documentos que almacena la fecha para ordenar.
                // Basado en tu ImprovementJournalEntry.kt, podría ser "entryDate" o "createdAt".
                // Usaré "entryDate" ya que parece ser la fecha principal de la entrada.
                // Si usas "createdAt", asegúrate que el tipo en Firestore permita ordenamiento (Timestamp).
                val dateField = "entryDate"
                // -----------------------------------------

                val snapshot = db.collection(collectionName)
                    .whereEqualTo(userIdField, userId)
                    .orderBy(dateField, Query.Direction.DESCENDING) // Ordenar por la fecha de la entrada
                    .get()
                    .await()

                _journalEntries.value = snapshot.documents.mapNotNull { doc ->
                    // Usar toObject() para convertir el documento directamente a tu data class.
                    // El .copy(id = doc.id) es útil si el 'id' en tu data class
                    // no está anotado para ser llenado por Firestore o si quieres asegurarte
                    // que el ID del documento sea el que se usa.
                    doc.toObject<ImprovementJournalEntry>()?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("JournalHistoryVM", "Error loading journal entries for user $userId: ${e.message}", e)
                _error.value = "Error al cargar el diario: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val userId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserImprovementJournalHistoryViewModel::class.java)) {
                return UserImprovementJournalHistoryViewModel(userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
