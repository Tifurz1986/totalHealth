// Archivo: com/miempresa/totalhealth/journal/ImprovementJournalEntry.kt
package com.miempresa.totalhealth.journal

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ImprovementJournalEntry(
    val id: String = "", // ID único para la entrada, podría ser generado por Firestore
    val userId: String = "", // ID del usuario al que pertenece la entrada
    val title: String = "", // Título opcional para la entrada
    val content: String = "", // El texto principal de la reflexión
    val category: String? = null, // Categoría opcional (Ej: "Logro", "Área de Mejora", "Gratitud")
    @ServerTimestamp val createdAt: Date? = null, // Fecha de creación, gestionada por Firestore
    val entryDate: Date = Date() // Fecha a la que se refiere la entrada (puede ser diferente a createdAt)
) {
    // Constructor sin argumentos necesario para Firestore
    constructor() : this("", "", "", "", null, null, Date())
}
