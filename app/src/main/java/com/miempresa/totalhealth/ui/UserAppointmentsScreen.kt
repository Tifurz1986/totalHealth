package com.miempresa.totalhealth.ui

import androidx.navigation.NavHostController
import com.miempresa.totalhealth.trainer.calendar.AppointmentsViewModel
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun UserAppointmentsScreen(
    navController: NavHostController,
    appointmentsViewModel: AppointmentsViewModel = viewModel(),
    userId: String
) {
    val appointments by appointmentsViewModel.appointments.collectAsState()
    val now = LocalDateTime.now()

    LaunchedEffect(userId) {
        appointmentsViewModel.fetchAppointmentsForUser(userId)
    }

    val (upcoming, past) = remember(appointments) {
        val parsed = appointments.mapNotNull {
            try {
                LocalDateTime.parse(it.timestamp) to it
            } catch (_: Exception) { null }
        }
        parsed.partition { (date, _) -> date.isAfter(now) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Citas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.History, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color(0xFF004D40))
                    )
                )
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Próximas citas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            if (upcoming.isEmpty()) {
                Text(
                    text = "Sin próximas citas.",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, false),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(upcoming.sortedBy { it.first }) { (fecha, cita) ->
                        AppointmentCard(
                            icon = Icons.Default.Schedule,
                            iconColor = Color(0xFFFFD700), // dorado
                            fecha = fecha,
                            notes = cita.notes
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp)

            Text(
                text = "Citas pasadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            if (past.isEmpty()) {
                Text(
                    text = "No tienes citas anteriores.",
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(past.sortedByDescending { it.first }) { (fecha, cita) ->
                        AppointmentCard(
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF4CAF50), // verde
                            fecha = fecha,
                            notes = cita.notes,
                            isPast = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    icon: ImageVector,
    iconColor: Color,
    fecha: LocalDateTime,
    notes: String?,
    isPast: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) Color(0xFFEEEEEE) else Color(0xFFFFF9C4)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = fecha.format(DateTimeFormatter.ofPattern("EEEE d MMMM, HH:mm")),
                    fontWeight = FontWeight.Bold
                )
                if (!notes.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}