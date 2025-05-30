package com.miempresa.totalhealth.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.miempresa.totalhealth.ui.EmotionGridSelector
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionReportEntryScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF002A32),
                        Color(0xFF003A45),
                        Color(0xFF001F25)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reportar emoción", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0B1E22)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "¿Qué sientes ahora?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                EmotionGridSelector(
                    userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                    modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    onEmotionSelected = { emotion ->
                        navController.navigate("subemotion/${emotion.name}/${FirebaseAuth.getInstance().currentUser?.uid}")
                    }
                )
            }
        }
    }
}