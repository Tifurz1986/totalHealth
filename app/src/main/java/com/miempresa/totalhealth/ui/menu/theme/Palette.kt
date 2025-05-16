package com.miempresa.totalhealth.ui.menu.theme // Ubicado en el paquete de tema

import androidx.compose.ui.graphics.Color

// Paleta de colores Dorados y Negro para el tema profesional
// ESTA ES LA ÚNICA DEFINICIÓN DE ESTE OBJETO EN TODO EL PROYECTO
object ProfessionalGoldPalette {
    val DeepBlack = Color(0xFF000000) // Negro profundo para el inicio del degradado
    val RichGold = Color(0xFFFFD700) // Un amarillo oro vibrante
    val MidGold = Color(0xFFEAA600) // Un dorado intermedio para la transición
    val SoftGold = Color(0xFFF0E68C) // Un dorado más suave, tipo amarillo pálido

    val CardBackground = Color(0xFFFFFFFF) // Blanco para las tarjetas
    val TextPrimary = Color(0xFF212121)    // Negro muy oscuro para texto principal en tarjetas
    val TextSecondary = Color(0xFF555555)  // Gris oscuro para texto secundario en tarjetas
    val AppBarBackground = Color(0xFF1F1F1F) // Un gris muy oscuro, casi negro, para la AppBar
    val AppBarContent = Color.White          // Contenido de la AppBar en blanco
    val IconTint = RichGold // Usar el dorado vibrante para iconos destacados
    val BorderColor = MidGold.copy(alpha = 0.5f)
    val TitleTextOnGradient = Color.White // Color para el título sobre el degradado oscuro
    val ErrorTextColor = Color.White // Color para el texto del mensaje de error
}

// Aquí podrías añadir otras paletas si las necesitas en el futuro.
