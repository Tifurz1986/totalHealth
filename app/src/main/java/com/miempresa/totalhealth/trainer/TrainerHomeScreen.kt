// 🗂 Paquete y imports
package com.miempresa.totalhealth.trainer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    trainerViewModel: TrainerViewModel = viewModel()
) {
    val userListUiState by trainerViewModel.userListUiState.collectAsState()

    val blackToGoldGradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF141414),
            Color(0xFF23211C),
            Color(0xFFFFD700)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Entrenador", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF181818)),
                actions = {
                    IconButton(onClick = { trainerViewModel.fetchAllUsers() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refrescar", tint = Color(0xFFFFD700))
                    }
                    IconButton(onClick = { authViewModel.logoutUser() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFFFFD700))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackToGoldGradientBrush)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {

                Text(
                    text = "Panel del Entrenador",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                DashboardMetrics()

                Spacer(modifier = Modifier.height(24.dp))

                TrainerCalendarSection()

                Text(
                    text = "Usuarios Registrados",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(bottom = 20.dp, top = 24.dp)
                )

                when (val state = userListUiState) {
                    is UserListUiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                        color = Color(0xFFFFD700)
                    )
                    is UserListUiState.Success -> {
                        if (state.users.isEmpty()) {
                            Text(
                                text = "No hay usuarios registrados.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                                color = Color(0xFFE3C15B),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            UserListPro(users = state.users, navController = navController)
                        }
                    }
                    is UserListUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 30.dp, start = 16.dp, end = 16.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrainerCalendarSection() {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            text = "Calendario de Citas",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFFFFD700),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF23211C))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                HorizontalCalendar(
                    startDate = today.minusDays(15),
                    endDate = today.plusDays(15),
                    selectedDate = selectedDate,
                    onDateSelected = { date -> selectedDate = date }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Citas para ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Sesión con María\n• Evaluación con Jorge",
                    fontSize = 14.sp,
                    color = Color(0xFFE7DFA1),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}


@Composable
fun HorizontalCalendar(
    startDate: LocalDate,
    endDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val dates = remember {
        generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .toList()
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val backgroundColor = if (isSelected) Color(0xFFFFD700) else Color.DarkGray
            val textColor = if (isSelected) Color.Black else Color.White

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onDateSelected(date) }
                    .padding(12.dp)
            ) {
                Text(
                    text = date.dayOfWeek.name.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun UserListPro(users: List<UserProfile>, navController: NavController) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(users) { user ->
            UserItemPro(user = user, navController = navController)
        }
    }
}

@Composable
fun UserItemPro(user: UserProfile, navController: NavController) {
    val TAG = "UserItemClick"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(28.dp))
            .clickable {
                Log.d(TAG, "UserItem clicked. User Name: ${user.fullName}, User Email: ${user.email}, User UID: '${user.uid}'")
                if (user.uid.isNotBlank()) {
                    navController.navigate("trainer_user_detail/${user.uid}")
                } else {
                    Log.w(TAG, "UID is blank. Navigation skipped.")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C).copy(alpha = 0.98f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Icono de Usuario",
                    modifier = Modifier.size(46.dp),
                    tint = Color(0xFF23211C)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName.ifEmpty { "Nombre no disponible" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email.ifEmpty { "Email no disponible" },
                    fontSize = 15.sp,
                    color = Color(0xFFEEE8BB)
                )
                if (user.role.isNotBlank()) {
                    Spacer(modifier = Modifier.height(7.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD700), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF23211C),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetrics() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MetricCard("Usuarios Activos", "25")
        }
        Column(modifier = Modifier.weight(1f)) {
            MetricCard("Reportes Hoy", "14")
        }
        Column(modifier = Modifier.weight(1f)) {
            MetricCard("Citas Hoy", "3")
        }
    }
}

@Composable
fun MetricCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = Color(0xFFFFD700), fontSize = 14.sp)
            Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}