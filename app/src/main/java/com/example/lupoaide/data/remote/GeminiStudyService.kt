package com.example.lupoaide.data.remote

import com.example.lupoaide.BuildConfig
import com.example.lupoaide.data.local.CourseEntity
import com.example.lupoaide.data.local.CourseModuleItem
import com.example.lupoaide.data.local.UserProfileEntity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class GeneratedLessonResult(
    val title: String,
    val subject: String,
    val summary: String,
    val content: String,
    val keyPoints: String,
    val reviewQuiz: String = ""
)

data class GeneratedFlashcard(
    val question: String,
    val answer: String,
    val hint: String = ""
)

data class QuizQuestion(
    val id: Int = 0,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class TaskVerificationResult(
    val isApproved: Boolean,
    val feedbackMessage: String,
    val bonusXp: Int = 15,
    val gradeTitle: String = "Comprobada con Éxito"
)

class GeminiStudyService {

    fun getEffectiveApiKey(customApiKey: String? = null): String {
        return customApiKey?.trim()?.takeIf { it.isNotBlank() }
            ?: try {
                BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "dummy_key_for_build" && it != "PLACEHOLDER_API_KEY" }
            } catch (e: Exception) { null }
            ?: ""
    }

    fun isAiConfigured(customApiKey: String? = null): Boolean {
        return try {
            val apiKey = getEffectiveApiKey(customApiKey)
            apiKey.isNotBlank() && apiKey != "dummy_key_for_build" && !apiKey.contains("YOUR_API_KEY")
        } catch (e: Exception) {
            false
        }
    }

    private fun getGenerativeModel(modelName: String = "gemini-3.5-flash", customApiKey: String? = null): GenerativeModel? {
        return try {
            val apiKey = getEffectiveApiKey(customApiKey)
            if (apiKey.isNotBlank() && apiKey != "dummy_key_for_build" && !apiKey.contains("YOUR_API_KEY")) {
                GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun testAiConnection(customApiKey: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "dummy_key_for_build" || apiKey.contains("YOUR_API_KEY")) {
            return@withContext Pair(
                false,
                "No hay una API Key de Gemini configurada. Ingresa tu clave en tu Perfil ⚙️ o agrégala en Secrets."
            )
        }

        val models = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview", "gemini-2.5-flash")
        var lastError = ""

        for (modelName in models) {
            try {
                val model = GenerativeModel(modelName = modelName, apiKey = apiKey)
                val response = model.generateContent("Hola Lupo, responde exactamente: ¡Conexión exitosa con Gemini!")
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return@withContext Pair(true, "¡Conexión exitosa con $modelName!\n\nLupo IA está activo y listo.")
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: e.message ?: "Error desconocido"
            }
        }

        Pair(false, "No se pudo comunicar con Gemini. Causa: $lastError\n\nVerifica tu conexión a internet o tu clave de API.")
    }

    private suspend fun executeWithModelFallback(
        prompt: String,
        customApiKey: String? = null
    ): Pair<String?, String?> {
        val apiKey = getEffectiveApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "dummy_key_for_build" || apiKey.contains("YOUR_API_KEY")) {
            return Pair(null, "API Key no configurada")
        }

        val models = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview", "gemini-2.5-flash")
        var lastError: String? = null

        for (modelName in models) {
            try {
                val model = getGenerativeModel(modelName, customApiKey) ?: continue
                val response = model.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return Pair(text.trim(), null)
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: e.message ?: "Error desconocido"
            }
        }
        return Pair(null, lastError)
    }

    suspend fun askLupo(
        userQuery: String,
        subjectContext: String = "",
        userProfile: UserProfileEntity? = null,
        recentChatContext: String = ""
    ): String = withContext(Dispatchers.IO) {
        val studentName = userProfile?.username?.ifBlank { "Estudiante" } ?: "Estudiante"
        val educationLevel = userProfile?.educationLevel ?: "Preparatoria / Universidad"
        val grade = userProfile?.grade ?: ""
        val country = userProfile?.country ?: "Latinoamérica"
        val customKey = userProfile?.customApiKey

        val systemPrompt = """
            Eres Lupo, un lobito tutor de estudio inteligente, empático y motivador con Inteligencia Artificial.
            Estás asesorando a tu estudiante $studentName (Nivel: $educationLevel, Grado: $grade, País: $country).
            
            Reglas de conversación:
            1. Responde SIEMPRE en español de manera clara, estructurada, cálida y pedagógica.
            2. Usa viñetas, negritas y explicaciones paso a paso si se trata de temas académicos, tareas o fórmulas.
            3. Sé motivador, da ánimos y consejos de estudio prácticos (técnica Pomodoro, Feynman, mapas mentales).
            4. Usa un tono cercano y algún emoji simpático (🐺, 📚, ✨, 💡, 🎯).
            
            ${if (subjectContext.isNotBlank()) "Contexto de materia: $subjectContext" else ""}
            ${if (recentChatContext.isNotBlank()) "Historial reciente:\n$recentChatContext" else ""}
            
            Pregunta o mensaje del estudiante:
            "$userQuery"
        """.trimIndent()

        val (responseText, error) = executeWithModelFallback(systemPrompt, customKey)
        if (!responseText.isNullOrBlank()) {
            responseText
        } else {
            val offlineResponse = getRichOfflineLupoResponse(userQuery, studentName, subjectContext)
            if (error == "API Key no configurada") {
                "$offlineResponse\n\n*(💡 Nota: Respuestas en modo tutor local. Para activar IA en tiempo real con Gemini 3.5 Flash, ingresa tu API Key en tu Perfil ⚙️)*"
            } else {
                "*(⚠️ Nota de conexión: $error. Mostrando respuesta de respaldo:)*\n\n$offlineResponse"
            }
        }
    }

    /**
     * Generación de lecciones de estudio estructuradas con IA.
     */
    suspend fun generateAiLesson(
        subject: String,
        topic: String,
        userProfile: UserProfileEntity? = null
    ): GeneratedLessonResult = withContext(Dispatchers.IO) {
        val educationLevel = userProfile?.educationLevel ?: "Preparatoria / Bachillerato"
        val grade = userProfile?.grade ?: "General"
        val cleanSubject = subject.ifBlank { "Materia General" }
        val cleanTopic = topic.ifBlank { "Conceptos Fundamentales" }

        val prompt = """
            Eres un profesor universitario experto y pedagogo de alto nivel.
            Genera una guía de estudio completa, estructurada y profunda sobre el tema: "$cleanTopic" para la materia: "$cleanSubject", adaptada al nivel educativo: "$educationLevel - $grade".
            
            Responde en español utilizando EXACTAMENTE los siguientes encabezados para separar las secciones:
            
            [RESUMEN]
            Resumen introductorio claro y conciso de 2 párrafos que explique qué es el tema, por qué es crucial aprenderlo y su aplicación en la vida real.
            
            [CONTENIDO]
            Desarrollo detallado del tema dividido con subtítulos, explicaciones paso a paso, fórmulas o fechas relevantes según aplique, y ejemplos prácticos resueltos.
            
            [PUNTOS_CLAVE]
            Lista con viñetas (•) de 4 a 6 conceptos o fórmulas imprescindibles que el estudiante debe memorizar y dominar para su examen.
            
            [QUIZ]
            2 o 3 preguntas de autoevaluación con sus respuestas explicadas para comprobar el aprendizaje activo.
        """.trimIndent()

        try {
            val (rawText, _) = executeWithModelFallback(prompt, userProfile?.customApiKey)
            if (!rawText.isNullOrBlank()) {
                parseGeneratedLesson(cleanTopic, cleanSubject, rawText)
            } else {
                getOfflineGeneratedLesson(cleanTopic, cleanSubject, educationLevel)
            }
        } catch (e: Exception) {
            getOfflineGeneratedLesson(cleanTopic, cleanSubject, educationLevel)
        }
    }

    /**
     * Comprobación de tarea con IA: evalúa en tiempo real si el estudiante demostró evidencia y comprensión.
     * La IA decide estrictamente si se cumplió o no para tacharla o rechazarla.
     */
    suspend fun verifyTaskWithAi(
        taskTitle: String,
        subject: String,
        studentProof: String,
        hasPhotoEvidence: Boolean = false,
        userProfile: UserProfileEntity? = null
    ): TaskVerificationResult = withContext(Dispatchers.IO) {
        val studentName = userProfile?.username?.ifBlank { "Estudiante" } ?: "Estudiante"
        val cleanProof = studentProof.trim()

        if (cleanProof.length < 5 && !hasPhotoEvidence) {
            return@withContext TaskVerificationResult(
                isApproved = false,
                feedbackMessage = "Evidencia insuficiente. Por favor describe tu procedimiento, resultados o adjunta una foto/captura de lo que hiciste.",
                bonusXp = 0,
                gradeTitle = "Evidencia Insuficiente"
            )
        }

        val prompt = """
            Eres Lupo, un lobito tutor de estudio estricto pero justo, pedagógico y motivador con Inteligencia Artificial.
            Estás evaluando si el estudiante $studentName REALMENTE cumplió y realizó su tarea escolar para permitirle tacharla y completarla.
            
            Datos de la tarea:
            - Título: "$taskTitle"
            - Materia: "$subject"
            - Evidencia o texto redactado por el estudiante: "$cleanProof"
            - Adjuntó foto/documento de evidencia física: ${if (hasPhotoEvidence) "SÍ" else "NO"}
            
            Instrucciones para Lupo:
            1. Determina si la evidencia presentada es coherente, relevante y suficiente para considerar la tarea como cumplida (o si solo es texto vacío o evasivo como "ya la hice", "listo", "hola").
            2. Si la evidencia es real y suficiente, responde iniciando con:
               ESTADO: APROBADA
               FEEDBACK: [Felicita al estudiante mencionando lo que explicó o entregó y dale un consejo pedagógico o refuerzo breve en tono Lupo].
            3. Si la evidencia NO demuestra que hizo la tarea (demasiado vaga, sin sentido o evasiva), responde iniciando con:
               ESTADO: RECHAZADA
               FEEDBACK: [Explica amablemente qué le faltó incluir o demostrar para poder validarla y aprobarla].
        """.trimIndent()

        try {
            val (aiRaw, _) = executeWithModelFallback(prompt, userProfile?.customApiKey)
            if (!aiRaw.isNullOrBlank()) {
                val isApproved = aiRaw.contains("ESTADO: APROBADA", ignoreCase = true) || 
                                (!aiRaw.contains("ESTADO: RECHAZADA", ignoreCase = true) && cleanProof.length > 20)
                val feedback = if (aiRaw.contains("FEEDBACK:", ignoreCase = true)) {
                    aiRaw.substringAfter("FEEDBACK:").trim()
                } else {
                    aiRaw.lines().filterNot { it.contains("ESTADO:", ignoreCase = true) }.joinToString(" ").trim()
                }

                val bonus = if (isApproved) (if (cleanProof.length > 50 || hasPhotoEvidence) 25 else 15) else 0
                TaskVerificationResult(
                    isApproved = isApproved,
                    feedbackMessage = feedback.ifBlank { if (isApproved) "¡Excelente trabajo! Tarea aprobada." else "Evidencia insuficiente para validar la tarea." },
                    bonusXp = bonus,
                    gradeTitle = if (isApproved) "¡Tarea Aprobada por Lupo IA! ✨" else "⚠️ Requiere Mejorar Evidencia"
                )
            } else {
                getSmartOfflineVerification(taskTitle, subject, cleanProof, hasPhotoEvidence, studentName)
            }
        } catch (e: Exception) {
            getSmartOfflineVerification(taskTitle, subject, cleanProof, hasPhotoEvidence, studentName)
        }
    }

    private fun getSmartOfflineVerification(
        taskTitle: String,
        subject: String,
        proof: String,
        hasPhoto: Boolean,
        studentName: String
    ): TaskVerificationResult {
        val lower = proof.lowercase()
        val isMeaningful = (proof.length >= 15 && !lower.matches(Regex("^(hola|ya|si|no|listo|ok|terminado)+$"))) || hasPhoto
        if (isMeaningful) {
            val feedback = "¡Auuu $studentName! Has demostrado compromiso en tu tarea de $subject: \"$taskTitle\". Tu evidencia ha sido verificada y aprobada por Lupo. ¡Sigue con esa gran disciplina!"
            return TaskVerificationResult(
                isApproved = true,
                feedbackMessage = feedback,
                bonusXp = if (hasPhoto) 25 else 15,
                gradeTitle = "¡Tarea Aprobada por Lupo!"
            )
        } else {
            return TaskVerificationResult(
                isApproved = false,
                feedbackMessage = "Lupo necesita más detalle para validar tu tarea. Por favor incluye una breve explicación de tu procedimiento, fórmulas utilizadas o una foto de tu apunte.",
                bonusXp = 0,
                gradeTitle = "⚠️ Evidencia Insuficiente"
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
            summary = "Lección estructurada sobre $topic en el área de $subject ($level). Diseñada para organizar tu estudio con explicaciones paso a paso, conceptos clave y ejercicios de repaso.",
            content = """
                # $topic en $subject
                
                ## 1. Introducción y Contexto
                El tema de **$topic** es un pilar fundamental en **$subject**. Comprender sus bases te permite resolver ejercicios complejos y conectar ideas con aplicaciones del mundo real.
                
                ## 2. Conceptos Centrales y Principios
                - **Definición**: Explicación sistemática de los componentes de $topic.
                - **Mecanismos y Fórmulas**: Cómo interactúan los elementos y qué leyes o reglas los rigen.
                - **Ejemplo Práctico**: Caso ilustrativo resuelto paso a paso para afianzar el aprendizaje.
                
                ## 3. Estrategias de Dominio y Estudio Activo
                Aplica la técnica de práctica activa: intenta resumir este contenido sin mirar tus notas y formula preguntas de autoevaluación.
            """.trimIndent(),
            keyPoints = "• Definición clara y contexto de $topic\n• Pasos clave para la resolución de ejercicios\n• Fórmulas o conceptos esenciales de $subject\n• Evitar errores comunes en exámenes",
            reviewQuiz = "¿Cuál es la idea principal de $topic y cómo se aplica en $subject?"
        )
    }

    private fun getRichOfflineLupoResponse(query: String, studentName: String, subjectContext: String): String {
        val lower = query.lowercase()
        return when {
            "hola" in lower || "buenos" in lower || "salud" in lower ->
                "¡Auuu $studentName! 🐺 ¡Qué gusto saludarte! Estoy listo para ayudarte con tus tareas, explicarte lecciones o planificar tu horario de estudio. ¿En qué materia empezamos hoy?"

            "matem" in lower || "calcul" in lower || "algebra" in lower || "integral" in lower || "derivad" in lower || "ecuac" in lower ->
                "🐺 **Consejo de Matemáticas para $studentName:**\n1. Escribe siempre la fórmula general antes de sustituir valores.\n2. Divide el problema en pasos pequeños: Datos conocidos ➡️ Incógnita ➡️ Procedimiento ➡️ Comprobación.\n3. Si un paso te confunde, comprueba los signos (+/-) que es el error más común."

            "fisic" in lower || "quimic" in lower || "biolog" in lower || "cienc" in lower ->
                "🐺 **Método Científico de Estudio:**\n• Relaciona cada concepto con un fenómeno cotidiano (por ejemplo, la inercia al frenar un auto).\n• Memoriza las unidades de medida (SI) de cada variable para no perder puntos en problemas numéricos."

            "histori" in lower || "geograf" in lower || "literatur" in lower || "filosof" in lower ->
                "🐺 **Técnica de Aprendizaje para Humanidades:**\n• No memorices fechas aisladas; crea una línea de tiempo con causas y consecuencias.\n• Utiliza mapas mentales asociando personajes clave con sus ideas principales."

            "estudi" in lower || "repas" in lower || "exam" in lower || "memoriz" in lower ->
                "🐺 **Técnica Feynman & Pomodoro:**\n1. Estudia en bloques de 25 minutos de enfoque total sin celular.\n2. Luego, toma 5 minutos de descanso.\n3. Explica el tema en voz alta con tus propias palabras. Si te trabas en una parte, ¡ese es justo el punto que debes repasar!"

            "tarea" in lower || "problema" in lower || "ejercic" in lower || "ayuda" in lower ->
                "🐺 ¡Vamos a resolver esa tarea juntos! Dime cuál es el enunciado o tema específico y te guiaré paso a paso para que lo domines por ti mismo."

            "cansad" in lower || "flojera" in lower || "motiva" in lower || "estres" in lower || "bloque" in lower ->
                "¡Respira hondo, $studentName! 🌟 El cansancio es normal, pero recuerda tu meta. Tómate un vaso de agua fresca, estira los brazos 2 minutos y retomamos con una sola tarea pequeña a la vez. ¡Tú puedes!"

            else ->
                "¡Auuu! 🐺 He recibido tu consulta sobre ${if (subjectContext.isNotBlank()) subjectContext else "tus estudios"}. ¿Te gustaría que te desglose el tema paso a paso, te dé ejemplos prácticos o te ayude a armar una guía de repaso?"
        }
    }

    suspend fun generateFlashcardsWithAi(
        subject: String,
        topic: String,
        content: String = "",
        userProfile: UserProfileEntity? = null
    ): List<GeneratedFlashcard> = withContext(Dispatchers.IO) {
        val level = userProfile?.educationLevel ?: "Preparatoria"
        val prompt = """
            Eres Lupo, el tutor inteligente de estudio. Genera exactamente 5 tarjetas de repaso (Flashcards) de alta calidad pedagógica sobre el tema '$topic' en la materia '$subject' para un estudiante de nivel '$level'.
            ${if (content.isNotBlank()) "Basándote también en este contenido de la lección:\n$content" else ""}

            Devuelve cada tarjeta en el siguiente formato estricto:
            ---TARJETA---
            PREGUNTA: [Pregunta o concepto a definir]
            RESPUESTA: [Respuesta clara, precisa y directa]
            PISTA: [Pista o mnemotecnia útil para recordar]
            ---FIN_TARJETA---
        """.trimIndent()

        val (aiResponse, _) = executeWithModelFallback(prompt, userProfile?.customApiKey)
        if (!aiResponse.isNullOrBlank()) {
            val parsed = parseFlashcards(aiResponse)
            if (parsed.isNotEmpty()) return@withContext parsed
        }

        getOfflineFlashcards(topic, subject)
    }

    suspend fun generateQuizWithAi(
        subject: String,
        topic: String,
        content: String = "",
        userProfile: UserProfileEntity? = null
    ): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val level = userProfile?.educationLevel ?: "Preparatoria"
        val prompt = """
            Eres Lupo, el tutor inteligente de estudio. Genera un simulacro de examen / quiz de 3 preguntas de opción múltiple sobre el tema '$topic' de la materia '$subject' para un estudiante de '$level'.
            ${if (content.isNotBlank()) "Basándote en este apunte:\n$content" else ""}

            Para cada pregunta proporciona 4 opciones (A, B, C, D), indica el índice correcto (0 para A, 1 para B, 2 para C, 3 para D) y una explicación pedagógica.
            
            Usa este formato estricto:
            ---PREGUNTA---
            ENUNCIADO: [Pregunta clara de examen]
            OPCION_A: [Opción A]
            OPCION_B: [Opción B]
            OPCION_C: [Opción C]
            OPCION_D: [Opción D]
            CORRECTA: [0, 1, 2, o 3]
            EXPLICACION: [Por qué es la respuesta correcta y consejo de estudio de Lupo]
            ---FIN_PREGUNTA---
        """.trimIndent()

        val (aiResponse, _) = executeWithModelFallback(prompt, userProfile?.customApiKey)
        if (!aiResponse.isNullOrBlank()) {
            val parsed = parseQuiz(aiResponse)
            if (parsed.isNotEmpty()) return@withContext parsed
        }

        getOfflineQuiz(topic, subject)
    }

    private fun parseFlashcards(rawText: String): List<GeneratedFlashcard> {
        val list = mutableListOf<GeneratedFlashcard>()
        val blocks = rawText.split("---TARJETA---")
        for (block in blocks) {
            val clean = block.substringBefore("---FIN_TARJETA---").trim()
            if (clean.isBlank()) continue

            var q = ""
            var a = ""
            var hint = ""

            clean.lines().forEach { line ->
                val l = line.trim()
                when {
                    l.startsWith("PREGUNTA:", ignoreCase = true) -> q = l.substringAfter(":").trim()
                    l.startsWith("RESPUESTA:", ignoreCase = true) -> a = l.substringAfter(":").trim()
                    l.startsWith("PISTA:", ignoreCase = true) -> hint = l.substringAfter(":").trim()
                }
            }

            if (q.isNotBlank() && a.isNotBlank()) {
                list.add(GeneratedFlashcard(question = q, answer = a, hint = hint))
            }
        }
        return list
    }

    private fun parseQuiz(rawText: String): List<QuizQuestion> {
        val list = mutableListOf<QuizQuestion>()
        val blocks = rawText.split("---PREGUNTA---")
        var idCounter = 1
        for (block in blocks) {
            val clean = block.substringBefore("---FIN_PREGUNTA---").trim()
            if (clean.isBlank()) continue

            var statement = ""
            var opA = ""
            var opB = ""
            var opC = ""
            var opD = ""
            var correct = 0
            var explanation = ""

            clean.lines().forEach { line ->
                val l = line.trim()
                when {
                    l.startsWith("ENUNCIADO:", ignoreCase = true) -> statement = l.substringAfter(":").trim()
                    l.startsWith("OPCION_A:", ignoreCase = true) -> opA = l.substringAfter(":").trim()
                    l.startsWith("OPCION_B:", ignoreCase = true) -> opB = l.substringAfter(":").trim()
                    l.startsWith("OPCION_C:", ignoreCase = true) -> opC = l.substringAfter(":").trim()
                    l.startsWith("OPCION_D:", ignoreCase = true) -> opD = l.substringAfter(":").trim()
                    l.startsWith("CORRECTA:", ignoreCase = true) -> {
                        val numStr = l.substringAfter(":").trim()
                        correct = numStr.filter { it.isDigit() }.toIntOrNull() ?: 0
                    }
                    l.startsWith("EXPLICACION:", ignoreCase = true) -> explanation = l.substringAfter(":").trim()
                }
            }

            if (statement.isNotBlank() && opA.isNotBlank() && opB.isNotBlank()) {
                val options = listOf(
                    opA,
                    opB,
                    if (opC.isNotBlank()) opC else "Opción C",
                    if (opD.isNotBlank()) opD else "Opción D"
                )
                list.add(
                    QuizQuestion(
                        id = idCounter++,
                        question = statement,
                        options = options,
                        correctIndex = correct.coerceIn(0, options.size - 1),
                        explanation = explanation.ifBlank { "¡Excelente respuesta analizada por Lupo IA!" }
                    )
                )
            }
        }
        return list
    }

    private fun getOfflineFlashcards(topic: String, subject: String): List<GeneratedFlashcard> {
        return listOf(
            GeneratedFlashcard(
                question = "¿Cuál es el concepto principal de $topic en $subject?",
                answer = "Es el principio fundamental que explica cómo interactúan los elementos y variables en $subject.",
                hint = "Recuerda la definición base que vimos en la lección."
            ),
            GeneratedFlashcard(
                question = "¿Cuál es la aplicación práctica o fórmula de $topic?",
                answer = "Permite calcular, predecir o resolver problemas clave siguiendo un procedimiento sistemático.",
                hint = "Aplica los pasos en orden lógico sin saltarte operaciones."
            ),
            GeneratedFlashcard(
                question = "¿Qué error común se debe evitar al resolver ejercicios de $topic?",
                answer = "Confundir las unidades de medida o saltarse la comprobación de los signos y resultados.",
                hint = "Revisa siempre el resultado final con sentido común."
            ),
            GeneratedFlashcard(
                question = "¿Cómo se relaciona $topic con otros temas de $subject?",
                answer = "Sirve como base para temas avanzados y conecta teorías con situaciones cotidianas reales.",
                hint = "Piensa en un ejemplo del mundo real."
            ),
            GeneratedFlashcard(
                question = "¿Cuál es la conclusión clave sobre $topic?",
                answer = "Dominar $topic te permite resolver exámenes con confianza y rapidez.",
                hint = "La práctica constante consolida la memoria a largo plazo."
            )
        )
    }

    private fun getOfflineQuiz(topic: String, subject: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = 1,
                question = "¿Cuál es el objetivo principal al estudiar '$topic' en $subject?",
                options = listOf(
                    "Comprender sus fundamentos y aplicaciones prácticas",
                    "Memorizar sin entender los conceptos",
                    "Omitir las fórmulas y procedimientos",
                    "Solo leer sin hacer ejercicios"
                ),
                correctIndex = 0,
                explanation = "Comprender los fundamentos y ejercitar su aplicación es el método más efectivo según Lupo."
            ),
            QuizQuestion(
                id = 2,
                question = "Al resolver un problema de $topic, ¿cuál es el primer paso recomendado?",
                options = listOf(
                    "Adivinar la respuesta de inmediato",
                    "Identificar los datos conocidos y la incógnita a resolver",
                    "Borrar el enunciado del problema",
                    "Pasar a otra materia sin intentar"
                ),
                correctIndex = 1,
                explanation = "Organizar los datos conocidos y la incógnita evita errores en un 80% de los casos."
            ),
            QuizQuestion(
                id = 3,
                question = "¿Cómo puedes comprobar que dominas $topic?",
                options = listOf(
                    "Solo mirando las respuestas del libro",
                    "Explicándolo con tus propias palabras a otra persona (Técnica Feynman)",
                    "Estudiando 5 minutos antes del examen",
                    "Memorizando una sola fórmula"
                ),
                correctIndex = 1,
                explanation = "La técnica Feynman de explicar el tema con tus palabras consolida el aprendizaje profundo."
            )
        )
    }

    /**
     * Generación de Cursos y Rutas de Aprendizaje Personalizadas con Lupo IA
     */
    suspend fun generateCourseWithAi(
        subject: String,
        goal: String,
        level: String,
        weeks: Int,
        userProfile: UserProfileEntity? = null
    ): CourseEntity = withContext(Dispatchers.IO) {
        val cleanSubject = subject.ifBlank { "Materia General" }
        val cleanGoal = goal.ifBlank { "Dominar los fundamentos de $cleanSubject" }
        val cleanLevel = level.ifBlank { userProfile?.educationLevel ?: "Preparatoria" }
        val prompt = """
            Eres Lupo, el tutor inteligente y diseñador de rutas de aprendizaje. Crea un curso de estudio estructurado y personalizado para:
            - Materia: "$cleanSubject"
            - Objetivo del estudiante: "$cleanGoal"
            - Nivel educativo: "$cleanLevel"
            - Duración estimada: $weeks semanas

            Genera exactamente 4 módulos secuenciales y progresivos. Devuelve la información en el siguiente formato estricto:
            TITULO_CURSO: [Título motivador del curso]
            DESCRIPCION: [Breve descripción clara de 2 líneas]
            HORAS_TOTALES: [Número de horas estimadas, ej. 8]
            
            ---MODULO---
            NUMERO: 1
            TITULO: [Título del Módulo 1]
            RESUMEN: [Resumen pedagógico del módulo]
            CONCEPTOS: [Concepto 1 | Concepto 2 | Concepto 3]
            ---FIN_MODULO---
            
            ---MODULO---
            NUMERO: 2
            TITULO: [Título del Módulo 2]
            RESUMEN: [Resumen pedagógico del módulo]
            CONCEPTOS: [Concepto 1 | Concepto 2 | Concepto 3]
            ---FIN_MODULO---
            
            ---MODULO---
            NUMERO: 3
            TITULO: [Título del Módulo 3]
            RESUMEN: [Resumen pedagógico del módulo]
            CONCEPTOS: [Concepto 1 | Concepto 2 | Concepto 3]
            ---FIN_MODULO---
            
            ---MODULO---
            NUMERO: 4
            TITULO: [Título del Módulo 4]
            RESUMEN: [Resumen pedagógico del módulo]
            CONCEPTOS: [Concepto 1 | Concepto 2 | Concepto 3]
            ---FIN_MODULO---
        """.trimIndent()

        val (rawResponse, _) = executeWithModelFallback(prompt, userProfile?.customApiKey)
        if (!rawResponse.isNullOrBlank()) {
            val parsedCourse = parseCourseResponse(cleanSubject, cleanGoal, cleanLevel, rawResponse)
            if (parsedCourse != null) return@withContext parsedCourse
        }

        getOfflineCourse(cleanSubject, cleanGoal, cleanLevel, weeks)
    }

    private fun parseCourseResponse(
        subject: String,
        goal: String,
        level: String,
        rawText: String
    ): CourseEntity? {
        try {
            var title = "Curso de $subject"
            var description = "Ruta de aprendizaje personalizada para $goal."
            var hours = 8

            val lines = rawText.lines()
            for (line in lines) {
                val l = line.trim()
                when {
                    l.startsWith("TITULO_CURSO:", ignoreCase = true) -> title = l.substringAfter(":").trim()
                    l.startsWith("DESCRIPCION:", ignoreCase = true) -> description = l.substringAfter(":").trim()
                    l.startsWith("HORAS_TOTALES:", ignoreCase = true) -> {
                        hours = l.substringAfter(":").trim().filter { it.isDigit() }.toIntOrNull() ?: 8
                    }
                }
            }

            val modules = mutableListOf<CourseModuleItem>()
            val moduleBlocks = rawText.split("---MODULO---")
            var idCounter = 1

            for (block in moduleBlocks) {
                val clean = block.substringBefore("---FIN_MODULO---").trim()
                if (clean.isBlank()) continue

                var mTitle = ""
                var mSummary = ""
                val mConcepts = mutableListOf<String>()

                clean.lines().forEach { line ->
                    val l = line.trim()
                    when {
                        l.startsWith("TITULO:", ignoreCase = true) -> mTitle = l.substringAfter(":").trim()
                        l.startsWith("RESUMEN:", ignoreCase = true) -> mSummary = l.substringAfter(":").trim()
                        l.startsWith("CONCEPTOS:", ignoreCase = true) -> {
                            val rawConcepts = l.substringAfter(":").split("|")
                            mConcepts.addAll(rawConcepts.map { it.trim() }.filter { it.isNotBlank() })
                        }
                    }
                }

                if (mTitle.isNotBlank()) {
                    modules.add(
                        CourseModuleItem(
                            id = idCounter++,
                            title = mTitle,
                            summary = mSummary.ifBlank { "Aprenderás conceptos y ejercicios clave." },
                            keyPoints = if (mConcepts.isNotEmpty()) mConcepts else listOf("Fundamentos", "Ejercicios", "Autoevaluación"),
                            isCompleted = false
                        )
                    )
                }
            }

            if (modules.isNotEmpty()) {
                val jsonModules = Json.encodeToString(modules)
                return CourseEntity(
                    title = title,
                    subject = subject,
                    description = description,
                    level = level,
                    estimatedHours = hours,
                    totalLessons = modules.size,
                    completedLessons = 0,
                    isCustom = true,
                    createdWithAi = true,
                    syllabusJson = jsonModules,
                    colorHex = "#6366F1"
                )
            }
        } catch (e: Exception) {
            // Fallback to offline course
        }
        return null
    }

    fun getOfflineCourse(
        subject: String,
        goal: String,
        level: String,
        weeks: Int
    ): CourseEntity {
        val modules = listOf(
            CourseModuleItem(
                id = 1,
                title = "Fundamentos y Conceptos Clave de $subject",
                summary = "Introducción sistemática y comprensión de los pilares principales.",
                keyPoints = listOf("Definiciones base", "Leyes o fórmulas iniciales", "Ejemplos introductorios"),
                isCompleted = false
            ),
            CourseModuleItem(
                id = 2,
                title = "Mecanismos y Resolución de Problemas",
                summary = "Técnicas prácticas para resolver ejercicios típicos de examen paso a paso.",
                keyPoints = listOf("Identificación de datos", "Procedimientos estándar", "Comprobación de respuestas"),
                isCompleted = false
            ),
            CourseModuleItem(
                id = 3,
                title = "Aplicaciones Prácticas y Casos Complejos",
                summary = "Conexión de conceptos teóricos con problemas avanzados y casos reales.",
                keyPoints = listOf("Problemas combinados", "Trampas comunes en evaluaciones", "Mnemotecnias"),
                isCompleted = false
            ),
            CourseModuleItem(
                id = 4,
                title = "Repaso Maestro y Simulacro Final",
                summary = "Consolidación de todo lo aprendido con tarjetas de memoria y simulacro de evaluación.",
                keyPoints = listOf("Resumen Feynman", "Flashcards de repaso", "Examen de dominio final"),
                isCompleted = false
            )
        )

        return CourseEntity(
            title = "Curso Maestro de $subject",
            subject = subject,
            description = "Ruta de estudio personalizada creada por Lupo para $goal ($level).",
            level = level,
            estimatedHours = (weeks * 2).coerceAtLeast(6),
            totalLessons = modules.size,
            completedLessons = 0,
            isCustom = true,
            createdWithAi = false,
            syllabusJson = Json.encodeToString(modules),
            colorHex = "#6366F1"
        )
    }
}

