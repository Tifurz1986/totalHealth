package com.miempresa.totalhealth.trainer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val dashboardMetricsState by trainerViewModel.dashboardMetricsUiState.collectAsState() // Observar el nuevo estado

    val blackToGoldGradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF141414), // Un negro más profundo
            Color(0xFF23211C), // Un carbón oscuro
            Color(0xFFFFD700)  // Oro
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Entrenador", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF181818)), // Un gris muy oscuro para la barra
                actions = {
                    IconButton(onClick = {
                        trainerViewModel.fetchAllUsers()
                        trainerViewModel.fetchDashboardMetrics() // Refrescar también las métricas
                    }) {
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
            Column(modifier = Modifier.padding(14.dp).fillMaxSize()) { // Asegurar que la columna pueda hacer scroll si el contenido excede

                // Acceso premium al calendario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(bottom = 20.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp))
                        .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
                        .clickable { navController.navigate("trainer_calendar") },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFF23211C), Color(0xFF181818))
                                )
                            )
                            .padding(horizontal = 18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Calendario de citas",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = "Ver Calendario de Citas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Text(
                    text = "Panel del Entrenador",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Mostrar las métricas del dashboard basadas en el estado
                when (val metricsState = dashboardMetricsState) {
                    is DashboardMetricsUiState.Loading -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricCardPro(title = "Usuarios", valueString = "...", icon = Icons.Default.People, modifier = Modifier.weight(1f))
                            MetricCardPro(title = "Reportes Hoy", valueString = "...", icon = Icons.Default.Assessment, modifier = Modifier.weight(1f))
                            MetricCardPro(title = "Citas Hoy", valueString = "...", icon = Icons.Default.Event, modifier = Modifier.weight(1f))
                        }
                    }
                    is DashboardMetricsUiState.Success -> {
                        DashboardMetricsContent( // Cambiado el nombre para evitar conflicto con el composable anterior
                            totalUsers = metricsState.totalUsers,
                            reportsToday = metricsState.reportsToday,
                            appointmentsToday = metricsState.appointmentsToday
                        )
                    }
                    is DashboardMetricsUiState.Error -> {
                        Text(
                            text = "Error al cargar métricas: ${metricsState.message}",
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        // Opcionalmente, mostrar las cards con valores de error o guiones
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricCardPro(title = "Usuarios", valueString = "-", icon = Icons.Default.People, modifier = Modifier.weight(1f))
                            MetricCardPro(title = "Reportes Hoy", valueString = "-", icon = Icons.Default.Assessment, modifier = Modifier.weight(1f))
                            MetricCardPro(title = "Citas Hoy", valueString = "-", icon = Icons.Default.Event, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TrainerCalendarSection() // Esto sigue con datos de ejemplo por ahora

                Text(
                    text = "Usuarios Registrados",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700), // Dorado para el título
                    modifier = Modifier.padding(bottom = 20.dp, top = 24.dp)
                )

                when (val state = userListUiState) {
                    is UserListUiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                        color = Color(0xFFFFD700) // Dorado para el indicador
                    )
                    is UserListUiState.Success -> {
                        if (state.users.isEmpty()) {
                            Text(
                                text = "No hay usuarios registrados.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                                color = Color(0xFFE3C15B), // Un dorado más suave
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
                            color = Color(0xFFFFA500), // Naranja/ámbar para errores
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
fun DashboardMetricsContent( // Nuevo nombre para el Composable de contenido de métricas
    totalUsers: Int,
    reportsToday: Int,
    appointmentsToday: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MetricCardPro(
            title = "Usuarios Totales",
            value = totalUsers,
            icon = Icons.Default.People,
            modifier = Modifier.weight(1f)
        )
        MetricCardPro(
            title = "Reportes Hoy",
            value = reportsToday,
            icon = Icons.Default.Assessment,
            modifier = Modifier.weight(1f)
        )
        MetricCardPro(
            title = "Citas Hoy",
            value = appointmentsToday,
            icon = Icons.Default.Event,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCardPro(
    title: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateIntAsState(targetValue = value, label = title + "_int_val") // Etiqueta única para la animación
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp))
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(22.dp)), // Borde dorado
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)), // Fondo oscuro para la tarjeta
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFFD700), // Icono dorado
                modifier = Modifier
                    .size(38.dp)
                    .padding(end = 8.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color(0xFFE3C15B), // Dorado suave para el título
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = animatedValue.toString(),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// Sobrecarga de MetricCardPro para el estado de carga o error, mostrando un String
@Composable
fun MetricCardPro(
    title: String,
    valueString: String, // Para mostrar "..." o "-"
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp))
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier
                    .size(38.dp)
                    .padding(end = 8.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color(0xFFE3C15B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = valueString, // Mostrar el string directamente
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
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
            color = Color(0xFFFFD700), // Dorado
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF23211C)) // Carbón oscuro
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
                    text = "Citas para ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM uuuu"))}", // 'uuuu' para año
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                // DATOS DE EJEMPLO - REEMPLAZAR CON DATOS REALES DEL VIEWMODEL
                Text(
                    text = "• Sesión con María (10:00 AM)\n• Evaluación con Jorge (2:00 PM)",
                    fontSize = 14.sp,
                    color = Color(0xFFE7DFA1), // Un blanco hueso/dorado pálido
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 20.sp
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp) // Añadir un poco de padding
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val backgroundColor = if (isSelected) Color(0xFFFFD700) else Color.DarkGray.copy(alpha = 0.5f) // Más sutil si no está seleccionada
            val textColor = if (isSelected) Color.Black else Color.White

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp)) // Bordes más redondeados
                    .background(backgroundColor)
                    .clickable { onDateSelected(date) }
                    .padding(horizontal = 12.dp, vertical = 10.dp) // Ajustar padding
                    .width(60.dp) // Ancho fijo para consistencia
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase(), // Mejor formato para día
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    fontSize = 10.sp // Ligeramente más pequeño
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), // Más grande y negrita
                    color = textColor,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun UserListPro(users: List<UserProfile>, navController: NavController) {
    // Filtrar usuarios con uid vacío y duplicados
    val uniqueUsers = users
        .filter { it.uid.isNotBlank() }
        .distinctBy { it.uid }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(uniqueUsers, key = { user -> user.uid }) { user ->
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
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(28.dp)) // Borde más sutil
            .clickable {
                Log.d(TAG, "UserItem clicked. User Name: ${user.fullName}, User Email: ${user.email}, User UID: '${user.uid}'")
                if (user.uid.isNotBlank()) {
                    navController.navigate("trainer_user_detail/${user.uid}")
                } else {
                    Log.w(TAG, "UID is blank. Navigation skipped.")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C).copy(alpha = 0.9f)) // Ligeramente más transparente
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFFFFD700), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape), // Borde sutil al círculo
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Icono de Usuario",
                    modifier = Modifier.size(46.dp),
                    tint = Color(0xFF23211C) // Icono oscuro sobre fondo dorado
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
                    color = Color(0xFFEEE8BB) // Dorado pálido para el email
                )
                if (user.role.isNotBlank()) {
                    Spacer(modifier = Modifier.height(7.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(8.dp)) // Fondo más sutil para el rol
                            .padding(horizontal = 10.dp, vertical = 3.dp) // Ajustar padding vertical
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF23211C), // Texto oscuro
                            fontSize = 12.sp // Ligeramente más pequeño
                        )
                    }
                }
            }
            // Botón para agendar cita
            IconButton(
                onClick = {
                    // Aquí deberías lanzar una pantalla o diálogo para crear la cita
                    navController.navigate("create_appointment/${user.uid}")
                },
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Crear Cita",
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }
}