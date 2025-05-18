package com.miempresa.totalhealth.dailylog

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DailyLog(
    var id: String = "", // ID del documento de Firestore (userId_yyyy-MM-dd)

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("date") @set:PropertyName("date")
    var date: Date = Date(), // Fecha del registro diario

    @get:PropertyName("foodEntries") @set:PropertyName("foodEntries")
    var foodEntries: List<FoodEntry> = emptyList(),

    @get:PropertyName("activityEntries") @set:PropertyName("activityEntries")
    var activityEntries: List<ActivityEntry> = emptyList(),

    @get:PropertyName("emotionEntry") @set:PropertyName("emotionEntry")
    var emotionEntry: EmotionEntry? = null,

    @get:PropertyName("sleepEntry") @set:PropertyName("sleepEntry")
    var sleepEntry: SleepEntry? = null,

    @get:PropertyName("waterIntakeLiters") @set:PropertyName("waterIntakeLiters")
    var waterIntakeLiters: Double? = null, // Consumo de agua en litros

    @get:PropertyName("notes") @set:PropertyName("notes")
    var notes: String = "", // Notas generales del día

    @ServerTimestamp @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Date? = null,

    @ServerTimestamp @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Date? = null
) {
    // Constructor sin argumentos necesario para Firestore
    constructor() : this(
        id = "",
        userId = "",
        date = Date(),
        foodEntries = emptyList(),
        activityEntries = emptyList(),
        emotionEntry = null,
        sleepEntry = null,
        waterIntakeLiters = null,
        notes = "",
        createdAt = null,
        updatedAt = null
    )
}