package com.miempresa.totalhealth.common.composables // O la ubicación que hayas elegido

import androidx.compose.foundation.layout.Column // Importación añadida para Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil // Asegurarse que se usa o quitar si no.
import kotlin.math.floor // Asegurarse que se usa o quitar si no.

@Composable
fun StarRatingDisplay(
    modifier: Modifier = Modifier,
    rating: Float,
    maxStars: Int = 5,
    starSize: Dp = 24.dp,
    starColor: Color = Color(0xFFFFD700)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val coercedRating = rating.coerceIn(0f, maxStars.toFloat())

        val fullStars = floor(coercedRating).toInt()
        val decimalPart = coercedRating - fullStars

        var displayedFullStars = fullStars
        var hasHalfStar = false

        if (decimalPart >= 0.75f && fullStars < maxStars) {
            displayedFullStars++
        } else if (decimalPart >= 0.25f) {
            hasHalfStar = true
        }

        // Estrellas llenas
        repeat(displayedFullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Estrella llena",
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
            // Añadir Spacer solo si no es la última estrella que se va a dibujar en total
            if ( (it < displayedFullStars - 1) || (it == displayedFullStars -1 && (hasHalfStar || (maxStars - displayedFullStars - (if (hasHalfStar) 1 else 0)) > 0) ) ) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }

        // Media estrella
        if (hasHalfStar && displayedFullStars < maxStars) {
            Icon(
                imageVector = Icons.Filled.StarHalf,
                contentDescription = "Media estrella",
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
            // Añadir Spacer solo si no es la última estrella que se va a dibujar en total
            if ( (maxStars - displayedFullStars - 1) > 0 ) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }

        // Estrellas vacías
        val emptyStars = maxStars - displayedFullStars - (if (hasHalfStar) 1 else 0)
        repeat(emptyStars.coerceAtLeast(0)) { index ->
            Icon(
                imageVector = Icons.Filled.StarOutline,
                contentDescription = "Estrella vacía",
                tint = starColor.copy(alpha = 0.5f),
                modifier = Modifier.size(starSize)
            )
            // Añadir Spacer solo si no es la última estrella vacía
            if (index < emptyStars -1) Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StarRatingPreview() {
    // Ahora Column debería resolverse correctamente
    Column {
        StarRatingDisplay(rating = 0.0f)
        StarRatingDisplay(rating = 0.5f)
        StarRatingDisplay(rating = 0.75f)
        StarRatingDisplay(rating = 1.0f)
        StarRatingDisplay(rating = 2.3f)
        StarRatingDisplay(rating = 3.5f)
        StarRatingDisplay(rating = 4.8f)
        StarRatingDisplay(rating = 5.0f)
        StarRatingDisplay(rating = 3.0f, starSize = 32.dp, starColor = Color.Magenta)
        StarRatingDisplay(rating = 2.6f, starSize = 16.dp)
        StarRatingDisplay(rating = 2.75f) // Para probar el redondeo a estrella completa
    }
}
