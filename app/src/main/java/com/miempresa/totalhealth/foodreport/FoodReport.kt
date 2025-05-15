package com.miempresa.totalhealth.foodreport // Asegúrate que el paquete sea este

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FoodReport(
    var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("comment") @set:PropertyName("comment") var comment: String = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String? = null,
    @get:PropertyName("feedback") @set:PropertyName("feedback") var feedback: String? = null,

    @get:PropertyName("mealType") @set:PropertyName("mealType") var mealType: String = "",
    @get:PropertyName("mealTimestamp") @set:PropertyName("mealTimestamp") var mealTimestamp: Date? = null,
    @get:PropertyName("mealNumberInDay") @set:PropertyName("mealNumberInDay") var mealNumberInDay: Int? = null, // Número de comida del día

    @ServerTimestamp @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Date? = null,
    @ServerTimestamp @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Date? = null
)
