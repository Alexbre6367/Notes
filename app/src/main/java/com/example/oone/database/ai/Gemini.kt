package com.example.oone.database.ai

import android.util.Log
import com.example.oone.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content



object Gemini {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun analyze(descriptor: String): String? {
        Log.d("MyLog", "Начало анализа: '$descriptor'")
        return try {
            val response = model.generateContent(
                content {
                    text(
                        """
                        Ты помощник для структурирования, анализа
                        и добавления своих комментариев(кратко, комментарии должны соотвествовать контексту, и смотря какое настроение у текста,
                        такие и комментарии(поддержка, мотивация, понимании и т.п) заметки. В итоге должна получиться готовая заметка которой можно пользоваться.
                        Текст: $descriptor
                        """.trimIndent()
                    )
                }
            )
            val analysisResult = response.text // Извлекаем текст из ответа.
            Log.d("MyLog", "Анализ успешно завершен. Результат: '$analysisResult'") // Обновлен лог успеха
            analysisResult
        } catch (e: Exception) {
            Log.e("MyLog", "Ошибка во время анализа Gemini: ${e.message}", e)
            null
        }
    }
}
