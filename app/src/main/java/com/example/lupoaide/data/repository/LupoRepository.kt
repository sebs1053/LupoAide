package com.example.lupoaide.data.repository

import com.example.lupoaide.data.local.*
import kotlinx.coroutines.flow.Flow

class LupoRepository(private val dao: LupoDao) {

    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val allSlots: Flow<List<TimetableSlotEntity>> = dao.getAllTimetableSlots()
    val allLessons: Flow<List<LessonEntity>> = dao.getAllLessons()
    val backpackMaterials: Flow<List<BackpackMaterialEntity>> = dao.getAllBackpackMaterials()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allBlockedApps: Flow<List<BlockedAppEntity>> = dao.getAllBlockedApps()
    val activeBlockedApps: Flow<List<BlockedAppEntity>> = dao.getActiveBlockedApps()
    val allFlashcards: Flow<List<FlashcardEntity>> = dao.getAllFlashcards()
    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()
    val upcomingExams: Flow<List<ExamEntity>> = dao.getUpcomingExams()

    fun getSlotsForDay(day: String): Flow<List<TimetableSlotEntity>> = dao.getSlotsByDay(day)
    fun getFlashcardsForLesson(lessonId: Int): Flow<List<FlashcardEntity>> = dao.getFlashcardsByLesson(lessonId)

    // Tareas con validación estricta de EXP y Recompensas (sin bugs de duplicación)
    suspend fun addTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Int) = dao.deleteTask(id)

    suspend fun verifyAndCompleteTask(
        task: TaskEntity,
        proofText: String,
        aiFeedback: String,
        bonusXp: Int,
        currentProfile: UserProfileEntity?
    ) {
        val wasAlreadyClaimed = task.rewardClaimed
        val updatedTask = task.copy(
            isCompleted = true,
            rewardClaimed = true,
            isVerified = true,
            verificationProof = proofText,
            aiFeedback = aiFeedback
        )
        dao.updateTask(updatedTask)

        if (!wasAlreadyClaimed && currentProfile != null) {
            val totalXp = task.xpReward + bonusXp
            awardExperienceAndCoins(
                currentProfile = currentProfile,
                earnedXp = totalXp,
                earnedCoins = task.coinReward
            )
        }
    }

    suspend fun toggleTaskCompletion(task: TaskEntity, currentProfile: UserProfileEntity?) {
        val willBeCompleted = !task.isCompleted

        if (willBeCompleted) {
            // Si nunca se había reclamado la recompensa de esta tarea
            if (!task.rewardClaimed && currentProfile != null) {
                val updatedTask = task.copy(isCompleted = true, rewardClaimed = true, isVerified = true)
                dao.updateTask(updatedTask)

                // Otorgar experiencia y monedas de forma segura
                awardExperienceAndCoins(
                    currentProfile = currentProfile,
                    earnedXp = task.xpReward,
                    earnedCoins = task.coinReward
                )
            } else {
                // Ya se había reclamado antes, solo marcar como completada sin dar EXP extra
                dao.updateTask(task.copy(isCompleted = true))
            }
        } else {
            // Desmarcar tarea: no da EXP adicional
            dao.updateTask(task.copy(isCompleted = false))
        }
    }

    // Lecciones y Apuntes
    suspend fun addLesson(lesson: LessonEntity) = dao.insertLesson(lesson)
    suspend fun updateLesson(lesson: LessonEntity) = dao.updateLesson(lesson)
    suspend fun deleteLesson(id: Int) = dao.deleteLesson(id)

    // Registro verificado de tiempo de estudio
    suspend fun recordStudySession(lessonId: Int, minutes: Int, currentProfile: UserProfileEntity?) {
        if (minutes <= 0) return

        if (currentProfile != null) {
            val earnedXp = minutes * 2 // 2 XP por minuto de estudio verificado
            val earnedCoins = (minutes / 5).coerceAtLeast(1)

            val newTotalMinutes = currentProfile.totalMinutesStudied + minutes
            val profileWithMinutes = currentProfile.copy(totalMinutesStudied = newTotalMinutes)
            awardExperienceAndCoins(profileWithMinutes, earnedXp, earnedCoins)
        }
    }

    // Horario
    suspend fun addSlot(slot: TimetableSlotEntity) = dao.insertTimetableSlot(slot)
    suspend fun addMultipleSlots(slots: List<TimetableSlotEntity>) {
        for (slot in slots) {
            dao.insertTimetableSlot(slot)
        }
    }
    suspend fun deleteSlot(slot: TimetableSlotEntity) = dao.deleteTimetableSlot(slot)

    // Mochila y Materiales del día siguiente
    suspend fun addBackpackMaterial(material: BackpackMaterialEntity) = dao.insertBackpackMaterial(material)
    suspend fun updateBackpackMaterial(material: BackpackMaterialEntity) = dao.updateBackpackMaterial(material)
    suspend fun deleteBackpackMaterial(id: Int) = dao.deleteBackpackMaterial(id)
    suspend fun clearAllMaterials() = dao.clearAllMaterials()

    suspend fun toggleMaterialPacked(material: BackpackMaterialEntity, currentProfile: UserProfileEntity?) {
        val updated = material.copy(isPacked = !material.isPacked)
        dao.updateBackpackMaterial(updated)
    }

    // Perfil
    suspend fun updateProfile(profile: UserProfileEntity) = dao.saveUserProfile(profile)

    // Bloqueador de Apps y Plan Gradual
    suspend fun addBlockedApp(app: BlockedAppEntity) = dao.insertBlockedApp(app)
    suspend fun updateBlockedApp(app: BlockedAppEntity) = dao.updateBlockedApp(app)
    suspend fun deleteBlockedApp(id: Int) = dao.deleteBlockedApp(id)
    suspend fun getBlockedAppByPackage(pkg: String) = dao.getBlockedAppByPackage(pkg)

    suspend fun rewardResistingDistraction(appName: String, currentProfile: UserProfileEntity?) {
        if (currentProfile != null) {
            // Recompensar al estudiante por resistir una distracción con +10 XP y +5 monedas
            awardExperienceAndCoins(
                currentProfile = currentProfile,
                earnedXp = 10,
                earnedCoins = 5
            )
        }
    }

    // Flashcards / Tarjetas de Repaso
    suspend fun addFlashcard(flashcard: FlashcardEntity) = dao.insertFlashcard(flashcard)
    suspend fun addFlashcards(flashcards: List<FlashcardEntity>) = dao.insertFlashcards(flashcards)
    suspend fun updateFlashcard(flashcard: FlashcardEntity) = dao.updateFlashcard(flashcard)
    suspend fun deleteFlashcard(id: Int) = dao.deleteFlashcard(id)

    suspend fun recordFlashcardStudy(flashcard: FlashcardEntity, isMastered: Boolean, currentProfile: UserProfileEntity?) {
        val updated = flashcard.copy(
            reviewCount = flashcard.reviewCount + 1,
            isMastered = isMastered,
            lastReviewed = System.currentTimeMillis()
        )
        dao.updateFlashcard(updated)

        if (currentProfile != null && isMastered) {
            awardExperienceAndCoins(currentProfile, earnedXp = 8, earnedCoins = 3)
        }
    }

    // Exámenes y Evaluaciones
    suspend fun addExam(exam: ExamEntity) = dao.insertExam(exam)
    suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    suspend fun deleteExam(id: Int) = dao.deleteExam(id)

    // Tienda de Lupo & Atuendos
    suspend fun buyAndEquipOutfit(outfitId: String, cost: Int, currentProfile: UserProfileEntity) {
        if (currentProfile.coins >= cost) {
            val currentUnlocked = currentProfile.unlockedOutfits.split(",").map { it.trim() }.toMutableSet()
            currentUnlocked.add(outfitId)
            val updated = currentProfile.copy(
                coins = currentProfile.coins - cost,
                activeOutfitId = outfitId,
                unlockedOutfits = currentUnlocked.joinToString(",")
            )
            dao.saveUserProfile(updated)
        }
    }

    suspend fun equipOutfit(outfitId: String, currentProfile: UserProfileEntity) {
        val updated = currentProfile.copy(activeOutfitId = outfitId)
        dao.saveUserProfile(updated)
    }

    // Simulacros / Quizzes de Examen
    suspend fun recordQuizResult(earnedXp: Int, earnedCoins: Int, currentProfile: UserProfileEntity?) {
        if (currentProfile != null && (earnedXp > 0 || earnedCoins > 0)) {
            awardExperienceAndCoins(currentProfile, earnedXp, earnedCoins)
        }
    }

    // Helper interno para cálculo seguro de nivel y recompensas
    private suspend fun awardExperienceAndCoins(
        currentProfile: UserProfileEntity,
        earnedXp: Int,
        earnedCoins: Int
    ) {
        var newXp = currentProfile.currentXp + earnedXp
        var newLevel = currentProfile.level
        var newTargetXp = currentProfile.targetXp

        while (newXp >= newTargetXp) {
            newXp -= newTargetXp
            newLevel += 1
            newTargetXp = (newTargetXp * 1.35).toInt().coerceAtLeast(50)
        }

        val updatedProfile = currentProfile.copy(
            level = newLevel,
            currentXp = newXp,
            targetXp = newTargetXp,
            coins = currentProfile.coins + earnedCoins
        )
        dao.saveUserProfile(updatedProfile)
    }
}
