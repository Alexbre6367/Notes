package com.example.oone.database.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oone.screen.theme.dataStore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PlaceViewModel(application: Application) : AndroidViewModel(application){

    private val dataStore = application.dataStore

    private val _isPlaceActivated = MutableStateFlow(false)
    val isPlaceActivated = _isPlaceActivated.asStateFlow()

    private companion object {
        val PLACE_ACTIVATED = booleanPreferencesKey("place_activated")
    }

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isPlaceActivated.value = preferences[PLACE_ACTIVATED] ?: false
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