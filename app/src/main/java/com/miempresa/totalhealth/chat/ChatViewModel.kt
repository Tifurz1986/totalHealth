package com.miempresa.totalhealth.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userId: String,
    private val trainerId: String
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val conversationId = getConversationId(userId, trainerId)

    init {
        listenMessages()
    }

    private fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }

    private fun listenMessages() {
        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    _messages.value = list
                }
            }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val newMsg = ChatMessage(senderId = userId, text = text, timestamp = System.currentTimeMillis())
        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(newMsg)
    }
}