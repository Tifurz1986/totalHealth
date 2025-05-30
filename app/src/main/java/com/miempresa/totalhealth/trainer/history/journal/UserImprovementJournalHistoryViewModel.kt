package com.miempresa.totalhealth.trainer.history.journal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.miempresa.totalhealth.journal.ImprovementJournalEntry
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
        listenJournalEntries()
    }

    // Usamos addSnapshotListener como en los otros history para que reaccione en tiempo real
    private fun listenJournalEntries() {
        Log.d("ImprovementJournalVM", "Listening improvement_journal for user $userId")
        db.collection("users")
            .document(userId)
            .collection("improvement_journal")
            .orderBy("entryDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ImprovementJournalVM", "Error loading improvement journal: ${e.message}", e)
                    _error.value = "Error al cargar el diario de mejoras: ${e.localizedMessage}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    Log.d("ImprovementJournalVM", "Snapshot received, ${snapshot.size()} documents.")
                    val entries = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(ImprovementJournalEntry::class.java)?.copy(id = doc.id)
                        } catch (ex: Exception) {
                            Log.e("ImprovementJournalVM", "Error mapping document: ${ex.message}")
                            null
                        }
                    }
                    _journalEntries.value = entries
                    _isLoading.value = false
                    Log.d("ImprovementJournalVM", "Improvement journals loaded: ${entries.size}")
                } else {
                    _journalEntries.value = emptyList()
                    _isLoading.value = false
                    Log.d("ImprovementJournalVM", "Snapshot null. No entries loaded.")
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