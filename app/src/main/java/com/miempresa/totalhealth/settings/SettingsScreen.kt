package com.miempresa.totalhealth.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
// import androidx.compose.material.icons.filled.Palette // Icono de Apariencia eliminado
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// Importar AuthViewModel si necesitas verificar el estado de autenticación o pasar datos
// import com.miempresa.totalhealth.auth.AuthViewModel
// import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
    // authViewModel: AuthViewModel = viewModel() // Descomentar si es necesario
) {
    val context = LocalContext.current

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorNegro)
            )
        },
        containerColor = colorNegro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = Brush.verticalGradient(colors = listOf(colorNegro, colorVerdeOscuroDegradado)))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            SettingOptionItem(
                icon = Icons.Filled.Person,
                title = "Editar Perfil",
                description = "Actualiza tu información personal.",
                onClick = {
                    // Navegar a la nueva pantalla de edición de perfil
                    navController.navigate("edit_profile_screen")
                }
            )

            // Opción "Apariencia" eliminada

            SettingOptionItem(
                icon = Icons.Filled.Info,
                title = "Acerca de",
                description = "Información sobre Total Health y versión.",
                onClick = {
                    navController.navigate("about_screen")
                }
            )
        }
    }
}

@Composable
fun SettingOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)
    val colorTextoSecundarioClaro = Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = colorVerdePrincipal,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorTextoClaro,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorTextoSecundarioClaro
            )
        }
    }
    Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
}
