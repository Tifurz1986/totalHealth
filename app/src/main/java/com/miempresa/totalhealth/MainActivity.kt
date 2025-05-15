package com.miempresa.totalhealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// Asegúrate que la importación de AppNavigation sea la correcta si moviste el archivo
import com.miempresa.totalhealth.navigation.AppNavigation
import com.miempresa.totalhealth.ui.theme.TotalHealthTheme
// import com.google.firebase.FirebaseApp // Firebase se inicializa automáticamente por el manifest merger si usas la librería firebase-bom y google-services plugin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // FirebaseApp.initializeApp(this) // No suele ser necesario si google-services.json está bien configurado.
        // La inicialización automática es preferida.

        enableEdgeToEdge() // Para UI que se extiende a los bordes de la pantalla
        setContent {
            TotalHealthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // AppNavigation es el punto de entrada de la navegación
                    AppNavigation()
                }
            }
        }
    }
}