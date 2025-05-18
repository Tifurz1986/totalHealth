package com.miempresa.totalhealth.dailylog

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class FoodEntry(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = System.currentTimeMillis().toString() + "_" + (0..1000).random(), // Simple unique ID
    @get:PropertyName("mealType") @set:PropertyName("mealType")
    var mealType: String = "", // Ej: Desayuno, Almuerzo, Cena, Snack
    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "", // Ej: Tostada con aguacate y huevo
    @get:PropertyName("calories") @set:PropertyName("calories")
    var calories: Int? = null, // Calorías (opcional)
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Date = Date() // Hora de la comida (opcional, por defecto ahora)
) {
    // Constructor sin argumentos necesario para Firestore, aunque si se anida no es estrictamente necesario
    // si los campos tienen valores por defecto.
    constructor() : this("", "", "", null, Date())
}