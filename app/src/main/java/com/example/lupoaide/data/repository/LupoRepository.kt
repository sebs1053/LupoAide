package com.example.lupoaide.data.repository

import com.example.lupoaide.data.local.*
import kotlinx.coroutines.flow.Flow

class LupoRepository(private val dao: LupoDao) {

    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val allSlots: Flow<List<TimetableSlotEntity>> = dao.getAllTimetableSlots()
    val allContracts: Flow<List<StudyContractEntity>> = dao.getAllContracts()
    val backpackItems: Flow<List<BackpackItemEntity>> = dao.getAllBackpackItems()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    fun getSlotsForDay(day: String): Flow<List<TimetableSlotEntity>> = dao.getSlotsByDay(day)

    suspend fun addTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Int) = dao.deleteTask(id)

    suspend fun completeTask(task: TaskEntity, currentProfile: UserProfileEntity?) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        dao.updateTask(updatedTask)

        if (updatedTask.isCompleted && currentProfile != null) {
            val addedXp = task.xpReward
            val addedCoins = task.coinReward
            var newXp = currentProfile.currentXp + addedXp
            var newLevel = currentProfile.level
            var newTargetXp = currentProfile.targetXp

            while (newXp >= newTargetXp) {
                newXp -= newTargetXp
                newLevel += 1
                newTargetXp = (newTargetXp * 1.3).toInt()
            }

            val updatedProfile = currentProfile.copy(
                level = newLevel,
                currentXp = newXp,
                targetXp = newTargetXp,
                coins = currentProfile.coins + addedCoins
            )
            dao.saveUserProfile(updatedProfile)
        }
    }

    suspend fun addSlot(slot: TimetableSlotEntity) = dao.insertTimetableSlot(slot)
    suspend fun deleteSlot(slot: TimetableSlotEntity) = dao.deleteTimetableSlot(slot)

    suspend fun addContract(contract: StudyContractEntity) = dao.insertContract(contract)
    suspend fun updateContract(contract: StudyContractEntity) = dao.updateContract(contract)
    suspend fun deleteContract(id: Int) = dao.deleteContract(id)

    suspend fun useItem(item: BackpackItemEntity, currentProfile: UserProfileEntity?) {
        if (item.quantity > 0) {
            val updatedItem = item.copy(quantity = item.quantity - 1)
            if (updatedItem.quantity <= 0) {
                dao.updateBackpackItem(updatedItem.copy(quantity = 0))
            } else {
                dao.updateBackpackItem(updatedItem)
            }
            if (currentProfile != null && item.category == "Food") {
                val newHunger = (currentProfile.lupoHunger + 25).coerceAtMost(100)
                dao.saveUserProfile(currentProfile.copy(lupoHunger = newHunger, lupoMood = "Very Happy!"))
            }
        }
    }

    suspend fun updateProfile(profile: UserProfileEntity) = dao.saveUserProfile(profile)
}
