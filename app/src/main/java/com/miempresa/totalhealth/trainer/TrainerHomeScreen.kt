package com.miempresa.totalhealth.trainer

// VERIFICA TUS IMPORTACIONES AQUÍ. DEBEN ESTAR TODAS LAS NECESARIAS.
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme // Asegúrate que MaterialTheme está importado si lo usas para errorTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.UserProfile
// ESTA ES LA IMPORTACIÓN CRUCIAL. ASEGÚRATE DE QUE ES LA ÚNICA RELACIONADA CON ProfessionalGoldPalette
import com.miempresa.totalhealth.ui.menu.theme.ProfessionalGoldPalette

// NO DEBE HABER NINGUNA DEFINICIÓN DE 'object ProfessionalGoldPalette { ... }' EN ESTE ARCHIVO.
// SI VES UNA, BORRALA COMPLETAMENTE.

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    trainerViewModel: TrainerViewModel = viewModel()
) {
    val userListUiState by trainerViewModel.userListUiState.collectAsState()

    val blackToGoldGradientBrush = Brush.linearGradient(
        colors = listOf(
            ProfessionalGoldPalette.DeepBlack, // Esto debe resolverse desde la importación
            ProfessionalGoldPalette.MidGold,
            ProfessionalGoldPalette.RichGold
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Entrenador", color = ProfessionalGoldPalette.AppBarContent) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfessionalGoldPalette.AppBarBackground,
                ),
                actions = {
                    IconButton(onClick = { trainerViewModel.fetchAllUsers() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refrescar Lista",
                            tint = ProfessionalGoldPalette.AppBarContent
                        )
                    }
                    IconButton(onClick = {
                        authViewModel.logoutUser()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = ProfessionalGoldPalette.AppBarContent
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackToGoldGradientBrush)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Usuarios Registrados",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfessionalGoldPalette.TitleTextOnGradient,
                    modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
                )

                when (val state = userListUiState) {
                    is UserListUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                            color = ProfessionalGoldPalette.RichGold
                        )
                    }
                    is UserListUiState.Success -> {
                        if (state.users.isEmpty()) {
                            Text(
                                text = "No hay usuarios registrados.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                                color = ProfessionalGoldPalette.SoftGold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            UserList(users = state.users, navController = navController)
                        }
                    }
                    is UserListUiState.Error -> {
                        val errorTextStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = ProfessionalGoldPalette.ErrorTextColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = state.message,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 30.dp, start = 16.dp, end = 16.dp)
                                .background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            style = errorTextStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserList(users: List<UserProfile>, navController: NavController) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(users) { user ->
            UserItem(user = user, navController = navController)
        }
    }
}

@Composable
fun UserItem(user: UserProfile, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ProfessionalGoldPalette.BorderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (user.uid.isNotBlank()) {
                    navController.navigate("trainer_user_detail/${user.uid}")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfessionalGoldPalette.CardBackground.copy(alpha = 0.92f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Icono de Usuario",
                modifier = Modifier.size(48.dp),
                tint = ProfessionalGoldPalette.IconTint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName.ifEmpty { "Nombre no disponible" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = ProfessionalGoldPalette.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email.ifEmpty { "Email no disponible" },
                    fontSize = 15.sp,
                    color = ProfessionalGoldPalette.TextSecondary
                )
                if (user.role.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rol: ${user.role.uppercase()}",
                        fontSize = 13.sp,
                        color = ProfessionalGoldPalette.IconTint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
