package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val gradeLevel: String,
    val description: String,
    val progress: Float = 0f,
    val totalNodes: Int = 5,
    val completedNodes: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "course_nodes")
data class CourseNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val nodeType: String, // LESSON, QUIZ, FLASHCARD, SUMMARY
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = true,
    val contentText: String,
    val optionsJson: String = "", // For quizzes e.g. "Option A|Option B|Option C|Option D"
    val correctAnswerIndex: Int = 0
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val category: String, // TAREA, PROYECTO, MATERIAL_MAÑANA
    val isCompleted: Boolean = false,
    val dueDateMillis: Long = System.currentTimeMillis(),
    val expReward: Int = 50,
    val priority: String = "MEDIA" // ALTA, MEDIA, BAJA
)

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1 = Lunes, 5 = Viernes
    val timeSlot: String, // e.g. "08:00 - 09:00"
    val subject: String,
    val room: String,
    val colorHex: String = "#3F51B5"
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Estudiante Lupo",
    val gradeLevel: String = "Preparatoria / Bachillerato",
    val streaksCount: Int = 5,
    val expPoints: Int = 340,
    val level: Int = 3,
    val bonesCount: Int = 12,
    val lupoMood: String = "Motivado", // Motivado, Enfocado, Hambriento, Durmiendo
    val chestsCount: Int = 2,
    val totalFocusMinutes: Int = 125
)

@Entity(tableName = "blacklisted_apps")
data class BlacklistedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val category: String = "Redes Sociales"
)

@Entity(tableName = "chest_rewards")
data class ChestRewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rarity: String, // COMÚN, RARO, ÉPICO, LEGENDARIO
    val isOpened: Boolean = false,
    val expBonus: Int,
    val bonesBonus: Int,
    val rewardText: String
)
