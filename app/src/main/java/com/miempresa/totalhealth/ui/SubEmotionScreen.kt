package com.miempresa.totalhealth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun SubEmotionScreen(
    navController: NavController,
    emotionName: String,
    userId: String
) {
    val context = LocalContext.current

    val baseColor = when (emotionName) {
        "Ira" -> Color(0xFF8D1931)
        "Alegría" -> Color(0xFFFFD700)
        "Tristeza" -> Color(0xFF1D4E89)
        "Calma" -> Color(0xFF6B8E23)
        else -> Color(0xFF23211C)
    }

    val subemotions = when (emotionName) {
        "Ira" -> listOf("Irritación", "Rencor", "Rabia", "Impaciencia", "Celos", "Desprecio", "Enfado", "Envidia")
        "Alegría" -> listOf("Euforia", "Gozo", "Optimismo", "Plenitud", "Entusiasmo", "Vitalidad", "Brillo", "Ilusión")
        "Tristeza" -> listOf("Soledad", "Abatimiento", "Desilusión", "Melancolía", "Desesperanza", "Desconsuelo", "Decepción", "Apatía")
        "Calma" -> listOf("Serenidad", "Confianza", "Paz interior", "Tranquilidad", "Gratitud", "Relajación", "Esperanza", "Estabilidad")
        else -> List(8) { "Sub $it" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0F2027),
                        Color(0xFF2C5364)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 1000f)
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        // Botón volver atrás (arriba izquierda)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(top = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text(
            //     text = "Elige una subcategoría de $emotionName",
            //     color = Color.White,
            //     fontWeight = FontWeight.Bold,
            //     fontSize = 23.sp,
            //     modifier = Modifier.padding(bottom = 36.dp, top = 12.dp)
            // )
            // Grid 2 columnas, 4 filas
            for (row in 0 until 4) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    for (col in 0 until 2) {
                        val idx = row * 2 + col
                        if (idx < subemotions.size) {
                            val sub = subemotions[idx]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(baseColor)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xB0FFD700),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable {
                                        val db = FirebaseFirestore.getInstance()
                                        val data = hashMapOf(
                                            "userId" to userId,
                                            "emotion" to emotionName,
                                            "subemotion" to sub,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        db.collection("emotions").add(data).addOnSuccessListener {
                                            Toast.makeText(context, "Subemoción: $sub guardada", Toast.LENGTH_SHORT).show()
                                            navController.navigate("home_user") {
                                                popUpTo("emotionreport") { inclusive = true }
                                            }
                                        }.addOnFailureListener {
                                            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sub,
                                    color = if (emotionName == "Alegría") Color(0xFF222222) else Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}