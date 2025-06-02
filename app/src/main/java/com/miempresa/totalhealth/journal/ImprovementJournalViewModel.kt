package com.miempresa.totalhealth.journal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// Estados para la UI del Diario de Mejoras
sealed class JournalUiState {
    object Idle : JournalUiState() // Estado inicial o después de una operación completada
    object Loading : JournalUiState() // Cargando lista de entradas
    data class Success(val entries: List<ImprovementJournalEntry>) : JournalUiState() // Entradas cargadas exitosamente
    data class Error(val message: String) : JournalUiState() // Error al cargar entradas
}

// Estados para la operación de añadir/editar una entrada individual
sealed class EntryOperationUiState {
    object Idle : EntryOperationUiState() // Estado inicial
    object Loading : EntryOperationUiState() // Guardando/actualizando entrada
    object Success : EntryOperationUiState() // Operación exitosa
    data class Error(val message: String) : EntryOperationUiState() // Error en la operación
}

class ImprovementJournalViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    // StateFlow para la lista de entradas del diario
    private val _journalUiState = MutableStateFlow<JournalUiState>(JournalUiState.Idle)
    val journalUiState: StateFlow<JournalUiState> = _journalUiState.asStateFlow()

    // StateFlow para el resultado de operaciones de añadir/editar/eliminar entradas
    private val _entryOperationUiState = MutableStateFlow<EntryOperationUiState>(EntryOperationUiState.Idle)
    val entryOperationUiState: StateFlow<EntryOperationUiState> = _entryOperationUiState.asStateFlow()

    // Obtener el UID del usuario actual de forma segura
    private val userId: String?
        get() = auth.currentUser?.uid

    /**
     * Carga las entradas del diario para el usuario actual desde Firestore.
     * Las entradas se ordenan por fecha de entrada descendente (las más nuevas primero).
     *
     * Nota: Se usa la subcolección estándar y profesional "users/{userId}/journal_entries"
     * en lugar de la antigua colección raíz "improvement_journal".
     */
    fun loadJournalEntries() {
        val currentUserId = userId
        if (currentUserId == null) {
            _journalUiState.value = JournalUiState.Error("Usuario no autenticado.")
            Log.w("JournalVM", "loadJournalEntries: Usuario no autenticado.")
            return
        }

        _journalUiState.value = JournalUiState.Loading
        Log.d("JournalVM", "Cargando entradas del diario para el usuario: $currentUserId")
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(currentUserId)
                    .collection("journal_entries") // Subcolección estándar para las entradas del diario
                    .orderBy("entryDate", Query.Direction.DESCENDING) // Ordenar por fecha de la entrada
                    .get()
                    .await()

                val entries = snapshot.documents.mapNotNull { doc ->
                    // Mapear el documento de Firestore al objeto ImprovementJournalEntry
                    // y asignar el ID del documento al campo 'id' del objeto.
                    doc.toObject(ImprovementJournalEntry::class.java)?.copy(id = doc.id)
                }
                _journalUiState.value = JournalUiState.Success(entries)
                Log.d("JournalVM", "Se cargaron ${entries.size} entradas del diario.")
            } catch (e: Exception) {
                Log.e("JournalVM", "Error al cargar entradas del diario", e)
                _journalUiState.value = JournalUiState.Error("Error al cargar las entradas del diario: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Añade una nueva entrada al diario de mejoras.
     * @param title Título de la entrada (opcional).
     * @param content Contenido principal de la reflexión.
     * @param category Categoría de la entrada (opcional).
     * @param entryDate Fecha a la que se refiere la entrada.
     *
     * Nota: Se usa la subcolección estándar y profesional "users/{userId}/journal_entries"
     * en lugar de la antigua colección raíz "improvement_journal".
     */
    fun addJournalEntry(title: String, content: String, category: String?, entryDate: Date) {
        val currentUserId = userId
        if (currentUserId == null) {
            _entryOperationUiState.value = EntryOperationUiState.Error("Usuario no autenticado para añadir entrada.")
            Log.w("JournalVM", "addJournalEntry: Usuario no autenticado.")
            return
        }
        if (content.isBlank()) {
            _entryOperationUiState.value = EntryOperationUiState.Error("El contenido de la entrada no puede estar vacío.")
            return
        }

        _entryOperationUiState.value = EntryOperationUiState.Loading
        Log.d("JournalVM", "Añadiendo nueva entrada al diario para el usuario: $currentUserId")
        viewModelScope.launch {
            try {
                val newEntry = ImprovementJournalEntry(
                    // El ID se generará automáticamente por Firestore al usar .add()
                    userId = currentUserId,
                    title = title.trim(),
                    content = content.trim(),
                    category = category?.trim()?.takeIf { it.isNotBlank() }, // Guardar solo si no está vacío
                    entryDate = entryDate
                    // createdAt será añadido por Firestore si se usa @ServerTimestamp en el modelo
                )

                db.collection("users").document(currentUserId)
                    .collection("journal_entries") // Subcolección estándar para las entradas del diario
                    .add(newEntry)
                    .await()

                _entryOperationUiState.value = EntryOperationUiState.Success
                Log.d("JournalVM", "Nueva entrada del diario añadida exitosamente.")
                loadJournalEntries() // Recargar la lista de entradas para reflejar el cambio
            } catch (e: Exception) {
                Log.e("JournalVM", "Error al añadir entrada del diario", e)
                _entryOperationUiState.value = EntryOperationUiState.Error("Error al guardar la entrada: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Resetea el estado de la operación de entrada (ej. después de mostrar un Toast de éxito/error).
     */
    fun resetEntryOperationState() {
        _entryOperationUiState.value = EntryOperationUiState.Idle
    }

    /**
     * Resetea el estado de la lista de entradas del diario (ej. para forzar una recarga).
     */
    fun resetJournalUiState() {
        _journalUiState.value = JournalUiState.Idle
    }

    // Método para eliminar profesionalmente una entrada del diario de mejoras.
    fun deleteJournalEntry(entryId: String) {
        val currentUserId = userId
        if (currentUserId == null) {
            _entryOperationUiState.value = EntryOperationUiState.Error("Usuario no autenticado para eliminar entrada.")
            Log.w("JournalVM", "deleteJournalEntry: Usuario no autenticado.")
            return
        }
        _entryOperationUiState.value = EntryOperationUiState.Loading
        Log.d("JournalVM", "Eliminando entrada del diario $entryId para usuario $currentUserId")
        viewModelScope.launch {
            try {
                db.collection("users").document(currentUserId)
                    .collection("journal_entries")
                    .document(entryId)
                    .delete()
                    .await()
                _entryOperationUiState.value = EntryOperationUiState.Success
                Log.d("JournalVM", "Entrada del diario eliminada exitosamente.")
                loadJournalEntries() // Recargar lista tras eliminar
            } catch (e: Exception) {
                Log.e("JournalVM", "Error al eliminar entrada del diario", e)
                _entryOperationUiState.value = EntryOperationUiState.Error("Error al eliminar la entrada: ${e.localizedMessage}")
            }
        }
    }
}