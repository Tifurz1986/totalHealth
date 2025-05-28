package com.miempresa.totalhealth.trainer.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Appointment(
    val id: String = "",
    val trainerId: String = "",
    val userId: String = "",
    val timestamp: String = "", // Fecha/hora en formato ISO 8601 (ej: "2025-05-24T10:00:00Z")
    val notes: String = "",     // Notas u objetivo de la cita
    @get:Exclude @set:Exclude var userName: String? = null // Ignorado por Firestore
)