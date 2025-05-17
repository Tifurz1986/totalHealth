package com.miempresa.totalhealth.progress

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.auth.AuthAndRoleUiState
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.common.composables.StarRatingDisplay
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    progressViewModel: ProgressViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val coachRatingsState by progressViewModel.coachRatingsUiState.collectAsState()
    val authState by authViewModel.authAndRoleUiState.collectAsState()

    // Smart cast fix
    LaunchedEffect(authState) {
        val currentAuthState = authState
        if (currentAuthState is AuthAndRoleUiState.Authenticated) {
            currentAuthState.userProfile?.uid?.takeIf { it.isNotBlank() }?.let { userId ->
                progressViewModel.loadLatestCoachRatingsForUser(userId)
            } ?: progressViewModel.resetCoachRatingsUiState()
        } else {
            progressViewModel.resetCoachRatingsUiState()
        }
    }

    // Degradado de fondo idéntico al de la imagen
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00312C), // Verde oscuro arriba
            Color(0xFF04343C), // Azul verdoso medio
            Color(0xFF03151B)  // Casi negro abajo
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Progreso (Valoración del Coach)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03151B))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(paddingValues)
        ) {
            when (val state = coachRatingsState) {
                is CoachRatingsUiState.Success -> {
                    state.progressRatings?.let { ratings ->
                        RatingsContentStyled(ratings)
                    } ?: EmptyState("AÚN NO HAY VALORACIONES DE TU COACH.")
                }
                is CoachRatingsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFFD700)
                    )
                }
                is CoachRatingsUiState.Error -> {
                    EmptyState("ERROR: ${state.message.uppercase()}")
                }
                else -> EmptyState("CARGANDO...")
            }
        }
    }
}

@Composable
private fun RatingsContentStyled(ratings: UserProgressRatings) {
    val formatter = remember { SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale("es", "ES")) }
    val period = ratings.periodId?.uppercase() ?: "PERÍODO NO DISPONIBLE"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = period,
                color = Color(0xFF00CCBC),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
        item {
            Text(
                text = "Valoración General del Período",
                color = Color(0xFF19E3C6),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09423D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StarRatingDisplay(
                        rating = ratings.overallAverageRating,
                        maxStars = 5,
                        starSize = 34.dp,
                        starColor = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ratings.generalCoachFeedback ?: "Sin feedback disponible.",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    ratings.lastUpdated?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Última actualización: ${formatter.format(it)}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
        item {
            Divider(color = Color(0xFF19E3C6).copy(alpha = 0.6f), thickness = 1.dp)
        }
        item {
            Text(
                text = "Valoraciones por Categoría:",
                color = Color(0xFF19E3C6),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
            )
        }
        items(ratings.ratings) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09423D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = category.categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StarRatingDisplay(
                        rating = category.rating,
                        maxStars = 5,
                        starSize = 23.dp,
                        starColor = Color(0xFFFFD700)
                    )
                    if (!category.coachNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Notas del Coach:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                        Text(
                            text = category.coachNotes,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.EmptyState(message: String) {
    Text(
        text = message,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.Center)
            .padding(24.dp)
    )
}
