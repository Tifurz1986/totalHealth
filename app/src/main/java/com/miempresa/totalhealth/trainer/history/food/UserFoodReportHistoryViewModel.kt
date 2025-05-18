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
                // --- AJUSTA ESTOS VALORES SI ES NECESARIO ---
                val collectionName = "food_reports"
                val userIdField = "userId"
                val dateField = "date"
                // -----------------------------------------

                val snapshot = db.collection(collectionName)
                    .whereEqualTo(userIdField, userId)
                    .orderBy(dateField, Query.Direction.DESCENDING)
                    .get()
                    .await()

                _foodReports.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FoodReport::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("FoodReportHistoryVM", "Error loading food reports for user $userId", e)
                _error.value = "Error al cargar reportes de comida: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val userId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserFoodReportHistoryViewModel::class.java)) {
                return UserFoodReportHistoryViewModel(userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}