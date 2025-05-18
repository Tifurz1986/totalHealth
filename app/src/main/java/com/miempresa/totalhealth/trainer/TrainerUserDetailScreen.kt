package com.miempresa.totalhealth.trainer

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Summarize // Icono para Reportes Diarios
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.miempresa.totalhealth.ui.common.PremiumButton
import com.miempresa.totalhealth.ui.theme.PremiumDarkCharcoal
import com.miempresa.totalhealth.ui.theme.PremiumGold
import com.miempresa.totalhealth.ui.theme.PremiumIconGold
import com.miempresa.totalhealth.ui.theme.OriginalDetailLabelColor
import com.miempresa.totalhealth.ui.theme.OriginalDetailValueColor
import com.miempresa.totalhealth.ui.theme.OriginalGradientTop
import com.miempresa.totalhealth.ui.theme.OriginalGradientMid
import com.miempresa.totalhealth.ui.theme.OriginalButtonTextColor
import com.miempresa.totalhealth.ui.theme.OriginalCardBackground
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerUserDetailScreen(
    navController: NavController,
    viewModel: TrainerUserDetailViewModel = viewModel(),
    userId: String? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            viewModel.fetchUserProfile(userId)
        }
    }

    val blackGoldGradient = Brush.verticalGradient(
        colors = listOf(
            OriginalGradientTop,
            OriginalGradientMid,
            PremiumGold
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del Usuario",
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
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .background(blackGoldGradient)
                .padding(paddingValues)
        ) {
            when (val currentState = uiState) {
                is UserProfileDetailUiState.Success -> {
                    val user = currentState.userProfile
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize()
                            .padding(top = 32.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(PremiumGold)
                        ) {
                            if (!user.profilePictureUrl.isNullOrBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(user.profilePictureUrl),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(88.dp).clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Avatar",
                                    tint = OriginalButtonTextColor,
                                    modifier = Modifier.size(88.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "${user.name ?: ""} ${user.surname ?: ""}".trim(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = user.email ?: "",
                            color = OriginalDetailValueColor,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = OriginalCardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp, horizontal = 12.dp)
                            ) {
                                DatosPersonalesRow("Rol", user.role ?: "-")
                                DatosPersonalesRow("Edad", user.age?.toString() ?: "-")
                                DatosPersonalesRow("Sexo", user.sex ?: "-")
                                DatosPersonalesRow("Altura", user.height?.let { "$it cm" } ?: "-")
                                DatosPersonalesRow("Peso", user.weight?.let { "$it kg" } ?: "-")
                                DatosPersonalesRow("Nivel de Actividad", user.activityLevel ?: "-")
                                DatosPersonalesRow("Objetivo de Salud", user.healthGoals ?: "-")
                                DatosPersonalesRow(
                                    "Miembro desde",
                                    user.createdAt?.let {
                                        SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(it)
                                    } ?: "-"
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        RegisterProgressButtonOriginal(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            navController.navigate("record_user_progress/${user.uid}")
                        }

                        Spacer(Modifier.height(16.dp))

                        PremiumButton(
                            text = "Ver Reporte de Comida",
                            onClick = {
                                navController.navigate("user_food_report_history/${user.uid}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            icon = Icons.Filled.RestaurantMenu,
                            contentDescription = "Reportes de comida del usuario"
                        )

                        Spacer(Modifier.height(12.dp))

                        PremiumButton(
                            text = "Ver Diario de Mejoras",
                            onClick = {
                                navController.navigate("user_improvement_journal_history/${user.uid}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            icon = Icons.Filled.HistoryEdu,
                            contentDescription = "Diario de mejoras del usuario"
                        )

                        // --- NUEVO BOTÓN PARA REPORTES DIARIOS ---
                        Spacer(Modifier.height(12.dp))

                        PremiumButton(
                            text = "Ver Reportes Diarios",
                            onClick = {
                                navController.navigate("user_daily_log_history/${user.uid}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            icon = Icons.Filled.Summarize, // O Icons.Filled.CalendarToday
                            contentDescription = "Reportes diarios del usuario"
                        )
                        // --- FIN DE NUEVO BOTÓN ---
                    }
                }
                is UserProfileDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PremiumGold
                    )
                }
                is UserProfileDetailUiState.Error -> {
                    val errorMsg = currentState.message
                    Text(
                        text = "Error: $errorMsg",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UserProfileDetailUiState.Idle -> {
                    Text(
                        text = if (userId.isNullOrBlank()) "No se ha seleccionado un usuario." else "Cargando datos del usuario...",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DatosPersonalesRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = OriginalDetailLabelColor,
            fontSize = 15.sp,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            color = OriginalDetailValueColor,
            fontSize = 15.sp
        )
    }
    Divider(
        color = OriginalDetailLabelColor.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}

@Composable
fun RegisterProgressButtonOriginal(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PremiumGold,
            contentColor = OriginalButtonTextColor
        ),
        modifier = modifier
            .height(60.dp)
            .shadow(18.dp, RoundedCornerShape(32.dp))
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Registrar Progreso",
            tint = OriginalButtonTextColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Registrar Progreso",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
