package com.miempresa.totalhealth.dailylog

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class SleepEntry(
    @get:PropertyName("timeToBed") @set:PropertyName("timeToBed")
    var timeToBed: Date? = null, // Hora de acostarse
    @get:PropertyName("timeWokeUp") @set:PropertyName("timeWokeUp")
    var timeWokeUp: Date? = null, // Hora de levantarse
    @get:PropertyName("sleepQuality") @set:PropertyName("sleepQuality")
    var sleepQuality: String = "", // Ej: Buena, Regular, Mala
    @get:PropertyName("interruptions") @set:PropertyName("interruptions")
    var interruptions: Int? = null, // Número de interrupciones (opcional)
    @get:PropertyName("notes") @set:PropertyName("notes")
    var notes: String? = null // Notas sobre el sueño (opcional)
) {
    // Constructor sin argumentos
    constructor() : this(null, null, "", null, null)
}