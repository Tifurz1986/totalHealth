package com.miempresa.totalhealth.trainer.calendar

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import java.time.format.TextStyle
import java.util.Locale

import androidx.compose.ui.unit.sp
import com.miempresa.totalhealth.trainer.model.Appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun TrainerAppointmentsCalendarSection(
    trainerId: String,
    appointmentsViewModel: AppointmentsViewModel = viewModel()
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    val appointments by appointmentsViewModel.appointments.collectAsState()

    // Traer citas del entrenador para el día seleccionado
    LaunchedEffect(trainerId, selectedDate) {
        appointmentsViewModel.fetchAppointmentsForTrainerDay(trainerId, selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color(0xFF004D40))
                )
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Calendario de Citas",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFFFFD700),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Calendario horizontal de días
        HorizontalCalendar(
            startDate = today.minusDays(1),
            endDate = today.plusDays(10),
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Citas para ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val citasDelDia = appointments.filter { cita ->
            cita.trainerId == trainerId && cita.timestamp.take(10) == selectedDate.toString()
        }

        if (citasDelDia.isEmpty()) {
            Text(
                text = "No hay citas para este día.",
                color = Color(0xFFE7DFA1),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(citasDelDia.sortedBy { it.timestamp }) { cita ->
                    TrainerAppointmentCard(cita)
                }
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
    val days = generateSequence(startDate) { it.plusDays(1) }
        .takeWhile { !it.isAfter(endDate) }
        .toList()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days) { day ->
            val isSelected = day == selectedDate
            Surface(
                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF23211C),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(2.dp)
                    .clickable { onDateSelected(day) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        color = if (isSelected) Color.Black else Color(0xFFE7DFA1),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TrainerAppointmentCard(cita: Appointment) {
    val fecha = try {
        LocalDateTime.parse(cita.timestamp)
    } catch (_: Exception) {
        null
    }

    var userName by remember { mutableStateOf("Cargando...") }

    LaunchedEffect(cita.userId) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("users").document(cita.userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val surname = document.getString("surname") ?: ""
                    userName = "$name $surname".trim().ifEmpty {
                        document.getString("email") ?: "Usuario desconocido"
                    }
                } else {
                    userName = "Usuario no encontrado"
                }
            }
            .addOnFailureListener {
                userName = "Error al cargar"
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23211C)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = fecha?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Sin hora",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp
                )
                Text(
                    text = "Usuario: $userName",
                    color = Color(0xFFE7DFA1),
                    fontSize = 13.sp
                )
                if (!cita.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = cita.notes,
                        color = Color.White
                    )
                }
            }
        }
    }
}
