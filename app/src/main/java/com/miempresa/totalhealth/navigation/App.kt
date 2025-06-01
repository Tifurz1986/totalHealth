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
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Brush
import com.miempresa.totalhealth.trainer.calendar.CreateAppointmentScreen
import com.miempresa.totalhealth.trainer.calendar.AppointmentsViewModel
import com.miempresa.totalhealth.chat.ChatScreen
import com.miempresa.totalhealth.chat.ChatViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miempresa.totalhealth.foodreport.FoodReportScreen
import com.miempresa.totalhealth.progress.RecordUserProgressScreen
import com.miempresa.totalhealth.progress.ProgressScreen
import com.miempresa.totalhealth.settings.SettingsScreen
import com.miempresa.totalhealth.auth.AuthAndRoleUiState
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.LoginScreen
import com.miempresa.totalhealth.auth.RegisterScreen
import com.miempresa.totalhealth.auth.UserRole
import com.miempresa.totalhealth.ui.HomeScreen
import com.miempresa.totalhealth.ui.UserAppointmentsScreen
import com.miempresa.totalhealth.settings.EditProfileScreen
import com.miempresa.totalhealth.dailylog.DailyLogScreen // Pantalla de usuario para registrar
import com.miempresa.totalhealth.journal.ImprovementJournalScreen
import com.miempresa.totalhealth.journal.AddEditImprovementEntryScreen
import com.miempresa.totalhealth.trainer.TrainerHomeScreen
import com.miempresa.totalhealth.trainer.TrainerUserDetailScreen
import com.miempresa.totalhealth.trainer.history.food.UserFoodReportHistoryScreen
import com.miempresa.totalhealth.trainer.history.journal.UserImprovementJournalHistoryScreen
// --- IMPORTACIÓN PARA NUEVA PANTALLA DE HISTORIAL DE REGISTROS DIARIOS ---
import com.miempresa.totalhealth.trainer.history.dailylog.UserDailyLogHistoryScreen
import com.miempresa.totalhealth.ui.SubEmotionScreen
// --- FIN DE IMPORTACIÓN ---

import com.miempresa.totalhealth.trainer.injury.TrainerInjuryReportsScreen
import com.miempresa.totalhealth.ui.screens.EmotionReportEntryScreen

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME_USER = "home_user"
    const val HOME_ADMIN = "home_admin"
    const val HOME_TRAINER = "home_trainer"
    const val TRAINER_HOME = "trainer_home"
    const val TRAINER_USER_DETAIL = "trainer_user_detail/{userId}"
    const val RECORD_USER_PROGRESS = "record_user_progress/{userId}"
    const val FOOD_REPORT = "food_report"
    const val PROGRESS_SCREEN = "progress_screen"
    const val SETTINGS_SCREEN = "settings_screen"
    const val EDIT_PROFILE_SCREEN = "edit_profile_screen"
    const val DAILY_LOG_SCREEN = "daily_log_screen" // Pantalla del usuario para registrar
    const val IMPROVEMENT_JOURNAL_SCREEN = "improvement_journal_screen"
    const val ADD_EDIT_IMPROVEMENT_ENTRY_SCREEN = "add_edit_improvement_entry_screen"
    const val ADD_EDIT_IMPROVEMENT_ENTRY_SCREEN_WITH_ID = "add_edit_improvement_entry_screen/{entryId}"

    const val USER_FOOD_REPORT_HISTORY = "user_food_report_history/{userId}"
    const val USER_IMPROVEMENT_JOURNAL_HISTORY = "user_improvement_journal_history/{userId}"
    // --- NUEVA RUTA PARA HISTORIAL DE REGISTROS DIARIOS ---
    const val USER_DAILY_LOG_HISTORY = "user_daily_log_history/{userId}"
    // --- FIN DE NUEVA RUTA ---

    // --- NUEVA RUTA PARA REPORTES DE LESIONES DEL USUARIO ---
    const val USER_INJURY_REPORTS = "user_injury_reports/{userId}"
    fun userInjuryReports(userId: String) = "user_injury_reports/$userId"
    // --- FIN DE NUEVA RUTA ---

    fun trainerUserDetail(documentId: String) = "trainer_user_detail/$documentId"
    fun recordUserProgress(userId: String) = "record_user_progress/$userId"
    fun addEditImprovementEntry(entryId: String? = null) =
        if (entryId != null) "add_edit_improvement_entry_screen/$entryId"
        else "add_edit_improvement_entry_screen"
    fun userFoodReportHistory(userId: String) = "user_food_report_history/$userId"
    fun userImprovementJournalHistory(userId: String) = "user_improvement_journal_history/$userId"
    // --- FUNCIÓN PARA NUEVA RUTA ---
    fun userDailyLogHistory(userId: String) = "user_daily_log_history/$userId"
    // --- FIN DE FUNCIÓN ---
}

@Composable
fun AppNavigation() {
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
                if (currentRoute == AppRoutes.LOGIN || currentRoute == AppRoutes.REGISTER) {
                    val destination = when (currentAuthState.role) {
                        UserRole.ADMIN -> AppRoutes.HOME_ADMIN
                        UserRole.TRAINER -> AppRoutes.HOME_TRAINER
                        UserRole.USER -> AppRoutes.HOME_USER
                        UserRole.UNKNOWN, UserRole.LOADING_ROLE -> AppRoutes.HOME_USER
                    }
                    Log.d("AppNavigationEffect", "User authenticated on $currentRoute. Navigating to $destination.")
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.Idle -> {
                if (currentRoute != AppRoutes.LOGIN && currentRoute != AppRoutes.REGISTER && !currentRoute.isNullOrEmpty()) {
                    Log.d("AppNavigationEffect", "User not authenticated (State: Idle). Current route: $currentRoute. Navigating to login.")
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthAndRoleUiState.Error -> {
                if (currentRoute != AppRoutes.LOGIN && currentRoute != AppRoutes.REGISTER) {
                    Log.d("AppNavigationEffect", "Error state, navigating to login from $currentRoute")
                    navController.navigate(AppRoutes.LOGIN) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF181818), Color(0xFF23211C), Color(0xFFFFD700))
                )
            )
    ) {
        NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {
        composable(AppRoutes.LOGIN) { LoginScreen(navController, authViewModel) }
        composable(AppRoutes.REGISTER) { RegisterScreen(navController, authViewModel) }

        composable(AppRoutes.HOME_USER) { HomeScreen(navController, authViewModel) }
        // --- Pantalla de citas del usuario ---
        composable("appointments_screen") {
            val appointmentsViewModel: AppointmentsViewModel = viewModel()
            val userId = authViewModel.getCurrentUser()?.uid ?: ""
            com.miempresa.totalhealth.ui.UserAppointmentsScreen(
                navController = navController,
                appointmentsViewModel = appointmentsViewModel,
                userId = userId
            )
        }
        // --- Pantalla de chat real ---
        composable("chat_screen") {
            val chatViewModel: ChatViewModel = viewModel()
            val role = when (val state = authUiState) {
                is AuthAndRoleUiState.Authenticated -> state.role
                else -> UserRole.USER
            }
            ChatScreen(
                navController = navController,
                chatViewModel = chatViewModel,
                userId = authViewModel.getCurrentUser()?.uid ?: "",
                trainerId = "",
                userRole = role
            )
        }
        composable(AppRoutes.HOME_ADMIN) {
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
        composable(AppRoutes.HOME_TRAINER) {
            TrainerHomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppRoutes.TRAINER_HOME) {
            TrainerHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        // Calendario premium del entrenador
        composable("trainer_calendar") {
            val appointmentsViewModel: AppointmentsViewModel = viewModel()
            com.miempresa.totalhealth.trainer.calendar.TrainerCalendarSection(
                appointmentsViewModel = appointmentsViewModel,
                navController = navController
            )
        }

        composable(
            route = AppRoutes.TRAINER_USER_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("userId")
            TrainerUserDetailScreen(
                navController = navController,
                documentId = documentId
            )
        }

        composable(
            route = AppRoutes.RECORD_USER_PROGRESS,
            arguments = listOf(navArgument("userId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            RecordUserProgressScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(AppRoutes.FOOD_REPORT) { FoodReportScreen(navController) }
        composable(AppRoutes.PROGRESS_SCREEN) { ProgressScreen(navController) }
        composable(AppRoutes.SETTINGS_SCREEN) { SettingsScreen(navController) }
        composable(AppRoutes.EDIT_PROFILE_SCREEN) { EditProfileScreen(navController, authViewModel) }
        composable(AppRoutes.DAILY_LOG_SCREEN) { DailyLogScreen(navController) }

        composable(AppRoutes.IMPROVEMENT_JOURNAL_SCREEN) {
            ImprovementJournalScreen(navController = navController)
        }
        composable(AppRoutes.ADD_EDIT_IMPROVEMENT_ENTRY_SCREEN) {
            AddEditImprovementEntryScreen(navController = navController, entryId = null)
        }
        composable(
            route = AppRoutes.ADD_EDIT_IMPROVEMENT_ENTRY_SCREEN_WITH_ID,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType; })
        ) { backStackEntry ->
            AddEditImprovementEntryScreen(
                navController = navController,
                entryId = backStackEntry.arguments?.getString("entryId")
            )
        }

        composable(
            route = AppRoutes.USER_FOOD_REPORT_HISTORY,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                UserFoodReportHistoryScreen(navController = navController, userId = userId)
            } else {
                Text("Error: User ID no encontrado para historial de comida.")
            }
        }

        composable(
            route = AppRoutes.USER_IMPROVEMENT_JOURNAL_HISTORY,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                UserImprovementJournalHistoryScreen(navController = navController, userId = userId)
            } else {
                Text("Error: User ID no encontrado para historial de diario.")
            }
        }

        // --- NAVEGACIÓN PARA NUEVA PANTALLA DE HISTORIAL DE REGISTROS DIARIOS ---
        composable(
            route = AppRoutes.USER_DAILY_LOG_HISTORY,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                UserDailyLogHistoryScreen(navController = navController, userId = userId)
            } else {
                Text("Error: User ID no encontrado para historial de registros diarios.")
            }
        }
        // --- FIN DE NAVEGACIÓN ---

        // --- NAVEGACIÓN PARA REPORTES DE LESIONES DEL USUARIO ---
        composable(
            route = AppRoutes.USER_INJURY_REPORTS,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                TrainerInjuryReportsScreen(navController = navController, userId = userId)
            } else {
                Text("Error: User ID no encontrado para reportes de lesiones.")
            }
        }
        // --- NAVEGACIÓN PARA REPORTES DE LESIONES GENERALES DEL ENTRENADOR ---
        composable(
            route = "trainer_injury_reports"
        ) {
            TrainerInjuryReportsScreen(navController = navController, userId = "")
        }
        // --- FIN DE NAVEGACIÓN ---

        // Ruta para crear cita (trainer)
        composable(
            route = "create_appointment/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val trainerId = authViewModel.getCurrentUser()?.uid ?: ""
            val appointmentsViewModel: AppointmentsViewModel = viewModel()
            CreateAppointmentScreen(
                userId = userId,
                trainerId = trainerId,
                appointmentsViewModel = appointmentsViewModel,
                navController = navController
            )
        }

        // Pantalla de registro de reporte emocional
        composable("emotion_report_entry_screen") {
            EmotionReportEntryScreen(navController)
        }


        // Ruta para pantalla de subemociones
        composable(
            route = "subemotion/{emotionName}/{userId}",
            arguments = listOf(
                navArgument("emotionName") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val emotionName = backStackEntry.arguments?.getString("emotionName") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            com.miempresa.totalhealth.ui.SubEmotionScreen(navController, emotionName, userId)
        }

        // Ruta dinámica para chat con userId
        composable(
            route = "chat/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val trainerId = authViewModel.getCurrentUser()?.uid ?: ""
            ChatScreen(
                navController = navController,
                userId = userId,
                trainerId = trainerId,
                userRole = UserRole.TRAINER
            )
        }
    }
}
}