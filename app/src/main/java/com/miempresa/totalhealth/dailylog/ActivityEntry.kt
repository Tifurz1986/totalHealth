package com.miempresa.totalhealth.dailylog

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class ActivityEntry(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = System.currentTimeMillis().toString() + "_" + (0..1000).random(), // Simple unique ID
    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "", // Ej: Correr, Pesas, Yoga
    @get:PropertyName("durationMinutes") @set:PropertyName("durationMinutes")
    var durationMinutes: Int? = null, // Duración en minutos
    @get:PropertyName("intensity") @set:PropertyName("intensity")
    var intensity: String = "", // Ej: Ligera, Moderada, Intensa
    @get:PropertyName("notes") @set:PropertyName("notes")
    var notes: String? = null, // Notas adicionales (opcional)
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Date = Date() // Hora de la actividad (opcional, por defecto ahora)
) {
    // Constructor sin argumentos
    constructor() : this("", "", null, "", null, Date())
}