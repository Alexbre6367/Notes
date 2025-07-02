package com.example.oone.screen.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore("settings")

class ThemePreference(private val context: Context) {
    companion object {
        private val THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
    }

    val themeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: true
        }
    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { settings ->
            settings[THEME_KEY] = isDark
        }
    }
}