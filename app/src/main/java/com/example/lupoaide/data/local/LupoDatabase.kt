package com.example.lupoaide.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        TimetableSlotEntity::class,
        StudyContractEntity::class,
        BackpackItemEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LupoDatabase : RoomDatabase() {
    abstract fun lupoDao(): LupoDao

    companion object {
        @Volatile
        private var INSTANCE: LupoDatabase? = null

        fun getDatabase(context: Context): LupoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LupoDatabase::class.java,
                    "lupoaide_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.lupoDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: LupoDao) {
                // Initial Profile
                dao.saveUserProfile(
                    UserProfileEntity(
                        id = 1,
                        username = "Alex Explorer",
                        level = 2,
                        currentXp = 80,
                        targetXp = 150,
                        coins = 240,
                        lupoMood = "Energetic",
                        lupoHunger = 90,
                        studyStreak = 4,
                        totalMinutesStudied = 240
                    )
                )

                // Initial Tasks
                dao.insertTask(
                    TaskEntity(
                        title = "Math: Differential Equations review",
                        description = "Solve problem set #4 (Page 45)",
                        subject = "Math",
                        xpReward = 40,
                        coinReward = 20,
                        dueDate = "Today, 6:00 PM",
                        priority = "High"
                    )
                )
                dao.insertTask(
                    TaskEntity(
                        title = "History: World War II Essay Draft",
                        description = "Complete intro and outline arguments",
                        subject = "History",
                        xpReward = 30,
                        coinReward = 15,
                        dueDate = "Tomorrow",
                        priority = "Medium"
                    )
                )
                dao.insertTask(
                    TaskEntity(
                        title = "Physics: Laboratory report",
                        description = "Analyze harmonic oscillation dataset",
                        subject = "Physics",
                        xpReward = 50,
                        coinReward = 25,
                        dueDate = "Friday",
                        priority = "High"
                    )
                )

                // Initial Timetable
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
                val sampleClasses = listOf(
                    Triple("Advanced Mathematics", "08:30", "10:00"),
                    Triple("Computer Science & AI", "10:15", "11:45"),
                    Triple("Physics & Mechanics", "13:00", "14:30"),
                    Triple("English & Literature", "14:45", "16:15")
                )

                for (day in days) {
                    for (c in sampleClasses) {
                        dao.insertTimetableSlot(
                            TimetableSlotEntity(
                                subject = c.first,
                                dayOfWeek = day,
                                startTime = c.second,
                                endTime = c.third,
                                room = "Hall A-102",
                                teacher = "Prof. Martinez"
                            )
                        )
                    }
                }

                // Initial Contracts
                dao.insertContract(
                    StudyContractEntity(
                        title = "2-Hour Deep Focus Commitment",
                        goalMinutes = 120,
                        targetDays = 5,
                        currentStreak = 3,
                        penaltyDescription = "Give 30 coins to Lupo penalty jar",
                        rewardCoins = 100,
                        rewardXp = 200
                    )
                )

                // Initial Backpack Items
                dao.insertBackpackItem(
                    BackpackItemEntity(
                        name = "Focus Elixir",
                        category = "Potion",
                        iconName = "elixir",
                        description = "+20% XP boost for 1 hour of study",
                        quantity = 3,
                        price = 30
                    )
                )
                dao.insertBackpackItem(
                    BackpackItemEntity(
                        name = "Scholar Glasses",
                        category = "Accessory",
                        iconName = "glasses",
                        description = "Gives Lupo a studious wizard look",
                        quantity = 1,
                        price = 75,
                        isEquipped = true
                    )
                )
                dao.insertBackpackItem(
                    BackpackItemEntity(
                        name = "Energy Berry",
                        category = "Food",
                        iconName = "berry",
                        description = "Restores +30 Lupo energy and happiness",
                        quantity = 5,
                        price = 15
                    )
                )
            }
        }
    }
}
