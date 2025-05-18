package com.miempresa.totalhealth.dailylog

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class EmotionEntry(
    @get:PropertyName("mood") @set:PropertyName("mood")
    var mood: String = "", // Ej: Feliz, Triste, Estresado
    @get:PropertyName("moodIntensity") @set:PropertyName("moodIntensity")
    var moodIntensity: Int? = null, // Intensidad del ánimo (ej. 1-5 estrellas), nullable si no se registra
    @get:PropertyName("triggers") @set:PropertyName("triggers")
    var triggers: String = "", // Desencadenantes (opcional)
    @get:PropertyName("journalEntry") @set:PropertyName("journalEntry")
    var journalEntry: String = "", // Entrada de diario o reflexión
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Date = Date() // Hora del registro (opcional, por defecto ahora)
) {
    // Constructor sin argumentos
    constructor() : this("", null, "", "", Date())
}