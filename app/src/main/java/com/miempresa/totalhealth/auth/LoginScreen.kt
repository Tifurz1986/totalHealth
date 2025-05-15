package com.miempresa.totalhealth.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.R // Asegúrate que esta R sea la correcta

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // Corrección: Usar authAndRoleUiState y renombrar la variable local para mayor claridad
    val authState by authViewModel.authAndRoleUiState.collectAsState()
    val email by authViewModel.email
    val password by authViewModel.password
    val passwordVisible by authViewModel.passwordVisible
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)

    LaunchedEffect(key1 = authState) {
        Log.d("LoginScreen", "authState changed: $authState")
        when (val state = authState) {
            // Corrección: Usar AuthAndRoleUiState.Authenticated
            is AuthAndRoleUiState.Authenticated -> {
                Log.d("LoginScreen", "AuthAndRoleUiState.Authenticated detected. Navigating to home.")
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true } // Limpia el backstack hasta "login"
                    launchSingleTop = true
                }
                Log.d("LoginScreen", "Navigation to home called. State is Authenticated.")
                // No es necesario resetear el estado aquí, AuthViewModel lo maneja o la navegación.
                // authViewModel.resetState() // Opcional: si quieres que el estado vuelva a Idle inmediatamente después de la acción.
                // Sin embargo, al navegar a otra pantalla, el ViewModel de esta podría reiniciarse
                // o el usuario podría volver y encontrar un estado inesperado.
                // Es mejor que el ViewModel gestione su estado interno post-autenticación.
            }
            // Corrección: Usar AuthAndRoleUiState.Error
            is AuthAndRoleUiState.Error -> {
                Log.d("LoginScreen", "AuthAndRoleUiState.Error: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState() // Vuelve a Idle para permitir nuevos intentos
            }
            is AuthAndRoleUiState.AuthLoading -> {
                Log.d("LoginScreen", "State is AuthLoading.")
                // El CircularProgressIndicator en el botón ya maneja la UI de carga para AuthLoading
            }
            is AuthAndRoleUiState.RoleLoading -> {
                Log.d("LoginScreen", "State is RoleLoading.")
                // Similar a AuthLoading, el indicador puede cubrir esto.
            }
            is AuthAndRoleUiState.Idle -> {
                Log.d("LoginScreen", "State is Idle.")
                // Estado inicial o después de un reset o logout.
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(colorNegro),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_totalhealth),
                    contentDescription = "Logo de Total Health",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Bienvenido a Total Health",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(colorNegro, colorVerdeOscuroDegradado)
                    )
                )
                .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { authViewModel.onEmailChange(it) },
                            label = { Text("Correo Electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = "Icono de Email"
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            // Corrección: Comprobar estados de carga de AuthAndRoleUiState
                            enabled = authState !is AuthAndRoleUiState.AuthLoading && authState !is AuthAndRoleUiState.RoleLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorVerdePrincipal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = colorVerdePrincipal,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                cursorColor = colorVerdePrincipal,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                                focusedLeadingIconColor = colorVerdePrincipal,
                                unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color.DarkGray,
                                disabledLabelColor = Color.Gray,
                                disabledLeadingIconColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { authViewModel.onPasswordChange(it) },
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Icono de Contraseña"
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        authViewModel.loginUser()
                                    }
                                }
                            ),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                        tint = Color.White.copy(alpha = 0.7f))
                                }
                            },
                            singleLine = true,
                            // Corrección: Comprobar estados de carga de AuthAndRoleUiState
                            enabled = authState !is AuthAndRoleUiState.AuthLoading && authState !is AuthAndRoleUiState.RoleLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorVerdePrincipal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = colorVerdePrincipal,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                cursorColor = colorVerdePrincipal,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                                focusedLeadingIconColor = colorVerdePrincipal,
                                unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
                                focusedTrailingIconColor = colorVerdePrincipal, // Añadido para consistencia
                                unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f), // Añadido para consistencia
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color.DarkGray,
                                disabledLabelColor = Color.Gray,
                                disabledLeadingIconColor = Color.Gray,
                                disabledTrailingIconColor = Color.Gray // Añadido para consistencia
                            )
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.loginUser()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            // Corrección: Comprobar estados de carga de AuthAndRoleUiState
                            enabled = authState !is AuthAndRoleUiState.AuthLoading && authState !is AuthAndRoleUiState.RoleLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorVerdePrincipal,
                                contentColor = Color.White,
                                disabledContainerColor = colorVerdePrincipal.copy(alpha = 0.5f), // Estilo para deshabilitado
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            // Corrección: Mostrar indicador si está en AuthLoading o RoleLoading
                            if (authState is AuthAndRoleUiState.AuthLoading || authState is AuthAndRoleUiState.RoleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp // Opcional: ajustar grosor
                                )
                            } else {
                                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = {
                        authViewModel.clearFields()
                        authViewModel.resetState() // Vuelve a Idle antes de navegar
                        navController.navigate("register")
                    },
                    // Corrección: Comprobar estados de carga de AuthAndRoleUiState
                    enabled = authState !is AuthAndRoleUiState.AuthLoading && authState !is AuthAndRoleUiState.RoleLoading
                ) {
                    Text("¿No tienes cuenta? Regístrate aquí", color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}