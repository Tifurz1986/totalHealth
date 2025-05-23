// Código profesional y funcional para HomeScreen.kt con acceso rápido a chat, colores modernos,
// saludo y botón "Registrar Mi Día", espacio para AnimatedFeatureCard y pill cards,
// estructura limpia y sin duplicados. Incluye los composables auxiliares.

package com.miempresa.totalhealth.ui

import com.miempresa.totalhealth.injuryreport.InjuryReportScreen

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
    val colorVerdeTurquesa = Color(0xFF00BFA5)
    val colorGlowBorde = Color(0xFF00BFA5).copy(alpha = 0.55f)
    val colorGlowAmarillo = Color(0xFFFFD700).copy(alpha = 0.60f)
    val colorAmarilloFuerte = Color(0xFFFFD700)
    val fondoDegradado = Brush.verticalGradient(
        colors = listOf(colorVerdeOscuro, colorNegro)
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
            BottomBarHome(
                navController = navController,
                activeColor = colorVerdeTurquesa,
                fondoOscuro = colorNegro.copy(alpha = 0.90f)
            )
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

                Spacer(modifier = Modifier.height(24.dp))

                // Saludo personalizado
                val displayName = when (val state = userProfileState) {
                    is UserProfileUiState.Success -> state.profile.name.ifBlank { currentUser.email?.substringBefore('@') ?: "Usuario" }
                    else -> currentUser.email?.substringBefore('@') ?: "Usuario"
                }
                Text(
                    text = "¡Hola, $displayName!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
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
                        .height(62.dp)
                        .shadow(elevation = elevation, shape = RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.68f)),
                    contentPadding = PaddingValues(),
                    interactionSource = interactionSource,
                    enabled = !isLoading,
                    border = BorderStroke(2.dp, colorGlowAmarillo)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = colorAmarilloFuerte,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.PostAdd,
                                    contentDescription = "Registrar",
                                    tint = colorAmarilloFuerte,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    "Registrar Mi Día",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorAmarilloFuerte
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
                    colorGlow = colorVerdeSecundario,
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
                    colorGlow = colorVerdeSecundario,
                    animationDelay = 200
                )
                Spacer(modifier = Modifier.height(16.dp))
                val proximaCita = remember { mutableStateOf("Viernes 24 mayo, 18:30") }
                AnimatedFeatureCard(
                    title = "Próxima cita",
                    description = if (proximaCita.value.isNotBlank()) proximaCita.value else "Sin próximas citas",
                    icon = Icons.Filled.CalendarMonth,
                    onClick = { navController.navigate("appointments_screen") },
                    colorGlow = colorVerdeSecundario,
                    animationDelay = 300
                )
                Spacer(modifier = Modifier.height(14.dp))
                // InfoFeatureCards destacados - Frase Semanal y Libro del Mes como mini cards
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val phraseContent = weeklyPhraseState
                    FeaturedMiniCard(
                        icon = Icons.Filled.FormatQuote,
                        title = "Frase Semanal",
                        mainText = when (phraseContent) {
                            is WeeklyPhraseUiState.Success -> phraseContent.phrase.phrase
                            is WeeklyPhraseUiState.Loading -> "Cargando..."
                            is WeeklyPhraseUiState.Error -> "Error"
                            is WeeklyPhraseUiState.Empty -> "Sin frase"
                        },
                        secondaryText = when (phraseContent) {
                            is WeeklyPhraseUiState.Success -> phraseContent.phrase.author
                            else -> null
                        },
                        onClick = {
                            if (phraseContent is WeeklyPhraseUiState.Success) {
                                currentPhraseToShow = phraseContent.phrase
                                showPhraseDialog = true
                            } else if (phraseContent is WeeklyPhraseUiState.Error || phraseContent is WeeklyPhraseUiState.Empty) {
                                weeklyPhraseViewModel.loadWeeklyPhrase()
                            }
                        },
                        borderColor = colorVerdeSecundario,
                        modifier = Modifier.weight(1f)
                    )
                    val bookContent = dailyBookState
                    FeaturedMiniCard(
                        icon = Icons.Filled.Book,
                        title = "Libro del Mes",
                        mainText = when (bookContent) {
                            is DailyBookUiState.Success -> bookContent.book.title
                            is DailyBookUiState.Loading -> "Cargando..."
                            is DailyBookUiState.Error -> "Error"
                            is DailyBookUiState.Empty -> "Sin libro"
                        },
                        secondaryText = when (bookContent) {
                            is DailyBookUiState.Success -> bookContent.book.author
                            else -> null
                        },
                        onClick = {
                            if (bookContent is DailyBookUiState.Success) {
                                currentBookToShow = bookContent.book
                                showBookDialog = true
                            } else if (bookContent is DailyBookUiState.Error || bookContent is DailyBookUiState.Empty) {
                                dailyBookViewModel.loadBookOfTheMonth()
                            }
                        },
                        borderColor = colorVerdeSecundario,
                        modifier = Modifier.weight(1f)
                    )
                }
                AnimatedFeatureCard(
                    title = "Reportar lesión o molestia",
                    description = "Informa cualquier lesión o molestia física.",
                    icon = Icons.Filled.HealthAndSafety,
                    onClick = { showInjuryReportDialog = true },
                    colorGlow = Color(0xFFD32F2F), // Rojo para borde
                    animationDelay = 500
                )
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedFeatureCard(
                    title = "Ajustes",
                    description = "Configura tu perfil y preferencias.",
                    icon = Icons.Filled.Settings,
                    onClick = { navController.navigate("settings_screen") },
                    colorGlow = colorVerdeSecundario,
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
        InjuryReportScreen(
            userId = currentUser.uid,
            onClose = { showInjuryReportDialog = false }
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
                .shadow(10.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.68f)),
            border = BorderStroke(2.dp, Color(0xFF00BFA5).copy(alpha = 0.55f))
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
                                colors = listOf(colorGlow.copy(alpha = 0.20f), Color.Transparent),
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
                        color = Color.White
                    )
                    Text(
                        text = description,
                        color = Color(0xFFB2FFF2).copy(alpha = 0.88f),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}


@Composable
fun InfoFeatureCard(
    icon: ImageVector,
    title: String,
    mainText: String,
    secondaryText: String?,
    onClick: () -> Unit,
    borderColor: Color = Color(0xFF00BFA5)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.68f)),
        border = BorderStroke(2.dp, borderColor.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = borderColor,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 18.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = mainText,
                    color = Color(0xFFB2FFF2).copy(alpha = 0.88f),
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                secondaryText?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    active: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Todos los botones de la barra inferior tienen el mismo tamaño, alineación y diseño.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(44.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.68f),
                border = if (active) BorderStroke(2.dp, color) else BorderStroke(2.dp, Color.Transparent),
                shadowElevation = if (active) 10.dp else 6.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .then(
                            if (active)
                                Modifier
                                    .background(color.copy(alpha = 0.14f), shape = CircleShape)
                            else Modifier
                        )
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(30.dp)
                    )
                }
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
fun BottomBarHome(
    navController: NavController,
    activeColor: Color,
    fondoOscuro: Color
) {
    // Lista de botones en orden: Home, Progreso, Citas, Chat
    val items = listOf(
        Triple(Icons.Filled.Home, "Home", "home_user"),
        Triple(Icons.Filled.Assessment, "Progreso", "progress_screen"),
        Triple(Icons.Filled.CalendarMonth, "Citas", "appointments_screen"),
        Triple(Icons.Filled.Chat, "Chat", "chat_screen")
    )
    // Puedes usar navController.currentDestination?.route para la ruta activa
    val currentRoute = navController.currentDestination?.route
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = fondoOscuro
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (icon, label, route) ->
                QuickActionButton(
                    icon = icon,
                    label = label,
                    onClick = { navController.navigate(route) },
                    color = activeColor,
                    active = currentRoute == route,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
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

@Composable
fun FeaturedMiniCard(
    icon: ImageVector,
    title: String,
    mainText: String,
    secondaryText: String?,
    onClick: () -> Unit,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(134.dp)
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.68f)),
        border = BorderStroke(2.dp, borderColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = borderColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                text = mainText,
                color = Color(0xFFB2FFF2).copy(alpha = 0.88f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (!secondaryText.isNullOrBlank()) {
                Text(
                    text = secondaryText,
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}