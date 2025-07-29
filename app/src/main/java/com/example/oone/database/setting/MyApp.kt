package com.example.oone.database.setting

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d("MyLog", "Firebase инициализирован")

        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously()
                .addOnFailureListener {
                    Log.e("MyLog", "Ошибка входа: ${it.message}")
                }
        } else {
            Log.d("MyLog", "Пользователь уже авторизован: ${Firebase.auth.currentUser?.uid}")
        }
    }
}