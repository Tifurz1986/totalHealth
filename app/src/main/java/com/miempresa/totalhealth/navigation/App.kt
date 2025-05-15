package com.miempresa.totalhealth.navigation // Paquete correcto

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // Importación añadida para Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button // Importación añadida para Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.miempresa.totalhealth.foodreport.FoodReportScreen
import com.miempresa.totalhealth.progress.ProgressScreen
import com.miempresa.totalhealth.settings.SettingsScreen
// Imports desde el paquete auth (asumiendo que AuthViewModel está en com.miempresa.totalhealth.auth)
import com.miempresa.totalhealth.auth.AuthAndRoleUiState
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.LoginScreen
import com.miempresa.totalhealth.auth.RegisterScreen
import com.miempresa.totalhealth.auth.UserRole
import com.miempresa.totalhealth.ui.HomeScreen // HomeScreen del paquete ui
// import com.miempresa.totalhealth.admin.AdminHomeScreen // A crear en el futuro

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Asegúrate que el ViewModel se obtenga correctamente. Si usas Hilt, sería hiltViewModel()
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.authAndRoleUiState.collectAsState()

    // Observamos la ruta actual para tomar decisiones de navegación más precisas
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Start destination es login
    val startDestination = "login"

    // Efecto para manejar la navegación global basada en el estado de autenticación
    LaunchedEffect(authUiState, currentRoute) {
        Log.d("AppNavigation", "Effect triggered. AuthState: $authUiState, CurrentRoute: $currentRoute")

        val currentAuthState = authUiState // Capturar para evitar problemas de concurrencia en when

        when (currentAuthState) {
            is AuthAndRoleUiState.Authenticated -> {
                // Si el usuario está autenticado y se encuentra en login o register,
                // navegar a la pantalla principal correspondiente.
                if (currentRoute == "login" || currentRoute == "register") {
                    val destination = if (currentAuthState.role == UserRole.ADMIN) "home_admin" else "home_user"
                    Log.d("AppNavigation", "User authenticated on $currentRoute. Navigating to $destination.")
                    navController.navigate(destination) {
                        // Limpiar el backstack hasta el inicio del grafo de autenticación (login)
                        // para que el usuario no pueda volver a login/register con el botón "atrás".
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true // Evita múltiples instancias de la pantalla home
                    }
                }
            }
            is AuthAndRoleUiState.Idle, is AuthAndRoleUiState.Error -> {
                // Si el usuario no está autenticado (Idle después de logout o error que lo llevó a Idle),
                // y no está ya en las pantallas de login o registro,
                // entonces navegar a la pantalla de login.
                // Esto es crucial para el logout.
                if (currentAuthState is AuthAndRoleUiState.Idle &&
                    currentRoute != "login" && currentRoute != "register") {
                    Log.d("AppNavigation", "User not authenticated (State: Idle). Current route: $currentRoute. Navigating to login.")
                    navController.navigate("login") {
                        // Limpiar todo el backstack actual porque estamos volviendo al flujo de autenticación.
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                // Si es un AuthAndRoleUiState.Error y ya estamos en login/register, la pantalla local mostrará el error.
            }
            is AuthAndRoleUiState.AuthLoading, is AuthAndRoleUiState.RoleLoading -> {
                // Estado de carga, no se requiere acción de navegación aquí.
                // Las pantallas individuales (Login/Register) mostrarán sus propios indicadores.
                Log.d("AppNavigation", "Authentication or role is loading. No navigation change from AppNavigation.")
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home_user") {
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home_admin") {
            Log.d("AppNavigation", "Navigated to home_admin (placeholder).")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Placeholder UI para Admin con Column y Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pantalla Admin (En construcción)", color = Color.Black) // Asumiendo tema claro, o ajustar color
                    Button(onClick = {
                        authViewModel.logoutUser()
                        // La navegación a "login" será manejada por el LaunchedEffect de AppNavigation
                        // cuando el estado cambie a Idle después del logout.
                    }) {
                        Text("Logout Admin")
                    }
                }
            }
        }
        composable("food_report") {
            // Asegúrate que FoodReportScreen pueda manejar el authViewModel si necesita info de usuario o logout
            FoodReportScreen(navController = navController /*, authViewModel = authViewModel */)
        }
        composable("progress_screen") {
            ProgressScreen(navController = navController /*, authViewModel = authViewModel */)
        }
        composable("settings_screen") {
            SettingsScreen(navController = navController /*, authViewModel = authViewModel */)
        }
        // Otras rutas...
    }
}
