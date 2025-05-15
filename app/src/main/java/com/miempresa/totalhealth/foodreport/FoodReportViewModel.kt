package com.miempresa.totalhealth.foodreport

import android.net.Uri // Importación para Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage // Importación para Firebase Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.UUID // Para nombres de archivo únicos

// Estados para la UI de FoodReport
sealed class FoodReportUiState {
    object Idle : FoodReportUiState()
    object Loading : FoodReportUiState() // Para guardar en Firestore
    object ImageUploading : FoodReportUiState() // Nuevo estado para subida de imagen
    object Success : FoodReportUiState()
    data class Error(val message: String) : FoodReportUiState()
}

class FoodReportViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage // Instancia de Firebase Storage

    private val _addReportUiState = MutableStateFlow<FoodReportUiState>(FoodReportUiState.Idle)
    val addReportUiState: StateFlow<FoodReportUiState> = _addReportUiState

    private val _comment = mutableStateOf("")
    val comment: State<String> = _comment

    // Estado para el URI de la imagen seleccionada localmente
    private val _selectedImageUri = mutableStateOf<Uri?>(null)
    val selectedImageUri: State<Uri?> = _selectedImageUri // Exponer para la UI si es necesario para preview

    // Este almacenará la URL de descarga de Firebase Storage una vez subida
    private var uploadedImageUrl: String? = null

    private val _mealType = mutableStateOf("")
    val mealType: State<String> = _mealType

    private val _selectedDateMillis = mutableLongStateOf(System.currentTimeMillis())
    val selectedDateMillis: State<Long> = _selectedDateMillis

    private val _selectedHour = mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    val selectedHour: State<Int> = _selectedHour

    private val _selectedMinute = mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE))
    val selectedMinute: State<Int> = _selectedMinute

    private val _mealNumberInDay = mutableStateOf<Int?>(null)
    val mealNumberInDay: State<Int?> = _mealNumberInDay


    fun onCommentChange(newComment: String) {
        _comment.value = newComment
    }

    // Llamado desde la UI cuando se selecciona/deselecciona una imagen
    fun onImageUriSelected(uri: Uri?) {
        _selectedImageUri.value = uri
        if (uri == null) {
            uploadedImageUrl = null // Si se quita la imagen, limpiar la URL de descarga también
        }
    }

    fun onMealTypeChange(newMealType: String) {
        _mealType.value = newMealType
    }

    fun onDateSelected(millis: Long) {
        _selectedDateMillis.longValue = millis
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        _selectedHour.intValue = hour
        _selectedMinute.intValue = minute
    }

    fun onMealNumberInDayChange(newNumber: Int?) {
        _mealNumberInDay.value = newNumber
    }

    // Función interna para subir la imagen y obtener su URL
    private suspend fun uploadImageToStorageAndGetUrl(uri: Uri, userId: String): String? {
        _addReportUiState.value = FoodReportUiState.ImageUploading
        return try {
            val fileName = "food_${userId}_${UUID.randomUUID()}.jpg"
            // Ruta en Storage: food_reports_images/{userId}/{fileName}
            val imageRef = storage.reference.child("food_reports_images/$userId/$fileName")

            imageRef.putFile(uri).await() // Subir el archivo
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d("FoodReportViewModel", "Image uploaded successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("FoodReportViewModel", "Error uploading image", e)
            _addReportUiState.value = FoodReportUiState.Error("Error al subir la imagen: ${e.message}")
            null
        }
    }

    fun addFoodReport() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _addReportUiState.value = FoodReportUiState.Error("Usuario no autenticado.")
            return
        }
        if (comment.value.isBlank()) {
            _addReportUiState.value = FoodReportUiState.Error("El comentario no puede estar vacío.")
            return
        }
        if (mealType.value.isBlank()) {
            _addReportUiState.value = FoodReportUiState.Error("Selecciona el tipo de comida.")
            return
        }
        if (_mealNumberInDay.value == null) {
            _addReportUiState.value = FoodReportUiState.Error("Selecciona el número de comida del día.")
            return
        }

        viewModelScope.launch {
            // 1. Subir imagen si hay una seleccionada
            if (_selectedImageUri.value != null) {
                uploadedImageUrl = uploadImageToStorageAndGetUrl(_selectedImageUri.value!!, userId)
                if (uploadedImageUrl == null) {
                    // El error ya se estableció en uploadImageToStorageAndGetUrl, no continuar.
                    return@launch
                }
            } else {
                uploadedImageUrl = null // Asegurarse de que sea null si no hay imagen
            }

            // 2. Guardar el reporte en Firestore (con o sin URL de imagen)
            _addReportUiState.value = FoodReportUiState.Loading // Estado para guardar en Firestore
            try {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateMillis.longValue
                    set(Calendar.HOUR_OF_DAY, _selectedHour.intValue)
                    set(Calendar.MINUTE, _selectedMinute.intValue)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val mealTimestampDate = calendar.time

                val newReport = FoodReport(
                    userId = userId,
                    comment = comment.value.trim(),
                    imageUrl = uploadedImageUrl, // Usar la URL de descarga obtenida (o null)
                    mealType = mealType.value,
                    mealTimestamp = mealTimestampDate,
                    mealNumberInDay = _mealNumberInDay.value,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                db.collection("users").document(userId)
                    .collection("food_reports")
                    .add(newReport)
                    .await()

                _addReportUiState.value = FoodReportUiState.Success
                Log.d("FoodReportViewModel", "Food report added to Firestore. Image URL: $uploadedImageUrl")
                clearFields()
            } catch (e: Exception) {
                Log.e("FoodReportViewModel", "Error adding food report to Firestore", e)
                _addReportUiState.value = FoodReportUiState.Error("Error al guardar datos del reporte: ${e.message}")
            }
        }
    }

    fun clearFields() {
        _comment.value = ""
        _selectedImageUri.value = null // Limpiar URI seleccionado
        uploadedImageUrl = null      // Limpiar URL de descarga
        _mealType.value = ""
        _selectedDateMillis.longValue = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        _selectedHour.intValue = calendar.get(Calendar.HOUR_OF_DAY)
        _selectedMinute.intValue = calendar.get(Calendar.MINUTE)
        _mealNumberInDay.value = null
    }

    fun resetAddReportUiState() {
        _addReportUiState.value = FoodReportUiState.Idle
    }
}
