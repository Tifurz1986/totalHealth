package com.miempresa.totalhealth.trainer.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.totalhealth.trainer.model.Appointment

@Composable
fun AppointmentListForDay(appointments: List<Appointment>) {
    if (appointments.isEmpty()) {
        Text(
            text = "No hay citas para este día.",
            color = Color(0xFFE7DFA1),
            fontSize = 15.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            appointments.forEach { appointment ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF23211C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Usuario: ${appointment.userId}",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hora: ${appointment.timestamp.takeLast(8).dropLast(3)}",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (appointment.notes.isNotBlank()) {
                            Text(
                                text = "Notas: ${appointment.notes}",
                                color = Color(0xFFE7DFA1),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}