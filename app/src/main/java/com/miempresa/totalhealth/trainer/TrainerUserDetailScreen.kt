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
import com.miempresa.totalhealth.auth.UserProfile
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
            Color(0xFF141414),
            Color(0xFF23211C),
            Color(0xFFFFD700)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del Usuario",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF111111))
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .background(blackGoldGradient)
                .padding(paddingValues)
        ) {
            when (uiState) {
                is UserProfileDetailUiState.Success -> {
                    val user = (uiState as UserProfileDetailUiState.Success).userProfile
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize()
                            .padding(top = 32.dp, bottom = 24.dp)
                    ) {
                        // Avatar dorado
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700))
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
                                    tint = Color(0xFF222222),
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
                            color = Color(0xFFCCC18A),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        // Card de datos principales
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
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

                        // BOTÓN FUNCIONAL - fuera de la tarjeta
                        Spacer(Modifier.height(36.dp))
                        RegisterProgressButton {
                            // NAVEGACIÓN FUNCIONAL (lógica original adaptada)
                            navController.navigate("record_user_progress/${user.uid}")
                        }
                    }
                }
                is UserProfileDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFFD700)
                    )
                }
                is UserProfileDetailUiState.Error -> {
                    val errorMsg = (uiState as UserProfileDetailUiState.Error).message
                    Text(
                        text = "Error: $errorMsg",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Text(
                        text = "Selecciona un usuario para ver detalles.",
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.Center)
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
            color = Color(0xFFFFD700),
            fontSize = 15.sp,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            color = Color(0xFFEEE8BB),
            fontSize = 15.sp
        )
    }
    Divider(
        color = Color(0x33FFD700),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 0.dp)
    )
}

@Composable
fun RegisterProgressButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD700),
            contentColor = Color(0xFF23211C)
        ),
        modifier = Modifier
            .padding(horizontal = 48.dp)
            .height(60.dp)
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(32.dp))
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Registrar Progreso",
            tint = Color(0xFF23211C),
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
