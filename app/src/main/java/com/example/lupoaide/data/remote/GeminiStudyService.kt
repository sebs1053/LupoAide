package com.example.lupoaide.data.remote

import com.example.lupoaide.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiStudyService {

    private val model: GenerativeModel? by lazy {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "dummy_key_for_build") {
                GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun askLupo(userQuery: String, subjectContext: String = ""): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are Lupo, an enthusiastic, super smart, encouraging wolf companion and study buddy for students!
            Help the student understand difficult topics, organize study schedules, break down tasks, or provide study motivation.
            Keep answers clear, highly educational, friendly, with occasional energetic wolf-like enthusiasm.
            Context subject: $subjectContext
            Question: $userQuery
        """.trimIndent()

        try {
            val generative = model
            if (generative != null) {
                val response = generative.generateContent(systemPrompt)
                response.text ?: "¡Auuu! Entendido. Sigue con toda la energía en tus estudios, ¡estoy aquí para apoyarte!"
            } else {
                getOfflineLupoResponse(userQuery)
            }
        } catch (e: Exception) {
            getOfflineLupoResponse(userQuery)
        }
    }

    private fun getOfflineLupoResponse(query: String): String {
        val lower = query.lowercase()
        return when {
            "estudi" in lower || "study" in lower ->
                "¡Auuu! Recuerda la técnica Pomodoro: 25 minutos de enfoque total y 5 minutos de descanso. ¡Tú puedes dominar este tema!"
            "tarea" in lower || "task" in lower || "math" in lower || "matem" in lower ->
                "Divide el problema en pasos pequeños: 1) Identifica variables conocidas, 2) Formula las ecuaciones clave y 3) Resuelve paso a paso."
            "motiva" in lower || "cansad" in lower || "tired" in lower ->
                "¡No te rindas! Cada minuto de estudio suma experiencia y monedas en tu aventura. Toma agua, estira 2 minutos y ¡vamos con todo!"
            else ->
                "¡Auuu! Estoy listo para ayudarte a organizar tus horarios, completar misiones y repasar conceptos clave. ¡Pregúntame lo que necesites!"
        }
    }
}
