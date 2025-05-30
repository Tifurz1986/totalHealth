package com.miempresa.totalhealth.auth

import java.util.Date

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val profilePictureUrl: String? = null,
    val age: Int? = null,
    val sex: String = "",
    val height: Int? = null,
    val weight: Double? = null,
    val activityLevel: String = "",
    val healthGoals: String = "",
    val role: String = "USER",
    val createdAt: Date? = null,
    val trackEmotions: Boolean = false // <-- Campo nuevo, por defecto en false
) {
    // Constructor sin argumentos para Firestore/serialización automática
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
        createdAt = null,
        trackEmotions = false
    )

    val fullName: String
        get() = "$name $surname".trim()
}