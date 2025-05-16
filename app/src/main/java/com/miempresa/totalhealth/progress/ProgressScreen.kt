package com.miempresa.totalhealth.progress

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.* // Importar todos los iconos filled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.common.composables.StarRatingDisplay // Importar el Composable de estrellas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    progressViewModel: ProgressViewModel = viewModel()
) {
    val context = LocalContext.current
    val coachRatingsState by progressViewModel.coachRatingsUiState.collectAsState()

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)
    val colorTextoSecundarioClaro = Color.White.copy(alpha = 0.7f)
    val starColor = Color(0xFFFFD700) // Amarillo para las estrellas

    // Efecto para mostrar errores o mensajes
    LaunchedEffect(coachRatingsState) {
        if (coachRatingsState is CoachRatingsUiState.Error) {
            Toast.makeText(context, (coachRatingsState as CoachRatingsUiState.Error).message, Toast.LENGTH_LONG).show()
            progressViewModel.resetCoachRatingsUiState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Progreso (Valoración del Coach)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorNegro)
            )
        },
        containerColor = colorNegro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = Brush.verticalGradient(colors = listOf(colorNegro, colorVerdeOscuroDegradado)))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = coachRatingsState) {
                is CoachRatingsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp), color = colorVerdePrincipal)
                }
                is CoachRatingsUiState.Success -> {
                    val userRatings = state.progressRatings
                    if (userRatings != null) {
                        Text(
                            text = "Valoración General del Período", // Podrías mostrar userRatings.periodId
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorVerdePrincipal,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        StarRatingDisplay(
                            rating = userRatings.overallAverageRating,
                            starSize = 36.dp,
                            starColor = starColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        userRatings.generalCoachFeedback?.let { feedback ->
                            if (feedback.isNotBlank()) {
                                Text(
                                    text = "Feedback General del Coach:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorTextoClaro,
                                    modifier = Modifier.align(Alignment.Start).padding(top = 8.dp, bottom = 4.dp)
                                )
                                Text(
                                    text = feedback,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorTextoSecundarioClaro,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                )
                            }
                        }
                        Divider(color = colorVerdePrincipal.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 16.dp))

                        Text(
                            text = "Valoraciones por Categoría:",
                            style = MaterialTheme.typography.titleLarge,
                            color = colorTextoClaro,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                        )

                        userRatings.ratings.forEach { categoryRating ->
                            RatingCategoryItem(
                                categoryName = categoryRating.categoryName,
                                rating = categoryRating.rating,
                                coachNotes = categoryRating.coachNotes,
                                starColor = starColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                    } else {
                        Text(
                            "Aún no hay valoraciones de tu coach para este período.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorTextoSecundarioClaro,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
                is CoachRatingsUiState.Error -> {
                    // El mensaje de error se muestra mediante el Toast en LaunchedEffect
                    // Aquí podrías mostrar un botón de reintento o un mensaje más persistente si lo deseas.
                    Text(
                        "No se pudieron cargar las valoraciones.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { progressViewModel.loadCoachRatingsForCurrentPeriod() }) {
                        Text("Reintentar")
                    }
                }
                is CoachRatingsUiState.Idle -> {
                    // Podría mostrar un mensaje o simplemente esperar a que se carguen los datos.
                    Text("Cargando datos de progreso...", color = colorTextoSecundarioClaro)
                }
            }
        }
    }
}

@Composable
fun RatingCategoryItem(
    categoryName: String,
    rating: Float,
    coachNotes: String?,
    starColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            StarRatingDisplay(
                rating = rating,
                starSize = 28.dp, // Estrellas un poco más grandes para las categorías
                starColor = starColor
            )
            coachNotes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notas del Coach:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
