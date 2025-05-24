package com.miempresa.totalhealth.trainer.model

import java.util.Date

data class Appointment(
    val id: String = "",
    val trainerId: String = "",
    val userId: String = "",
    val timestamp: String = "", // Fecha/hora en formato ISO 8601 (ej: "2025-05-24T10:00:00Z")
    val notes: String = ""      // Notas u objetivo de la cita
)