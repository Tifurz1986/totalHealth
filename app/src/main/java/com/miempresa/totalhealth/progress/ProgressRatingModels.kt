package com.miempresa.totalhealth.progress

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Representa la valoración para una categoría específica del progreso
data class ProgressCategoryRating(
    val categoryId: String = "", // Ej: "nutrition", "exercise", "mental_wellbeing", "goal_adherence"
    val categoryName: String = "", // Ej: "Nutrición", "Ejercicio Físico", "Bienestar Mental"
    val rating: Float = 0.0f,    // Valoración de 0.0 a 5.0
    val coachNotes: String? = null // Notas opcionales del coach para esta categoría
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", 0.0f, null)
}

// Representa el conjunto de valoraciones de progreso para un usuario en un período específico
data class UserProgressRatings(
    val userId: String = "",
    val periodId: String = "", // Ej: "2025-W20" (Semana 20 de 2025), o una fecha específica "2025-05-16"
    val ratings: List<ProgressCategoryRating> = emptyList(),
    val overallAverageRating: Float = 0.0f, // Media calculada de todas las categorías
    val generalCoachFeedback: String? = null, // Feedback general del coach para este período
    @ServerTimestamp val lastUpdated: Date? = null // Fecha de la última actualización
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", emptyList(), 0.0f, null, null)
}
