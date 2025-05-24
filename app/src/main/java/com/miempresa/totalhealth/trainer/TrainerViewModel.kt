package com.miempresa.totalhealth.trainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.miempresa.totalhealth.auth.UserProfile // Asegúrate que esta es la ruta correcta a tu UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar // Necesario para fechas
import java.util.Date // Necesario para el tipo de dato en Firestore si usas Timestamp

sealed class UserListUiState {
    object Loading : UserListUiState()
    data class Success(val users: List<UserProfile>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}

// Nuevo sealed class para el estado de las métricas del dashboard
sealed class DashboardMetricsUiState {
    object Loading : DashboardMetricsUiState()
    data class Success(
        val totalUsers: Int,
        val reportsToday: Int,
        val appointmentsToday: Int
    ) : DashboardMetricsUiState()
    data class Error(val message: String) : DashboardMetricsUiState()
}

class TrainerViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore

    private val _userListUiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val userListUiState: StateFlow<UserListUiState> = _userListUiState.asStateFlow()

    // StateFlow para las métricas del Dashboard
    private val _dashboardMetricsUiState = MutableStateFlow<DashboardMetricsUiState>(DashboardMetricsUiState.Loading)
    val dashboardMetricsUiState: StateFlow<DashboardMetricsUiState> = _dashboardMetricsUiState.asStateFlow()

    init {
        fetchAllUsers()
        fetchDashboardMetrics() // Llamar para cargar las métricas
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _userListUiState.value = UserListUiState.Loading
            try {
                val result = db.collection("users")
                    // Si solo quieres usuarios con rol "USER" (no otros trainers o admins)
                    // .whereEqualTo("role", "USER") // Descomenta si es necesario
                    .get()
                    .await()

                val users = result.documents.mapNotNull { document ->
                    document.toObject(UserProfile::class.java)
                }
                _userListUiState.value = UserListUiState.Success(users)
                Log.d("TrainerViewModel", "Fetched ${users.size} users.")

            } catch (e: Exception) {
                Log.e("TrainerViewModel", "Error fetching users", e)
                _userListUiState.value = UserListUiState.Error("Error al cargar usuarios: ${e.localizedMessage}")
            }
        }
    }

    fun fetchDashboardMetrics() {
        viewModelScope.launch {
            _dashboardMetricsUiState.value = DashboardMetricsUiState.Loading
            try {
                // --- 1. Total de Usuarios ---
                val usersSnapshot = db.collection("users")
                    // .whereEqualTo("role", "USER") // Considera filtrar por rol si es necesario
                    .get()
                    .await()
                val totalUsersCount = usersSnapshot.size()

                // --- 2. Reportes de Hoy (Ejemplo con 'daily_logs') ---
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val todayStart: Date = calendar.time // Date object for start of today

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val todayEnd: Date = calendar.time // Date object for end of today

                // Asumiendo que tu campo 'date' en 'daily_logs' es un Timestamp de Firestore
                val dailyLogsTodaySnapshot = db.collection("daily_logs")
                    .whereGreaterThanOrEqualTo("date", todayStart)
                    .whereLessThanOrEqualTo("date", todayEnd)
                    .get()
                    .await()
                val reportsTodayCount = dailyLogsTodaySnapshot.size()
                // Podrías sumar de 'food_reports' también si cuentan como reportes diarios
                // y tienen un campo de fecha similar.

                // --- 3. Citas de Hoy ---
                // Esto asume que tienes una colección "appointments" y un campo "appointmentDate" (Timestamp)
                var appointmentsTodayCount = 0
                // try {
                //     val appointmentsSnapshot = db.collection("appointments")
                //         .whereGreaterThanOrEqualTo("appointmentDate", todayStart)
                //         .whereLessThanOrEqualTo("appointmentDate", todayEnd)
                //         // .whereEqualTo("trainerId", /* ID del trainer actual si es necesario */)
                //         .get()
                //         .await()
                //     appointmentsTodayCount = appointmentsSnapshot.size()
                //     Log.d("TrainerViewModel", "Fetched ${appointmentsTodayCount} appointments for today.")
                // } catch (e: Exception) {
                //     Log.w("TrainerViewModel", "Could not fetch appointments, defaulting to 0. Error: ${e.message}")
                // }


                _dashboardMetricsUiState.value = DashboardMetricsUiState.Success(
                    totalUsers = totalUsersCount,
                    reportsToday = reportsTodayCount,
                    appointmentsToday = appointmentsTodayCount
                )
                Log.d("TrainerViewModel", "Dashboard metrics: Users=$totalUsersCount, Reports=$reportsTodayCount, Appts=$appointmentsTodayCount")

            } catch (e: Exception) {
                Log.e("TrainerViewModel", "Error fetching dashboard metrics", e)
                _dashboardMetricsUiState.value = DashboardMetricsUiState.Error("Error al cargar métricas: ${e.localizedMessage}. Asegúrate de tener los índices de Firestore configurados si el error lo indica.")
            }
        }
    }
}