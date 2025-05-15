package com.miempresa.totalhealth.foodreport

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter // <--- IMPORTACIÓN CLAVE PARA COIL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodReportScreen(
    navController: NavController,
    foodReportViewModel: FoodReportViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val comment by foodReportViewModel.comment
    val addReportUiState by foodReportViewModel.addReportUiState.collectAsState()

    val mealType by foodReportViewModel.mealType
    val selectedDateMillis by foodReportViewModel.selectedDateMillis
    val selectedHour by foodReportViewModel.selectedHour
    val selectedMinute by foodReportViewModel.selectedMinute
    val mealNumberInDay by foodReportViewModel.mealNumberInDay
    val selectedImageUri by foodReportViewModel.selectedImageUri

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        foodReportViewModel.onImageUriSelected(uri)
    }

    val mealTypes = listOf("Desayuno", "Almuerzo", "Cena", "Snack Mañana", "Snack Tarde", "Otro")
    var mealTypeExpanded by remember { mutableStateOf(false) }

    val mealNumberOptions = (1..6).toList()
    var mealNumberExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog( context, { _, year, month, dayOfMonth -> calendar.timeInMillis = selectedDateMillis; calendar.set(year, month, dayOfMonth); foodReportViewModel.onDateSelected(calendar.timeInMillis) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) ).apply { datePicker.maxDate = System.currentTimeMillis() }
    val timePickerDialog = TimePickerDialog( context, { _, hourOfDay, minuteOfHour -> foodReportViewModel.onTimeSelected(hourOfDay, minuteOfHour) }, selectedHour, selectedMinute, true )

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)
    val colorTextoSecundarioClaro = Color.White.copy(alpha = 0.7f)
    val colorIconoNoEnfocado = Color.White.copy(alpha = 0.7f)
    val colorIconoEnfocado = colorVerdePrincipal

    LaunchedEffect(key1 = addReportUiState) {
        when (val state = addReportUiState) {
            is FoodReportUiState.Success -> {
                Toast.makeText(context, "Reporte de comida guardado", Toast.LENGTH_SHORT).show()
                foodReportViewModel.resetAddReportUiState()
                navController.popBackStack()
            }
            is FoodReportUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                foodReportViewModel.resetAddReportUiState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Reporte de Comida", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorNegro)
            )
        },
        containerColor = colorNegro
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(brush = Brush.verticalGradient(colors = listOf(colorNegro, colorVerdeOscuroDegradado)))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Describe tu comida", style = MaterialTheme.typography.titleLarge, color = colorVerdePrincipal, modifier = Modifier.padding(bottom = 8.dp))
                Text("Añade detalles, tipo, número, hora y una foto de tu comida.", style = MaterialTheme.typography.bodyMedium, color = colorTextoSecundarioClaro, modifier = Modifier.padding(bottom = 24.dp), textAlign = TextAlign.Center)

                ExposedDropdownMenuBox( expanded = mealTypeExpanded, onExpandedChange = { mealTypeExpanded = !mealTypeExpanded }, modifier = Modifier.fillMaxWidth() ) {
                    OutlinedTextField(
                        value = mealType.ifEmpty { "Selecciona tipo de comida" },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Tipo de Comida", color = colorTextoSecundarioClaro) },
                        leadingIcon = { Icon(Icons.Filled.Restaurant, "Tipo de Comida") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro,
                            cursorColor = colorVerdePrincipal, focusedBorderColor = colorVerdePrincipal,
                            unfocusedBorderColor = colorTextoSecundarioClaro.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal,
                            unfocusedLabelColor = colorTextoSecundarioClaro,
                            focusedLeadingIconColor = colorIconoEnfocado,
                            unfocusedLeadingIconColor = colorIconoNoEnfocado,
                            focusedTrailingIconColor = colorIconoEnfocado,
                            unfocusedTrailingIconColor = colorIconoNoEnfocado,
                            disabledTextColor = Color.Gray
                        )
                    )
                    ExposedDropdownMenu( expanded = mealTypeExpanded, onDismissRequest = { mealTypeExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant) ) {
                        mealTypes.forEach { selectionOption -> DropdownMenuItem( text = { Text(selectionOption, color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = { foodReportViewModel.onMealTypeChange(selectionOption); mealTypeExpanded = false } ) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox( expanded = mealNumberExpanded, onExpandedChange = { mealNumberExpanded = !mealNumberExpanded }, modifier = Modifier.fillMaxWidth() ) {
                    OutlinedTextField(
                        value = mealNumberInDay?.toString() ?: "Selecciona número", onValueChange = {}, readOnly = true,
                        label = { Text("Nº de Comida del Día", color = colorTextoSecundarioClaro) },
                        leadingIcon = { Icon(Icons.Filled.FormatListNumbered, "Número de Comida") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealNumberExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro,
                            cursorColor = colorVerdePrincipal, focusedBorderColor = colorVerdePrincipal,
                            unfocusedBorderColor = colorTextoSecundarioClaro.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal,
                            unfocusedLabelColor = colorTextoSecundarioClaro,
                            focusedLeadingIconColor = colorIconoEnfocado,
                            unfocusedLeadingIconColor = colorIconoNoEnfocado,
                            focusedTrailingIconColor = colorIconoEnfocado,
                            unfocusedTrailingIconColor = colorIconoNoEnfocado,
                            disabledTextColor = Color.Gray
                        )
                    )
                    ExposedDropdownMenu( expanded = mealNumberExpanded, onDismissRequest = { mealNumberExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant) ) {
                        mealNumberOptions.forEach { selectionOption -> DropdownMenuItem( text = { Text(selectionOption.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = { foodReportViewModel.onMealNumberInDayChange(selectionOption); mealNumberExpanded = false } ) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton( onClick = { datePickerDialog.show() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = colorTextoClaro), border = BorderStroke(1.dp, colorVerdePrincipal) ) {
                        Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text(dateFormatter.format(Date(selectedDateMillis)))
                    }
                    OutlinedButton( onClick = { timePickerDialog.show() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = colorTextoClaro), border = BorderStroke(1.dp, colorVerdePrincipal) ) {
                        Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text(timeFormatter.format(Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, selectedHour); set(Calendar.MINUTE, selectedMinute) }.time))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField( value = comment, onValueChange = { newCommentValue: String -> foodReportViewModel.onCommentChange(newCommentValue) }, label = { Text("Comentario sobre la comida", color = colorTextoSecundarioClaro) }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    leadingIcon = { Icon(Icons.Filled.Fastfood, "Icono Comida") }, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro,
                        cursorColor = colorVerdePrincipal, focusedBorderColor = colorVerdePrincipal,
                        unfocusedBorderColor = colorTextoSecundarioClaro.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal,
                        unfocusedLabelColor = colorTextoSecundarioClaro,
                        focusedLeadingIconColor = colorIconoEnfocado,
                        unfocusedLeadingIconColor = colorIconoNoEnfocado,
                        disabledTextColor = Color.Gray
                    ),
                    enabled = addReportUiState !is FoodReportUiState.Loading, keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }) )
                Spacer(modifier = Modifier.height(16.dp))

                selectedImageUri?.let { uri ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image( painter = rememberAsyncImagePainter(model = uri), contentDescription = "Imagen seleccionada", modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)), contentScale = ContentScale.Crop )
                        IconButton( onClick = { foodReportViewModel.onImageUriSelected(null) }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape) ) { Icon(Icons.Filled.Clear, contentDescription = "Quitar imagen", tint = Color.White) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedButton( onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = colorVerdePrincipal), border = BorderStroke(1.dp, colorVerdePrincipal), enabled = addReportUiState !is FoodReportUiState.Loading && addReportUiState !is FoodReportUiState.ImageUploading ) {
                    Icon( if (selectedImageUri == null) Icons.Filled.PhotoLibrary else Icons.Filled.CameraAlt, contentDescription = "Añadir/Cambiar Foto", modifier = Modifier.size(ButtonDefaults.IconSize) )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text(if (selectedImageUri == null) "Añadir Foto" else "Cambiar Foto")
                }
                Spacer(modifier = Modifier.height(80.dp))
            }

            Button(
                onClick = { focusManager.clearFocus(); foodReportViewModel.addFoodReport() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp).height(50.dp).align(Alignment.BottomCenter),
                enabled = addReportUiState !is FoodReportUiState.Loading && addReportUiState !is FoodReportUiState.ImageUploading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal, contentColor = Color.White)
            ) {
                when (addReportUiState) {
                    is FoodReportUiState.Loading, FoodReportUiState.ImageUploading -> { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) }
                    else -> { Icon(Icons.Filled.Save, "Guardar Reporte", modifier = Modifier.size(ButtonDefaults.IconSize)); Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text("Guardar Reporte", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
