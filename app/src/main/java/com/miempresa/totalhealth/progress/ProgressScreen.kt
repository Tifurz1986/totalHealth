package com.miempresa.totalhealth.progress // O 'package progress;' si esa es tu estructura

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke // <--- IMPORTACIÓN AÑADIDA
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
// import androidx.compose.material.icons.filled.CheckCircleOutline // No se usa en el último código
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.platform.LocalFocusManager // No se usa explícitamente en el último código de esta pantalla
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Asegúrate que ProgressViewModel y ProgressUiState estén en el mismo paquete
// o importados correctamente.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    progressViewModel: ProgressViewModel = viewModel()
) {
    val context = LocalContext.current

    val physicalLevel by progressViewModel.physicalLevel
    val mentalLevel by progressViewModel.mentalLevel
    val nutritionLevel by progressViewModel.nutritionLevel
    val notes by progressViewModel.notes
    val entryDateMillis by progressViewModel.entryDateMillis
    val addProgressUiState by progressViewModel.addProgressUiState.collectAsState()

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B) // Teal 700
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)
    val colorTextoSecundarioClaro = Color.White.copy(alpha = 0.7f)
    val colorIconoClaro = Color.White.copy(alpha = 0.8f)

    val calendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            progressViewModel.onEntryDateSelected(selectedCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    LaunchedEffect(key1 = addProgressUiState) {
        when (val state = addProgressUiState) {
            is ProgressUiState.Success -> {
                Toast.makeText(context, "Progreso guardado exitosamente", Toast.LENGTH_SHORT).show()
                progressViewModel.resetAddProgressUiState()
                navController.popBackStack()
            }
            is ProgressUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                progressViewModel.resetAddProgressUiState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registrar Mi Progreso", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Evalúa tu día",
                style = MaterialTheme.typography.titleLarge,
                color = colorVerdePrincipal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Selecciona la fecha y califica tu bienestar físico, mental y nutricional.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorTextoSecundarioClaro,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorTextoClaro),
                border = BorderStroke(1.dp, colorVerdePrincipal) // Ahora BorderStroke debería resolverse
            ) {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = colorIconoClaro)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Fecha de la entrada: ${dateFormatter.format(Date(entryDateMillis))}")
            }
            Spacer(modifier = Modifier.height(24.dp))

            ProgressSliderItem(
                label = "Bienestar Físico",
                value = physicalLevel,
                onValueChange = { progressViewModel.onPhysicalLevelChange(it) },
                icon = Icons.Filled.FitnessCenter,
                color = colorVerdePrincipal
            )
            ProgressSliderItem(
                label = "Bienestar Mental",
                value = mentalLevel,
                onValueChange = { progressViewModel.onMentalLevelChange(it) },
                icon = Icons.Filled.Psychology,
                color = colorVerdePrincipal
            )
            ProgressSliderItem(
                label = "Nutrición",
                value = nutritionLevel,
                onValueChange = { progressViewModel.onNutritionLevelChange(it) },
                icon = Icons.Filled.LocalDining,
                color = colorVerdePrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { progressViewModel.onNotesChange(it) },
                label = { Text("Notas Adicionales (Opcional)", color = colorTextoSecundarioClaro) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                leadingIcon = { Icon(Icons.Filled.Notes, "Notas", tint = colorIconoClaro) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro,
                    cursorColor = colorVerdePrincipal, focusedBorderColor = colorVerdePrincipal,
                    unfocusedBorderColor = colorTextoSecundarioClaro.copy(alpha = 0.5f),
                    focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = colorTextoSecundarioClaro,
                    focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = colorIconoClaro,
                    disabledTextColor = Color.Gray
                ),
                enabled = addProgressUiState !is ProgressUiState.Loading
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { progressViewModel.addProgressEntry() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .height(50.dp),
                enabled = addProgressUiState !is ProgressUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorVerdePrincipal,
                    contentColor = Color.White
                )
            ) {
                if (addProgressUiState is ProgressUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Filled.Save, "Guardar Progreso", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Guardar Progreso", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ProgressSliderItem(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$label: ${value.roundToInt()}/10", color = Color.White.copy(alpha = 0.9f))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.3f),
                activeTickColor = color.copy(alpha = 0.5f),
                inactiveTickColor = Color.Transparent
            )
        )
    }
}
