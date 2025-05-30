package com.miempresa.totalhealth.trainer.history.food

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.miempresa.totalhealth.foodreport.FoodReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserFoodReportHistoryViewModel(private val userId: String) : ViewModel() {

    private val _foodReports = MutableStateFlow<List<FoodReport>>(emptyList())
    val foodReports: StateFlow<List<FoodReport>> = _foodReports.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        loadFoodReports()
    }

    private fun loadFoodReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("food_reports")
                    .orderBy("mealTimestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                Log.d("food_debug", "Documentos obtenidos: ${snapshot.size()}")
                _foodReports.value = snapshot.documents.mapNotNull { document ->
                    val mealType = document.getString("mealType")
                    val comment = document.getString("comment")
                    val timestamp = document.getTimestamp("mealTimestamp")
                    val imageUrl = document.getString("imageUrl")
                    if (mealType != null && timestamp != null) {
                        FoodReport(
                            id = document.id,
                            mealType = mealType,
                            comment = comment ?: "",
                            mealTimestamp = timestamp.toDate(),
                            imageUrl = imageUrl
                        )
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("FoodReportHistoryVM", "Error loading food reports for user $userId", e)
                _error.value = "Error al cargar reportes de comida: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}

class UserFoodReportHistoryViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserFoodReportHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserFoodReportHistoryViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}