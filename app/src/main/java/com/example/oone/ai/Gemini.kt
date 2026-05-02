package com.example.oone.ai

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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

class Gemma(private val context: Context) {
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    suspend fun init() = withContext(Dispatchers.IO) {
        if(conversation != null) return@withContext

        if(engine == null) {
            val config = EngineConfig(
                modelPath = "/data/local/tmp/llm/gemma-4-E2B-it.litertlm",
                backend = Backend.CPU(),
                cacheDir = context.cacheDir.absolutePath
            )

            val newEngine = Engine(config)
            newEngine.initialize()
            engine = newEngine
        }

        if(conversation == null) {
            conversation = engine!!.createConversation()
        }
    }

    fun replay(prompt: String): String {
        Log.d("MyLog", "Начало анализа gemma: '$prompt'")
        return conversation?.sendMessage(
            """
                Твоя задача помочь пользователю в приложении заметок. 
                В первой строчке твоего ответа укажи название для заметки, в следующей строке сразу заметка.
                Твой ответ должен быть в виде готовой, структурированной заметки 
                Язык ответа должен быть таким же как в тексте ниже
            """.trimIndent() + prompt
        ).toString()
    }

    fun close() {
        conversation?.close()
        conversation = null
        engine?.close()
        engine = null
    }
}
