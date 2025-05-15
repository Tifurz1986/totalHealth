package com.miempresa.totalhealth.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake // Icono para edad
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter // Icono para peso/actividad
import androidx.compose.material.icons.filled.Height // Icono para estatura
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Wc // Icono para sexo
import androidx.compose.material.icons.filled.Flag // Icono para objetivos
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType // Para campos numéricos
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.miempresa.totalhealth.R
import com.miempresa.totalhealth.auth.AuthViewModel
import com.miempresa.totalhealth.auth.UserProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val userProfileState by authViewModel.userProfileUiState.collectAsState()

    // Estados locales para los campos del formulario
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var emailDisplay by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Nuevos campos de estado para el perfil expandido
    var ageString by remember { mutableStateOf("") } // Edad como String para TextField
    var sex by remember { mutableStateOf("") }
    var heightString by remember { mutableStateOf("") } // Estatura como String
    var weightString by remember { mutableStateOf("") } // Peso como String
    var activityLevel by remember { mutableStateOf("") }
    var healthGoals by remember { mutableStateOf("") }


    var isSavingProfile by remember { mutableStateOf(false) }
    var isUploadingPicture by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(authViewModel.getCurrentUser()?.uid, userProfileState) {
        val currentUser = authViewModel.getCurrentUser()
        if (currentUser != null) {
            when (val state = userProfileState) {
                is UserProfileUiState.Success -> {
                    name = state.profile.name
                    surname = state.profile.surname
                    emailDisplay = state.profile.email
                    profilePictureUrl = state.profile.profilePictureUrl

                    // Inicializar nuevos campos
                    ageString = state.profile.age?.toString() ?: ""
                    sex = state.profile.sex
                    heightString = state.profile.height?.toString() ?: ""
                    weightString = state.profile.weight?.toString() ?: ""
                    activityLevel = state.profile.activityLevel
                    healthGoals = state.profile.healthGoals

                    isSavingProfile = false
                    isUploadingPicture = false
                }
                is UserProfileUiState.Idle -> {
                    authViewModel.loadUserProfile(currentUser.uid)
                }
                is UserProfileUiState.Loading -> { /* No cambiar flags de guardado/subida */ }
                is UserProfileUiState.Error -> {
                    Toast.makeText(context, "Error al cargar perfil: ${state.message}", Toast.LENGTH_LONG).show()
                    isSavingProfile = false
                    isUploadingPicture = false
                }
            }
        } else {
            navController.popBackStack()
        }
    }

    val colorNegro = Color.Black
    val colorVerdePrincipal = Color(0xFF00897B)
    val colorVerdeOscuroDegradado = Color(0xFF004D40)
    val colorTextoClaro = Color.White.copy(alpha = 0.9f)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        authViewModel.resetUserProfileState()
                        navController.popBackStack()
                    }) {
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (userProfileState is UserProfileUiState.Loading && !isUploadingPicture && !isSavingProfile) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp), color = colorVerdePrincipal)
            } else {
                // Sección de Foto de Perfil
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = if (selectedImageUri != null) {
                            rememberAsyncImagePainter(model = selectedImageUri)
                        } else if (!profilePictureUrl.isNullOrBlank()) {
                            rememberAsyncImagePainter(model = profilePictureUrl)
                        } else {
                            painterResource(id = R.drawable.ic_launcher_foreground) // Cambiar por un placeholder de perfil
                        },
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.3f))
                            .border(2.dp, colorVerdePrincipal, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorVerdePrincipal.copy(alpha = 0.8f))
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = "Cambiar foto", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Email (No editable)
                OutlinedTextField( /* ... igual que antes ... */
                    value = emailDisplay,
                    onValueChange = { /* No editable */ },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = colorTextoClaro.copy(alpha = 0.7f),
                        disabledBorderColor = Color.White.copy(alpha = 0.3f),
                        disabledLabelColor = Color.White.copy(alpha = 0.5f),
                        disabledLeadingIconColor = Color.White.copy(alpha = 0.5f)
                    )
                )

                // Nombre
                OutlinedTextField( /* ... igual que antes ... */
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = "Nombre") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    singleLine = true,
                    enabled = !isSavingProfile && !isUploadingPicture,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro,
                        focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                // Apellidos
                OutlinedTextField( /* ... igual que antes ... */
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), // Reducido padding inferior
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = "Apellidos") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next), // Cambiado a Next
                    singleLine = true,
                    enabled = !isSavingProfile && !isUploadingPicture,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro,
                        focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                // NUEVOS CAMPOS
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = ageString,
                        onValueChange = { ageString = it.filter { char -> char.isDigit() }.take(3) },
                        label = { Text("Edad") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        leadingIcon = { Icon(Icons.Filled.Cake, contentDescription = "Edad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true, enabled = !isSavingProfile && !isUploadingPicture, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro, focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f))
                    )
                    OutlinedTextField( // Podría ser un DropdownMenu
                        value = sex,
                        onValueChange = { sex = it },
                        label = { Text("Sexo") },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        leadingIcon = { Icon(Icons.Filled.Wc, contentDescription = "Sexo") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                        singleLine = true, enabled = !isSavingProfile && !isUploadingPicture, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro, focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = heightString,
                        onValueChange = { heightString = it.filter { char -> char.isDigit() }.take(3) },
                        label = { Text("Estatura (cm)") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        leadingIcon = { Icon(Icons.Filled.Height, contentDescription = "Estatura") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true, enabled = !isSavingProfile && !isUploadingPicture, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro, focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f))
                    )
                    OutlinedTextField(
                        value = weightString,
                        onValueChange = { weightString = it.filter { char -> char.isDigit() || char == '.' }.take(6) }, // Permite decimal
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        leadingIcon = { Icon(Icons.Filled.FitnessCenter, contentDescription = "Peso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        singleLine = true, enabled = !isSavingProfile && !isUploadingPicture, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro, focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField( // Podría ser un DropdownMenu
                    value = activityLevel,
                    onValueChange = { activityLevel = it },
                    label = { Text("Nivel de Actividad") },
                    placeholder = { Text("Ej: Sedentario, Ligero, Moderado...", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    leadingIcon = { Icon(Icons.Filled.FitnessCenter, contentDescription = "Nivel de Actividad") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    singleLine = true, enabled = !isSavingProfile && !isUploadingPicture, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors( focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro, focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f))
                )

                OutlinedTextField(
                    value = healthGoals,
                    onValueChange = { healthGoals = it },
                    label = { Text("Mis Objetivos de Salud") },
                    placeholder = { Text("Ej: Perder peso, ganar músculo, correr 5km...", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).heightIn(min = 80.dp), // Para múltiples líneas
                    leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = "Objetivos de Salud") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    enabled = !isSavingProfile && !isUploadingPicture, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors( focusedBorderColor = colorVerdePrincipal, unfocusedBorderColor = Color.White.copy(alpha = 0.5f), focusedLabelColor = colorVerdePrincipal, unfocusedLabelColor = Color.White.copy(alpha = 0.7f), cursorColor = colorVerdePrincipal, focusedTextColor = colorTextoClaro, unfocusedTextColor = colorTextoClaro, focusedLeadingIconColor = colorVerdePrincipal, unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f))
                )

                // Botón Guardar Cambios
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // Convertir strings a números, manejando errores de formato o campos vacíos
                        val ageValue = ageString.toIntOrNull()
                        val heightValue = heightString.toIntOrNull()
                        val weightValue = weightString.toDoubleOrNull()

                        // Lógica de guardado (foto primero si hay, luego datos)
                        if (selectedImageUri != null) {
                            isUploadingPicture = true
                            authViewModel.uploadAndSaveProfilePictureUrl(
                                imageUri = selectedImageUri!!,
                                onSuccess = {
                                    selectedImageUri = null
                                    // Ahora guardar el resto de los datos del perfil
                                    authViewModel.updateUserProfile(
                                        name = name, surname = surname, age = ageValue, sex = sex,
                                        height = heightValue, weight = weightValue,
                                        activityLevel = activityLevel, healthGoals = healthGoals,
                                        onSuccess = {
                                            isUploadingPicture = false
                                            Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        },
                                        onError = { errorMsg ->
                                            isUploadingPicture = false
                                            Toast.makeText(context, "Error al actualizar datos: $errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onError = { errorMsg ->
                                    isUploadingPicture = false
                                    Toast.makeText(context, "Error al subir foto: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            // Solo actualizar datos del perfil (sin nueva foto)
                            isSavingProfile = true
                            authViewModel.updateUserProfile(
                                name = name, surname = surname, age = ageValue, sex = sex,
                                height = heightValue, weight = weightValue,
                                activityLevel = activityLevel, healthGoals = healthGoals,
                                onSuccess = {
                                    isSavingProfile = false
                                    Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                },
                                onError = { errorMsg ->
                                    isSavingProfile = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSavingProfile && !isUploadingPicture && userProfileState !is UserProfileUiState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorVerdePrincipal)
                ) {
                    if (isSavingProfile || isUploadingPicture) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (userProfileState is UserProfileUiState.Error && !isSavingProfile && !isUploadingPicture) {
                    Text(
                        text = "Error: ${(userProfileState as UserProfileUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
