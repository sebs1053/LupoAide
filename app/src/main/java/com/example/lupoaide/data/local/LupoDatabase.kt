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
        LessonEntity::class,
        BackpackMaterialEntity::class,
        UserProfileEntity::class,
        BlockedAppEntity::class
    ],
    version = 4,
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
                        // Perfil inicial limpio para que el usuario configure todo desde cero
                        database.lupoDao().saveUserProfile(
                            UserProfileEntity(
                                id = 1,
                                username = "",
                                level = 1,
                                currentXp = 0,
                                targetXp = 100,
                                coins = 0,
                                lupoMood = "Listo para estudiar",
                                lupoHunger = 100,
                                studyStreak = 1,
                                totalMinutesStudied = 0,
                                language = "Español",
                                country = "México",
                                educationLevel = "Preparatoria / Bachillerato",
                                grade = "1° Semestre / Año",
                                institution = "",
                                additionalInfo = "",
                                isOnboardingCompleted = false
                            )
                        )
                    }
                }
            }
        }
    }
}
