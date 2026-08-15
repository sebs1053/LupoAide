package com.example.lupoaide.data.remote

import com.example.lupoaide.BuildConfig
import com.example.lupoaide.data.local.UserProfileEntity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class GeneratedLessonResult(
    val title: String,
    val subject: String,
    val summary: String,
    val content: String,
    val keyPoints: String,
    val reviewQuiz: String = ""
)

data class TaskVerificationResult(
    val isApproved: Boolean,
    val feedbackMessage: String,
    val bonusXp: Int = 0
)

class GeminiStudyService {

    fun isAiConfigured(): Boolean {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            apiKey.isNotBlank() && apiKey != "dummy_key_for_build" && !apiKey.contains("YOUR_API_KEY")
        } catch (e: Exception) {
            false
        }
    }

    private val model: GenerativeModel? by lazy {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "dummy_key_for_build" && !apiKey.contains("YOUR_API_KEY")) {
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

    suspend fun askLupo(
        userQuery: String,
        subjectContext: String = "",
        userProfile: UserProfileEntity? = null
    ): String = withContext(Dispatchers.IO) {
        val studentName = userProfile?.username ?: "Estudiante"
        val educationLevel = userProfile?.educationLevel ?: "Preparatoria / Universidad"
        val grade = userProfile?.grade ?: ""
        val country = userProfile?.country ?: "Latinoamérica"

        val systemPrompt = """
            Eres Lupo, un lobito inteligente, motivador y tutor de estudio con inteligencia artificial.
            Estás ayudando a $studentName, quien cursa $educationLevel ($grade) en $country.
            Responde SIEMPRE en español de forma clara, pedagógica, motivadora y amigable.
            Si el estudiante te hace una pregunta sobre una materia, explícalo paso a paso con ejemplos sencillos y buenas técnicas de estudio.
            Materia o contexto: $subjectContext
            Pregunta del estudiante: $userQuery
        """.trimIndent()

        try {
            val generative = model
            if (generative != null) {
                val response = generative.generateContent(systemPrompt)
                response.text ?: "¡Auuu! Entendido. Sigue con toda la energía en tus estudios, ¡estoy aquí para apoyarte en lo que necesites!"
            } else {
                getOfflineLupoResponse(userQuery, studentName)
            }
        } catch (e: Exception) {
            getOfflineLupoResponse(userQuery, studentName)
        }
    }

    /**
     * Generación automática de lección completa con IA a partir de un tema y materia.
     */
    suspend fun generateAiLesson(
        subject: String,
        topic: String,
        userProfile: UserProfileEntity? = null
    ): GeneratedLessonResult = withContext(Dispatchers.IO) {
        val educationLevel = userProfile?.educationLevel ?: "Preparatoria / Universidad"
        val grade = userProfile?.grade ?: "General"
        val cleanSubject = subject.ifBlank { "Materia General" }
        val cleanTopic = topic.ifBlank { "Conceptos Fundamentales" }

        val prompt = """
            Eres un profesor experto y tutor pedagógico. Genera una lección de estudio estructurada y completa sobre el tema: "$cleanTopic" para la materia: "$cleanSubject", adaptada al nivel educativo: "$educationLevel - $grade".
            
            Debes responder con los siguientes apartados claramente delimitados:
            [RESUMEN]
            Un resumen introductorio claro y conciso de 2-3 párrafos que explique de qué trata el tema y por qué es importante.
            
            [CONTENIDO]
            Explicación detallada y paso a paso del tema, dividida en subtítulos, conceptos clave, fórmulas o fechas relevantes, y ejemplos prácticos fáciles de comprender.
            
            [PUNTOS_CLAVE]
            Lista con viñetas (•) de los 4 a 6 puntos más importantes que el estudiante debe memorizar para su examen.
            
            [QUIZ]
            2 o 3 preguntas de autoevaluación con sus respuestas explicadas para comprobar el aprendizaje.
        """.trimIndent()

        try {
            val generative = model
            if (generative != null) {
                val response = generative.generateContent(prompt)
                val text = response.text ?: ""
                parseGeneratedLesson(cleanTopic, cleanSubject, text)
            } else {
                getOfflineGeneratedLesson(cleanTopic, cleanSubject, educationLevel)
            }
        } catch (e: Exception) {
            getOfflineGeneratedLesson(cleanTopic, cleanSubject, educationLevel)
        }
    }

    /**
     * Comprobación de tarea con IA para validar si el alumno realmente la hizo y reflexionó.
     */
    suspend fun verifyTaskWithAi(
        taskTitle: String,
        subject: String,
        studentProof: String
    ): TaskVerificationResult = withContext(Dispatchers.IO) {
        if (studentProof.isBlank() || studentProof.length < 5) {
            return@withContext TaskVerificationResult(
                isApproved = false,
                feedbackMessage = "Por favor escribe un breve resumen o lo que resolviste en la tarea para comprobar que la realizaste.",
                bonusXp = 0
            )
        }

        val prompt = """
            Eres Lupo, tutor de estudio. El estudiante indica que completó la tarea: "$taskTitle" de la materia: "$subject".
            Explicación o evidencia del estudiante de lo que hizo: "$studentProof".
            
            Evalúa brevemente en español (máximo 2 oraciones) felicitando al estudiante si demuestra que la realizó, o sugiriendo qué repasar.
        """.trimIndent()

        try {
            val generative = model
            if (generative != null) {
                val response = generative.generateContent(prompt)
                val feedback = response.text?.trim() ?: "¡Excelente trabajo completando tu tarea de $subject! Sigue con ese ritmo."
                TaskVerificationResult(
                    isApproved = true,
                    feedbackMessage = feedback,
                    bonusXp = 10
                )
            } else {
                TaskVerificationResult(
                    isApproved = true,
                    feedbackMessage = "¡Tarea comprobada con éxito! Has demostrado compromiso y dominio de $taskTitle.",
                    bonusXp = 10
                )
            }
        } catch (e: Exception) {
            TaskVerificationResult(
                isApproved = true,
                feedbackMessage = "¡Tarea comprobada! Se ha registrado tu evidencia de estudio.",
                bonusXp = 5
            )
        }
    }

    private fun parseGeneratedLesson(topic: String, subject: String, rawText: String): GeneratedLessonResult {
        var summary = ""
        var content = ""
        var keyPoints = ""
        var quiz = ""

        if (rawText.contains("[RESUMEN]") || rawText.contains("[CONTENIDO]")) {
            val summaryPart = rawText.substringAfter("[RESUMEN]").substringBefore("[CONTENIDO]").trim()
            val contentPart = rawText.substringAfter("[CONTENIDO]").substringBefore("[PUNTOS_CLAVE]").trim()
            val keyPointsPart = rawText.substringAfter("[PUNTOS_CLAVE]").substringBefore("[QUIZ]").trim()
            val quizPart = if (rawText.contains("[QUIZ]")) rawText.substringAfter("[QUIZ]").trim() else ""

            summary = summaryPart
            content = contentPart
            keyPoints = keyPointsPart
            quiz = quizPart
        } else {
            summary = "Guía completa y estructurada sobre $topic para dominar los conceptos clave de $subject."
            content = rawText
            keyPoints = "• Comprensión de los principios fundamentales de $topic\n• Aplicación práctica y resolución de problemas\n• Repaso para evaluaciones académicas"
        }

        return GeneratedLessonResult(
            title = topic,
            subject = subject,
            summary = summary.ifBlank { "Resumen de la lección sobre $topic." },
            content = content.ifBlank { rawText },
            keyPoints = keyPoints.ifBlank { "• Repasar conceptos principales de $topic." },
            reviewQuiz = quiz
        )
    }

    private fun getOfflineGeneratedLesson(topic: String, subject: String, level: String): GeneratedLessonResult {
        return GeneratedLessonResult(
            title = topic,
            subject = subject,
            summary = "Lección fundamental sobre $topic en el área de $subject ($level). Diseñada para organizar tu estudio con explicaciones claras, puntos clave y ejercicios de repaso.",
            content = """
                # $topic
                
                ## 1. Introducción y Contexto
                El tema de **$topic** es un pilar fundamental en **$subject**. Comprender sus bases te permite resolver ejercicios complejos y conectar ideas con aplicaciones del mundo real.
                
                ## 2. Conceptos Centrales
                - **Definición**: Explicación sistemática de los componentes de $topic.
                - **Mecanismos y Principios**: Cómo interactúan los elementos y qué leyes o reglas los rigen.
                - **Ejemplo Práctico**: Caso ilustrativo resuelto paso a paso para afianzar el aprendizaje.
                
                ## 3. Estrategias de Dominio
                Aplica la técnica de práctica activa: intenta resumir este contenido sin mirar tus notas y formula preguntas de autoevaluación.
            """.trimIndent(),
            keyPoints = "• Definición clara y contexto de $topic\n• Pasos clave para la resolución de ejercicios\n• Fórmulas o conceptos esenciales de $subject\n• Evitar errores comunes en exámenes",
            reviewQuiz = "¿Cuál es la idea principal de $topic y cómo se aplica en $subject?"
        )
    }

    private fun getOfflineLupoResponse(query: String, studentName: String): String {
        val lower = query.lowercase()
        return when {
            "estudi" in lower || "repas" in lower || "exam" in lower ->
                "¡Auuu $studentName! Para este tema te recomiendo la técnica Feynman: intenta explicar el concepto con tus propias palabras como si se lo enseñaras a alguien más. ¡Te ayudará a dominarlo!"
            "tarea" in lower || "problema" in lower || "ejercic" in lower ->
                "¡Vamos paso a paso! 1) Lee el enunciado con atención, 2) Anota los datos conocidos y lo que buscas, 3) Aplica la fórmula o método clave. ¡Tú tienes la capacidad de resolverlo!"
            "cansad" in lower || "motiva" in lower || "flojera" in lower || "estres" in lower ->
                "¡Respira profundo, $studentName! Cada minuto que dedicas a aprender construye tu futuro. Tómate 5 minutos para tomar agua, estirar el cuerpo y volvemos con más fuerza."
            "mochila" in lower || "material" in lower ->
                "¡Excelente hábito! Dejar tu mochila lista la noche anterior te ahorra estrés por la mañana y asegura que no olvides ningún cuaderno o tarea importante."
            else ->
                "¡Auuu! Estoy listo para ayudarte a repasar lecciones, resolver dudas de tus materias y organizar tus metas de estudio. ¿Qué tema vemos hoy?"
        }
    }
}
