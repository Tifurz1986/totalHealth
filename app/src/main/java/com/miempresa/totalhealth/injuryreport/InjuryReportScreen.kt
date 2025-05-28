package com.miempresa.totalhealth.injuryreport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InjuryReportScreen(
    userId: String,
    userName: String,
    userEmail: String,
    onClose: () -> Unit
) {
    var zona by remember { mutableStateOf("") }
    var gravedad by remember { mutableStateOf("Leve") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (success) {
        AlertDialog(
            onDismissRequest = { onClose() },
            title = { Text("¡Enviado!", color = Color(0xFFD32F2F), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = { Text("El reporte se ha guardado correctamente.") },
            confirmButton = {
                Button(onClick = { onClose() }) { Text("Cerrar") }
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(min = 380.dp, max = 480.dp)
                .border(3.dp, Color(0xFFD32F2F), RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2222))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.HealthAndSafety,
                    contentDescription = "Lesión",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp).padding(bottom = 14.dp)
                )
                Text("Reportar lesión o molestia", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.White, fontSize = 19.sp)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = zona,
                    onValueChange = { zona = it },
                    label = { Text("Zona afectada (ej: rodilla)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )
                Spacer(Modifier.height(8.dp))
                DropdownMenuBox(
                    value = gravedad,
                    onValueChange = { gravedad = it }
                )
                if (gravedad == "Grave") {
                    Text(
                        text = "⚠️ Lesión grave. Tu entrenador será notificado.",
                        color = Color(0xFFD32F2F),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción breve") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )
                if (error != null) {
                    Text(error ?: "", color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { onClose() }) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            isLoading = true
                            error = null
                            InjuryReportRepository.saveInjuryReport(
                                userId = userId,
                                userName = userName,
                                userEmail = userEmail,
                                zona = zona,
                                gravedad = gravedad,
                                descripcion = descripcion,
                                onSuccess = {
                                    isLoading = false
                                    success = true
                                },
                                onError = {
                                    isLoading = false
                                    error = it
                                }
                            )
                        },
                        enabled = !isLoading && zona.isNotBlank() && descripcion.isNotBlank()
                    ) {
                        if (isLoading)
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        else
                            Text("Enviar")
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("Leve", "Moderada", "Grave")
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text("Gravedad") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = if (expanded) "Cerrar" else "Desplegar",
                    tint = Color.White,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C853), // Verde claro profesional
                unfocusedBorderColor = Color.White,
                focusedTextColor = if (value == "Grave") Color(0xFFD32F2F) else Color.White,
                unfocusedTextColor = if (value == "Grave") Color(0xFFD32F2F) else Color.White,
                disabledTextColor = Color.White,
                cursorColor = Color.White
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onValueChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}