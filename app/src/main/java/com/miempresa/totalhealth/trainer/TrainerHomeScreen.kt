package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.UserProfile
// Importa tus colores oro si los definiste en Color.kt, por ejemplo:
// import com.miempresa.totalhealth.ui.theme.GoldYellowStart
// import com.miempresa.totalhealth.ui.theme.GoldYellowMid
// import com.miempresa.totalhealth.ui.theme.GoldYellowEnd

// Colores para el degradado (puedes moverlos a tu archivo Color.kt si lo prefieres)
val GoldYellowStart = Color(0xFFFDB813) // Dorado más brillante para la parte inferior del degradado
// val GoldYellowMid = Color(0xFFFFAA00) // No se usará en este degradado específico
val GoldYellowEnd = Color(0xFFFF8C00)   // Dorado más oscuro/naranja para la parte media del degradado
val TopBackgroundColor = Color.Black    // Color superior del degradado, como en el ejemplo de Login


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    trainerViewModel: TrainerViewModel = viewModel()
) {
    val trainerScreenState by trainerViewModel.uiState.collectAsState()

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var selectedUserForFeedback by remember { mutableStateOf<UserProfile?>(null) }
    var feedbackText by remember { mutableStateOf(TextFieldValue("")) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(trainerScreenState.errorMessage) {
        trainerScreenState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            trainerViewModel.clearErrorMessage()
        }
    }
    LaunchedEffect(trainerScreenState.feedbackSentMessage) {
        trainerScreenState.feedbackSentMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            trainerViewModel.clearFeedbackMessage()
            showFeedbackDialog = false
            feedbackText = TextFieldValue("")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Portal del Entrenador") },
                // Hacemos la TopAppBar transparente para que se vea el degradado detrás
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Fondo transparente
                    titleContentColor = Color.White // Color del título que contraste con el negro/dorado
                ),
                actions = {
                    IconButton(onClick = { authViewModel.logoutUser() }) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White // Icono que contraste
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TopBackgroundColor, // Negro en la parte superior
                            GoldYellowEnd,      // Dorado oscuro/naranja en el medio
                            GoldYellowStart     // Dorado brillante en la parte inferior
                        ),
                        // Opcional: puedes ajustar los startY y endY si quieres controlar más la transición
                        // startY = 0.0f,
                        // endY = Float.POSITIVE_INFINITY
                    )
                )
                // Aplicar el padding de Scaffold DESPUÉS del fondo para que el fondo cubra toda la pantalla
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Padding para el contenido dentro del Box
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Gestión de Usuarios",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White // Color de texto que contraste con el fondo oscuro/dorado
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = trainerScreenState.searchQuery,
                    onValueChange = { trainerViewModel.onSearchQueryChanged(it) },
                    label = { Text("Buscar usuario...", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldYellowStart,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = GoldYellowStart,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                        disabledTextColor = Color.Gray,
                        errorTextColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = GoldYellowStart,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        // Un fondo sutil para el TextField si se desea, o dejarlo transparente
                        containerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (trainerScreenState.isLoadingUsers) {
                    CircularProgressIndicator(color = GoldYellowStart)
                } else if (trainerScreenState.users.isEmpty() && trainerScreenState.searchQuery.isNotBlank()) {
                    Text("No se encontraron usuarios.", color = Color.White.copy(alpha = 0.8f))
                } else if (trainerScreenState.users.isEmpty()) {
                    Text("No hay usuarios para mostrar.", color = Color.White.copy(alpha = 0.8f))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trainerScreenState.users, key = { user -> user.uid }) { user ->
                            UserItem(
                                user = user,
                                onClick = {
                                    selectedUserForFeedback = user
                                    showFeedbackDialog = true
                                    Log.d("TrainerHome", "Usuario seleccionado para feedback: ${user.email}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFeedbackDialog && selectedUserForFeedback != null) {
        AlertDialog(
            containerColor = Color.DarkGray, // Un color de fondo para el diálogo
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            onDismissRequest = {
                showFeedbackDialog = false
                feedbackText = TextFieldValue("")
            },
            title = { Text("Enviar Feedback a ${selectedUserForFeedback?.name ?: "Usuario"}") },
            text = {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Escribe tu feedback...", color = GoldYellowStart.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 5,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldYellowStart,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = GoldYellowStart,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.8f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.text.isNotBlank()) {
                            trainerViewModel.sendFeedbackToUser(selectedUserForFeedback!!.uid, feedbackText.text)
                        }
                    },
                    enabled = feedbackText.text.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldYellowStart)
                ) {
                    Text("Enviar", color = Color.Black)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showFeedbackDialog = false
                        feedbackText = TextFieldValue("")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
                ) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun UserItem(user: UserProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)) // Fondo oscuro semi-transparente para las tarjetas
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} ${user.surname}".trim().ifEmpty { "Nombre no disponible" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White // Texto blanco para contraste
                )
                Text(
                    text = user.email.ifEmpty { "Email no disponible" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f) // Texto blanco semi-transparente
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Enviar Feedback",
                tint = GoldYellowStart // Icono en dorado
            )
        }
    }
}
