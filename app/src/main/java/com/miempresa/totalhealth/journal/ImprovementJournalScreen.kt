package com.miempresa.totalhealth.journal

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book // Icono para entrada de diario
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovementJournalScreen(
    navController: NavController,
    journalViewModel: ImprovementJournalViewModel = viewModel()
) {
    val context = LocalContext.current
    val journalState by journalViewModel.journalUiState.collectAsState()

    // Cargar las entradas del diario cuando la pantalla se compone por primera vez
    LaunchedEffect(Unit) {
        journalViewModel.loadJournalEntries()
    }

    // Observar el estado de la operación de entrada (para refrescar después de añadir, si es necesario)
    // Esto es más relevante en la pantalla de añadir/editar, pero lo dejamos por si se añaden
    // operaciones directas en esta pantalla en el futuro.
    val entryOperationState by journalViewModel.entryOperationUiState.collectAsState()
    LaunchedEffect(entryOperationState) {
        if (entryOperationState is EntryOperationUiState.Success) {
            // La lista se recarga desde el ViewModel después de añadir, así que no es necesario aquí.
            // journalViewModel.loadJournalEntries() // Opcional: forzar recarga
            journalViewModel.resetEntryOperationState()
        } else if (entryOperationState is EntryOperationUiState.Error) {
            Toast.makeText(context, (entryOperationState as EntryOperationUiState.Error).message, Toast.LENGTH_LONG).show()
            journalViewModel.resetEntryOperationState()
        }
    }

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)
    val colorTextoSecundarioClaro = Color.White.copy(alpha = 0.7f)

    var entryToDelete by remember { mutableStateOf<ImprovementJournalEntry?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Diario de Mejoras", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorNegro)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navegar a la pantalla de añadir nueva entrada
                    navController.navigate("add_edit_improvement_entry_screen") // Usaremos un ID de entrada nulo o especial para "nueva"
                },
                containerColor = colorVerdePrincipal,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Añadir nueva entrada al diario")
            }
        },
        containerColor = colorNegro
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = Brush.verticalGradient(colors = listOf(colorNegro, colorVerdeOscuroDegradado)))
        ) {
            when (val state = journalState) {
                is JournalUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colorVerdePrincipal)
                }
                is JournalUiState.Success -> {
                    if (state.entries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Aún no has añadido ninguna entrada al diario.\n¡Toca el '+' para empezar!",
                                color = colorTextoSecundarioClaro,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.entries, key = { it.id }) { entry ->
                                JournalEntryCard(
                                    entry = entry,
                                    onClick = {
                                        navController.navigate("add_edit_improvement_entry_screen/${entry.id}")
                                    },
                                    onEdit = {
                                        navController.navigate("add_edit_improvement_entry_screen/${entry.id}")
                                    },
                                    onDelete = {
                                        entryToDelete = entry
                                    }
                                )
                            }
                        }
                    }
                }
                is JournalUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Error al cargar entradas: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is JournalUiState.Idle -> {
                    // Estado inicial, podría mostrar un loader o esperar la carga automática
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colorVerdePrincipal)
                }
            }
        }

        if (entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Eliminar entrada") },
                text = { Text("¿Seguro que quieres eliminar esta entrada del diario?") },
                confirmButton = {
                    TextButton(onClick = {
                        journalViewModel.deleteJournalEntry(entryToDelete!!.id)
                        entryToDelete = null
                    }) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: ImprovementJournalEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val colorVerdePrincipal = Color(0xFF00897B)

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Book, // O un icono más específico para diario
                        contentDescription = "Entrada de Diario",
                        tint = colorVerdePrincipal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (entry.title.isNotBlank()) entry.title else "Entrada del ${dateFormatter.format(entry.entryDate)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.title.isNotBlank()) {
                            Text(
                                text = dateFormatter.format(entry.entryDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones", tint = Color.White.copy(alpha = 0.7f))
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Borrar") },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 3, // Mostrar un extracto
                    overflow = TextOverflow.Ellipsis
                )
                entry.category?.let { category ->
                    if (category.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Categoría: $category",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorVerdePrincipal.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
