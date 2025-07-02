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
        private const val ENCRYPTION_KEY_ALIAS = "note_encryption_key"
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

//    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) } //обращение к хронилищу
//
//    init { //генерация ключа
//        if(!keyStore.containsAlias(ENCRYPTION_KEY_ALIAS)) {
//            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
//            keyGenerator.init(
//                KeyGenParameterSpec.Builder(
//                    ENCRYPTION_KEY_ALIAS,
//                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
//                )
//                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                    .build()
//            )
//            keyGenerator.generateKey()
//        }
//    }
//
//    private fun getSecretKey(): SecretKey { //получение ключа
//        return (keyStore.getEntry(ENCRYPTION_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
//    }
//
//    fun encrypt(text: String): String {
//        val cipher = Cipher.getInstance("AES/GCM/NoPadding") //создание объекта шифрования
//        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey()) //инициализация шфира на шифрвание с ключом
//        val iv = cipher.iv //генерация вектора инициализации(12 байт)
//        val encryptedByte = cipher.doFinal(text.toByteArray(Charsets.UTF_8)) //шифрование
//        val combined = iv + encryptedByte //для дольнейшей расшифровки
//        return Base64.encodeToString(combined, Base64.DEFAULT) //кодирование в одну строку
//    }
//
//    fun decrypt(text: String): String {
//        val combined = Base64.decode(text, Base64.DEFAULT) //декодировка в байты
//        val iv = combined.sliceArray(0 until 12) //разделение combined
//        val encryptedBytes = combined.sliceArray(12 until combined.size) //весь массив байт
//        val cipher = Cipher.getInstance("AES/GCM/NoPadding") //создание объекта шифра с конкретной конфигурацией
//        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv)) //инициализация для дешифровки с передачей ключа
//        val decryptedBytes = cipher.doFinal(encryptedBytes) //расшифровка
//        return String(decryptedBytes, Charsets.UTF_8) //байты в строку
//    }
}

