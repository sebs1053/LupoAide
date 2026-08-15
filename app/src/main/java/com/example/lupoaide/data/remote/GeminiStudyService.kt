package com.example.lupoaide.data.remote

import com.example.lupoaide.BuildConfig
import com.example.lupoaide.data.local.UserProfileEntity
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
