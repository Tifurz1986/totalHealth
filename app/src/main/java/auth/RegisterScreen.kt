package auth

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
import com.miempresa.totalhealth.R

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val email by authViewModel.email
    val password by authViewModel.password
    val passwordVisible by authViewModel.passwordVisible
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val colorNegro = Color.Black
    // Usaremos los mismos colores principales que en Login para consistencia
    val colorVerdePrincipal = Color(0xFF00897B) // Teal 700
    val colorVerdeOscuroDegradado = Color(0xFF004D40) // Verde más oscuro para el final del degradado


    LaunchedEffect(key1 = uiState) {
        Log.d("RegisterScreen", "uiState changed: $uiState")
        when (val state = uiState) {
            is AuthUiState.Success -> {
                Log.d("RegisterScreen", "AuthUiState.Success detected. Navigating to login.")
                Toast.makeText(context, "Registro completado. Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                authViewModel.clearFields() // Limpiar campos después de un registro exitoso
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                    launchSingleTop = true
                }
                // El estado ya es Success, no es necesario resetearlo a Idle aquí,
                // ya que el usuario será redirigido a Login y el estado se manejará allí.
                // authViewModel.resetState() // Comentado para mantener el estado Success hasta la siguiente acción
                Log.d("RegisterScreen", "Navigation to login called. State remains Success.")
            }
            is AuthUiState.Error -> {
                Log.d("RegisterScreen", "AuthUiState.Error: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sección superior con fondo negro sólido para el logo y bienvenida
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f) // Un poco menos de peso para el registro, más espacio para el form
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
                        .size(80.dp) // Un poco más pequeño para el registro
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Crea tu Cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Únete a Total Health",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }

        // Sección inferior con degradado y tarjeta de registro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f) // Más peso para el formulario
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorNegro,
                            colorVerdeOscuroDegradado
                        )
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
                            enabled = uiState != AuthUiState.Loading,
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
                            label = { Text("Contraseña (mín. 6 caracteres)") },
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
                                        authViewModel.registerUser()
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
                            enabled = uiState != AuthUiState.Loading,
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
                                focusedTrailingIconColor = colorVerdePrincipal,
                                unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color.DarkGray,
                                disabledLabelColor = Color.Gray,
                                disabledLeadingIconColor = Color.Gray,
                                disabledTrailingIconColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.registerUser()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = uiState != AuthUiState.Loading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorVerdePrincipal,
                                contentColor = Color.White
                            )
                        ) {
                            if (uiState == AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = {
                        authViewModel.clearFields()
                        authViewModel.resetState()
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    enabled = uiState != AuthUiState.Loading
                ) {
                    Text("¿Ya tienes cuenta? Inicia sesión", color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
