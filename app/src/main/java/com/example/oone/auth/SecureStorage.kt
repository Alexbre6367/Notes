package com.example.oone.auth

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) { //шифрование данных пользователя и передача внутри приложения

    companion object {
        private const val PREF_FILE_NAME = "secure_prefs"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    private val masterKey = MasterKey.Builder(context) //ключ для шифрования
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create( //шифрование самих данных
        context,
        PREF_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(email: String, password: String) { //сохранение данных
        sharedPreferences.edit {
            putString("email", email)
            putString("password", password)
        }
    }

    fun getEmail(): String? = sharedPreferences.getString(KEY_EMAIL, null) //возвращает почту
    fun getPassword(): String? = sharedPreferences.getString(KEY_PASSWORD, null) //возвращает пароль
}