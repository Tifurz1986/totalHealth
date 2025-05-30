package com.miempresa.totalhealth.trainer

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.miempresa.totalhealth.dailylog.DailyLogViewModel
import com.miempresa.totalhealth.ui.common.PremiumButton
import com.miempresa.totalhealth.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerUserDetailScreen(
    navController: NavController,
    viewModel: TrainerUserDetailViewModel = viewModel(),
    userId: String? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var hasInjuries by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            viewModel.fetchUserProfile(userId)
            // Consulta Firestore para ver si el usuario tiene lesiones
            FirebaseFirestore.getInstance()
                .collection("injury_reports")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    hasInjuries = snapshot != null && !snapshot.isEmpty
                }
                .addOnFailureListener { exception ->
                    println("Error cargando lesiones: ${exception.message}")
                    hasInjuries = false
                }
        }
    }

    val blackGoldGradient = Brush.verticalGradient(
        colors = listOf(
            OriginalGradientTop,
            OriginalGradientMid,
            PremiumGold
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del Usuario",
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
        // Añadir snackbarHostState aquí
        snackbarHost = {
            // Se declara más abajo en Success, pero aquí solo referencia si existe
            // Lo inicializaremos en Success y lo pasaremos aquí mediante remember (ver abajo)
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .background(blackGoldGradient)
                .padding(paddingValues)
        ) {
            when (val currentState = uiState) {
                is UserProfileDetailUiState.Success -> {
                    // --- Estado para Snackbar y Switch ---
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()
                    val user = currentState.userProfile
                    var isTrackingEmotions by remember { mutableStateOf(user.trackEmotions) }

                    // --- Actualizar el Scaffold para usar snackbarHostState ---
                    // Necesitamos un Scaffold anidado para el snackbar, porque el principal ya está arriba.
                    // O bien, pasamos el snackbarHostState hacia arriba. Aquí, por simplicidad, Scaffold anidado:
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = Color.Transparent,
                        contentColor = Color.Unspecified
                    ) { innerPadding ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxSize()
                                .padding(top = 32.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                                .padding(innerPadding)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(PremiumGold)
                            ) {
                                if (!user.profilePictureUrl.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(user.profilePictureUrl),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(88.dp).clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Avatar",
                                        tint = OriginalButtonTextColor,
                                        modifier = Modifier.size(88.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "${user.name ?: ""} ${user.surname ?: ""}".trim(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = user.email ?: "",
                                color = OriginalDetailValueColor,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )


                            if (hasInjuries) {
                                IconButton(
                                    onClick = {
                                        navController.navigate("trainer_injury_reports?userId=${user.uid}")
                                    },
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = "Ver lesiones",
                                        tint = Color.Red
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = OriginalCardBackground),
                                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp, horizontal = 12.dp)
                                ) {
                                    DatosPersonalesRow("Rol", user.role ?: "-")
                                    DatosPersonalesRow("Edad", user.age?.toString() ?: "-")
                                    DatosPersonalesRow("Sexo", user.sex ?: "-")
                                    DatosPersonalesRow("Altura", user.height?.let { "$it cm" } ?: "-")
                                    DatosPersonalesRow("Peso", user.weight?.let { "$it kg" } ?: "-")
                                    DatosPersonalesRow("Nivel de Actividad", user.activityLevel ?: "-")
                                    DatosPersonalesRow("Objetivo de Salud", user.healthGoals ?: "-")
                                    DatosPersonalesRow(
                                        "Miembro desde",
                                        user.createdAt?.let {
                                            SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(it)
                                        } ?: "-"
                                    )
                                }
                            }

                            // --- NUEVA CARD PREMIUM PARA ACCIONES Y ESTADÍSTICAS ---
                            Spacer(Modifier.height(24.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(28.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF161616).copy(alpha = 0.92f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 26.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Encabezado premium
                                    Text(
                                        text = "${user.name} ${user.surname}".trim().ifBlank { "Usuario sin nombre" },
                                        color = PremiumGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 23.sp,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = "Miembro desde ${user.createdAt?.let { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(it) } ?: ""}",
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    Divider(Modifier.padding(vertical = 6.dp), color = PremiumGold.copy(alpha = 0.16f))

                                    // Botón registrar progreso
                                    RegisterProgressButtonOriginal(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp)
                                    ) { navController.navigate("record_user_progress/${user.uid}") }

                                    // Botones de acción premium juntos
                                    Spacer(Modifier.height(10.dp))
                                    PremiumButton(
                                        text = "Ver Reporte de Comida",
                                        onClick = { navController.navigate("user_food_report_history/${user.uid}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        icon = Icons.Filled.RestaurantMenu,
                                        contentDescription = "Reportes de comida del usuario"
                                    )
                                    PremiumButton(
                                        text = "Ver Diario de Mejoras",
                                        onClick = { navController.navigate("user_improvement_journal_history/${user.uid}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        icon = Icons.Filled.HistoryEdu,
                                        contentDescription = "Diario de mejoras del usuario"
                                    )
                                    PremiumButton(
                                        text = "Ver Reportes Diarios",
                                        onClick = { navController.navigate("user_daily_log_history/${user.uid}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        icon = Icons.Filled.Summarize,
                                        contentDescription = "Reportes diarios del usuario"
                                    )

                                    // Divider dorado antes de estadísticas
                                    Divider(Modifier.padding(vertical = 14.dp), color = PremiumGold.copy(alpha = 0.15f))

                                    // Título estadísticas premium
                                    Text(
                                        text = "Estadísticas de Estados de Ánimo",
                                        color = PremiumGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    )

                                    // Gráfico
                                    EmotionBarChart(userId = user.uid)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // --- CARD PREMIUM PARA SWITCH SEGUIMIENTO EMOCIONES ---
                            Spacer(modifier = Modifier.height(18.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.35f)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF181818).copy(alpha = 0.93f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = null,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "Seguimiento exhaustivo de emociones",
                                        color = PremiumGold,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(start = 10.dp).weight(1f)
                                    )
                                    Switch(
                                        checked = isTrackingEmotions,
                                        onCheckedChange = { checked ->
                                            isTrackingEmotions = checked
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.uid)
                                                .update("trackEmotions", checked)
                                                .addOnSuccessListener {
                                                    scope.launch { snackbarHostState.showSnackbar(if (checked) "Seguimiento activado" else "Seguimiento desactivado") }
                                                }
                                                .addOnFailureListener {
                                                    scope.launch { snackbarHostState.showSnackbar("Error al actualizar seguimiento") }
                                                }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(22.dp))

                            // Botón eliminar usuario bien separado debajo de la gráfica
                            Button(
                                onClick = { showDeleteConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text("Eliminar usuario", color = Color.White)
                            }

                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("¿Eliminar usuario?") },
                                    text = { Text("Esta acción eliminará al usuario y todos sus datos. ¿Estás seguro?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            userId?.let {
                                                eliminarUsuarioCompleto(it)
                                                navController.popBackStack()
                                            }
                                            showDeleteConfirm = false
                                        }) {
                                            Text("Eliminar", color = Color.Red)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirm = false }) {
                                            Text("Cancelar")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is UserProfileDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PremiumGold
                    )
                }
                is UserProfileDetailUiState.Error -> {
                    val errorMsg = currentState.message
                    Text(
                        text = "Error: $errorMsg",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UserProfileDetailUiState.Idle -> {
                    Text(
                        text = if (userId.isNullOrBlank()) "No se ha seleccionado un usuario." else "Cargando datos del usuario...",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DatosPersonalesRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = OriginalDetailLabelColor,
            fontSize = 15.sp,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            color = OriginalDetailValueColor,
            fontSize = 15.sp
        )
    }
    Divider(
        color = OriginalDetailLabelColor.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}

@Composable
fun RegisterProgressButtonOriginal(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PremiumGold,
            contentColor = OriginalButtonTextColor
        ),
        modifier = modifier
            .height(60.dp)
            .shadow(18.dp, RoundedCornerShape(32.dp))
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Registrar Progreso",
            tint = OriginalButtonTextColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Registrar Progreso",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun EmotionBarChart(
    userId: String,
    viewModel: DailyLogViewModel = viewModel()
) {
    val emotionEntries by viewModel.getUserEmotionEntries(userId).collectAsState(initial = emptyList())
    LaunchedEffect(emotionEntries) {
        println("DEBUG Emotions: $emotionEntries")
    }
    val emotionOrder = listOf("Alegría", "Calma", "Tristeza", "Ira")
    val emotionColorMap = mapOf(
        "Alegría" to AndroidColor.parseColor("#1ABC9C"), // Verde
        "Calma" to AndroidColor.parseColor("#FFD700"), // Amarillo
        "Tristeza" to AndroidColor.parseColor("#2980B9"), // Azul
        "Ira" to AndroidColor.parseColor("#E74C3C") // Rojo
    )
    val grouped = emotionEntries.groupingBy { it.mood }.eachCount()
    val labels = emotionOrder
    val values = emotionOrder.mapIndexed { index, emotion ->
        BarEntry(index.toFloat(), grouped[emotion]?.toFloat() ?: 0f)
    }

    if (labels.isNotEmpty()) {
        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    description.isEnabled = false
                    setDrawGridBackground(false)
                    setDrawBarShadow(false)
                    setTouchEnabled(false)
                    legend.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    xAxis.labelCount = labels.size
                    xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    // Cambiar color de texto del eje X para fondo oscuro
                    xAxis.textColor = AndroidColor.WHITE
                    // Opcional: aumentar tamaño de fuente de labels
                    xAxis.textSize = 12f
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    setScaleEnabled(false)
                    setPinchZoom(false)
                    // Mejorar visibilidad del eje Y izquierdo
                    axisLeft.isEnabled = true
                    axisLeft.textColor = AndroidColor.WHITE
                    axisLeft.textSize = 12f
                    // Mostrar solo números enteros en el eje Y
                    axisLeft.granularity = 1f
                    axisLeft.setGranularityEnabled(true)
                    axisLeft.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }
                }
            },
            update = { chart ->
                val dataSet = BarDataSet(values, "Emociones")
                dataSet.colors = emotionOrder.map { emotionColorMap[it] ?: AndroidColor.GRAY }
                val data = BarData(dataSet)
                data.barWidth = 0.8f
                chart.data = data
                chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 8.dp, vertical = 16.dp)
        )
    }
}
fun eliminarUsuarioCompleto(userId: String) {
    val db = FirebaseFirestore.getInstance()

    println("🔥 Empezando eliminación del usuario: $userId")

    db.collection("users").document(userId).collection("daily_log")
        .get().addOnSuccessListener { dailyLogs ->
            println("✅ daily_log obtenidos: ${dailyLogs.size()}")
            for (doc in dailyLogs) {
                doc.reference.delete()
            }

            db.collection("users").document(userId).collection("emotion_entries")
                .get().addOnSuccessListener { emotions ->
                    println("✅ emotion_entries obtenidos: ${emotions.size()}")
                    for (doc in emotions) {
                        doc.reference.delete()
                    }

                    db.collection("injury_reports")
                        .whereEqualTo("userId", userId)
                        .get().addOnSuccessListener { injuries ->
                            println("✅ injury_reports obtenidos: ${injuries.size()}")
                            for (doc in injuries) {
                                doc.reference.delete()
                            }

                            println("🧨 Eliminando documento del usuario")
                            db.collection("users").document(userId).delete()
                                .addOnSuccessListener {
                                    println("🎉 Usuario eliminado con éxito")
                                }
                                .addOnFailureListener {
                                    println("❌ Falló al eliminar usuario: ${it.message}")
                                }
                        }
                }
        }
        .addOnFailureListener { e ->
            println("❌ Fallo al obtener daily_log: ${e.message}")
        }
}