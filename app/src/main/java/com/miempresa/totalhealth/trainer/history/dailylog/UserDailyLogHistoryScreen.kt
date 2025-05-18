package com.miempresa.totalhealth.trainer.history.dailylog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Asegúrate que esta importación sea la correcta
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.dailylog.DailyLog // Importa la clase DailyLog corregida
import com.miempresa.totalhealth.ui.common.PremiumHistoryCard // Asumo que esta es tu tarjeta personalizada
import com.miempresa.totalhealth.ui.theme.PremiumDarkCharcoal // Asumo que estos son tus colores
import com.miempresa.totalhealth.ui.theme.PremiumGold
import com.miempresa.totalhealth.ui.theme.PremiumIconGold
import com.miempresa.totalhealth.ui.theme.PremiumTextGold
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDailyLogHistoryScreen(
    navController: NavController,
    userId: String,
    // Utiliza el Factory para pasar el userId al ViewModel
    viewModel: UserDailyLogHistoryViewModel = viewModel(factory = UserDailyLogHistoryViewModel.Factory(userId))
) {
    val dailyLogs by viewModel.dailyLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reportes Diarios",
                        color = PremiumGold, // Asegúrate que estos colores estén definidos en tu tema
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PremiumIconGold // Asegúrate que estos colores estén definidos
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumDarkCharcoal // Asegúrate que estos colores estén definidos
                )
            )
        },
        containerColor = PremiumDarkCharcoal // Fondo general
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PremiumGold
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Ocurrió un error desconocido.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                dailyLogs.isEmpty() && !isLoading -> {
                    Text(
                        text = "Este usuario aún no tiene registros diarios.",
                        color = PremiumTextGold, // Asegúrate que estos colores estén definidos
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = dailyLogs,
                            key = { log -> log.id } // Usar el id del DailyLog como clave
                        ) { log -> // log es de tipo DailyLog
                            val descriptionParts = mutableListOf<String>()

                            log.waterIntakeLiters?.let {
                                descriptionParts.add("Agua: ${it}L")
                            }
                            if (log.notes.isNotBlank()) {
                                descriptionParts.add("Notas Generales: ${log.notes}")
                            }

                            // Información de Comidas
                            if (log.foodEntries.isNotEmpty()) {
                                descriptionParts.add("Comidas: ${log.foodEntries.size} registradas")
                                // Opcional: detallar más sobre las comidas
                                // log.foodEntries.take(1).forEach { food ->
                                //     descriptionParts.add("  - ${food.mealType}: ${food.description.take(20)}...")
                                // }
                            }

                            // Información de Actividades
                            if (log.activityEntries.isNotEmpty()) {
                                descriptionParts.add("Actividades: ${log.activityEntries.size} registradas")
                                // Opcional: detallar más sobre las actividades
                                // log.activityEntries.take(1).forEach { activity ->
                                // descriptionParts.add("  - ${activity.type} (${activity.durationMinutes} min)")
                                // }
                            }

                            // Información de Emoción
                            log.emotionEntry?.let { emotion ->
                                var emotionDesc = "Emoción: ${emotion.mood}"
                                emotion.moodIntensity?.let { intensity ->
                                    emotionDesc += " (Nivel: $intensity)"
                                }
                                if (emotion.journalEntry.isNotBlank()){
                                    emotionDesc += "\n  Diario: ${emotion.journalEntry.take(30)}..."
                                }
                                descriptionParts.add(emotionDesc)
                            }

                            // Información de Sueño
                            log.sleepEntry?.let { sleep ->
                                var sleepDesc = "Sueño: Calidad ${sleep.sleepQuality}"
                                if (sleep.timeToBed != null && sleep.timeWokeUp != null) {
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    sleepDesc += " (${sdf.format(sleep.timeToBed!!)} - ${sdf.format(sleep.timeWokeUp!!)})"
                                }
                                descriptionParts.add(sleepDesc)
                            }

                            val cardDescription = descriptionParts.joinToString(separator = "\n").ifEmpty { "Sin detalles adicionales." }
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                            PremiumHistoryCard( // Asegúrate que PremiumHistoryCard acepte estos parámetros
                                date = log.date.time, // PremiumHistoryCard espera un Long para la fecha
                                title = "Registro del ${dateFormat.format(log.date)}",
                                description = cardDescription
                                // Puedes añadir más parámetros a PremiumHistoryCard si es necesario
                            )
                        }
                    }
                }
            }
        }
    }
}