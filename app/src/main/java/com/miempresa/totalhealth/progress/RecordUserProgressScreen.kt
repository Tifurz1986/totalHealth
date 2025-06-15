package com.miempresa.totalhealth.progress

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.miempresa.totalhealth.R // Asegúrate de que este recurso exista
import com.miempresa.totalhealth.common.InteractiveStarRatingInput
import com.miempresa.totalhealth.trainer.UserProfileDetailUiState
import com.miempresa.totalhealth.trainer.TrainerUserDetailViewModel
import com.miempresa.totalhealth.ui.menu.theme.ProfessionalGoldPalette

data class UIRatingCategoryState(
    val categoryId: String,
    val categoryName: String,
    var rating: Int,
    var notes: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionInvalidation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordUserProgressScreen(
    navController: NavController,
    userId: String?,
    progressViewModel: ProgressViewModel = viewModel(),
    userDetailViewModel: TrainerUserDetailViewModel = viewModel()
) {
    val context = LocalContext.current

    val blackToGoldGradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF141414),
            Color(0xFF23211C),
            Color(0xFFFFD700)
        )
    )

    val userProfileState by userDetailViewModel.uiState.collectAsState()
    var userName by remember { mutableStateOf("Cargando usuario...") }
    var userProfilePictureUrl by remember { mutableStateOf<String?>(null) }

    var overallRating by remember { mutableIntStateOf(0) }
    var generalFeedback by remember { mutableStateOf("") }

    val categoriesState = remember {
        mutableStateListOf(
            UIRatingCategoryState("nutrition", "Nutrición", 0, ""),
            UIRatingCategoryState("exercise", "Ejercicio Físico", 0, ""),
            UIRatingCategoryState("mental_wellbeing", "Bienestar Mental", 0, ""),
            UIRatingCategoryState("goal_adherence", "Cumplimiento de Objetivos", 0, "")
        )
    }

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            userDetailViewModel.fetchUserProfile(userId)
            Log.d("RecordUserProgress", "Solicitando perfil para usuario ID: $userId")
        } else {
            Log.w("RecordUserProgress", "userId es nulo o vacío al iniciar.")
            userName = "Usuario no especificado"
        }
    }

    LaunchedEffect(userProfileState) {
        when (val state = userProfileState) {
            is UserProfileDetailUiState.Success -> {
                val profile = state.userProfile
                userName = profile.fullName.ifEmpty { profile.email.ifEmpty { "Usuario" } }
                userProfilePictureUrl = profile.profilePictureUrl
                Log.d("RecordUserProgress", "Perfil de usuario cargado: $userName")
            }
            is UserProfileDetailUiState.Error -> {
                userName = "Error al cargar usuario"
                Log.e("RecordUserProgress", "Error cargando perfil: ${state.message}")
            }
            is UserProfileDetailUiState.Loading -> {
                userName = "Cargando..."
            }
            is UserProfileDetailUiState.Idle -> {
                Log.d("RecordUserProgress", "Estado del perfil de usuario: Idle")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registro de Progreso Semanal",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF181818)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    if (userProfilePictureUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfilePictureUrl)
                                .crossfade(true)
                                .placeholder(R.drawable.ic_default_profile_placeholder)
                                .error(R.drawable.ic_default_profile_placeholder)
                                .build(),
                            contentDescription = "Foto de perfil de $userName",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.22f)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Foto de perfil por defecto",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.22f))
                                .padding(8.dp),
                            tint = Color(0xFFFFD700)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userName.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    RatingSectionCardPro(title = "Valoración General del Coach") {
                        InteractiveStarRatingInput(
                            currentRating = overallRating,
                            onRatingChange = { rating -> overallRating = rating },
                            starSize = 40.dp,
                            selectedColor = Color(0xFFFFD700),
                            unselectedColor = Color(0xFFEEE8BB).copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = generalFeedback,
                            onValueChange = { feedback -> generalFeedback = feedback },
                            label = { Text("Feedback General", color = Color(0xFFFFD700)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFFEEE8BB),
                                cursorColor = Color(0xFFFFD700),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFFFFD700),
                                unfocusedLabelColor = Color(0xFFEEE8BB),
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                itemsIndexed(categoriesState) { index, category ->
                    RatingSectionCardPro(title = category.categoryName) {
                        InteractiveStarRatingInput(
                            currentRating = category.rating,
                            onRatingChange = { newRating ->
                                val updatedCategory = categoriesState[index].copy(rating = newRating)
                                categoriesState[index] = updatedCategory
                            },
                            starSize = 36.dp,
                            selectedColor = Color(0xFFFFD700),
                            unselectedColor = Color(0xFFEEE8BB).copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = category.notes,
                            onValueChange = { newNotes ->
                                val updatedCategory = categoriesState[index].copy(notes = newNotes)
                                categoriesState[index] = updatedCategory
                            },
                            label = { Text("Notas para ${category.categoryName}", color = Color(0xFFFFD700)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFFEEE8BB),
                                cursorColor = Color(0xFFFFD700),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFFFFD700),
                                unfocusedLabelColor = Color(0xFFEEE8BB),
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = if (index == categoriesState.size - 1) ImeAction.Done else ImeAction.Next
                            ),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (userId.isNullOrBlank()) {
                                Toast.makeText(context, "Error: ID de usuario no disponible.", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            val periodId = "2025-CURRENT"

                            val finalCategoryRatings = categoriesState.map { uiState ->
                                ProgressCategoryRating(
                                    categoryId = uiState.categoryId,
                                    categoryName = uiState.categoryName,
                                    rating = uiState.rating.toFloat(),
                                    coachNotes = uiState.notes.takeIf { it.isNotBlank() }
                                )
                            }

                            progressViewModel.saveCoachRatingsForUser(
                                userId = userId,
                                periodId = periodId,
                                overallRating = overallRating.toFloat(),
                                generalFeedback = generalFeedback.takeIf { it.isNotBlank() },
                                categoryRatings = finalCategoryRatings
                            )

                            Toast.makeText(context, "Guardando progreso...", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color(0xFF181818)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Guardar",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = Color(0xFF181818)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text("Guardar Progreso", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// Card visual con solo cambios de color
@Composable
fun RatingSectionCardPro(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF181818).copy(alpha = 0.93f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
