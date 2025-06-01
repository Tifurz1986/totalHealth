package com.miempresa.totalhealth.ui
import com.miempresa.totalhealth.trainer.model.Appointment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete


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

    // Dialog state for editing
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

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
                            iconColor = Color(0xFF3A3A3A), // gris oscuro para mejor contraste
                            fecha = fecha,
                            notes = cita.notes,
                            isPast = false,
                            containerColor = Color(0xFFE0E0E0), // color claro neutro para fondo
                            onDelete = {
                                appointmentsViewModel.deleteAppointment(cita.id, onResult = {})
                            },
                            onEdit = {
                                selectedAppointment = cita
                                showEditDialog = true
                            }
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
                            iconColor = Color(0xFF2E7D32), // verde más oscuro para mejor contraste
                            fecha = fecha,
                            notes = cita.notes,
                            isPast = true,
                            containerColor = Color(0xFFD0F0C0)
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && selectedAppointment != null) {
        val cita = selectedAppointment!!
        var newNotes by remember { mutableStateOf(cita.notes ?: "") }
        var newDateTime by remember { mutableStateOf(LocalDateTime.parse(cita.timestamp)) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    appointmentsViewModel.updateAppointment(
                        appointmentId = cita.id,
                        newTimestamp = newDateTime.toString(),
                        newNotes = newNotes,
                        onResult = { showEditDialog = false }
                    )
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Editar Cita") },
            text = {
                Column {
                    Text("Fecha y hora (formato: yyyy-MM-ddTHH:mm):")
                    OutlinedTextField(
                        value = newDateTime.toString(),
                        onValueChange = {
                            try {
                                newDateTime = LocalDateTime.parse(it)
                            } catch (_: Exception) {}
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Notas:")
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        singleLine = false
                    )
                }
            }
        )
    }
}

@Composable
fun AppointmentCard(
    icon: ImageVector,
    iconColor: Color,
    fecha: LocalDateTime,
    notes: String?,
    isPast: Boolean = false,
    containerColor: Color = if (isPast) Color(0xFFF8F8F8) else Color(0xFFF5F5DC),
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = fecha.format(DateTimeFormatter.ofPattern("EEEE d MMMM, HH:mm", java.util.Locale.getDefault())),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (!notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }
            if (!isPast) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onEdit?.invoke() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar cita", tint = Color(0xFF1976D2))
                }
                IconButton(onClick = { onDelete?.invoke() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar cita", tint = Color.Red)
                }
            }
        }
    }
}