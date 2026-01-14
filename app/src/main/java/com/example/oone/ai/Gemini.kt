package com.example.oone.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend


object Gemini {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash-lite")

    suspend fun analyze(descriptor: String): String? {
        Log.d("MyLog", "Начало анализа: '$descriptor'")
        return try {
            val prompt = """
                        Твоя задача помочь пользователю в приложении заметок. 
                        В первой строчке твоего ответа укажи название для заметки, в следующей строке сразу заметка.
                        Твой ответ должен быть в виде готовой, структурированной заметки 
                        Язык ответа должен быть таким же как в тексте ниже
                        $descriptor
                        """.trimIndent()

            val response = model.generateContent(prompt)
            val analysisResult = response.text // Извлекаем текст из ответа.
            Log.d("MyLog", "Анализ успешно завершен. Результат: '$analysisResult'") // Обновлен лог успеха
            analysisResult
        } catch (e: Exception) {
            Log.e("MyLog", "Ошибка во время анализа Gemini: ${e.message}", e)
            null
        }
    }
}
