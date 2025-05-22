package com.miempresa.totalhealth.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

@Composable
fun ChatScreen(
    navController: NavController,
    userId: String,
    trainerId: String,
    chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(userId = userId, trainerId = trainerId) as T
        }
    })
) {
    val messages by chatViewModel.messages.collectAsState()

    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isOwnMessage = msg.senderId == userId
                ChatMessageItem(msg, isOwnMessage)
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje") }
            )
            IconButton(
                onClick = {
                    chatViewModel.sendMessage(messageText.trim())
                    messageText = ""
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, isOwnMessage: Boolean) {
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val backgroundColor = if (isOwnMessage) Color(0xFFDCF8C6) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(text = message.text)
        }
    }
}