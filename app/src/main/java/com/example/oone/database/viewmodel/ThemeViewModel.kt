package com.example.oone.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oone.screen.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val pref = ThemePreference(application)

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    init {
        pref.themeFlow.onEach {
            _isDarkTheme.value = it
        }.launchIn(viewModelScope)
    }

    fun toggleTheme() {
        viewModelScope.launch {
            pref.saveTheme(!_isDarkTheme.value)
        }
    }
}