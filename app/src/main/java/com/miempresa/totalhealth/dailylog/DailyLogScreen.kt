package com.miempresa.totalhealth.dailylog

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.common.InteractiveStarRatingInput
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colores personalizados para los OutlinedTextField en un tema oscuro
@Composable
fun dailyLogTextFieldColors(
    textColor: Color = Color.White.copy(alpha = 0.9f),
    disabledTextColor: Color = Color.Gray,
    cursorColor: Color = Color(0xFF00897B),
    focusedBorderColor: Color = Color(0xFF00897B),
    unfocusedBorderColor: Color = Color.White.copy(alpha = 0.5f),
    disabledBorderColor: Color = Color.DarkGray,
    focusedLeadingIconColor: Color = Color(0xFF00897B),
    unfocusedLeadingIconColor: Color = Color.White.copy(alpha = 0.7f),
    disabledLeadingIconColor: Color = Color.Gray,
    focusedLabelColor: Color = Color(0xFF00897B),
    unfocusedLabelColor: Color = Color.White.copy(alpha = 0.7f),
    disabledLabelColor: Color = Color.Gray,
    placeholderColor: Color = Color.White.copy(alpha = 0.5f),
    disabledPlaceholderColor: Color = Color.DarkGray
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    disabledTextColor = disabledTextColor,
    cursorColor = cursorColor,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    disabledBorderColor = disabledBorderColor,
    focusedLeadingIconColor = focusedLeadingIconColor,
    unfocusedLeadingIconColor = unfocusedLeadingIconColor,
    disabledLeadingIconColor = disabledLeadingIconColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    disabledLabelColor = disabledLabelColor,
    focusedPlaceholderColor = placeholderColor,
    unfocusedPlaceholderColor = placeholderColor,
    disabledPlaceholderColor = disabledPlaceholderColor
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    navController: NavController,
    dailyLogViewModel: DailyLogViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val uiState by dailyLogViewModel.dailyLogUiState.collectAsState()

    // Estados para los campos del formulario
    var selectedDate by remember { mutableStateOf(dailyLogViewModel.getTodayDate()) }
    var foodEntries by remember { mutableStateOf(listOf(FoodEntry())) }
    var mood by remember { mutableStateOf("") }
    var sleepQualityStars by remember { mutableStateOf(0) } // 0: no seleccionado, 1-5: estrellas
    // Estado para estrellas de intensidad de actividad física
    var activityIntensityStars by remember { mutableStateOf(0) } // 0: no seleccionado, 1-5: estrellas
    var journalEntry by remember { mutableStateOf("") }
    var sleepTimeToBedString by remember { mutableStateOf("") }
    var sleepTimeWokeUpString by remember { mutableStateOf("") }

    // Estados para mostrar el TimePickerDialog en la sección de Sueño
    var showTimePickerToBed by remember { mutableStateOf(false) }
    var showTimePickerWokeUp by remember { mutableStateOf(false) }
    // Guardar la hora seleccionada (Pair<Int, Int>? para hora y minuto)
    var sleepToBedHour by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var sleepWokeUpHour by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Función para formatear hora:minuto
    fun formatHourMinute(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)
    var sleepQuality by remember { mutableStateOf("") }
    var activityEntries by remember { mutableStateOf(listOf(ActivityEntry())) }
    var waterIntakeString by remember { mutableStateOf("") }
    var generalNotes by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Cargar datos cuando la fecha seleccionada cambia
    LaunchedEffect(selectedDate) {
        dailyLogViewModel.loadDailyLogForDate(selectedDate)
    }

    // Observar el estado de la UI del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is DailyLogUiState.Success -> {
                isLoading = false
                val log = state.log
                if (log != null) {
                    // Actualizar los estados locales con los datos cargados
                    foodEntries = if (log.foodEntries.isNotEmpty()) log.foodEntries else listOf(FoodEntry())
                    mood = log.emotionEntry?.mood ?: ""
                    journalEntry = log.emotionEntry?.journalEntry ?: ""
                    sleepTimeToBedString = log.sleepEntry?.timeToBed?.let { timeFormat.format(it) } ?: ""
                    sleepTimeWokeUpString = log.sleepEntry?.timeWokeUp?.let { timeFormat.format(it) } ?: ""
                    sleepQuality = log.sleepEntry?.sleepQuality ?: ""
                    activityEntries = if (log.activityEntries.isNotEmpty()) log.activityEntries else listOf(ActivityEntry())
                    waterIntakeString = log.waterIntakeLiters?.toString() ?: ""
                    generalNotes = log.notes
                } else {
                    // Resetear campos si no hay log para la fecha (nuevo registro)
                    foodEntries = listOf(FoodEntry())
                    mood = ""
                    journalEntry = ""
                    sleepTimeToBedString = ""
                    sleepTimeWokeUpString = ""
                    sleepQuality = ""
                    activityEntries = listOf(ActivityEntry())
                    waterIntakeString = ""
                    generalNotes = ""
                }
            }
            is DailyLogUiState.Loading -> isLoading = true
            is DailyLogUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                dailyLogViewModel.resetUiState() // Resetear después de mostrar error
            }
            DailyLogUiState.Idle -> isLoading = false // Estado inicial o después de reset
        }
    }


    val calendar = Calendar.getInstance()
    calendar.time = selectedDate // Asegurarse que el DatePicker inicie con la fecha seleccionada
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val newDateCalendar = Calendar.getInstance()
            newDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth, 0, 0, 0)
            newDateCalendar.set(Calendar.MILLISECOND, 0) // Normalizar la hora
            selectedDate = newDateCalendar.time
        }, year, month, day
    )

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val textFieldColors = dailyLogTextFieldColors()


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registro Diario", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Seleccionar Fecha", tint = Color.White)
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
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Registrando para: ${displayDateFormat.format(selectedDate)}",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Sección Alimentación
            SectionTitle("Alimentación")
            foodEntries.forEachIndexed { index, foodEntry ->
                FoodEntryItem(
                    foodEntry = foodEntry,
                    onDescriptionChange = { newDesc ->
                        foodEntries = foodEntries.toMutableList().apply { this[index] = foodEntry.copy(description = newDesc) }
                    },
                    onMealTypeChange = { newType ->
                        foodEntries = foodEntries.toMutableList().apply { this[index] = foodEntry.copy(mealType = newType) }
                    },
                    onCaloriesChange = { newCals ->
                        foodEntries = foodEntries.toMutableList().apply { this[index] = foodEntry.copy(calories = newCals.toIntOrNull()) }
                    },
                    onRemove = {
                        foodEntries = if (foodEntries.size > 1) {
                            foodEntries.toMutableList().apply { removeAt(index) }
                        } else {
                            listOf(FoodEntry())
                        }
                    },
                    showRemoveButton = foodEntries.size > 1 || (foodEntries.size == 1 && (foodEntry.description.isNotBlank() || foodEntry.mealType.isNotBlank() || foodEntry.calories != null)),
                    textFieldColors = textFieldColors
                )
            }
            Button(
                onClick = { foodEntries = foodEntries + FoodEntry() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Comida", tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Añadir Comida", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Sección Estado de Ánimo y Diario
            SectionTitle("Estado de Ánimo y Diario")
            Text(
                text = "¿Cómo te sientes hoy? (1 = Muy mal, 5 = Excelente)",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Estado de ánimo $i",
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { mood = i.toString() },
                        tint = if (i <= (mood.toIntOrNull() ?: 0)) Color(0xFFFFD600) else Color.LightGray
                    )
                }
            }
            OutlinedTextField(
                value = journalEntry,
                onValueChange = { journalEntry = it },
                label = { Text("Notas del diario / Reflexión") },
                leadingIcon = { Icon(Icons.Filled.Book, "Journal") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .padding(top = 16.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
            )
            Spacer(modifier = Modifier.height(16.dp))


            // Sección Sueño
            SectionTitle("Sueño")

            // Hora de acostarse (TimePicker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePickerToBed = true }
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = "Bedtime", tint = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Hora de acostarse: ${sleepTimeToBedString.takeIf { it.isNotEmpty() } ?: "--:--"}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (showTimePickerToBed) {
                val context = LocalContext.current
                AndroidView(factory = {
                    android.widget.FrameLayout(it).apply {
                        post {
                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    sleepTimeToBedString = formatHourMinute(hour, minute)
                                    sleepToBedHour = hour to minute
                                    showTimePickerToBed = false
                                },
                                sleepToBedHour?.first ?: 22,
                                sleepToBedHour?.second ?: 0,
                                true
                            ).apply {
                                setOnDismissListener { showTimePickerToBed = false }
                            }.show()
                        }
                    }
                })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hora de levantarse (TimePicker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePickerWokeUp = true }
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.WbSunny, contentDescription = "Wake up", tint = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Hora de levantarse: ${sleepTimeWokeUpString.takeIf { it.isNotEmpty() } ?: "--:--"}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (showTimePickerWokeUp) {
                val context = LocalContext.current
                AndroidView(factory = {
                    android.widget.FrameLayout(it).apply {
                        post {
                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    sleepTimeWokeUpString = formatHourMinute(hour, minute)
                                    sleepWokeUpHour = hour to minute
                                    showTimePickerWokeUp = false
                                },
                                sleepWokeUpHour?.first ?: 7,
                                sleepWokeUpHour?.second ?: 0,
                                true
                            ).apply {
                                setOnDismissListener { showTimePickerWokeUp = false }
                            }.show()
                        }
                    }
                })
            }

            // Sustituir campo de texto de calidad de sueño por estrellas
            Text(
                text = "Calidad del sueño:",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Estrella $i",
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { sleepQualityStars = i },
                        tint = if (i <= sleepQualityStars) Color(0xFFFFD600) else Color.LightGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Sección Actividad Física
            SectionTitle("Actividad Física")
            activityEntries.forEachIndexed { index, activityEntry ->
                ActivityEntryItem(
                    activityEntry = activityEntry,
                    onTypeChange = { newType ->
                        activityEntries = activityEntries.toMutableList().apply { this[index] = activityEntry.copy(type = newType) }
                    },
                    onDurationChange = { newDuration ->
                        activityEntries = activityEntries.toMutableList().apply { this[index] = activityEntry.copy(durationMinutes = newDuration.toIntOrNull()) }
                    },
                    onIntensityChange = { newIntensity ->
                        activityEntries = activityEntries.toMutableList().apply { this[index] = activityEntry.copy(intensity = newIntensity) }
                    },
                    onRemove = {
                        activityEntries = if (activityEntries.size > 1) {
                            activityEntries.toMutableList().apply { removeAt(index) }
                        } else {
                            listOf(ActivityEntry())
                        }
                    },
                    showRemoveButton = activityEntries.size > 1 || (activityEntries.size == 1 && (activityEntry.type.isNotBlank() || activityEntry.durationMinutes != null || activityEntry.intensity.isNotBlank())),
                    textFieldColors = textFieldColors
                )
                // Intensidad de actividad física con estrellas (después de cada bloque)
                Text(
                    text = "Intensidad de actividad física:",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Estrella intensidad $i",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { activityIntensityStars = i },
                            tint = if (i <= activityIntensityStars) Color(0xFFFFD600) else Color.LightGray
                        )
                    }
                }
            }
            Button(
                onClick = { activityEntries = activityEntries + ActivityEntry() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Actividad", tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Añadir Actividad", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Sección Hidratación
            SectionTitle("Hidratación")
            OutlinedTextField(
                value = waterIntakeString,
                onValueChange = { waterIntakeString = it.filter { char -> char.isDigit() || char == '.' }.take(4) },
                label = { Text("Agua consumida (Litros)") },
                leadingIcon = { Icon(Icons.Filled.LocalDrink, "Water") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))


            // Sección Notas Adicionales
            SectionTitle("Notas Adicionales del Día")
            OutlinedTextField(
                value = generalNotes,
                onValueChange = { generalNotes = it },
                label = { Text("Cualquier otra observación...") },
                leadingIcon = { Icon(Icons.Filled.Notes, "Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
            )
            Spacer(modifier = Modifier.height(32.dp))


            // Botón Guardar
            Button(
                onClick = {
                    isLoading = true
                    val currentUserId = authViewModel.getCurrentUser()?.uid
                    if (currentUserId.isNullOrBlank()) {
                        Toast.makeText(context, "Error: Usuario no identificado.", Toast.LENGTH_LONG).show()
                        isLoading = false
                        return@Button
                    }

                    var dateToBed: Date? = null
                    var dateWokeUp: Date? = null
                    try {
                        if (sleepTimeToBedString.isNotBlank()) dateToBed = timeFormat.parse(sleepTimeToBedString)
                        if (sleepTimeWokeUpString.isNotBlank()) dateWokeUp = timeFormat.parse(sleepTimeWokeUpString)
                    } catch (e: Exception) {
                        Log.w("DailyLogScreen", "Error parsing sleep times: ${e.message}")
                        Toast.makeText(context, "Formato de hora de sueño inválido. Use HH:mm", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@Button
                    }

                    val emotionEntryToSave = if (mood.isNotBlank() || journalEntry.isNotBlank()) {
                        EmotionEntry(
                            mood = mood,
                            moodIntensity = null, // Ya no se usa el nivel de estrellas
                            journalEntry = journalEntry
                        )
                    } else null

                    // Guardar calidad del sueño como número de estrellas (convertir a texto si es necesario)
                    val sleepEntryToSave = if (dateToBed != null || dateWokeUp != null || sleepQualityStars > 0) {
                        SleepEntry(
                            timeToBed = dateToBed,
                            timeWokeUp = dateWokeUp,
                            sleepQuality = if (sleepQualityStars > 0) sleepQualityStars.toString() else ""
                        )
                    } else null

                    val currentLog = DailyLog(
                        userId = currentUserId,
                        date = selectedDate,
                        foodEntries = foodEntries.filter { it.description.isNotBlank() || it.mealType.isNotBlank() || it.calories != null },
                        emotionEntry = emotionEntryToSave,
                        sleepEntry = sleepEntryToSave,
                        activityEntries = activityEntries.filter { it.type.isNotBlank() || it.durationMinutes != null || it.intensity.isNotBlank() },
                        waterIntakeLiters = waterIntakeString.toDoubleOrNull(),
                        notes = generalNotes
                    )

                    dailyLogViewModel.saveDailyLog(currentLog,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Registro guardado con éxito", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMsg ->
                            isLoading = false
                            Toast.makeText(context, "Error al guardar: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Guardar Registro", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = Color.White.copy(alpha = 0.9f),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
    Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun FoodEntryItem(
    foodEntry: FoodEntry,
    onDescriptionChange: (String) -> Unit,
    onMealTypeChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onRemove: () -> Unit,
    showRemoveButton: Boolean,
    textFieldColors: TextFieldColors
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        OutlinedTextField(value = foodEntry.mealType, onValueChange = onMealTypeChange, label = { Text("Tipo de Comida (Ej: Desayuno)") }, leadingIcon = { Icon(Icons.Filled.RestaurantMenu, "Meal Type") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
        OutlinedTextField(value = foodEntry.description, onValueChange = onDescriptionChange, label = { Text("Descripción de la comida") }, leadingIcon = { Icon(Icons.Filled.Fastfood, "Description") }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
        OutlinedTextField(value = foodEntry.calories?.toString() ?: "", onValueChange = onCaloriesChange, label = { Text("Calorías (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), leadingIcon = { Icon(Icons.Filled.LocalFireDepartment, "Calories") }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
        if (showRemoveButton) {
            TextButton(onClick = onRemove, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Quitar Comida", tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(4.dp))
                Text("Quitar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ActivityEntryItem(
    activityEntry: ActivityEntry,
    onTypeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onIntensityChange: (String) -> Unit,
    onRemove: () -> Unit,
    showRemoveButton: Boolean,
    textFieldColors: TextFieldColors
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        OutlinedTextField(value = activityEntry.type, onValueChange = onTypeChange, label = { Text("Tipo de Actividad (Ej: Correr)") }, leadingIcon = { Icon(Icons.Filled.FitnessCenter, "Activity Type") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
        OutlinedTextField(value = activityEntry.durationMinutes?.toString() ?: "", onValueChange = onDurationChange, label = { Text("Duración (minutos)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), leadingIcon = { Icon(Icons.Filled.Timer, "Duration") }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
        // Eliminado el campo de intensidad por texto
        if (showRemoveButton) {
            TextButton(onClick = onRemove, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Quitar Actividad", tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(4.dp))
                Text("Quitar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}