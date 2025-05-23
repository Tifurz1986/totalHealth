package com.miempresa.totalhealth.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Estado interno de la conversación
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    /**
     * Flujo de mensajes de la conversación.
     */
    val messages: StateFlow<List<ChatMessage>> get() = _messages

    private var conversationId: String = ""

    private val _error = MutableStateFlow<String?>(null)
    /**
     * Flujo de errores para mostrar en la UI.
     */
    val error: StateFlow<String?> get() = _error

    /**
     * Inicia la conversación con IDs de usuario y entrenador.
     *
     * @param userId ID del usuario.
     * @param trainerId ID del entrenador.
     */
    fun start(userId: String, trainerId: String) {
        conversationId = generateConversationId(userId, trainerId)
        Log.d("ChatViewModel", "Iniciando conversación con ID: $conversationId")
        observeMessages()
    }

    /**
     * Genera un ID de conversación único y ordenado alfabéticamente.
     *
     * @param user1 ID del primer usuario.
     * @param user2 ID del segundo usuario.
     * @return ID de conversación único.
     */
    private fun generateConversationId(user1: String, user2: String): String {
        val id = if (user1 < user2) "$user1-$user2" else "$user2-$user1"
        Log.v("ChatViewModel", "Generado conversationId: $id")
        return id
    }

    /**
     * Escucha los mensajes en tiempo real desde Firestore.
     */
    private fun observeMessages() {
        if (conversationId.isBlank()) {
            val warningMsg = "Conversation ID is blank. Cannot observe messages."
            Log.w("ChatViewModel", warningMsg)
            _error.value = warningMsg
            return
        }

        Log.d("ChatViewModel", "Observando mensajes para conversationId: $conversationId")

        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error al escuchar mensajes", error)
                    _error.value = "Error al escuchar mensajes: ${error.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    Log.v("ChatViewModel", "Mensajes recibidos: ${list.size}")
                    _messages.value = list
                } else {
                    Log.w("ChatViewModel", "Snapshot de mensajes es null")
                }
            }
    }

    /**
     * Envía un nuevo mensaje de texto si es válido.
     *
     * @param userId ID del usuario que envía el mensaje.
     * @param text Texto del mensaje.
     */
    fun sendMessage(userId: String, text: String) {
        if (text.isBlank()) {
            Log.w("ChatViewModel", "Intento de enviar mensaje vacío o en blanco")
            return
        }
        if (conversationId.isBlank()) {
            val warningMsg = "No se ha iniciado la conversación."
            Log.w("ChatViewModel", warningMsg)
            _error.value = warningMsg
            return
        }

        try {
            val newMsg = ChatMessage(
                senderId = userId,
                text = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            Log.d("ChatViewModel", "Enviando mensaje: $newMsg")

            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(newMsg)
                .addOnSuccessListener {
                    Log.d("ChatViewModel", "Mensaje enviado correctamente con ID: ${it.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error al enviar mensaje", e)
                    _error.value = "Error al enviar mensaje: ${e.message}"
                }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Excepción inesperada al enviar mensaje", e)
            _error.value = "Error inesperado al enviar mensaje: ${e.message}"
        }
    }

    /**
     * Limpia el error actual después de que la UI lo haya mostrado.
     */
    fun clearError() {
        Log.d("ChatViewModel", "Error limpiado")
        _error.value = null
    }
}