// Código profesional y funcional para HomeScreen.kt con acceso rápido a chat, colores modernos,
// saludo y botón "Registrar Mi Día", espacio para AnimatedFeatureCard y pill cards,
// estructura limpia y sin duplicados. Incluye los composables auxiliares.

package com.miempresa.totalhealth.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.miempresa.totalhealth.content.DailyBook
import com.miempresa.totalhealth.content.DailyBookUiState
import com.miempresa.totalhealth.content.DailyBookViewModel
import com.miempresa.totalhealth.content.WeeklyPhrase
import com.miempresa.totalhealth.content.WeeklyPhraseUiState
import com.miempresa.totalhealth.content.WeeklyPhraseViewModel
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.UserProfileUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    weeklyPhraseViewModel: WeeklyPhraseViewModel = viewModel(),
    dailyBookViewModel: DailyBookViewModel = viewModel()
) {
    val currentUser = authViewModel.getCurrentUser()
    val userProfileState by authViewModel.userProfileUiState.collectAsState()
    val context = LocalContext.current

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuro = Color(0xFF004D40)
    val colorVerdeSecundario = Color(0xFF00BFA5)
    val colorBotonGradienteEnd = Color(0xFF00A99D)
    val colorTextoBotonProminente = Color.White

    val fondoDegradado = Brush.verticalGradient(
        colors = listOf(colorNegro, colorVerdeOscuro, colorNegro)
    )

    LaunchedEffect(key1 = currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null && uid.isNotBlank()) {
            if (userProfileState is UserProfileUiState.Idle || userProfileState is UserProfileUiState.Error) {
                authViewModel.loadUserProfile(uid)
            }
        }
    }

    var showPhraseDialog by remember { mutableStateOf(false) }
    var currentPhraseToShow by remember { mutableStateOf<WeeklyPhrase?>(null) }
    val weeklyPhraseState by weeklyPhraseViewModel.uiState.collectAsState()

    var showBookDialog by remember { mutableStateOf(false) }
    var currentBookToShow by remember { mutableStateOf<DailyBook?>(null) }
    val dailyBookState by dailyBookViewModel.uiState.collectAsState()

    var showInjuryReportDialog by remember { mutableStateOf(false) }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize().background(colorNegro), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorVerdePrincipal)
        }
        return
    }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = Color(0xEE181C1B)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        icon = Icons.Filled.RestaurantMenu,
                        label = "Comida",
                        onClick = { navController.navigate("food_report") },
                        color = colorVerdeSecundario
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Assessment,
                        label = "Progreso",
                        onClick = { navController.navigate("progress_screen") },
                        color = colorVerdeSecundario
                    )
                    QuickActionButton(
                        icon = Icons.Filled.CalendarMonth,
                        label = "Citas",
                        onClick = { navController.navigate("appointments_screen") },
                        color = colorVerdeSecundario
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Chat,
                        label = "Chat",
                        onClick = { navController.navigate("chat_screen") },
                        color = Color(0xFF43A047)
                    )
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Total Health", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("edit_profile_screen") }) {
                        when (val state = userProfileState) {
                            is UserProfileUiState.Success -> {
                                if (!state.profile.profilePictureUrl.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = state.profile.profilePictureUrl),
                                        contentDescription = "Foto de Perfil (Editar)",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, colorVerdeSecundario, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Editar Perfil",
                                        tint = colorVerdeSecundario,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            else -> Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Editar Perfil",
                                tint = colorVerdeSecundario,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logoutUser()
                        Toast.makeText(context, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = colorVerdeSecundario)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xCC000000))
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoDegradado)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Banda decorativa top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    colorVerdeSecundario.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    colorVerdeSecundario.copy(alpha = 0.25f)
                                )
                            ),
                            RoundedCornerShape(bottomStart = 90.dp, bottomEnd = 90.dp)
                        )
                        .alpha(0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.height(20.dp))

                // Saludo personalizado
                val displayName = when (val state = userProfileState) {
                    is UserProfileUiState.Success -> state.profile.name.ifBlank { currentUser.email?.substringBefore('@') ?: "Usuario" }
                    else -> currentUser.email?.substringBefore('@') ?: "Usuario"
                }
                Text(
                    text = "¡Hola, $displayName!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = Color(0xFFFFD700),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF212121), Color.Transparent, Color(0xFF212121))
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tu bienestar es nuestra prioridad.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 22.dp)
                )

                var isLoading by remember { mutableStateOf(false) }
                if (isLoading) {
                    LaunchedEffect(isLoading) {
                        delay(1200)
                        isLoading = false
                        navController.navigate("daily_log_screen")
                    }
                }
                Text(
                    "Haz click cada día para registrar tu bienestar",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp, start = 6.dp)
                )
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val elevation by animateDpAsState(
                    targetValue = if (isLoading || pressed) 22.dp else 12.dp,
                    label = "button-elevation"
                )
                Button(
                    onClick = { isLoading = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .height(56.dp)
                        .shadow(elevation = elevation, shape = RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    interactionSource = interactionSource,
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF101010),
                                        Color(0xFFFFD700),
                                        colorBotonGradienteEnd
                                    )
                                ),
                                RoundedCornerShape(30.dp)
                            )
                            .border(1.8.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PostAdd, contentDescription = "Registrar", tint = Color.White, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Registrar Mi Día",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorTextoBotonProminente
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                // AnimatedFeatureCard y espacio para pill cards
                AnimatedFeatureCard(
                    title = "Mi Progreso",
                    description = "Visualiza tu avance físico y mental.",
                    icon = Icons.Filled.Assessment,
                    onClick = { navController.navigate("progress_screen") },
                    colorGlow = Color(0xFF43A047),
                    animationDelay = 0
                )
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedFeatureCard(
                    title = "Reporte de Comida",
                    description = "Lleva un control de tu alimentación.",
                    icon = Icons.Filled.RestaurantMenu,
                    onClick = { navController.navigate("food_report") },
                    colorGlow = colorVerdeSecundario,
                    animationDelay = 100
                )
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedFeatureCard(
                    title = "Diario de Mejoras",
                    description = "Reflexiona sobre tus logros y metas.",
                    icon = Icons.Filled.EditNote,
                    onClick = { navController.navigate("improvement_journal_screen") },
                    colorGlow = Color(0xFFFFD700),
                    animationDelay = 200
                )
                Spacer(modifier = Modifier.height(16.dp))
                val proximaCita = remember { mutableStateOf("Viernes 24 mayo, 18:30") }
                AnimatedFeatureCard(
                    title = "Próxima cita",
                    description = if (proximaCita.value.isNotBlank()) proximaCita.value else "Sin próximas citas",
                    icon = Icons.Filled.CalendarMonth,
                    onClick = { navController.navigate("appointments_screen") },
                    colorGlow = Color(0xFFFFD700),
                    animationDelay = 300
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val phraseContent = weeklyPhraseState
                    PillMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.FormatQuote,
                        label = "Frase",
                        content = when (phraseContent) {
                            is WeeklyPhraseUiState.Success -> "\"${phraseContent.phrase.phrase.take(28)}${if (phraseContent.phrase.phrase.length > 28) "..." else ""}\""
                            is WeeklyPhraseUiState.Loading -> "Cargando..."
                            is WeeklyPhraseUiState.Error -> "Error"
                            is WeeklyPhraseUiState.Empty -> "Sin frase"
                        },
                        onClick = {
                            if (phraseContent is WeeklyPhraseUiState.Success) {
                                currentPhraseToShow = phraseContent.phrase
                                showPhraseDialog = true
                            } else if (phraseContent is WeeklyPhraseUiState.Error || phraseContent is WeeklyPhraseUiState.Empty) {
                                weeklyPhraseViewModel.loadWeeklyPhrase()
                            }
                        },
                        color = Color(0xFFFFD700)
                    )
                    val bookContent = dailyBookState
                    PillMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Book,
                        label = "Libro",
                        content = when (bookContent) {
                            is DailyBookUiState.Success -> bookContent.book.title.take(22) + if (bookContent.book.title.length > 22) "..." else ""
                            is DailyBookUiState.Loading -> "Cargando..."
                            is DailyBookUiState.Error -> "Error"
                            is DailyBookUiState.Empty -> "Sin libro"
                        },
                        onClick = {
                            if (bookContent is DailyBookUiState.Success) {
                                currentBookToShow = bookContent.book
                                showBookDialog = true
                            } else if (bookContent is DailyBookUiState.Error || bookContent is DailyBookUiState.Empty) {
                                dailyBookViewModel.loadBookOfTheMonth()
                            }
                        },
                        color = Color(0xFF43A047)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedFeatureCard(
                    title = "Reportar lesión o molestia",
                    description = "Informa cualquier lesión o molestia física.",
                    icon = Icons.Filled.HealthAndSafety,
                    onClick = { showInjuryReportDialog = true },
                    colorGlow = Color(0xFFD32F2F),
                    animationDelay = 500
                )
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedFeatureCard(
                    title = "Ajustes",
                    description = "Configura tu perfil y preferencias.",
                    icon = Icons.Filled.Settings,
                    onClick = { navController.navigate("settings_screen") },
                    colorGlow = Color(0xFF00897B),
                    animationDelay = 600
                )
                Spacer(modifier = Modifier.height(28.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Total Health - Cuidando de ti.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                )
            }
        }
    }

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
                            ) { Icon(Icons.Filled.Book, contentDescription = "Libro sin imagen", tint = colorVerdePrincipal.copy(alpha = 0.7f), modifier = Modifier.size(60.dp)) }
                        }
                    } ?: Box(
                        modifier = Modifier.height(150.dp).fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Book, contentDescription = "Libro", tint = colorVerdePrincipal.copy(alpha = 0.7f), modifier = Modifier.size(60.dp)) }

                    Text(
                        text = currentBookToShow!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "por ${currentBookToShow!!.author}",
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    currentBookToShow!!.genre?.let { genre ->
                        if (genre.isNotBlank())
                            Text(
                                "Género: $genre",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                    }
                    currentBookToShow!!.publicationDate?.let { date ->
                        if (date.isNotBlank())
                            Text(
                                "Publicado: $date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                    }
                    currentBookToShow!!.description?.let { description ->
                        if (description.isNotBlank())
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Justify,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showBookDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }

    if (showInjuryReportDialog) {
        InjuryReportDialog(
            onDismiss = { showInjuryReportDialog = false },
            onReport = { zona, gravedad, descripcion ->
                showInjuryReportDialog = false
                // Aquí puedes guardar el reporte en Firestore o mostrar un toast
            }
        )
    }
}

@Composable
fun AnimatedFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    colorGlow: Color,
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .shadow(18.dp, RoundedCornerShape(18.dp))
                .border(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            colorGlow.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.08f),
                            colorGlow.copy(alpha = 0.7f)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f))
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(colorGlow.copy(alpha = 0.25f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(24f, 24f),
                                radius = 36f
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = colorGlow, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorGlow
                    )
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PillMiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    content: String,
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .shadow(3.dp, RoundedCornerShape(30.dp)),
        color = Color.White.copy(alpha = 0.13f),
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.38f))
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                content,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.13f),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun InjuryReportDialog(
    onDismiss: () -> Unit,
    onReport: (String, String, String) -> Unit
) {
    var zona by remember { mutableStateOf("") }
    var gravedad by remember { mutableStateOf("Leve") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar lesión o molestia") },
        text = {
            Column {
                OutlinedTextField(
                    value = zona,
                    onValueChange = { zona = it },
                    label = { Text("Zona afectada (ej: rodilla)") }
                )
                Spacer(Modifier.height(8.dp))
                DropdownMenuBox(
                    value = gravedad,
                    onValueChange = { gravedad = it }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onReport(zona, gravedad, descripcion) },
                enabled = zona.isNotBlank() && descripcion.isNotBlank()
            ) { Text("Reportar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DropdownMenuBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("Leve", "Moderada", "Grave")
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text("Gravedad") },
            readOnly = true,
            modifier = Modifier.clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onValueChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}
