// Archivo: com/miempresa/totalhealth/dailylog/DailyLog.kt
package com.miempresa.totalhealth.dailylog

import com.google.firebase.firestore.PropertyName // Importar para nombres de campo en Firestore
import java.util.Date

// Clase principal para el registro diario
data class DailyLog(
    val userId: String = "", // ID del usuario al que pertenece el log
    val date: Date = Date(), // Fecha del registro (podría ser solo la parte de la fecha, sin hora)
    val foodEntries: List<FoodEntry> = emptyList(),
    val emotionEntry: EmotionEntry? = null, // Asumimos un registro de emoción principal por día, o podría ser una lista
    val sleepEntry: SleepEntry? = null,
    val activityEntries: List<ActivityEntry> = emptyList(),
    val waterIntakeLiters: Double? = null, // Consumo de agua en litros
    val notes: String = "" // Notas generales del día
) {
    // Constructor sin argumentos necesario para la deserialización de Firestore
    constructor() : this("", Date(), emptyList(), null, null, emptyList(), null, "")
}

// Clase para una entrada de comida
data class FoodEntry(
    val mealType: String = "", // Ej: "Desayuno", "Almuerzo", "Cena", "Snack"
    val description: String = "",
    val calories: Int? = null, // Opcional
    val photoUrl: String? = null, // Opcional: URL de la foto de la comida
    val timestamp: Date = Date()
) {
    constructor() : this("", "", null, null, Date())
}

// Clase para el registro de emociones
data class EmotionEntry(
    // Usar @get:PropertyName y @set:PropertyName si los nombres de campo en Firestore
    // difieren de los nombres de propiedad en Kotlin o si contienen caracteres no permitidos.
    // Por ejemplo, si usaras "current-mood" en Firestore:
    // @get:PropertyName("current-mood") @set:PropertyName("current-mood") var currentMood: String = ""
    var mood: String = "", // Ej: "Feliz", "Triste", "Estresado", "Ansioso", "Tranquilo"
    var moodIntensity: Int? = null, // Opcional: Intensidad del 1 al 5, por ejemplo
    var triggers: String = "", // Posibles desencadenantes o contexto
    var journalEntry: String = "", // Entrada de diario más detallada
    val timestamp: Date = Date()
) {
    constructor() : this("", null, "", "", Date())
}

// Clase para el registro de sueño
data class SleepEntry(
    val timeToBed: Date? = null,
    val timeWokeUp: Date? = null,
    val sleepQuality: String = "", // Ej: "Buena", "Regular", "Mala"
    val interruptions: Int? = null, // Número de interrupciones
    val notes: String = "" // Notas sobre el sueño
) {
    constructor() : this(null, null, "", null, "")

    // Propiedad calculada para la duración del sueño (ejemplo simple)
    // Necesitaría una lógica más robusta para manejar cruces de medianoche y fechas.
    @get:PropertyName("durationHours") // Para asegurar que se guarde en Firestore si es necesario
    val durationHours: Double?
        get() {
            if (timeToBed != null && timeWokeUp != null && timeWokeUp.after(timeToBed)) {
                val diffInMillis = timeWokeUp.time - timeToBed.time
                return diffInMillis / (1000.0 * 60.0 * 60.0)
            }
            return null
        }
}

// Clase para una entrada de actividad física
data class ActivityEntry(
    val type: String = "", // Ej: "Correr", "Caminar", "Gimnasio", "Yoga"
    val durationMinutes: Int? = null,
    val intensity: String = "", // Ej: "Ligera", "Moderada", "Intensa"
    val caloriesBurned: Int? = null, // Opcional
    val notes: String = "",
    val timestamp: Date = Date()
) {
    constructor() : this("", null, "", null, "", Date())
}

// --- Opciones predefinidas (puedes moverlas a un archivo de constantes o enum si prefieres) ---

object MealTypes {
    const val BREAKFAST = "Desayuno"
    const val LUNCH = "Almuerzo"
    const val DINNER = "Cena"
    const val SNACK = "Snack"
    val all = listOf(BREAKFAST, LUNCH, DINNER, SNACK)
}

object Moods {
    const val HAPPY = "Feliz"
    const val SAD = "Triste"
    const val STRESSED = "Estresado/a"
    const val ANXIOUS = "Ansioso/a"
    const val CALM = "Tranquilo/a"
    const val ENERGETIC = "Enérgico/a"
    const val TIRED = "Cansado/a"
    val all = listOf(HAPPY, SAD, STRESSED, ANXIOUS, CALM, ENERGETIC, TIRED)
}

object SleepQualities {
    const val GOOD = "Buena"
    const val REGULAR = "Regular"
    const val POOR = "Mala"
    val all = listOf(GOOD, REGULAR, POOR)
}

object ActivityIntensities {
    const val LIGHT = "Ligera"
    const val MODERATE = "Moderada"
    const val INTENSE = "Intensa"
    val all = listOf(LIGHT, MODERATE, INTENSE)
}
