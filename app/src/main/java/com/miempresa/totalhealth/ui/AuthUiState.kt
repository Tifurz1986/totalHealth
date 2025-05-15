package com.miempresa.totalhealth.ui

// Estados de la UI para el flujo de autenticación
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
