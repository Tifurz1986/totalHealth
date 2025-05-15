package com.miempresa.totalhealth.content

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Corrección: Nombre de la clase cambiado a "WeeklyPhrase" (sin caracteres especiales)
data class WeeklyPhrase(
    var id: String = "",
    @get:PropertyName("weekNumber") @set:PropertyName("weekNumber") var weekNumber: Int = 0,
    @get:PropertyName("phrase") @set:PropertyName("phrase") var phrase: String = "",
    @get:PropertyName("author") @set:PropertyName("author") var author: String? = null,
    @ServerTimestamp @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Date? = null,
    @ServerTimestamp @get:PropertyName("displayUntil") @set:PropertyName("displayUntil") var displayUntil: Date? = null
)