package com.miempresa.totalhealth.common // Paquete actualizado

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column // Asegúrate que esta importación esté si usas Column en Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Importación necesaria para 'by remember'
import androidx.compose.runtime.mutableStateOf // Importación necesaria para mutableStateOf
import androidx.compose.runtime.remember // Importación necesaria para remember
import androidx.compose.runtime.setValue // Importación necesaria para 'by remember'
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InteractiveStarRatingInput(
    modifier: Modifier = Modifier,
    currentRating: Int, // Valoración actual (0 a 5, donde 0 puede ser sin seleccionar)
    onRatingChange: (Int) -> Unit,
    maxStars: Int = 5,
    starSize: Dp = 36.dp,
    selectedColor: Color = Color(0xFFFFD700),
    unselectedColor: Color = Color.Gray.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (starIndex in 1..maxStars) {
            val icon = if (starIndex <= currentRating) Icons.Filled.Star else Icons.Filled.StarOutline
            val tint = if (starIndex <= currentRating) selectedColor else unselectedColor
            val contentDescription = if (starIndex <= currentRating) "Estrella $starIndex seleccionada" else "Estrella $starIndex no seleccionada"

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier
                    .size(starSize)
                    .clickable { onRatingChange(starIndex) }
            )
            if (starIndex < maxStars) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun InteractiveStarRatingInputPreview() {
    var rating by remember { mutableStateOf(3) } // Asegúrate de importar getValue, setValue y remember
    Column { // Asegúrate de importar Column
        InteractiveStarRatingInput(
            currentRating = rating,
            onRatingChange = { newRating -> rating = newRating }
        )
    }
}
