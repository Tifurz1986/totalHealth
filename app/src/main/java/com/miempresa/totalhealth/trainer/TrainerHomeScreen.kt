package com.miempresa.totalhealth.trainer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    trainerViewModel: TrainerViewModel = viewModel()
) {
    val userListUiState by trainerViewModel.userListUiState.collectAsState()

    val blackToGoldGradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF141414),
            Color(0xFF23211C),
            Color(0xFFFFD700)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Entrenador", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF181818),
                ),
                actions = {
                    IconButton(onClick = { trainerViewModel.fetchAllUsers() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refrescar Lista",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = {
                        authViewModel.logoutUser()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Color(0xFFFFD700)
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
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Usuarios Registrados",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
                )

                when (val state = userListUiState) {
                    is UserListUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                            color = Color(0xFFFFD700)
                        )
                    }
                    is UserListUiState.Success -> {
                        if (state.users.isEmpty()) {
                            Text(
                                text = "No hay usuarios registrados.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp),
                                color = Color(0xFFE3C15B),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            UserListPro(users = state.users, navController = navController)
                        }
                    }
                    is UserListUiState.Error -> {
                        val errorTextStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFFFFA500),
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
fun UserListPro(users: List<UserProfile>, navController: NavController) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(users) { user ->
            UserItemPro(user = user, navController = navController)
        }
    }
}

@Composable
fun UserItemPro(user: UserProfile, navController: NavController) {
    val TAG = "UserItemClick" // Tag para filtrar en Logcat

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(28.dp))
            .clickable {
                Log.d(TAG, "UserItem clicked. User Name: ${user.fullName}, User Email: ${user.email}, User UID: '${user.uid}'")
                if (user.uid.isNotBlank()) {
                    navController.navigate("trainer_user_detail/${user.uid}")
                } else {
                    Log.w(TAG, "UID is blank. Navigation skipped. User Name: ${user.fullName}")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1C).copy(alpha = 0.98f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con dorado
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Icono de Usuario",
                    modifier = Modifier.size(46.dp),
                    tint = Color(0xFF23211C)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName.ifEmpty { "Nombre no disponible" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email.ifEmpty { "Email no disponible" },
                    fontSize = 15.sp,
                    color = Color(0xFFEEE8BB)
                )
                if (user.role.isNotBlank()) {
                    Spacer(modifier = Modifier.height(7.dp))
                    // Badge dorado para el rol
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFD700),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF23211C),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
