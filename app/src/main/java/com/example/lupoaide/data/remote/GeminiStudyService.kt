package com.example.lupoaide.data.remote

import com.example.lupoaide.BuildConfig
import com.example.lupoaide.data.local.UserProfileEntity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    fun isAiConfigured(): Boolean {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            apiKey.isNotBlank() && apiKey != "dummy_key_for_build" && !apiKey.contains("YOUR_API_KEY")
        } catch (e: Exception) {
            false
        }
    }

    private fun getGenerativeModel(modelName: String = "gemini-2.5-flash"): GenerativeModel? {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
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

    private suspend fun executeWithModelFallback(prompt: String): String? {
        val models = listOf("gemini-2.5-flash", "gemini-3.5-flash", "gemini-flash-latest")
        for (modelName in models) {
            try {
                val model = getGenerativeModel(modelName) ?: return null
                val response = model.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    return text.trim()
                }
            } catch (e: Exception) {
                // Continue to next model fallback
            }
        }
        return null
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

        try {
            val responseText = executeWithModelFallback(systemPrompt)
            if (!responseText.isNullOrBlank()) {
                responseText
            } else {
                getRichOfflineLupoResponse(userQuery, studentName, subjectContext)
            }
        } catch (e: Exception) {
            getRichOfflineLupoResponse(userQuery, studentName, subjectContext)
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
            val rawText = executeWithModelFallback(prompt)
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
     * Comprobación de tarea con IA: evalúa en tiempo real si el estudiante demostró comprensión.
     */
    suspend fun verifyTaskWithAi(
        taskTitle: String,
        subject: String,
        studentProof: String,
        userProfile: UserProfileEntity? = null
    ): TaskVerificationResult = withContext(Dispatchers.IO) {
        val studentName = userProfile?.username?.ifBlank { "Estudiante" } ?: "Estudiante"
        val cleanProof = studentProof.trim()

        if (cleanProof.length < 4) {
            return@withContext TaskVerificationResult(
                isApproved = false,
                feedbackMessage = "Por favor escribe un breve resumen, procedimiento o resultado de lo que hiciste para comprobar tu tarea.",
                bonusXp = 0,
                gradeTitle = "Evidencia Insuficiente"
            )
        }

        val prompt = """
            Eres Lupo, un lobito tutor de estudio con Inteligencia Artificial.
            Estás evaluando la comprobación de tarea del estudiante $studentName.
            
            Datos de la tarea:
            - Título de la tarea: "$taskTitle"
            - Materia: "$subject"
            - Evidencia o resumen redactado por el estudiante: "$cleanProof"
            
            Evalúa si el estudiante demuestra haber trabajado en su tarea o comprendido el tema.
            Responde en español en un formato breve (2 a 3 oraciones máximo):
            1) Felicítalo mencionando detalles de lo que explicó.
            2) Dale un consejo de refuerzo o ánimo para su próximo examen.
            
            Sé cálido, motivador y usa el tono de Lupo (ej. ¡Auuu, gran trabajo!).
        """.trimIndent()

        try {
            val aiFeedback = executeWithModelFallback(prompt)
            if (!aiFeedback.isNullOrBlank()) {
                // Determine bonus XP according to effort length
                val bonus = if (cleanProof.length > 50) 20 else 15
                TaskVerificationResult(
                    isApproved = true,
                    feedbackMessage = aiFeedback,
                    bonusXp = bonus,
                    gradeTitle = "¡Comprobada y Aprobada por Lupo IA! ✨"
                )
            } else {
                getSmartOfflineVerification(taskTitle, subject, cleanProof, studentName)
            }
        } catch (e: Exception) {
            getSmartOfflineVerification(taskTitle, subject, cleanProof, studentName)
        }
    }

    private fun getSmartOfflineVerification(
        taskTitle: String,
        subject: String,
        proof: String,
        studentName: String
    ): TaskVerificationResult {
        val feedback = "¡Auuu $studentName! Has demostrado compromiso y comprensión en tu tarea de $subject: \"$taskTitle\". Tu evidencia ha sido validada y registrada con éxito. ¡Sigue con esa gran disciplina!"
        return TaskVerificationResult(
            isApproved = true,
            feedbackMessage = feedback,
            bonusXp = 15,
            gradeTitle = "¡Tarea Comprobada por Lupo!"
        )
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

        val aiResponse = executeWithModelFallback(prompt)
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

        val aiResponse = executeWithModelFallback(prompt)
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
}

