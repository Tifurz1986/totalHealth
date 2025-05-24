package com.miempresa.totalhealth.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    val Burdeos = Color(0xFF7C2338)
    val Dorado = Color(0xFFD4AF37)
    val AzulSereno = Color(0xFF3B4B66)
    val VerdeOliva = Color(0xFF35523A)
}

data class Emotion(
    val name: String,
    val color: Color,
    val subemotions: List<String>
)

@Composable
fun EmotionGridSelector(
    userId: String,
    modifier: Modifier = Modifier,
    onEmotionSelected: (Emotion) -> Unit
) {
    val emotions = listOf(
        Emotion("Ira", AppColors.Burdeos, listOf("Irritabilidad", "Furia", "Resentimiento", "Hostilidad")),
        Emotion("Alegría", AppColors.Dorado, listOf("Gratitud", "Entusiasmo", "Orgullo", "Satisfacción")),
        Emotion("Tristeza", AppColors.AzulSereno, listOf("Culpa", "Nostalgia", "Pesimismo", "Soledad")),
        Emotion("Calma", AppColors.VerdeOliva, listOf("Paz", "Esperanza", "Relajación", "Seguridad"))
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Qué sientes ahora?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Spacer(modifier = Modifier.height(26.dp))
        // 2x2 Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (col in 0 until 2) {
                        val idx = row * 2 + col
                        val emotion = emotions[idx]
                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(emotion.color)
                                .border(
                                    width = 2.dp,
                                    color = Color(0x80FFD700),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(6.dp)
                                .clickable { onEmotionSelected(emotion) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emotion.name,
                                color = Color(0xFFF5F5F5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}