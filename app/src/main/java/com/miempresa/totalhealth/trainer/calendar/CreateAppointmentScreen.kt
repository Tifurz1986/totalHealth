package com.miempresa.totalhealth.trainer.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    userId: String,
    trainerId: String,
    appointmentsViewModel: AppointmentsViewModel,
    navController: NavController
) {
    var notes by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") } // Formato: yyyy-MM-ddTHH:mm
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila superior: Botón volver y título centrado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(13.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            // Título premium centrado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nueva cita con usuario",
                    color = Color(0xFFFFD700),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(2.dp)
                )
                Box(
                    Modifier
                        .height(4.dp)
                        .width(120.dp)
                        .background(Color(0xFFFFD700), RoundedCornerShape(2.dp))
                )
            }

            Spacer(Modifier.height(22.dp))

            // Card/Formulario elegante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181818), RoundedCornerShape(18.dp))
                    .border(2.dp, Color(0xFFE7DFA1), RoundedCornerShape(18.dp))
                    .padding(vertical = 26.dp, horizontal = 20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ---- Lógica de picker de fecha y hora ----
                    val context = LocalContext.current
                    var showDatePicker by remember { mutableStateOf(false) }
                    var showTimePicker by remember { mutableStateOf(false) }
                    var tempDate by remember { mutableStateOf<LocalDateTime?>(null) }

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = {},
                        label = { Text("Fecha y hora") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                            .clickable { showDatePicker = true },
                        enabled = false,
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack, // Puedes cambiar por un icono de calendario
                                contentDescription = "Seleccionar fecha",
                                tint = Color(0xFFE7DFA1)
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 17.sp),
                        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFE7DFA1),
                            cursorColor = Color(0xFFFFD700),
                            focusedLabelColor = Color(0xFFFFD700),
                            unfocusedLabelColor = Color(0xFFE7DFA1),
                            containerColor = Color(0xFF23211C)
                        )
                    )

                    // DatePickerDialog
                    if (showDatePicker) {
                        val now = LocalDateTime.now()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                tempDate = LocalDateTime.of(year, month + 1, dayOfMonth, now.hour, now.minute)
                                showDatePicker = false
                                showTimePicker = true
                            },
                            now.year, now.monthValue - 1, now.dayOfMonth
                        ).show()
                    }

                    // TimePickerDialog
                    if (showTimePicker) {
                        val now = LocalDateTime.now()
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                tempDate = tempDate?.withHour(hourOfDay)?.withMinute(minute)
                                tempDate?.let {
                                    dateInput = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                                }
                                showTimePicker = false
                            },
                            now.hour, now.minute, true
                        ).show()
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas de la cita") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFE7DFA1),
                            cursorColor = Color(0xFFFFD700),
                            focusedLabelColor = Color(0xFFFFD700),
                            unfocusedLabelColor = Color(0xFFE7DFA1),
                            containerColor = Color(0xFF23211C)
                        )
                    )
                    if (error != null) {
                        Text(
                            error!!,
                            color = Color.Red,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    Button(
                        onClick = {
                            isSaving = true
                            error = null
                            try {
                                val timestamp = LocalDateTime.parse(dateInput, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()
                                val appointment = com.miempresa.totalhealth.trainer.model.Appointment(
                                    userId = userId,         // ✅ usuario al que va la cita
                                    trainerId = trainerId,   // ✅ entrenador que la crea
                                    timestamp = timestamp,
                                    notes = notes
                                )
                                appointmentsViewModel.createAppointment(
                                    appointment,
                                    onSuccess = {
                                        isSaving = false
                                        navController.popBackStack()
                                    },
                                    onFailure = {
                                        isSaving = false
                                        error = "Error al guardar la cita"
                                    }
                                )
                            } catch (e: Exception) {
                                isSaving = false
                                error = "Formato de fecha/hora incorrecto"
                            }
                        },
                        enabled = !isSaving && dateInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(
                            if (isSaving) "Guardando..." else "Guardar cita",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}