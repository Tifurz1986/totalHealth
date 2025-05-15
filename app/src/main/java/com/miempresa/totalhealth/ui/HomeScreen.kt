package com.miempresa.totalhealth.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Esencial para la delegación 'by' con State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel // Para obtener ViewModels
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.miempresa.totalhealth.R
import com.miempresa.totalhealth.content.DailyBook // Asegúrate que estas clases de datos estén definidas
import com.miempresa.totalhealth.content.DailyBookUiState
import com.miempresa.totalhealth.content.DailyBookViewModel
import com.miempresa.totalhealth.content.WeeklyPhrase // Asegúrate que estas clases de datos estén definidas
import com.miempresa.totalhealth.content.WeeklyPhraseUiState
import com.miempresa.totalhealth.content.WeeklyPhraseViewModel
import com.miempresa.totalhealth.auth.AuthViewModel // ViewModel de autenticación

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    weeklyPhraseViewModel: WeeklyPhraseViewModel = viewModel(),
    dailyBookViewModel: DailyBookViewModel = viewModel()
) {
    val currentUser = authViewModel.getCurrentUser()
    val context = LocalContext.current
    Log.d("HomeScreen", "Composing HomeScreen. Current user: ${currentUser?.email}")

    var showPhraseDialog by remember { mutableStateOf(false) }
    var currentPhraseToShow by remember { mutableStateOf<WeeklyPhrase?>(null) }
    // Corrección: Ahora se usa collectAsState() porque los ViewModels exponen StateFlow
    val weeklyPhraseState by weeklyPhraseViewModel.uiState.collectAsState()

    var showBookDialog by remember { mutableStateOf(false) }
    var currentBookToShow by remember { mutableStateOf<DailyBook?>(null) }
    // Corrección: Ahora se usa collectAsState()
    val dailyBookState by dailyBookViewModel.uiState.collectAsState()

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)

    // AppNavigation se encarga de la redirección si currentUser es null.
    // Esta comprobación es una salvaguarda para esta pantalla específica.
    if (currentUser == null) {
        Log.d("HomeScreen", "Current user IS NULL. Displaying loading/empty or waiting for AppNavigation redirect.")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorNegro),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorVerdePrincipal)
        }
        return // No renderizar el resto si no hay usuario.
    }

    // Diálogo para mostrar la frase completa
    if (showPhraseDialog && currentPhraseToShow != null) {
        Dialog(onDismissRequest = { showPhraseDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatQuote,
                        contentDescription = "Frase",
                        tint = colorVerdePrincipal,
                        modifier = Modifier.size(48.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "\"${currentPhraseToShow!!.phrase}\"",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    currentPhraseToShow!!.author?.let { author ->
                        if (author.isNotBlank()) {
                            Text(
                                text = "- $author",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showPhraseDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }

    // Diálogo para mostrar los detalles del Libro
    if (showBookDialog && currentBookToShow != null) {
        Dialog(onDismissRequest = { showBookDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    currentBookToShow!!.imageUrl?.let { url ->
                        if (url.isNotBlank()){
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Portada de ${currentBookToShow!!.title}",
                                modifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(bottom = 16.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier.height(150.dp).fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) { Icon(imageVector = Icons.Filled.Book, contentDescription = "Libro sin imagen", tint = colorVerdePrincipal.copy(alpha = 0.7f), modifier = Modifier.size(60.dp)) }
                        }
                    } ?: Box(
                        modifier = Modifier.height(150.dp).fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Icon(imageVector = Icons.Filled.Book, contentDescription = "Libro", tint = colorVerdePrincipal.copy(alpha = 0.7f), modifier = Modifier.size(60.dp)) }


                    Text(text = currentBookToShow!!.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Text(text = "por ${currentBookToShow!!.author}", style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 12.dp))
                    currentBookToShow!!.genre?.let { genre -> if(genre.isNotBlank()) Text("Género: $genre", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 4.dp)) }
                    currentBookToShow!!.publicationDate?.let { date -> if(date.isNotBlank()) Text("Publicado: $date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 16.dp)) }
                    currentBookToShow!!.description?.let { description -> if(description.isNotBlank()) Text(text = description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Justify, color = MaterialTheme.colorScheme.onSurface) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showBookDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Total Health", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_totalhealth),
                        contentDescription = "Logo App",
                        modifier = Modifier.padding(start = 12.dp).size(32.dp).clip(CircleShape)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        Log.d("HomeScreen", "Logout button clicked.")
                        authViewModel.logoutUser()
                        Toast.makeText(context, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                        // La navegación a login será manejada por AppNavigation
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
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
                .background(
                    brush = Brush.verticalGradient(colors = listOf(colorNegro, colorVerdeOscuroDegradado))
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "¡Hola, ${currentUser.email?.substringBefore('@') ?: "Usuario"}!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Tu bienestar es nuestra prioridad.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            FeatureCard(title = "Mi Progreso", description = "Visualiza tu avance físico y mental.", icon = Icons.Filled.Assessment, onClick = { navController.navigate("progress_screen") })
            Spacer(modifier = Modifier.height(16.dp))
            FeatureCard(title = "Reporte de Comida", description = "Lleva un control de tu alimentación.", icon = Icons.Filled.RestaurantMenu, onClick = { navController.navigate("food_report") })
            Spacer(modifier = Modifier.height(16.dp))
            FeatureCard(title = "Diario de Mejoras", description = "Reflexiona sobre tus logros y metas.", icon = Icons.Filled.EditNote, onClick = {
                Toast.makeText(context, "Diario: Próximamente", Toast.LENGTH_SHORT).show()
            })
            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                val phraseContent = weeklyPhraseState
                FeatureCardSmall(
                    title = "Frase Semanal",
                    icon = Icons.Filled.FormatQuote,
                    onClick = {
                        if (phraseContent is WeeklyPhraseUiState.Success) {
                            currentPhraseToShow = phraseContent.phrase
                            showPhraseDialog = true
                        } else if (phraseContent is WeeklyPhraseUiState.Error || phraseContent is WeeklyPhraseUiState.Empty) {
                            weeklyPhraseViewModel.loadWeeklyPhrase()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    when (phraseContent) {
                        is WeeklyPhraseUiState.Success -> Text("\"${phraseContent.phrase.phrase.take(35)}${if (phraseContent.phrase.phrase.length > 35) "..." else ""}\"", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp)
                        is WeeklyPhraseUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorVerdePrincipal)
                        is WeeklyPhraseUiState.Error -> Text("Error al cargar", style = MaterialTheme.typography.bodySmall, color = Color.Red.copy(alpha = 0.8f))
                        is WeeklyPhraseUiState.Empty -> Text("No hay frase", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                val bookContent = dailyBookState
                FeatureCardSmall(
                    title = "Libro del Mes",
                    icon = Icons.Filled.Book,
                    onClick = {
                        if (bookContent is DailyBookUiState.Success) {
                            currentBookToShow = bookContent.book
                            showBookDialog = true
                        } else if (bookContent is DailyBookUiState.Error || bookContent is DailyBookUiState.Empty) {
                            dailyBookViewModel.loadBookOfTheMonth()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    when (bookContent) {
                        is DailyBookUiState.Success -> {
                            Text(bookContent.book.title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(bookContent.book.author, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        is DailyBookUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorVerdePrincipal)
                        is DailyBookUiState.Error -> Text("Error al cargar", style = MaterialTheme.typography.bodySmall, color = Color.Red.copy(alpha = 0.8f))
                        is DailyBookUiState.Empty -> Text("No hay libro", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            FeatureCard(title = "Ajustes", description = "Configura tu perfil y preferencias.", icon = Icons.Filled.Settings, onClick = {
                navController.navigate("settings_screen")
            })

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Total Health - Cuidando de ti.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )
        }
    }
}

// FeatureCard y FeatureCardSmall se mantienen igual, ya que parecen correctos.
@Composable
fun FeatureCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    val colorVerdePrincipal = Color(0xFF00897B)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colorVerdePrincipal,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun FeatureCardSmall(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val colorVerdePrincipal = Color(0xFF00897B)
    Card(
        modifier = modifier
            .aspectRatio(1.5f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colorVerdePrincipal,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = if (content != null) 4.dp else 0.dp)
            )
            if (content != null) {
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }
}
