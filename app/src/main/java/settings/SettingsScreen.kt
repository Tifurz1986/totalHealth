package com.miempresa.totalhealth.settings // Asegúrate de que este paquete sea correcto

import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Para el botón de volver
import androidx.compose.material.icons.filled.Notifications // Ejemplo de icono
import androidx.compose.material.icons.filled.Palette // Ejemplo de icono
import androidx.compose.material.icons.filled.Person // Ejemplo de icono
import androidx.compose.material.icons.filled.Info // Ejemplo de icono
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current // Para Toasts si los necesitas

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B) // Teal 700
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)
    val colorTextoSecundarioClaro = Color.White.copy(alpha = 0.7f)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorNegro
                )
            )
        },
        containerColor = colorNegro // Fondo general del Scaffold
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(colorNegro, colorVerdeOscuroDegradado)
                    )
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Para permitir scroll si hay muchas opciones
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Aquí puedes añadir las diferentes opciones de configuración
            // Por ahora, solo unos placeholders

            SettingOptionItem(
                icon = Icons.Filled.Person,
                title = "Editar Perfil",
                description = "Actualiza tu información personal y de salud.",
                onClick = {
                    Toast.makeText(context, "Editar Perfil (Próximamente)", Toast.LENGTH_SHORT).show()
                    // TODO: Navegar a una pantalla de edición de perfil si la tienes
                }
            )

            SettingOptionItem(
                icon = Icons.Filled.Notifications,
                title = "Notificaciones",
                description = "Configura tus alertas y recordatorios.",
                onClick = {
                    Toast.makeText(context, "Ajustes de Notificaciones (Próximamente)", Toast.LENGTH_SHORT).show()
                    // TODO: Navegar a una pantalla de ajustes de notificaciones
                }
            )

            SettingOptionItem(
                icon = Icons.Filled.Palette,
                title = "Apariencia",
                description = "Personaliza el tema de la aplicación.",
                onClick = {
                    Toast.makeText(context, "Ajustes de Apariencia (Próximamente)", Toast.LENGTH_SHORT).show()
                    // TODO: Implementar cambio de tema
                }
            )

            SettingOptionItem(
                icon = Icons.Filled.Info,
                title = "Acerca de",
                description = "Información sobre Total Health y versión.",
                onClick = {
                    Toast.makeText(context, "Pantalla Acerca de (Próximamente)", Toast.LENGTH_SHORT).show()
                    // TODO: Mostrar un diálogo o pantalla de "Acerca de"
                }
            )

            // Puedes añadir más opciones aquí
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
    val colorVerdePrincipal = Color(0xFF00897B) // Teal 700
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
        // Opcional: Icono de flecha a la derecha
        // Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorTextoSecundarioClaro)
    }
    Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
}
