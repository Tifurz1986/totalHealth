package com.miempresa.totalhealth.trainer

import android.annotation.SuppressLint
import android.util.Log // Asegúrate de importar Log
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit // Icono para editar/registrar progreso
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.miempresa.totalhealth.R
import com.miempresa.totalhealth.auth.UserProfile
import com.miempresa.totalhealth.ui.menu.theme.ProfessionalGoldPalette
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerUserDetailScreen(
    navController: NavController,
    userId: String?, // Este userId es el del perfil que se está viendo
    viewModel: TrainerUserDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            Log.d("TrainerUserDetailScreen", "Fetching profile for userId: $userId")
            viewModel.fetchUserProfile(userId)
        } else {
            Log.w("TrainerUserDetailScreen", "userId is null or blank. Cannot fetch profile.")
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
                title = { Text("Detalle del Usuario", color = ProfessionalGoldPalette.AppBarContent) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ProfessionalGoldPalette.AppBarContent
                        )
                    }
                },
                actions = { // ACCIONES EN LA TOPAPPBAR
                    if (!userId.isNullOrBlank()) { // Solo mostrar si tenemos un userId
                        IconButton(onClick = {
                            Log.d("TrainerUserDetailScreen", "Navigating to record_user_progress for userId: $userId")
                            navController.navigate("record_user_progress/$userId")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit, // O un ícono más específico para "progreso"
                                contentDescription = "Registrar Progreso",
                                tint = ProfessionalGoldPalette.AppBarContent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfessionalGoldPalette.AppBarBackground,
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
            when (val state = uiState) {
                is UserProfileDetailUiState.Loading -> {
                    Log.d("TrainerUserDetailScreen", "Displaying Loading state")
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ProfessionalGoldPalette.RichGold
                    )
                }
                is UserProfileDetailUiState.Success -> {
                    Log.d("TrainerUserDetailScreen", "Displaying Success state for UserProfile: ${state.userProfile.email}")
                    UserProfileContent(userProfile = state.userProfile)
                }
                is UserProfileDetailUiState.Error -> {
                    Log.e("TrainerUserDetailScreen", "Displaying Error state: ${state.message}")
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ProfessionalGoldPalette.ErrorTextColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                is UserProfileDetailUiState.Idle -> {
                    if (userId.isNullOrBlank()) {
                        Log.w("TrainerUserDetailScreen", "Displaying Idle state: userId is null or blank")
                        Text(
                            "ID de usuario no proporcionado o inválido.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            color = ProfessionalGoldPalette.SoftGold,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    } else {
                        Log.d("TrainerUserDetailScreen", "Displaying Idle state (profile not yet loaded for $userId, or ViewModel did not find it)")
                        // Podrías mostrar un texto indicando que se está cargando o que no se encontró
                        // si el LaunchedEffect ya se disparó. Por ahora se queda como estaba.
                        // Si el ViewModel emitió Error, se mostrará el mensaje de error.
                        // Si aún no se ha llamado a fetchUserProfile o está en proceso, se verá Loading o nada.
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(userProfile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imagePainter = if (!userProfile.profilePictureUrl.isNullOrEmpty()) {
            rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = userProfile.profilePictureUrl)
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                        placeholder(R.drawable.ic_default_profile_placeholder) // CAMBIAR POR UN PLACEHOLDER REAL
                        error(R.drawable.ic_default_profile_placeholder) // CAMBIAR POR UN ICONO DE ERROR/PLACEHOLDER REAL
                    }).build()
            )
        } else {
            null
        }

        if (imagePainter != null) {
            Image(
                painter = imagePainter,
                contentDescription = "Foto de perfil de ${userProfile.name}",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ProfessionalGoldPalette.SoftGold.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Foto de perfil por defecto",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ProfessionalGoldPalette.SoftGold.copy(alpha = 0.3f))
                    .padding(12.dp),
                tint = ProfessionalGoldPalette.RichGold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userProfile.fullName.ifEmpty { "Nombre no disponible" },
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ProfessionalGoldPalette.TitleTextOnGradient,
            textAlign = TextAlign.Center
        )
        Text(
            text = userProfile.email.ifEmpty { "Email no disponible" },
            fontSize = 16.sp,
            color = ProfessionalGoldPalette.SoftGold,
            modifier = Modifier.padding(bottom = 20.dp),
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfessionalGoldPalette.CardBackground.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailItem(icon = Icons.Filled.Work, label = "Rol", value = userProfile.role.uppercase())
                userProfile.age?.let { DetailItem(icon = Icons.Filled.CalendarToday, label = "Edad", value = "$it años") }
                if (userProfile.sex.isNotBlank()) {
                    DetailItem(icon = Icons.Filled.Person, label = "Sexo", value = userProfile.sex)
                }
                userProfile.height?.let { DetailItem(icon = Icons.Filled.Height, label = "Altura", value = "$it cm") }
                userProfile.weight?.let { DetailItem(icon = Icons.Filled.Scale, label = "Peso", value = "$it kg") }
                if (userProfile.activityLevel.isNotBlank()) {
                    DetailItem(icon = Icons.Filled.FitnessCenter, label = "Nivel de Actividad", value = userProfile.activityLevel)
                }
                if (userProfile.healthGoals.isNotBlank()) {
                    DetailItem(icon = Icons.Filled.Info, label = "Objetivos de Salud", value = userProfile.healthGoals, isMultiline = true)
                }
                userProfile.createdAt?.let { date ->
                    // Corrección en SimpleDateFormat: yyyy para el año
                    val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                    DetailItem(icon = Icons.Filled.CalendarToday, label = "Miembro desde", value = dateFormat.format(date))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String, isMultiline: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ProfessionalGoldPalette.IconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfessionalGoldPalette.TextSecondary
                )
                Text(
                    text = value.ifEmpty { "-" },
                    fontSize = 16.sp,
                    color = ProfessionalGoldPalette.TextPrimary,
                    lineHeight = if (isMultiline) 22.sp else TextUnit.Unspecified
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = ProfessionalGoldPalette.BorderColor.copy(alpha = 0.2f)
        )
    }
}