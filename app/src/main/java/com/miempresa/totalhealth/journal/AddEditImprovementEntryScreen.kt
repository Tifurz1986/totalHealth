package com.miempresa.totalhealth.journal // Asegúrate que el paquete sea este

import android.app.DatePickerDialog
import android.util.Log // Importación añadida para Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miempresa.totalhealth.dailylog.dailyLogTextFieldColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditImprovementEntryScreen( // Asegúrate que el nombre de la función sea este
    navController: NavController,
    journalViewModel: ImprovementJournalViewModel = viewModel(),
    entryId: String? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf(Calendar.getInstance().time) }

    val entryOperationState by journalViewModel.entryOperationUiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            Log.d("AddEditEntryScreen", "Modo edición para ID: $entryId (funcionalidad de carga no implementada)")
            // Aquí llamarías a journalViewModel.loadEntryById(entryId)
            // y luego actualizarías los 'remember' states (title, content, category, entryDate)
        }
    }

    LaunchedEffect(entryOperationState) {
        when (val state = entryOperationState) {
            is EntryOperationUiState.Loading -> isLoading = true
            is EntryOperationUiState.Success -> {
                isLoading = false
                Toast.makeText(context, "Entrada guardada con éxito", Toast.LENGTH_SHORT).show()
                journalViewModel.resetEntryOperationState()
                navController.popBackStack()
            }
            is EntryOperationUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                journalViewModel.resetEntryOperationState()
            }
            EntryOperationUiState.Idle -> isLoading = false
        }
    }

    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // yyyy para año completo
    val calendar = Calendar.getInstance(); calendar.time = entryDate
    val year = calendar.get(Calendar.YEAR); val month = calendar.get(Calendar.MONTH); val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val newDateCalendar = Calendar.getInstance()
            newDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
            entryDate = newDateCalendar.time
        }, year, month, day
    )
    // datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val textFieldColors = dailyLogTextFieldColors()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (entryId == null) "Nueva Entrada al Diario" else "Editar Entrada", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, colorVerdePrincipal)
            ) {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Fecha de la Entrada: ${displayDateFormat.format(entryDate)}")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título (Opcional)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                leadingIcon = { Icon(Icons.Filled.Title, "Título") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                singleLine = true,
                colors = textFieldColors,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Tu reflexión o mejora...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 300.dp).padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = textFieldColors,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (Opcional)") },
                placeholder = { Text("Ej: Logro, Aprendizaje, Bienestar...", color = Color.White.copy(alpha = 0.5f))},
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                leadingIcon = { Icon(Icons.Filled.Category, "Categoría") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                singleLine = true,
                colors = textFieldColors,
                enabled = !isLoading
            )

            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        focusManager.clearFocus()
                        journalViewModel.addJournalEntry(title, content, category, entryDate)
                    } else {
                        Toast.makeText(context, "El contenido no puede estar vacío.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(if (entryId == null) "Guardar Entrada" else "Actualizar Entrada", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}