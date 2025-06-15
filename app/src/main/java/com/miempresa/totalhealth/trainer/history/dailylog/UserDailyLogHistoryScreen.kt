package com.miempresa.totalhealth.trainer.history.dailylog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Asegúrate que esta importación sea la correcta
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var logAEliminar by remember { mutableStateOf<DailyLog?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var logAEditar by remember { mutableStateOf<DailyLog?>(null) }
    var editNotas by remember { mutableStateOf("") }

    val dailyLogs by viewModel.dailyLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reportes Diarios",
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PremiumIconGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumDarkCharcoal
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
                        color = PremiumTextGold,
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
                                descriptionParts.add("💧 Agua: ${it}L")
                            }

                            descriptionParts.add("📝 Notas Generales: ${log.notes.take(60).plus("...")}")

                            val comidas = log.foodEntries.mapNotNull { entry ->
                                if (entry.mealType.trim().isNotEmpty() || entry.description.trim().isNotEmpty()) {
                                    "${entry.mealType}: ${entry.description.take(30)}..."
                                } else null
                            }
                            if (comidas.isNotEmpty()) {
                                descriptionParts.add("🍽 Comida: ${comidas.take(2).joinToString(", ")}${if (comidas.size > 2) " +${comidas.size - 2} más" else ""}")
                            } else {
                                descriptionParts.add("🍽 Comida: Sin registrar")
                            }

                            val actividades = log.activityEntries.mapNotNull { entry ->
                                if (entry.type.trim().isNotEmpty()) {
                                    val intensidad = if (entry.intensity.trim().isNotEmpty()) " (${entry.intensity})" else ""
                                    val duracion = entry.durationMinutes?.let { " - ${it}min" } ?: ""
                                    "${entry.type}$intensidad$duracion"
                                } else null
                            }
                            if (actividades.isNotEmpty()) {
                                descriptionParts.add("🏃 Actividad: ${actividades.take(2).joinToString(", ")}${if (actividades.size > 2) " +${actividades.size - 2} más" else ""}")
                            } else {
                                descriptionParts.add("🏃 Actividad: Sin registrar")
                            }

                            log.emotionEntry?.let { emotion ->
                                var emotionDesc = "🧠 Emoción: ${emotion.mood}"
                                emotion.moodIntensity?.let { intensity ->
                                    emotionDesc += " (Nivel: $intensity)"
                                }
                                if (emotion.journalEntry.isNotBlank()){
                                    emotionDesc += "\n  Diario: ${emotion.journalEntry.take(60)}..."
                                }
                                descriptionParts.add(emotionDesc)
                            }

                            log.sleepEntry?.let { sleep ->
                                var sleepDesc = "😴 Sueño: Calidad ${sleep.sleepQuality}"
                                if (sleep.timeToBed != null && sleep.timeWokeUp != null) {
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    sleepDesc += " (${sdf.format(sleep.timeToBed!!)} - ${sdf.format(sleep.timeWokeUp!!)})"
                                }
                                descriptionParts.add(sleepDesc)
                            }

                            val cardDescription = descriptionParts.joinToString(separator = "\n").ifEmpty { "Sin detalles adicionales." }

                            var visible by remember { mutableStateOf(true) }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    PremiumHistoryCard(
                                        date = log.date.time,
                                        title = "🗓 Registro del ${dateFormat.format(log.date)}",
                                        description = cardDescription,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 60.dp) // Deja espacio a la derecha para los botones flotantes
                                    )
                                    // Botones de acción flotantes, superpuestos en la esquina inferior derecha
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                logAEditar = log
                                                editNotas = log.notes ?: ""
                                                showEditDialog = true
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(PremiumGold.copy(alpha = 0.2f), CircleShape)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Editar registro",
                                                tint = PremiumGold,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        IconButton(
                                            onClick = {
                                                logAEliminar = log
                                                showDeleteDialog = true
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color.Red.copy(alpha = 0.18f), CircleShape)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Eliminar registro",
                                                tint = Color.Red,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showDeleteDialog && logAEliminar != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("¿Eliminar registro?") },
                    text = { Text("¿Seguro que quieres eliminar este registro diario? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteDailyLog(logAEliminar!!.id) {
                                showDeleteDialog = false
                                logAEliminar = null
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Registro eliminado")
                                }
                            }
                        }) { Text("Sí, eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            logAEliminar = null
                        }) { Text("Cancelar") }
                    }
                )
            }
            if (showEditDialog && logAEditar != null) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Editar registro diario") },
                    text = {
                        Column {
                            Text("Notas:")
                            OutlinedTextField(
                                value = editNotas,
                                onValueChange = { editNotas = it },
                                singleLine = false
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.updateDailyLog(
                                logAEditar!!.id,
                                editNotas
                            ) {
                                showEditDialog = false
                                logAEditar = null
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Registro actualizado")
                                }
                            }
                        }) { Text("Guardar") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showEditDialog = false
                            logAEditar = null
                        }) { Text("Cancelar") }
                    }
                )
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}