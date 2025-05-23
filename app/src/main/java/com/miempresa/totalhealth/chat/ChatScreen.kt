package com.miempresa.totalhealth.chat

import com.miempresa.totalhealth.auth.UserRole
import com.miempresa.totalhealth.navigation.AppRoutes

import com.miempresa.totalhealth.chat.ChatMessage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    userId: String,
    trainerId: String,
    userRole: UserRole,
    chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val messages by chatViewModel.messages.collectAsState()

    // Iniciar la conversación al montar el Composable
    LaunchedEffect(Unit) {
        chatViewModel.start(userId, trainerId)
    }

    var messageText by remember { mutableStateOf("") }
    val messageList = messages ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF004D40), Color(0xFF00897B), Color(0xFF004D40))
                )
            )
    ) {
        TopAppBar(
            title = {
                Text("Chat", color = Color.White)
            },
            navigationIcon = {
                IconButton(onClick = {
                    val destination = when (userRole) {
                        UserRole.TRAINER -> AppRoutes.HOME_TRAINER
                        UserRole.ADMIN -> AppRoutes.HOME_ADMIN
                        else -> AppRoutes.HOME_USER
                    }
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver al inicio",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    val destination = when (userRole) {
                        UserRole.TRAINER -> AppRoutes.HOME_TRAINER
                        UserRole.ADMIN -> AppRoutes.HOME_ADMIN
                        else -> AppRoutes.HOME_USER
                    }
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar chat",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF004D40)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = false
        ) {
            if (messageList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Empieza una conversación con tu entrenador",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            items(messageList as List<ChatMessage>, key = { msg: ChatMessage -> msg.id }) { msg: ChatMessage ->
                val isOwnMessage = msg.senderId == userId
                ChatMessageItem(msg, isOwnMessage)
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(30.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje", color = Color.White.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(
                onClick = {
                    chatViewModel.sendMessage(userId, messageText.trim())
                    messageText = ""
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color(0xFFFFD700))
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, isOwnMessage: Boolean) {
    val backgroundColor = if (isOwnMessage) Color(0xFFFFD700) else Color(0xFF1E293B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                color = if (isOwnMessage) Color.Black else Color.White,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
        }
    }
}