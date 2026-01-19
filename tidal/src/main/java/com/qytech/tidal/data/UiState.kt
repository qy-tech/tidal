package com.qytech.tidal.data

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success<T>(val data: T) : UiState()
    data class Error(val message: String) : UiState()
}