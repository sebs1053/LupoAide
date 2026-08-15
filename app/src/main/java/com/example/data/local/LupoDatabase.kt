package com.example.data.local

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
        CourseEntity::class,
        CourseNodeEntity::class,
        TaskEntity::class,
        TimetableEntity::class,
        UserStatsEntity::class,
        BlacklistedAppEntity::class,
        ChestRewardEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LupoDatabase : RoomDatabase() {

    abstract fun lupoDao(): LupoDao

    companion object {
        @Volatile
        private var INSTANCE: LupoDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LupoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LupoDatabase::class.java,
                    "lupo_aide_database"
                )
                    .addCallback(LupoDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class LupoDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.lupoDao())
                }
            }
        }

        suspend fun populateInitialData(dao: LupoDao) {
            // Initial User Stats
            dao.insertOrUpdateUserStats(
                UserStatsEntity(
                    id = 1,
                    userName = "Sebastián M.",
                    gradeLevel = "Preparatoria / Hackathon Jalisco",
                    streaksCount = 5,
                    expPoints = 420,
                    level = 3,
                    bonesCount = 18,
                    lupoMood = "Motivado 🐺✨",
                    chestsCount = 3,
                    totalFocusMinutes = 180
                )
            )

            // Default Course 1: Biología Celular
            val course1Id = dao.insertCourse(
                CourseEntity(
                    title = "Biología y Estructura Celular",
                    subject = "Biología",
                    gradeLevel = "Preparatoria",
                    description = "Aprende el funcionamiento de la célula, organelos y respiración celular.",
                    progress = 0.4f,
                    totalNodes = 5,
                    completedNodes = 2
                )
            )

            val nodes1 = listOf(
                CourseNodeEntity(
                    courseId = course1Id,
                    title = "Introducción a la Célula",
                    nodeType = "LESSON",
                    isCompleted = true,
                    contentText = "La célula es la unidad anatómica y funcional de todos los seres vivos. Existen dos tipos principales: procariotas (sin núcleo definido) y eucariotas (con núcleo verdadero y organelos membranosos)."
                ),
                CourseNodeEntity(
                    courseId = course1Id,
                    title = "Quiz: Procariotas vs Eucariotas",
                    nodeType = "QUIZ",
                    isCompleted = true,
                    contentText = "¿Cuál es la característica principal de una célula eucariota?",
                    optionsJson = "Tienen núcleo definido|No tienen ADN|No poseen membrana|Son siempre unicelulares",
                    correctAnswerIndex = 0
                ),
                CourseNodeEntity(
                    courseId = course1Id,
                    title = "Mitocondria y Producción de ATP",
                    nodeType = "LESSON",
                    isCompleted = false,
                    contentText = "La mitocondria es la central energética de la célula. A través de la respiración celular y el Ciclo de Krebs, convierte glucosa y oxígeno en ATP (molécula de energía)."
                ),
                CourseNodeEntity(
                    courseId = course1Id,
                    title = "Simulacro Examen Biología",
                    nodeType = "QUIZ",
                    isCompleted = false,
                    contentText = "¿En qué organelo celular se genera la mayor cantidad de ATP?",
                    optionsJson = "Núcleo|Mitocondria|Ribosoma|Aparato de Golgi",
                    correctAnswerIndex = 1
                ),
                CourseNodeEntity(
                    courseId = course1Id,
                    title = "Tarjetas de Repaso Rápido",
                    nodeType = "FLASHCARD",
                    isCompleted = false,
                    contentText = "Conceptos clave: Membrana plasmática, Mitocondria, Retículo endoplásmico y Núcleo."
                )
            )
            dao.insertCourseNodes(nodes1)

            // Default Course 2: Álgebra y Ecuaciones
            val course2Id = dao.insertCourse(
                CourseEntity(
                    title = "Álgebra Lineal y Cuadrática",
                    subject = "Matemáticas",
                    gradeLevel = "Preparatoria",
                    description = "Métodos de resolución de ecuaciones de primer y segundo grado.",
                    progress = 0.2f,
                    totalNodes = 4,
                    completedNodes = 1
                )
            )

            val nodes2 = listOf(
                CourseNodeEntity(
                    courseId = course2Id,
                    title = "Ecuaciones de Primer Grado",
                    nodeType = "LESSON",
                    isCompleted = true,
                    contentText = "Una ecuación de primer grado tiene la forma ax + b = 0. Se despeja la incógnita x moviendo términos con operaciones inversas."
                ),
                CourseNodeEntity(
                    courseId = course2Id,
                    title = "Fórmula General Cuadrática",
                    nodeType = "LESSON",
                    isCompleted = false,
                    contentText = "Para ax² + bx + c = 0, x = (-b ± √(b² - 4ac)) / (2a). El discriminante (b² - 4ac) determina la cantidad de soluciones reales."
                ),
                CourseNodeEntity(
                    courseId = course2Id,
                    title = "Examen de Práctica Álgebra",
                    nodeType = "QUIZ",
                    isCompleted = false,
                    contentText = "Resuelve para x: 2x + 8 = 16",
                    optionsJson = "x = 4|x = 8|x = 2|x = 6",
                    correctAnswerIndex = 0
                ),
                CourseNodeEntity(
                    courseId = course2Id,
                    title = "Resumen de Fórmulas",
                    nodeType = "SUMMARY",
                    isCompleted = false,
                    contentText = "Compendio de binomios al cuadrado, diferencia de cuadrados y fórmula general."
                )
            )
            dao.insertCourseNodes(nodes2)

            // Default Tasks
            dao.insertTask(
                TaskEntity(
                    title = "Entregar Reporte de Laboratorio de Química",
                    subject = "Química",
                    category = "PROYECTO",
                    isCompleted = false,
                    priority = "ALTA",
                    expReward = 100
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Traer juego de geometría y calculadora científica",
                    subject = "Matemáticas",
                    category = "MATERIAL_MAÑANA",
                    isCompleted = false,
                    priority = "MEDIA",
                    expReward = 40
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Resolver ejercicios de Física Cap. 4 (Ley de Newton)",
                    subject = "Física",
                    category = "TAREA",
                    isCompleted = true,
                    priority = "MEDIA",
                    expReward = 60
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Repasar 15 min de Biología con LupoAide",
                    subject = "Biología",
                    category = "TAREA",
                    isCompleted = false,
                    priority = "ALTA",
                    expReward = 80
                )
            )

            // Default Timetable
            val timetableList = listOf(
                TimetableEntity(dayOfWeek = 1, timeSlot = "08:00 - 09:30", subject = "Biología Celular", room = "Lab B-02", colorHex = "#2A9D8F"),
                TimetableEntity(dayOfWeek = 1, timeSlot = "09:40 - 11:10", subject = "Matemáticas", room = "Aula 104", colorHex = "#E76F51"),
                TimetableEntity(dayOfWeek = 2, timeSlot = "08:00 - 09:30", subject = "Física I", room = "Aula 201", colorHex = "#4EA8DE"),
                TimetableEntity(dayOfWeek = 2, timeSlot = "09:40 - 11:10", subject = "Química Orgánica", room = "Lab Q-01", colorHex = "#F4A261"),
                TimetableEntity(dayOfWeek = 3, timeSlot = "08:00 - 09:30", subject = "Historia Universal", room = "Aula 102", colorHex = "#9B5DE5"),
                TimetableEntity(dayOfWeek = 3, timeSlot = "09:40 - 11:10", subject = "Biología Celular", room = "Lab B-02", colorHex = "#2A9D8F"),
                TimetableEntity(dayOfWeek = 4, timeSlot = "08:00 - 09:30", subject = "Matemáticas", room = "Aula 104", colorHex = "#E76F51"),
                TimetableEntity(dayOfWeek = 4, timeSlot = "09:40 - 11:10", subject = "Inglés Académico", room = "Aula 305", colorHex = "#00BBF9"),
                TimetableEntity(dayOfWeek = 5, timeSlot = "08:00 - 09:30", subject = "Física I", room = "Aula 201", colorHex = "#4EA8DE"),
                TimetableEntity(dayOfWeek = 5, timeSlot = "09:40 - 11:10", subject = "Taller de Redacción", room = "Biblioteca", colorHex = "#00F5D4")
            )
            timetableList.forEach { dao.insertTimetableItem(it) }

            // Default Blacklisted Apps
            val blockedApps = listOf(
                BlacklistedAppEntity("com.zhiliaoapp.musically", "TikTok", true, "Videos Cortos"),
                BlacklistedAppEntity("com.instagram.android", "Instagram", true, "Redes Sociales"),
                BlacklistedAppEntity("com.google.android.youtube", "YouTube Shorts", true, "Videos"),
                BlacklistedAppEntity("com.facebook.katana", "Facebook", true, "Redes Sociales"),
                BlacklistedAppEntity("com.supercell.clashroyale", "Clash Royale", true, "Videojuegos"),
                BlacklistedAppEntity("com.garena.game.kg", "Free Fire", true, "Videojuegos")
            )
            blockedApps.forEach { dao.insertBlacklistedApp(it) }

            // Default Chest Rewards
            dao.insertChestReward(
                ChestRewardEntity(
                    name = "Cofre de Sabiduría de Lupo",
                    rarity = "RARO",
                    isOpened = false,
                    expBonus = 150,
                    bonesBonus = 5,
                    rewardText = "¡Desbloqueaste +150 EXP, 5 Huesos de Lupo 🦴 y la Medalla 'Domador de Distracciones'!"
                )
            )
            dao.insertChestReward(
                ChestRewardEntity(
                    name = "Cofre del Contrato Cumplido",
                    rarity = "ÉPICO",
                    isOpened = false,
                    expBonus = 250,
                    bonesBonus = 10,
                    rewardText = "¡Lograste 45 minutos de estudio enfocado sin distracciones! +250 EXP y 10 Huesos de Lupo."
                )
            )
            dao.insertChestReward(
                ChestRewardEntity(
                    name = "Cofre de Racha Diaria",
                    rarity = "COMÚN",
                    isOpened = true,
                    expBonus = 50,
                    bonesBonus = 2,
                    rewardText = "¡Premios por tu racha de 5 días seguidos!"
                )
            )
        }
    }
}
