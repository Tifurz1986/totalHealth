package com.miempresa.totalhealth.progress

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.miempresa.totalhealth.ui.menu.theme.ProfessionalGoldPalette // Asegúrate que la importación es correcta

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordUserProgressScreen(
    navController: NavController,
    userId: String?
) {
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
                title = { Text("Registrar Progreso", color = ProfessionalGoldPalette.AppBarContent) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ProfessionalGoldPalette.AppBarContent
                        )
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pantalla para Registrar Progreso",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalGoldPalette.TitleTextOnGradient
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Usuario ID: ${userId ?: "No especificado"}",
                    fontSize = 18.sp,
                    color = ProfessionalGoldPalette.SoftGold
                )
                // Aquí iría el formulario para registrar el progreso
            }
        }
    }
}