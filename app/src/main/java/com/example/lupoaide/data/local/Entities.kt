package com.example.lupoaide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val subject: String = "General",
    val xpReward: Int = 30,
    val coinReward: Int = 15,
    val isCompleted: Boolean = false,
    val rewardClaimed: Boolean = false, // Evita bug de ganar EXP infinita al desmarcar/marcar
    val isVerified: Boolean = false,     // Comprobación real de que se hizo la tarea
    val verificationProof: String = "",  // Evidencia o respuesta de comprobación del estudiante
    val dueDate: String = "",
    val priority: String = "Media", // Baja, Media, Alta
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val dayOfWeek: String, // Lunes, Martes, Miércoles, Jueves, Viernes, Sábado, Domingo
    val startTime: String, // ej. "08:00"
    val endTime: String,   // ej. "09:30"
    val room: String = "",
    val teacher: String = "",
    val colorHex: String = "#6366F1"
)

@Serializable
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val summary: String = "",
    val content: String = "",
    val keyPoints: String = "",
    val reviewStatus: String = "Por repasar", // Por repasar, En progreso, Dominado
    val dateCreated: Long = System.currentTimeMillis(),
    val studyTimeMinutes: Int = 0
)

@Serializable
@Entity(tableName = "backpack_materials")
data class BackpackMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String = "",
    val isPacked: Boolean = false,
    val category: String = "Útil escolar", // Libro, Libreta, Material especial, Dispositivo, Útil escolar
    val targetDay: String = "" // Día para el que se prepara
)

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "",
    val level: Int = 1,
    val currentXp: Int = 0,
    val targetXp: Int = 100,
    val coins: Int = 0,
    val lupoMood: String = "Listo para estudiar", // Feliz, Enérgico, Listo para estudiar, Concentrado
    val lupoHunger: Int = 100,
    val studyStreak: Int = 1,
    val totalMinutesStudied: Int = 0,
    val language: String = "Español",
    val country: String = "México",
    val educationLevel: String = "Preparatoria / Bachillerato",
    val grade: String = "1° Semestre / Año",
    val institution: String = "",
    val additionalInfo: String = "",
    val isOnboardingCompleted: Boolean = false
)

@Serializable
@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val appCategory: String = "Redes Sociales", // Redes Sociales, Juegos, Streaming, Mensajería, Navegadores, Otro
    val isBlocked: Boolean = true,
    val goalType: String = "Plan Gradual", // "Plan Gradual", "Bloqueo Total", "Límite Diario"
    val initialDailyLimitMinutes: Int = 60,
    val targetDailyLimitMinutes: Int = 0, // 0 = dejar de usar por completo
    val currentDailyLimitMinutes: Int = 30,
    val planDurationDays: Int = 21,
    val startTimestamp: Long = System.currentTimeMillis(),
    val motivationReason: String = "Quiero concentrarme en mis estudios y no procrastinar",
    val dailyUsageMinutesToday: Int = 0,
    val lastResetDate: String = ""
)

