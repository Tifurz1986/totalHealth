package com.miempresa.totalhealth.trainer.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.totalhealth.trainer.model.Appointment
import com.google.firebase.firestore.FirebaseFirestore

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
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF181818)) // Fondo general muy oscuro
                .padding(16.dp)
        ) {
            appointments.forEach { appointment ->
                var userName by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(appointment.userId) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(appointment.userId).get()
                        .addOnSuccessListener { document ->
                            val name = document.getString("name").orEmpty()
                            val surname = document.getString("surname").orEmpty()
                            val email = document.getString("email").orEmpty()
                            userName = when {
                                name.isNotBlank() || surname.isNotBlank() -> "$name $surname".trim()
                                email.isNotBlank() -> email
                                else -> "Usuario desconocido"
                            }
                        }
                        .addOnFailureListener {
                            userName = "Error al cargar"
                        }
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                    // El color de fondo de la card:
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFF23211C)
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Usuario: ${userName ?: "Cargando..."}",
                            color = Color.White, // Texto principal en blanco puro
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hora: ${appointment.timestamp.takeLast(8).dropLast(3)}",
                            color = Color(0xFFFFD700), // Hora en dorado
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
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