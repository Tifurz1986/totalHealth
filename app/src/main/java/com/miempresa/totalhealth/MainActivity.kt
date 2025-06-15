package com.miempresa.totalhealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.miempresa.totalhealth.navigation.AppNavigation
import com.miempresa.totalhealth.ui.theme.TotalHealthTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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