package com.miempresa.totalhealth.trainer.history.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.foodreport.FoodReport // Importa tu data class
import com.miempresa.totalhealth.ui.common.PremiumHistoryCard
import com.miempresa.totalhealth.ui.theme.PremiumDarkCharcoal
import com.miempresa.totalhealth.ui.theme.PremiumGold
import com.miempresa.totalhealth.ui.theme.PremiumIconGold
import com.miempresa.totalhealth.ui.theme.PremiumTextGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFoodReportHistoryScreen(
    navController: NavController,
    userId: String,
    viewModel: UserFoodReportHistoryViewModel = viewModel(factory = UserFoodReportHistoryViewModelFactory(userId))
) {
    LaunchedEffect(userId) {
        Log.d("userId_debug", "userId recibido: $userId")
    }
    val foodReports by viewModel.foodReports.collectAsState()
    LaunchedEffect(foodReports) {
        Log.d("food_debug", "foodReports.size en UI: ${foodReports.size}")
        foodReports.forEach { report ->
            Log.d("food_debug", "FoodReport en UI: $report")
        }
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reportes de Comida",
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
        containerColor = PremiumDarkCharcoal
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
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                foodReports.isEmpty() && !isLoading -> {
                    Text(
                        text = "Este usuario aún no tiene reportes de comida.",
                        color = PremiumTextGold,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(foodReports, key = { report: FoodReport -> report.id }) { report: FoodReport ->
                            PremiumHistoryCard(
                                // --- ACCESO A PROPIEDADES ---
                                date = report.mealTimestamp?.time ?: 0L, // Convertir Date? a Long. Usar createdAt?.time si mealTimestamp no es el deseado.
                                title = report.mealType.ifBlank { null }, // mealType ya estaba bien
                                description = report.comment, // Cambiado de description a comment
                                photoUrl = report.imageUrl // Cambiado de photoUrl a imageUrl
                                // --- FIN DE ACCESO  ---
                            )
                        }
                    }
                }
            }
        }
    }
}
