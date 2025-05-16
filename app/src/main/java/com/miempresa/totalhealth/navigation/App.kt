package com.miempresa.totalhealth.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miempresa.totalhealth.foodreport.FoodReportScreen
import com.miempresa.totalhealth.progress.ProgressScreen
import com.miempresa.totalhealth.settings.SettingsScreen
import com.miempresa.totalhealth.auth.AuthAndRoleUiState
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.LoginScreen
import com.miempresa.totalhealth.auth.RegisterScreen
import com.miempresa.totalhealth.auth.UserRole
import com.miempresa.totalhealth.ui.HomeScreen
import com.miempresa.totalhealth.settings.EditProfileScreen
import com.miempresa.totalhealth.dailylog.DailyLogScreen
import com.miempresa.totalhealth.journal.ImprovementJournalScreen // NUEVA IMPORTACIÓN
import com.miempresa.totalhealth.journal.AddEditImprovementEntryScreen // NUEVA IMPORTACIÓN

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.authAndRoleUiState.collectAsState()

    val startDestination = "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(authUiState, currentRoute) {
        Log.d("AppNavigation", "Effect triggered. AuthState: $authUiState, CurrentRoute: $currentRoute")
        val currentAuthState = authUiState

        when (currentAuthState) {
            is AuthAndRoleUiState.Authenticated -> {
                if (currentRoute == "login" || currentRoute == "register") {
                    val destination = if (currentAuthState.role == UserRole.ADMIN) "home_admin" else "home_user"
                    Log.d("AppNavigation", "User authenticated on $currentRoute. Navigating to $destination.")
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.Idle -> {
                if (currentRoute != "login" && currentRoute != "register") {
                    Log.d("AppNavigation", "User not authenticated (State: Idle). Current route: $currentRoute. Navigating to login.")
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.Error -> {
                Log.d("AppNavigation", "Error state observed: ${currentAuthState.message}. No navigation action from AppNavigation unless it leads to Idle.")
            }
            is AuthAndRoleUiState.AuthLoading, is AuthAndRoleUiState.RoleLoading -> {
                Log.d("AppNavigation", "Authentication or role is loading. No navigation change from AppNavigation.")
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController, authViewModel) }
        composable("home_user") { HomeScreen(navController, authViewModel) }
        composable("home_admin") {
            Log.d("AppNavigation", "Navigated to home_admin (placeholder).")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pantalla Admin (En construcción)", color = Color.Black) // O Color.White si el tema es oscuro
                    Button(onClick = { authViewModel.logoutUser() }) {
                        Text("Logout Admin")
                    }
                }
            }
        }
        composable("food_report") { FoodReportScreen(navController) }
        composable("progress_screen") { ProgressScreen(navController) }
        composable("settings_screen") { SettingsScreen(navController) }
        composable("edit_profile_screen") { EditProfileScreen(navController, authViewModel) }
        composable("daily_log_screen") { DailyLogScreen(navController) }

        // NUEVAS RUTAS PARA EL DIARIO DE MEJORAS
        composable("improvement_journal_screen") {
            ImprovementJournalScreen(navController = navController) // ViewModel se obtiene dentro
        }
        // Ruta para añadir una nueva entrada (sin entryId explícito en la ruta, se maneja internamente)
        composable("add_edit_improvement_entry_screen") {
            AddEditImprovementEntryScreen(navController = navController, entryId = null)
        }
        // Ruta para editar una entrada existente (con entryId)
        composable(
            route = "add_edit_improvement_entry_screen/{entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.StringType; /* nullable = false si siempre hay ID para editar */ })
        ) { backStackEntry ->
            AddEditImprovementEntryScreen(
                navController = navController,
                entryId = backStackEntry.arguments?.getString("entryId")
                // Asegúrate de manejar el caso en que entryId sea null si la ruta lo permite
            )
        }
    }
}
