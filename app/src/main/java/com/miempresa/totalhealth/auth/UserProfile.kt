package com.miempresa.totalhealth.auth

import java.util.Date // Necesario para el campo createdAt

// Definición ÚNICA y CONSOLIDADA de UserProfile.
// Esta clase representa el modelo de datos para el perfil de un usuario.
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",      // Nombre del usuario
    val surname: String = "",   // Apellido del usuario
    val profilePictureUrl: String? = null,
    val age: Int? = null,
    val sex: String = "",
    val height: Int? = null,
    val weight: Double? = null,
    val activityLevel: String = "",
    val healthGoals: String = "",
    val role: String = "USER", // Rol del usuario (ej. "USER", "TRAINER")
    val createdAt: Date? = null // Fecha de creación del perfil
) {
    // Constructor sin argumentos para Firestore.
    constructor() : this(
        uid = "",
        email = "",
        name = "",
        surname = "",
        profilePictureUrl = null,
        age = null,
        sex = "",
        height = null,
        weight = null,
        activityLevel = "",
        healthGoals = "",
        role = "USER",
        createdAt = null // Se asignará explícitamente al crear un nuevo usuario
    )

    // Propiedad calculada para obtener el nombre completo.
    val fullName: String
        get() = "$name $surname".trim()
}