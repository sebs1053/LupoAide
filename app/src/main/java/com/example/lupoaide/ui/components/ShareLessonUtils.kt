package com.example.lupoaide.ui.components

import android.content.Context
import android.content.Intent
import com.example.lupoaide.data.local.LessonEntity

object ShareLessonUtils {
    fun shareLesson(context: Context, lesson: LessonEntity) {
        val shareText = buildString {
            append("🐺 Guía de Estudio LupoAide: ${lesson.title}\n")
            append("📚 Materia: ${lesson.subject}\n")
            append("───────────────────────────────\n\n")
            if (lesson.summary.isNotBlank()) {
                append("📌 RESUMEN:\n${lesson.summary}\n\n")
            }
            if (lesson.keyPoints.isNotBlank()) {
                append("🔑 PUNTOS CLAVE:\n${lesson.keyPoints}\n\n")
            }
            if (lesson.content.isNotBlank()) {
                append("📖 CONTENIDO:\n${lesson.content}\n\n")
            }
            append("───────────────────────────────\n")
            append("Estudiado con LupoAide - Tu asistente escolar con IA 🐺✨")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir Guía de Estudio")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
