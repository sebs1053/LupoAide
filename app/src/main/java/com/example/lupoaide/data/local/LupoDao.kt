package com.example.lupoaide.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LupoDao {

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Int)

    // Timetable
    @Query("SELECT * FROM timetable_slots ORDER BY startTime ASC")
    fun getAllTimetableSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getSlotsByDay(day: String): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity)

    @Delete
    suspend fun deleteTimetableSlot(slot: TimetableSlotEntity)

    // Study Contracts
    @Query("SELECT * FROM study_contracts ORDER BY isFulfilled ASC, id DESC")
    fun getAllContracts(): Flow<List<StudyContractEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: StudyContractEntity)

    @Update
    suspend fun updateContract(contract: StudyContractEntity)

    @Query("DELETE FROM study_contracts WHERE id = :id")
    suspend fun deleteContract(id: Int)

    // Backpack
    @Query("SELECT * FROM backpack_items ORDER BY id ASC")
    fun getAllBackpackItems(): Flow<List<BackpackItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackpackItem(item: BackpackItemEntity)

    @Update
    suspend fun updateBackpackItem(item: BackpackItemEntity)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
