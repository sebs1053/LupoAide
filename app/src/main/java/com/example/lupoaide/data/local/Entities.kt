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
    val xpReward: Int = 20,
    val coinReward: Int = 10,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val priority: String = "Medium", // Low, Medium, High
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val dayOfWeek: String, // Mon, Tue, Wed, Thu, Fri, Sat, Sun
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:30"
    val room: String = "",
    val teacher: String = "",
    val colorHex: String = "#6366F1"
)

@Serializable
@Entity(tableName = "study_contracts")
data class StudyContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val goalMinutes: Int = 60,
    val targetDays: Int = 5,
    val currentStreak: Int = 0,
    val isFulfilled: Boolean = false,
    val penaltyDescription: String = "No games for 2 hours",
    val rewardCoins: Int = 50,
    val rewardXp: Int = 100
)

@Serializable
@Entity(tableName = "backpack_items")
data class BackpackItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Potion, Accessory, Food, Badge
    val iconName: String,
    val description: String,
    val quantity: Int = 1,
    val price: Int = 25,
    val isEquipped: Boolean = false
)

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "Student Adventurer",
    val level: Int = 1,
    val currentXp: Int = 60,
    val targetXp: Int = 100,
    val coins: Int = 120,
    val lupoMood: String = "Energetic", // Happy, Energetic, Sleepy, Focused
    val lupoHunger: Int = 85, // 0 - 100
    val studyStreak: Int = 3,
    val totalMinutesStudied: Int = 180
)
