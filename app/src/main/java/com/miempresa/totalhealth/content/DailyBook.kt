package com.miempresa.totalhealth.content // Asegúrate de que este paquete sea correcto

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DailyBook(
    var id: String = "", // Puede ser ISBN o un ID generado
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("author") @set:PropertyName("author") var author: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String? = null, // Breve descripción
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String? = null, // URL de la portada del libro (opcional)
    @get:PropertyName("genre") @set:PropertyName("genre") var genre: String? = null, // Género del libro
    @get:PropertyName("publicationDate") @set:PropertyName("publicationDate") var publicationDate: String? = null, // Fecha de publicación como String
    @ServerTimestamp @get:PropertyName("featuredAt") @set:PropertyName("featuredAt") var featuredAt: Date? = null // Cuándo se destacó este libro (menos relevante para lista local)
)
