package com.miempresa.totalhealth.navigation // Paquete correcto

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
// Imports desde el paquete UI
import com.miempresa.totalhealth.ui.AuthAndRoleUiState
import com.miempresa.totalhealth.ui.AuthViewModel
import com.miempresa.totalhealth.ui.LoginScreen
import com.miempresa.totalhealth.ui.RegisterScreen
import com.miempresa.totalhealth.ui.UserRole
import com.miempresa.totalhealth.ui.menu.HomeScreen
// import com.miempresa.totalhealth.admin.AdminHomeScreen // A crear en el futuro

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel() // Se obtiene del paquete ui
    val authUiState by authViewModel.authAndRoleUiState.collectAsState()

    val startDestination = "login"

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
            // AdminHomeScreen(navController = navController, authViewModel = authViewModel) // Futuro
            Log.d("AppNavigation", "Navigated to home_admin (placeholder).")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Pantalla Admin (En construcción)", color = Color.White) // Placeholder
            }
        }
        composable("food_report") {
            FoodReportScreen(navController = navController)
        }
        composable("progress_screen") {
            ProgressScreen(navController = navController)
        }
        composable("settings_screen") {
            SettingsScreen(navController = navController)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(authUiState, currentRoute) {
        Log.d("AppNavigation", "Effect triggered. authUiState: $authUiState, currentRoute: $currentRoute")

        val currentAuthState = authUiState

        if (currentAuthState is AuthAndRoleUiState.Idle) {
            if (currentRoute != "login" && currentRoute != "register") {
                Log.d("AppNavigation", "Navigating to login because state is Idle and not on auth screens.")
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (currentAuthState is AuthAndRoleUiState.Authenticated) {
            // Este bloque es para asegurar la navegación si el estado cambia a Authenticated
            // mientras el usuario todavía está en login/register.
            // La navegación principal después de un login/registro exitoso se maneja
            // dentro de LoginScreen y RegisterScreen.
            if (currentRoute == "login" || currentRoute == "register") {
                val destination = if (currentAuthState.role == UserRole.ADMIN) "home_admin" else "home_user"
                Log.d("AppNavigation", "User authenticated on $currentRoute. Navigating to $destination.")
                navController.navigate(destination) {
                    popUpTo(currentRoute) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
