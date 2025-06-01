package com.miempresa.totalhealth.trainer.history.journal

// Importaciones de AndroidX y Material Components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// Importaciones de tu proyecto
import com.miempresa.totalhealth.journal.ImprovementJournalEntry // Tu data class
import com.miempresa.totalhealth.ui.common.PremiumHistoryCard
import com.miempresa.totalhealth.ui.theme.PremiumDarkCharcoal
import com.miempresa.totalhealth.ui.theme.PremiumGold
import com.miempresa.totalhealth.ui.theme.PremiumIconGold
import com.miempresa.totalhealth.ui.theme.PremiumTextGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserImprovementJournalHistoryScreen(
    navController: NavController,
    userId: String,
    viewModel: UserImprovementJournalHistoryViewModel = viewModel(factory = UserImprovementJournalHistoryViewModel.Factory(userId))
) {
    val journalEntries by viewModel.journalEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Diario de Mejoras",
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PremiumIconGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumDarkCharcoal
                )
            )
        },
        containerColor = PremiumDarkCharcoal
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PremiumGold
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Ocurrió un error desconocido.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                journalEntries.isEmpty() && !isLoading -> {
                    Text(
                        text = "Este usuario aún no tiene entradas en el diario de mejoras.",
                        color = PremiumTextGold,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val visibleEntries = journalEntries.filter { it.entryDate != null && (it.title.isNotBlank() || it.category?.isNotBlank() == true || it.content.isNotBlank()) }
                        items(visibleEntries, key = { entry: ImprovementJournalEntry -> entry.id }) { entry: ImprovementJournalEntry ->
                            // Determinar el título para la card
                            val cardTitle = if (entry.title.isNotBlank()) {
                                entry.title
                            } else {
                                entry.category?.takeIf { it.isNotBlank() } ?: "Entrada" // Usa categoría si el título está vacío, o "Entrada"
                            }

                            PremiumHistoryCard(
                                // --- ACCESO A PROPIEDADES CORREGIDO ---
                                date = entry.entryDate.time, // entryDate es Date no nula.
                                title = cardTitle,
                                description = entry.content // 'content' es el texto principal.
                                // No photoUrl para el diario
                                // --- FIN DE ACCESO CORREGIDO ---
                            )
                        }
                    }
                }
            }
        }
    }
}
