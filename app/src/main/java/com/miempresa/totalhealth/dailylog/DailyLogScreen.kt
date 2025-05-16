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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colores personalizados para los OutlinedTextField en un tema oscuro
@Composable
fun dailyLogTextFieldColors(
    textColor: Color = Color.White.copy(alpha = 0.9f),
    disabledTextColor: Color = Color.Gray,
    cursorColor: Color = Color(0xFF00897B), // colorVerdePrincipal
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
    // Puedes añadir trailingIconColor si lo usas
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

    var selectedDate by remember { mutableStateOf(dailyLogViewModel.getTodayDate()) }
    var foodEntries by remember { mutableStateOf(listOf(FoodEntry())) }
    var mood by remember { mutableStateOf("") }
    var moodIntensity by remember { mutableStateOf<Int?>(null) }
    var moodIntensityString by remember { mutableStateOf("") }
    var journalEntry by remember { mutableStateOf("") }
    var sleepTimeToBedString by remember { mutableStateOf("") }
    var sleepTimeWokeUpString by remember { mutableStateOf("") }
    var sleepQuality by remember { mutableStateOf("") }
    var activityEntries by remember { mutableStateOf(listOf(ActivityEntry())) }
    var waterIntakeString by remember { mutableStateOf("") }
    var generalNotes by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // Formato de fecha mejorado

    LaunchedEffect(selectedDate) {
        dailyLogViewModel.loadDailyLogForDate(selectedDate)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is DailyLogUiState.Success -> {
                isLoading = false
                val log = state.log
                if (log != null) {
                    foodEntries = if (log.foodEntries.isNotEmpty()) log.foodEntries else listOf(FoodEntry())
                    mood = log.emotionEntry?.mood ?: ""
                    moodIntensityString = log.emotionEntry?.moodIntensity?.toString() ?: ""
                    moodIntensity = log.emotionEntry?.moodIntensity
                    journalEntry = log.emotionEntry?.journalEntry ?: ""
                    sleepTimeToBedString = log.sleepEntry?.timeToBed?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: ""
                    sleepTimeWokeUpString = log.sleepEntry?.timeWokeUp?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: ""
                    sleepQuality = log.sleepEntry?.sleepQuality ?: ""
                    activityEntries = if (log.activityEntries.isNotEmpty()) log.activityEntries else listOf(ActivityEntry())
                    waterIntakeString = log.waterIntakeLiters?.toString() ?: ""
                    generalNotes = log.notes
                } else {
                    foodEntries = listOf(FoodEntry())
                    mood = ""
                    moodIntensityString = ""
                    moodIntensity = null
                    journalEntry = ""
                    sleepTimeToBedString = ""
                    sleepTimeWokeUpString = ""
                    sleepQuality = ""
                    activityEntries = listOf(ActivityEntry())
                    waterIntakeString = ""
                    generalNotes = ""
                }
                dailyLogViewModel.resetUiState()
            }
            is DailyLogUiState.Loading -> isLoading = true
            is DailyLogUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                dailyLogViewModel.resetUiState()
            }
            DailyLogUiState.Idle -> isLoading = false
        }
    }

    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val newDateCalendar = Calendar.getInstance()
            newDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth, 0, 0, 0)
            newDateCalendar.set(Calendar.MILLISECOND, 0)
            selectedDate = newDateCalendar.time
        }, year, month, day
    )

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val textFieldColors = dailyLogTextFieldColors() // Usar los colores definidos

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
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            )

            SectionTitle("Alimentación")
            foodEntries.forEachIndexed { index, foodEntry ->
                FoodEntryItem(
                    foodEntry = foodEntry,
                    onDescriptionChange = { newDesc ->
                        foodEntries = foodEntries.toMutableList().also { it[index] = foodEntry.copy(description = newDesc) }
                    },
                    onMealTypeChange = { newType ->
                        foodEntries = foodEntries.toMutableList().also { it[index] = foodEntry.copy(mealType = newType) }
                    },
                    onCaloriesChange = { newCals ->
                        foodEntries = foodEntries.toMutableList().also { it[index] = foodEntry.copy(calories = newCals.toIntOrNull()) }
                    },
                    onRemove = {
                        if (foodEntries.size > 1) foodEntries = foodEntries.filterIndexed { i, _ -> i != index }
                        else foodEntries = listOf(FoodEntry())
                    },
                    showRemoveButton = foodEntries.size > 1,
                    textFieldColors = textFieldColors // Aplicar colores
                )
            }
            Button(onClick = { foodEntries = foodEntries + FoodEntry() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Comida")
                Spacer(Modifier.width(4.dp))
                Text("Añadir Comida")
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Estado de Ánimo y Diario")
            OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("¿Cómo te sientes?") }, leadingIcon = { Icon(Icons.Filled.SentimentSatisfied, "Mood")}, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
            OutlinedTextField(value = moodIntensityString, onValueChange = { moodIntensityString = it.filter {c -> c.isDigit() }.take(1); moodIntensity = it.toIntOrNull()?.coerceIn(1,5) }, label = { Text("Intensidad (1-5)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(top=8.dp), colors = textFieldColors)
            OutlinedTextField(value = journalEntry, onValueChange = { journalEntry = it }, label = { Text("Notas del diario / Reflexión") }, leadingIcon = { Icon(Icons.Filled.Book, "Journal")}, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).padding(top=8.dp), colors = textFieldColors)
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Sueño")
            OutlinedTextField(value = sleepTimeToBedString, onValueChange = { sleepTimeToBedString = it }, label = { Text("Hora de acostarse (HH:mm)") }, leadingIcon = { Icon(Icons.Filled.Bedtime, "Bedtime")}, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
            OutlinedTextField(value = sleepTimeWokeUpString, onValueChange = { sleepTimeWokeUpString = it }, label = { Text("Hora de levantarse (HH:mm)") }, leadingIcon = { Icon(Icons.Filled.WbSunny, "Wake up")}, modifier = Modifier.fillMaxWidth().padding(top=8.dp), colors = textFieldColors)
            OutlinedTextField(value = sleepQuality, onValueChange = { sleepQuality = it }, label = { Text("Calidad del sueño (Buena, Regular, Mala)") }, leadingIcon = { Icon(Icons.Filled.Star, "Quality")}, modifier = Modifier.fillMaxWidth().padding(top=8.dp), colors = textFieldColors)
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Actividad Física")
            activityEntries.forEachIndexed { index, activityEntry ->
                ActivityEntryItem(
                    activityEntry = activityEntry,
                    onTypeChange = { newType ->
                        activityEntries = activityEntries.toMutableList().also { it[index] = activityEntry.copy(type = newType) }
                    },
                    onDurationChange = { newDuration ->
                        activityEntries = activityEntries.toMutableList().also { it[index] = activityEntry.copy(durationMinutes = newDuration.toIntOrNull()) }
                    },
                    onIntensityChange = { newIntensity ->
                        activityEntries = activityEntries.toMutableList().also { it[index] = activityEntry.copy(intensity = newIntensity) }
                    },
                    onRemove = {
                        if (activityEntries.size > 1) activityEntries = activityEntries.filterIndexed { i, _ -> i != index }
                        else activityEntries = listOf(ActivityEntry())
                    },
                    showRemoveButton = activityEntries.size > 1,
                    textFieldColors = textFieldColors // Aplicar colores
                )
            }
            Button(onClick = { activityEntries = activityEntries + ActivityEntry() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Actividad")
                Spacer(Modifier.width(4.dp))
                Text("Añadir Actividad")
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Hidratación")
            OutlinedTextField(value = waterIntakeString, onValueChange = { waterIntakeString = it.filter { c -> c.isDigit() || c == '.' }.take(4) }, label = { Text("Agua consumida (Litros)") }, leadingIcon = { Icon(Icons.Filled.LocalDrink, "Water")}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Notas Adicionales del Día")
            OutlinedTextField(value = generalNotes, onValueChange = { generalNotes = it }, label = { Text("Cualquier otra observación...") }, leadingIcon = { Icon(Icons.Filled.Notes, "Notes")}, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), colors = textFieldColors)
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    var dateToBed: Date? = null
                    var dateWokeUp: Date? = null
                    try {
                        if(sleepTimeToBedString.isNotBlank()) dateToBed = timeFormat.parse(sleepTimeToBedString)
                        if(sleepTimeWokeUpString.isNotBlank()) dateWokeUp = timeFormat.parse(sleepTimeWokeUpString)
                    } catch (e: Exception) {
                        Log.w("DailyLogScreen", "Error parsing sleep times: ${e.message}")
                    }

                    val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""
                    if (currentUserId.isBlank()) {
                        Toast.makeText(context, "Error: Usuario no identificado.", Toast.LENGTH_LONG).show()
                        isLoading = false
                        return@Button
                    }

                    val currentLog = DailyLog(
                        userId = currentUserId,
                        date = selectedDate,
                        foodEntries = foodEntries.filter { it.description.isNotBlank() || it.mealType.isNotBlank() },
                        emotionEntry = if (mood.isNotBlank() || journalEntry.isNotBlank()) EmotionEntry(mood, moodIntensity, "", journalEntry) else null,
                        sleepEntry = if (dateToBed != null || dateWokeUp != null || sleepQuality.isNotBlank()) SleepEntry(dateToBed, dateWokeUp, sleepQuality) else null,
                        activityEntries = activityEntries.filter { it.type.isNotBlank() },
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
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
    textFieldColors: TextFieldColors // Añadir parámetro para los colores
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        OutlinedTextField(value = foodEntry.mealType, onValueChange = onMealTypeChange, label = { Text("Tipo de Comida (Ej: Desayuno)") }, leadingIcon={Icon(Icons.Filled.RestaurantMenu, "Meal Type")}, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
        OutlinedTextField(value = foodEntry.description, onValueChange = onDescriptionChange, label = { Text("Descripción de la comida") }, leadingIcon={Icon(Icons.Filled.Fastfood, "Description")}, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
        OutlinedTextField(value = foodEntry.calories?.toString() ?: "", onValueChange = onCaloriesChange, label = { Text("Calorías (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), leadingIcon={Icon(Icons.Filled.LocalFireDepartment, "Calories")}, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
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
    textFieldColors: TextFieldColors // Añadir parámetro para los colores
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        OutlinedTextField(value = activityEntry.type, onValueChange = onTypeChange, label = { Text("Tipo de Actividad (Ej: Correr)") }, leadingIcon={Icon(Icons.Filled.FitnessCenter, "Activity Type")}, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
        OutlinedTextField(value = activityEntry.durationMinutes?.toString() ?: "", onValueChange = onDurationChange, label = { Text("Duración (minutos)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), leadingIcon={Icon(Icons.Filled.Timer, "Duration")}, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
        OutlinedTextField(value = activityEntry.intensity, onValueChange = onIntensityChange, label = { Text("Intensidad (Ej: Ligera, Moderada)") }, leadingIcon={Icon(Icons.Filled.Speed, "Intensity")}, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), colors = textFieldColors)
        if (showRemoveButton) {
            TextButton(onClick = onRemove, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Quitar Actividad", tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(4.dp))
                Text("Quitar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
