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
// Asegúrate de importar la nueva pantalla
import com.miempresa.totalhealth.progress.RecordUserProgressScreen
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
import com.miempresa.totalhealth.journal.ImprovementJournalScreen
import com.miempresa.totalhealth.journal.AddEditImprovementEntryScreen
import com.miempresa.totalhealth.trainer.TrainerHomeScreen
import com.miempresa.totalhealth.trainer.TrainerUserDetailScreen

@Composable
fun AppNavigation() { // <--- RENOMBRADA DE App() A AppNavigation()
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.authAndRoleUiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(authUiState, currentRoute) {
        Log.d("AppNavigationEffect", "Effect triggered. AuthState: $authUiState, CurrentRoute: $currentRoute")
        val currentAuthState = authUiState

        when (currentAuthState) {
            is AuthAndRoleUiState.Authenticated -> {
                if (currentRoute == "login" || currentRoute == "register") {
                    val destination = when (currentAuthState.role) {
                        UserRole.ADMIN -> "home_admin"
                        UserRole.TRAINER -> "home_trainer"
                        UserRole.USER -> "home_user"
                        UserRole.UNKNOWN, UserRole.LOADING_ROLE -> "home_user" // Default a home_user
                    }
                    Log.d("AppNavigationEffect", "User authenticated on $currentRoute. Navigating to $destination.")
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.Idle -> {
                if (currentRoute != "login" && currentRoute != "register" && !currentRoute.isNullOrEmpty()) { // Modificado para no navegar desde rutas vacías/nulas
                    Log.d("AppNavigationEffect", "User not authenticated (State: Idle). Current route: $currentRoute. Navigating to login.")
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.Error -> {
                if (currentRoute != "login" && currentRoute != "register") {
                    Log.d("AppNavigationEffect", "Error state, navigating to login from $currentRoute")
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.AuthLoading, is AuthAndRoleUiState.RoleLoading -> {
                Log.d("AppNavigationEffect", "Authentication or role is loading. No navigation change.")
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController, authViewModel) }

        composable("home_user") { HomeScreen(navController, authViewModel) }
        composable("home_admin") {
            Log.d("NavHost", "Navigated to home_admin (placeholder).")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pantalla Admin (En construcción)", color = Color.Black)
                    Button(onClick = { authViewModel.logoutUser() }) {
                        Text("Logout Admin")
                    }
                }
            }
        }
        composable("home_trainer") {
            TrainerHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(
            route = "trainer_user_detail/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            TrainerUserDetailScreen(
                navController = navController,
                userId = userId
            )
        }

        // NUEVA RUTA AÑADIDA AQUÍ
        composable(
            route = "record_user_progress/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            RecordUserProgressScreen( // Llama a la nueva pantalla
                navController = navController,
                userId = userId
            )
        }

        composable("food_report") { FoodReportScreen(navController) }
        composable("progress_screen") { ProgressScreen(navController) } // Esta es la pantalla de progreso general
        composable("settings_screen") { SettingsScreen(navController) }
        composable("edit_profile_screen") { EditProfileScreen(navController, authViewModel) }
        composable("daily_log_screen") { DailyLogScreen(navController) }

        composable("improvement_journal_screen") {
            ImprovementJournalScreen(navController = navController)
        }
        composable("add_edit_improvement_entry_screen") {
            AddEditImprovementEntryScreen(navController = navController, entryId = null)
        }
        composable(
            route = "add_edit_improvement_entry_screen/{entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            AddEditImprovementEntryScreen(
                navController = navController,
                entryId = backStackEntry.arguments?.getString("entryId")
            )
        }
    }
}