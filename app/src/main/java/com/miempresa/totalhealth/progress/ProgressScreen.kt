package com.miempresa.totalhealth.progress

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.auth.AuthAndRoleUiState // Importar
import com.miempresa.totalhealth.auth.AuthViewModel     // Importar
import com.miempresa.totalhealth.ui.menu.theme.ProfessionalGoldPalette
import java.text.SimpleDateFormat
import java.util.Locale
// Importación CORRECTA para StarRatingDisplay
import com.miempresa.totalhealth.common.composables.StarRatingDisplay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    progressViewModel: ProgressViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel() // Inyectar AuthViewModel
) {
    val coachRatingsState by progressViewModel.coachRatingsUiState.collectAsState()
    val authState by authViewModel.authAndRoleUiState.collectAsState()

    LaunchedEffect(authState) { // Se dispara si authState cambia
        val currentAuthState = authState
        Log.d("ProgressScreen", "AuthState changed: $currentAuthState")
        if (currentAuthState is AuthAndRoleUiState.Authenticated) {
            currentAuthState.userProfile?.uid?.let { userId ->
                if (userId.isNotBlank()) {
                    Log.d("ProgressScreen", "Usuario autenticado con UID: $userId. Cargando últimas valoraciones.")
                    progressViewModel.loadLatestCoachRatingsForUser(userId)
                } else {
                    Log.w("ProgressScreen", "UID del UserProfile está vacío en estado Authenticated.")
                    progressViewModel.resetCoachRatingsUiState() // Resetear si no hay UID
                }
            } ?: run {
                Log.w("ProgressScreen", "UserProfile es nulo en estado Authenticated.")
                progressViewModel.resetCoachRatingsUiState() // Resetear si no hay UserProfile
            }
        } else {
            Log.d("ProgressScreen", "Usuario no autenticado o estado de auth no es Authenticated ($currentAuthState). Reseteando valoraciones.")
            progressViewModel.resetCoachRatingsUiState() // Resetear si el usuario no está autenticado
        }
    }

    val blackToGoldGradientBrush = Brush.linearGradient(
        colors = listOf(
            ProfessionalGoldPalette.DeepBlack,
            ProfessionalGoldPalette.MidGold,
            ProfessionalGoldPalette.RichGold
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Último Progreso", color = ProfessionalGoldPalette.AppBarContent) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ProfessionalGoldPalette.AppBarContent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfessionalGoldPalette.AppBarBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackToGoldGradientBrush)
                .padding(paddingValues)
        ) {
            when (val state = coachRatingsState) {
                is CoachRatingsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ProfessionalGoldPalette.RichGold
                    )
                }
                is CoachRatingsUiState.Success -> {
                    val ratingsData = state.progressRatings
                    if (ratingsData != null) {
                        CoachRatingsContent(ratingsData = ratingsData)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay valoraciones de tu coach.",
                                color = ProfessionalGoldPalette.TextPrimary,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(ProfessionalGoldPalette.CardBackground.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            )
                        }
                    }
                }
                is CoachRatingsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error al cargar tus valoraciones: ${state.message}",
                            color = ProfessionalGoldPalette.ErrorTextColor,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(ProfessionalGoldPalette.CardBackground.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        )
                    }
                }
                is CoachRatingsUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Esperando datos...", // Mensaje para el estado Idle
                            color = ProfessionalGoldPalette.TextSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoachRatingsContent(ratingsData: UserProgressRatings) {
    val dateFormat = remember { SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale("es", "ES")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "Feedback General del Coach",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ProfessionalGoldPalette.TitleTextOnGradient,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // LLAMADA A StarRatingDisplay con el parámetro starColor
            StarRatingDisplay(
                rating = ratingsData.overallAverageRating,
                maxStars = 5,
                starSize = 36.dp,
                starColor = ProfessionalGoldPalette.RichGold // Ajustado al parámetro de tu función
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!ratingsData.generalCoachFeedback.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfessionalGoldPalette.CardBackground.copy(alpha = 0.85f))
                ) {
                    Text(
                        text = ratingsData.generalCoachFeedback,
                        color = ProfessionalGoldPalette.TextPrimary,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfessionalGoldPalette.CardBackground.copy(alpha = 0.85f))
                ) {
                    Text(
                        text = "No hay feedback general del coach para este período.",
                        color = ProfessionalGoldPalette.TextSecondary,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            }
            ratingsData.lastUpdated?.let {
                Text(
                    text = "Última actualización: ${dateFormat.format(it)}",
                    fontSize = 12.sp,
                    color = ProfessionalGoldPalette.TextSecondary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }
            HorizontalDivider(
                color = ProfessionalGoldPalette.BorderColor.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (ratingsData.ratings.isNotEmpty()) {
            item {
                Text(
                    "Valoraciones por Categoría",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfessionalGoldPalette.TitleTextOnGradient,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(ratingsData.ratings) { categoryRating ->
                CategoryRatingItem(categoryRating = categoryRating)
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfessionalGoldPalette.CardBackground.copy(alpha = 0.85f))
                ){
                    Text(
                        "No hay valoraciones detalladas por categoría.",
                        color = ProfessionalGoldPalette.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun CategoryRatingItem(categoryRating: ProgressCategoryRating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfessionalGoldPalette.CardBackground.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryRating.categoryName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = ProfessionalGoldPalette.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            // LLAMADA A StarRatingDisplay con el parámetro starColor
            StarRatingDisplay(
                rating = categoryRating.rating,
                maxStars = 5,
                starSize = 28.dp,
                starColor = ProfessionalGoldPalette.RichGold // Ajustado al parámetro de tu función
            )
            if (!categoryRating.coachNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notas del Coach:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProfessionalGoldPalette.TextSecondary.copy(alpha = 0.9f)
                )
                Text(
                    text = categoryRating.coachNotes,
                    fontSize = 15.sp,
                    color = ProfessionalGoldPalette.TextPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}