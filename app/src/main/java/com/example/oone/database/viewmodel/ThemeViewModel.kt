package com.example.oone.database.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    application: Application,
    initialDarkTheme: Boolean
) : ViewModel() {
    private val _isDarkTheme = mutableStateOf(initialDarkTheme)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    private val dataStore = application.dataStore

    private val _isPlaceActivated = MutableStateFlow(false)
    val isPlaceActivated = _isPlaceActivated.asStateFlow()

    private companion object {
        val PLACE_ACTIVATED = booleanPreferencesKey("place_activated")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_THEME] = _isDarkTheme.value
            }
        }
    }

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isPlaceActivated.value = preferences[PLACE_ACTIVATED] ?: false
                preferences[DARK_THEME]?.let { savedTheme ->
                    _isDarkTheme.value = savedTheme
                }
            }
        }
    }

    private fun saveState() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PLACE_ACTIVATED] = _isPlaceActivated.value
            }
        }
    }

    fun togglePlacePosition() {
        _isPlaceActivated.value = !_isPlaceActivated.value
        saveState()
    }

}

class ThemeViewModelFactory(
    private val application: Application,
    private val initialDarkTheme: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(application, initialDarkTheme) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val Context.dataStore by preferencesDataStore("settings")
