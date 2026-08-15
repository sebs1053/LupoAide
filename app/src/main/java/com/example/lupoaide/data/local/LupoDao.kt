package com.example.lupoaide.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LupoDao {

    // Tareas / Misiones
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Int)

    // Horario / Clases
    @Query("SELECT * FROM timetable_slots ORDER BY startTime ASC")
    fun getAllTimetableSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getSlotsByDay(day: String): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity)

    @Delete
    suspend fun deleteTimetableSlot(slot: TimetableSlotEntity)

    // Lecciones / Apuntes
    @Query("SELECT * FROM lessons ORDER BY dateCreated DESC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE subject = :subject ORDER BY dateCreated DESC")
    fun getLessonsBySubject(subject: String): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLesson(id: Int)

    // Mochila / Materiales del día siguiente
    @Query("SELECT * FROM backpack_materials ORDER BY isPacked ASC, id ASC")
    fun getAllBackpackMaterials(): Flow<List<BackpackMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackpackMaterial(material: BackpackMaterialEntity)

    @Update
    suspend fun updateBackpackMaterial(material: BackpackMaterialEntity)

    @Query("DELETE FROM backpack_materials WHERE id = :id")
    suspend fun deleteBackpackMaterial(id: Int)

    @Query("DELETE FROM backpack_materials")
    suspend fun clearAllMaterials()

    // Perfil de Usuario
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Bloqueador de Apps & Plan Gradual
    @Query("SELECT * FROM blocked_apps ORDER BY id DESC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getBlockedAppByPackage(packageName: String): BlockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Update
    suspend fun updateBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE id = :id")
    suspend fun deleteBlockedApp(id: Int)
}
