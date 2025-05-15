package com.miempresa.totalhealth.progress // ¡ASEGÚRATE DE QUE ESTE PAQUETE SEA CORRECTO!

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp // Asegúrate de tener esta importación
import java.util.Date
import java.util.UUID

data class ProgressEntry(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = UUID.randomUUID().toString(),
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("physical") @set:PropertyName("physical") var physical: Int = 0,
    @get:PropertyName("mental") @set:PropertyName("mental") var mental: Int = 0,
    @get:PropertyName("nutrition") @set:PropertyName("nutrition") var nutrition: Int = 0,
    @get:PropertyName("notes") @set:PropertyName("notes") var notes: String? = null,
    @ServerTimestamp @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Date? = null, // Firestore llenará esto si es null
    @ServerTimestamp @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Date? = null, // Firestore llenará esto si es null
    @get:PropertyName("entryDate") @set:PropertyName("entryDate") var entryDate: Date? = null
)
