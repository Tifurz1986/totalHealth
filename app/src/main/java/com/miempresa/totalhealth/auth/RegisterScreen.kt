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
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // Corrección: Usar authAndRoleUiState y renombrar la variable local
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
        Log.d("RegisterScreen", "authState changed: $authState")
        when (val state = authState) {
            // Corrección: Usar AuthAndRoleUiState.Authenticated para indicar registro exitoso
            // En el flujo de registro, Authenticated significa que el usuario se creó
            // y ahora puede proceder a iniciar sesión.
            is AuthAndRoleUiState.Authenticated -> {
                Log.d("RegisterScreen", "AuthAndRoleUiState.Authenticated (Registro exitoso) detected. Navigating to login.")
                Toast.makeText(context, "Registro completado. Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                authViewModel.clearFields() // Limpiar campos después de un registro exitoso
                // authViewModel.resetState() // Importante: resetear a Idle ANTES de navegar a login,
                // para que login no reaccione a un estado Authenticated
                // que no proviene de un intento de login.
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true } // Limpia el backstack hasta "register"
                    launchSingleTop = true
                }
                // El resetState se hace en AuthViewModel después de un registro exitoso en algunos casos,
                // o se puede manejar aquí para asegurar que la pantalla de login no lo interprete mal.
                // Es crucial que al llegar a LoginScreen, el estado sea Idle o uno que LoginScreen espere al inicio.
                // AuthViewModel.registerUser() ya pone Authenticated.
                // Si navegas y el AuthViewModel es el mismo (compartido), LoginScreen vería Authenticated.
                // Por eso, clearFields y resetState antes de navegar es más seguro.
                // La lógica actual en AuthViewModel es:
                // _authAndRoleUiState.value = AuthAndRoleUiState.Authenticated(firebaseUser, UserRole.USER)
                // clearFields()
                // Así que el resetState aquí asegura que si el usuario vuelve a Register, esté Idle.
                // Y LoginScreen comenzará con el estado que tenga el ViewModel en ese momento.
                // La navegación con popUpTo(login) { inclusive = true} podría ser mejor si registro es el inicio de ese flujo.
                // Si popUpTo("register") es correcto, entonces LoginScreen no debería reaccionar a Authenticated inmediatamente.
                // authViewModel.resetState() // <-- Es buena idea llamarlo después de clearFields() y ANTES de navegar.
                // Esto previene que si el ViewModel es compartido y no se destruye,
                // la siguiente pantalla (Login) reaccione a un estado Authenticated
                // no intencionado por ella.
                // Re-evaluando: AuthViewModel.registerUser() ya llama a clearFields().
                // El estado Authenticated es el resultado del registro. Es correcto.
                // El problema podría ser si LoginScreen reacciona a este Authenticated sin un login explícito.
                // Para RegisterScreen, este es el final exitoso del flujo.
                // Si queremos que LoginScreen no reaccione, el reset debe ocurrir en el ViewModel
                // o justo antes de la navegación aquí.
                // Como LoginScreen ahora tiene su propio LaunchedEffect que reacciona a Authenticated
                // para navegar a home, es VITAL que el estado NO sea Authenticated cuando
                // se llega a LoginScreen DESPUÉS de un registro.
                // Solución: AuthViewModel debería volver a Idle después de un registro exitoso si la navegación es a Login.
                // O, RegisterScreen fuerza un reset ANTES de navegar.
                // La llamada a `authViewModel.clearFields()` y `authViewModel.resetState()`
                // en el TextButton de "Ya tienes cuenta?" es un buen ejemplo.
                // Hagamos lo mismo aquí para consistencia.
                authViewModel.resetState() // Asegura que cuando se navegue a login, el estado no sea "Authenticated" del registro.

                Log.d("RegisterScreen", "Navigation to login called. State reset to Idle.")
            }
            // Corrección: Usar AuthAndRoleUiState.Error
            is AuthAndRoleUiState.Error -> {
                Log.d("RegisterScreen", "AuthAndRoleUiState.Error: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState() // Vuelve a Idle para permitir nuevos intentos
            }
            is AuthAndRoleUiState.AuthLoading -> {
                Log.d("RegisterScreen", "State is AuthLoading.")
                // El CircularProgressIndicator en el botón maneja la UI de carga
            }
            is AuthAndRoleUiState.RoleLoading -> {
                // Este estado no debería ocurrir durante el registro simple.
                // Si ocurre, es un estado inesperado aquí.
                Log.w("RegisterScreen", "Unexpected state: RoleLoading during registration flow.")
            }
            is AuthAndRoleUiState.Idle -> {
                Log.d("RegisterScreen", "State is Idle.")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
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
                        .size(80.dp)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
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
                            // Corrección: Comprobar estado de carga AuthLoading (RoleLoading no es relevante aquí)
                            enabled = authState !is AuthAndRoleUiState.AuthLoading,
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
                            // Corrección: Comprobar estado de carga AuthLoading
                            enabled = authState !is AuthAndRoleUiState.AuthLoading,
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
                            // Corrección: Comprobar estado de carga AuthLoading
                            enabled = authState !is AuthAndRoleUiState.AuthLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorVerdePrincipal,
                                contentColor = Color.White,
                                disabledContainerColor = colorVerdePrincipal.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            // Corrección: Mostrar indicador si está en AuthLoading
                            if (authState is AuthAndRoleUiState.AuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
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
                        authViewModel.resetState() // Vuelve a Idle antes de navegar
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    // Corrección: Comprobar estado de carga AuthLoading
                    enabled = authState !is AuthAndRoleUiState.AuthLoading
                ) {
                    Text("¿Ya tienes cuenta? Inicia sesión", color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}