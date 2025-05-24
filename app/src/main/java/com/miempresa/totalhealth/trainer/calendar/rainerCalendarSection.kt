package com.miempresa.totalhealth.trainer.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border

@Composable
fun TrainerCalendarSection(
    appointmentsViewModel: AppointmentsViewModel,
    navController: NavHostController
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    val appointments by appointmentsViewModel.appointments.collectAsState()

    // Cuando cambia la fecha, cargamos las citas de ese día
    LaunchedEffect(selectedDate) {
        appointmentsViewModel.fetchAppointmentsForDay(selectedDate)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF181818),
                        Color(0xFF23211C),
                        Color(0xFFE7DFA1)
                    ),
                    startY = 0.0f,
                    endY = 1800.0f
                )
            )
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con botón volver y título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(1.5.dp, Color(0xFFE7DFA1), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                // El espacio vacío para centrar el título
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título centrado con sombra y subrayado dorado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = "Calendario de citas",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(3.dp)
                )
                Box(
                    Modifier
                        .height(4.dp)
                        .width(160.dp)
                        .background(Color(0xFFFFD700), RoundedCornerShape(2.dp))
                )
            }

            // Selector de fecha bonito
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                // Día anterior
                Text(
                    text = "<",
                    color = Color(0xFFFFD700),
                    fontSize = 32.sp,
                    modifier = Modifier
                        .padding(end = 18.dp)
                        .clickable { selectedDate = selectedDate.minusDays(1) }
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                // Día siguiente
                Text(
                    text = ">",
                    color = Color(0xFFFFD700),
                    fontSize = 32.sp,
                    modifier = Modifier
                        .padding(start = 18.dp)
                        .clickable { selectedDate = selectedDate.plusDays(1) }
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Caja premium de citas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(18.dp))
                    .background(
                        Color(0xFF23211C),
                        RoundedCornerShape(18.dp)
                    )
                    .border(2.dp, Color(0xFFE7DFA1), RoundedCornerShape(18.dp))
                    .padding(vertical = 18.dp, horizontal = 12.dp)
            ) {
                AppointmentListForDay(appointments)
            }
        }
    }
}