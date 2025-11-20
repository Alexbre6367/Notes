package com.example.oone.database.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ThemeViewModel(initialDarkTheme: Boolean) : ViewModel() {
    private val _isDarkTheme = mutableStateOf(initialDarkTheme)
    val isDarkTheme: State<Boolean> = _isDarkTheme
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
}

class ThemeViewModelFactory(private val initialDarkTheme: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(initialDarkTheme) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
