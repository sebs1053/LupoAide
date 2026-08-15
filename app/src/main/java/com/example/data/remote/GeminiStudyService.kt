package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val isUser: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CourseNodeDraft(
    val title: String,
    val nodeType: String,
    val contentText: String,
    val optionsJson: String = "",
    val correctAnswerIndex: Int = 0
)

object GeminiStudyService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askLupoTutor(
        chatHistory: List<ChatMessage>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext mockLupoResponse(userMessage)
        }

        try {
            val rootObj = JSONObject()

            // System instruction
            val sysPart = JSONObject().put("text", "Eres Lupo, la mascota y tutor de IA de LupoAide (Compañero de Estudio IA). Tu tono es motivador, amigable, conciso, inteligente y protector. Ayudas al estudiante a comprender temas de escuela, resumir apuntes, resolver dudas y mantener la concentración sin procrastinar. Usa emojis oportunos (🐺, 📚, ⚡, 🔥) en español.")
            val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
            rootObj.put("systemInstruction", sysContent)

            // Contents array
            val contentsArr = JSONArray()
            chatHistory.takeLast(6).forEach { msg ->
                val part = JSONObject().put("text", msg.messageText)
                val content = JSONObject()
                    .put("role", if (msg.isUser) "user" else "model")
                    .put("parts", JSONArray().put(part))
                contentsArr.put(content)
            }

            // Current prompt
            val currentPart = JSONObject().put("text", userMessage)
            val currentContent = JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(currentPart))
            contentsArr.put(currentContent)

            rootObj.put("contents", contentsArr)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(rootObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val resObj = JSONObject(responseString)
                val candidates = resObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext text
                        }
                    }
                }
            }
            mockLupoResponse(userMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            mockLupoResponse(userMessage)
        }
    }

    suspend fun generateCourseTopic(
        subject: String,
        gradeLevel: String,
        userMaterial: String
    ): Pair<String, List<CourseNodeDraft>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val promptText = "Crea un curso expres de estudio de la materia '$subject' para grado '$gradeLevel'. " +
                if (userMaterial.isNotBlank()) "Basado en este material de clase: '$userMaterial'. " else "" +
                        "Proporciona una breve descripción del curso y 4 lecciones/módulos cortos con título y contenido explicativo."

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair(
                "Curso personalizado de $subject ($gradeLevel) para dominar los temas clave.",
                listOf(
                    CourseNodeDraft("Módulo 1: Fundamentos esenciales de $subject", "LESSON", "Aprende los pilares y conceptos principales de $subject."),
                    CourseNodeDraft("Quiz 1: Comprobación de conceptos", "QUIZ", "¿Cuál es el principio fundamental de este tema?", "Opción A|Opción B|Opción C|Opción D", 0),
                    CourseNodeDraft("Módulo 2: Aplicación práctica y fórmulas", "LESSON", "Explicación paso a paso de problemas resueltos y casos de estudio."),
                    CourseNodeDraft("Simulacro de Examen Final", "QUIZ", "Pregunta de repaso de examen:", "Respuesta Correcta|Respuesta Incorrecta 1|Respuesta Incorrecta 2|Respuesta Incorrecta 3", 0)
                )
            )
        }

        try {
            val rootObj = JSONObject()
            val sysPart = JSONObject().put("text", "Eres un generador pedagógico de cursos escolares en español.")
            val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
            rootObj.put("systemInstruction", sysContent)

            val part = JSONObject().put("text", promptText)
            val content = JSONObject().put("role", "user").put("parts", JSONArray().put(part))
            rootObj.put("contents", JSONArray().put(content))

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(rootObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val resObj = JSONObject(responseString)
                val candidates = resObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            val shortText = if (text.length > 250) text.substring(0, 250) else text
                            return@withContext Pair(
                                "Curso interactivo generado por Gemini para $subject.",
                                listOf(
                                    CourseNodeDraft("Lección 1: Visión General de $subject", "LESSON", shortText),
                                    CourseNodeDraft("Quiz de Autoevaluación", "QUIZ", "¿Qué concepto clave se aborda en $subject?", "Concepto Principal|Distractor 1|Distractor 2|Distractor 3", 0),
                                    CourseNodeDraft("Lección 2: Profundización de Temas", "LESSON", "Análisis detallado de los contenidos clave y estrategias de examen."),
                                    CourseNodeDraft("Examen Simulacro", "QUIZ", "¿Cuál es la conclusión principal del módulo?", "Resultado esperado|Falso 1|Falso 2|Falso 3", 0)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Pair(
            "Curso de $subject ($gradeLevel)",
            listOf(
                CourseNodeDraft("Introducción a $subject", "LESSON", "Conceptos básicos para preparar tu examen."),
                CourseNodeDraft("Quiz Rápido", "QUIZ", "¿Cuál es la respuesta correcta?", "Respuesta A|Respuesta B|Respuesta C|Respuesta D", 0),
                CourseNodeDraft("Estrategias de Repaso", "LESSON", "Resumen de fórmulas y teoremas importantes.")
            )
        )
    }

    private fun mockLupoResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hola") || lower.contains("lupo") ->
                "¡Hola! 🐺 Soy Lupo, tu compañero de estudio. ¿En qué materia necesitas ayuda hoy? ¡Vamos a derrotar la procrastinación!"
            lower.contains("examen") || lower.contains("estudiar") ->
                "¡Excelente iniciativa! 📚 Te recomiendo activar el 'Contrato de Estudio' para bloquear distracciones por 25 minutos. ¡Ganarás 100 EXP y 5 Huesos 🦴 al terminar!"
            lower.contains("tarea") || lower.contains("horario") ->
                "Puedes revisar tus tareas pendientes en la pestaña de 'Tareas' ✔️ o consultar tu horario de clases en 'Horario' 📅."
            else ->
                "🐺 ¡Excelente pregunta! Recuerda dividir tus temas en bloques cortos de 20 minutos con descansos de 5 min (Técnica Pomodoro). ¡Estás logrando un progreso constante!"
        }
    }
}
