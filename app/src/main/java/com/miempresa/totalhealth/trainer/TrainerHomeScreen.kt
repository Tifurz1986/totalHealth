package com.miempresa.totalhealth.trainer

import com.miempresa.totalhealth.trainer.calendar.TrainerAppointmentsCalendarSection
import com.miempresa.totalhealth.trainer.DashboardMetricsUiState
import com.miempresa.totalhealth.trainer.UserListUiState

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import com.miempresa.totalhealth.trainer.calendar.AppointmentsViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

val GoldColor = Color(0xFFFFD700)
val DarkCharcoalColor = Color(0xFF23211C)
val DeepBlackColor = Color(0xFF141414)
val CardBackgroundColor = Color(0xFF181818)
val SoftGoldColor = Color(0xFFE3C15B)
val PaleGoldColor = Color(0xFFEEE8BB)

fun LazyListScope.UserListPro(users: List<UserProfile>, navController: NavController) {
    val uniqueUsers = users.filter { it.uid.isNotBlank() }.distinctBy { it.uid }
    if (uniqueUsers.isEmpty()) {
        item {
            Text(
                text = "No hay usuarios para mostrar.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                color = SoftGoldColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        items(uniqueUsers) { user ->
            UserItemPro(user = user, navController = navController)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    trainerViewModel: TrainerViewModel = viewModel(),
    appointmentsViewModel: AppointmentsViewModel = viewModel()
) {
    val userListUiState by trainerViewModel.userListUiState.collectAsState()
    val dashboardMetricsState by trainerViewModel.dashboardMetricsUiState.collectAsState()

    val currentUserEmail = authViewModel.getCurrentUser()?.email
    val trainerName = currentUserEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Entrenador"

    val blackToGoldGradientBrush = Brush.verticalGradient(
        colors = listOf(DeepBlackColor, DarkCharcoalColor, GoldColor)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bienvenido, $trainerName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldColor,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackgroundColor),
                actions = {
                    IconButton(
                        onClick = {
                            trainerViewModel.fetchAllUsers()
                            trainerViewModel.fetchDashboardMetrics()
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refrescar", tint = GoldColor)
                    }
                    IconButton(onClick = { authViewModel.logoutUser() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = GoldColor)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepBlackColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .shadow(12.dp)
                    .padding(bottom = 2.dp)
            ) {
                val currentRoute = navController.currentDestination?.route

                // INICIO
                NavigationBarItem(
                    selected = currentRoute == "trainer_home",
                    onClick = {
                        navController.navigate("trainer_home") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Inicio",
                            modifier = Modifier.size(30.dp),
                            tint = if (currentRoute == "trainer_home") GoldColor else Color.LightGray
                        )
                    },
                    label = {
                        Text(
                            "Inicio",
                            color = if (currentRoute == "trainer_home") GoldColor else Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )

                // LESIONADOS
                NavigationBarItem(
                    selected = currentRoute == "trainer_injury_reports",
                    onClick = {
                        navController.navigate("trainer_injury_reports") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Assessment,
                            contentDescription = "Lesionados",
                            modifier = Modifier.size(30.dp),
                            tint = if (currentRoute == "trainer_injury_reports") GoldColor else Color.LightGray
                        )
                    },
                    label = {
                        Text(
                            "Lesionados",
                            color = if (currentRoute == "trainer_injury_reports") GoldColor else Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackToGoldGradientBrush)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SectionTitle(title = "Gestión de Citas")
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                    ) {
                        TrainerAppointmentsCalendarSection(
                            trainerId = authViewModel.getCurrentUser()?.uid ?: "",
                            appointmentsViewModel = appointmentsViewModel
                        )
                    }
                }

                item {
                    FullCalendarAccessCard(navController = navController)
                }

                item {
                    GoldHorizontalDivider()
                }

                item {
                    SectionTitle(title = "Usuarios Registrados")
                }

                when (val state = userListUiState) {
                    is UserListUiState.Loading -> item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(vertical = 30.dp),
                            color = GoldColor
                        )
                    }
                    is UserListUiState.Success -> {
                        val users = state.users.filter { it.uid.isNotBlank() }.distinctBy { it.uid }
                        if (users.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay usuarios para mostrar.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    color = SoftGoldColor,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(users) { user ->
                                UserItemPro(user = user, navController = navController)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                    is UserListUiState.Error -> item {
                        ErrorStateDisplay(message = "Error al cargar usuarios: ${state.message}")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 22.sp, // Ligeramente más pequeño que el saludo, pero prominente
        fontWeight = FontWeight.Bold,
        color = GoldColor, // O Color.White si prefieres variar
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun GoldHorizontalDivider() {
    HorizontalDivider(
        color = GoldColor.copy(alpha = 0.8f), // Un poco más sutil
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun FullCalendarAccessCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .border(2.dp, GoldColor, RoundedCornerShape(18.dp))
            .clickable { navController.navigate("trainer_calendar") }, // Ruta a la pantalla de calendario completo
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GoldColor, DarkCharcoalColor, CardBackgroundColor)
                    )
                )
                .padding(horizontal = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth, // Icono más descriptivo
                contentDescription = "Ver Calendario Completo",
                tint = Color.Black, // Para contraste con el fondo dorado
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = "Calendario Completo y Citas",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp // Ligeramente ajustado
            )
        }
    }
}



@Composable
fun ErrorStateDisplay(message: String) {
    Text(
        text = message,
        color = Color.Red,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(CardBackgroundColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    )
}






@Composable
fun HorizontalCalendar( // Este Composable parece estar definido en otro archivo (TrainerAppointmentsCalendarSection), lo incluyo aquí para completitud si fuera local.
    startDate: LocalDate,             // Si está en TrainerAppointmentsCalendarSection, esta copia local puede ser eliminada o ajustada.
    endDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dates = remember(startDate, endDate) { // Recalcular solo si las fechas de inicio/fin cambian
        generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .toList()
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items = dates, key = { date -> date.toEpochDay() }) { date -> // Usar una clave estable
            val isSelected = date == selectedDate
            val backgroundColor = if (isSelected) GoldColor else CardBackgroundColor.copy(alpha = 0.7f)
            val textColor = if (isSelected) DeepBlackColor else Color.White

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .clickable { onDateSelected(date) }
                    .padding(horizontal = 10.dp, vertical = 8.dp) // Ajustar padding
                    .width(56.dp) // Ancho fijo
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp)) // Un poco más de espacio
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                    fontSize = 18.sp
                )
            }
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
            .border(1.dp, GoldColor.copy(alpha = 0.7f), RoundedCornerShape(28.dp))
            .clickable {
                Log.d(TAG, "UserItem clicked. User Name: ${user.fullName}, User Email: ${user.email}, User UID: '${user.uid}'")
                if (user.uid.isNotBlank()) {
                    navController.navigate("trainer_user_detail/${user.uid}")
                } else {
                    Log.w(TAG, "UID is blank for user ${user.fullName}. Navigation skipped.")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoalColor.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(), // Padding ligeramente reducido
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp) // Ligeramente ajustado
                    .background(GoldColor, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape), // Borde más visible
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Icono de Usuario ${user.fullName}", // Mejor accesibilidad
                    modifier = Modifier.size(42.dp), // Ligeramente ajustado
                    tint = DeepBlackColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp)) // Espaciador ajustado
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName.ifEmpty { "Nombre no disponible" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp, // Ajustado
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email.ifEmpty { "Email no disponible" },
                    fontSize = 14.sp, // Ajustado
                    color = PaleGoldColor,
                    maxLines = 1
                )
                if (user.role.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp)) // Ajustado
                    Box(
                        modifier = Modifier
                            .background(GoldColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)) // Fondo más sutil para el rol
                            .border(1.dp, GoldColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = GoldColor, // Texto dorado para mejor contraste con fondo sutil
                            fontSize = 11.sp, // Ajustado
                            letterSpacing = 0.5.sp // Mejor legibilidad para uppercase
                        )
                    }
                }
            }
            // Botón para agendar cita
            IconButton(
                onClick = {
                    if (user.uid.isNotBlank()) {
                        navController.navigate("create_appointment/${user.uid}")
                    } else {
                        Log.w(TAG, "Cannot create appointment, UID is blank for user ${user.fullName}")
                    }
                },
                modifier = Modifier.size(40.dp) // Un poco más grande para facilitar el toque
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Crear Cita para ${user.fullName}", // Mejor accesibilidad
                    tint = GoldColor
                )
            }
            // Botón para abrir chat con el usuario
            IconButton(
                onClick = {
                    if (user.uid.isNotBlank()) {
                        navController.navigate("chat/${user.uid}")
                    } else {
                        Log.w(TAG, "Cannot navigate to chat, UID is blank for user ${user.fullName}")
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chatear con ${user.fullName}",
                    tint = SoftGoldColor
                )
            }
        }
    }
}