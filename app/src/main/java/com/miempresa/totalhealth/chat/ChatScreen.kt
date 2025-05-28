package com.miempresa.totalhealth.chat

import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch

import com.miempresa.totalhealth.auth.UserRole
import com.miempresa.totalhealth.navigation.AppRoutes
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    userId: String,
    trainerId: String,
    userRole: UserRole,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        chatViewModel.start(userId, trainerId)
    }

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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF004D40))
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = false,
            state = listState
        ) {
            val messageList = messages ?: emptyList()

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

            items(messageList, key = { it.id }) { msg ->
                val isOwnMessage = when (userRole) {
                    UserRole.TRAINER -> msg.senderId == trainerId
                    else -> msg.senderId == userId
                }
                ChatMessageItem(msg, isOwnMessage)
            }
        }

        LaunchedEffect(messages?.size) {
            messages?.let {
                if (it.isNotEmpty()) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(it.lastIndex)
                    }
                }
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
                placeholder = {
                    Text("Escribe un mensaje", color = Color.White.copy(alpha = 0.5f))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(
                onClick = {
                    val senderId = when (userRole) {
                        UserRole.TRAINER -> trainerId
                        else -> userId
                    }
                    val senderName = when (userRole) {
                        UserRole.TRAINER -> "Entrenador"
                        else -> "Usuario"
                    }
                    chatViewModel.sendMessage(senderId, senderName, messageText.trim())
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
    val textColor = if (isOwnMessage) Color.Black else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
        }
    }
}