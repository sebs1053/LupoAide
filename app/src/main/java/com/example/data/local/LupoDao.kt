package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LupoDao {

    // Courses
    @Query("SELECT * FROM courses ORDER BY createdAtMillis DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Query("UPDATE courses SET completedNodes = :completedNodes, progress = :progress WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: Long, completedNodes: Int, progress: Float)

    // Course Nodes
    @Query("SELECT * FROM course_nodes WHERE courseId = :courseId ORDER BY id ASC")
    fun getNodesForCourse(courseId: Long): Flow<List<CourseNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseNodes(nodes: List<CourseNodeEntity>)

    @Query("UPDATE course_nodes SET isCompleted = :isCompleted WHERE id = :nodeId")
    suspend fun updateNodeCompletion(nodeId: Long, isCompleted: Boolean)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    // Timetable
    @Query("SELECT * FROM timetable ORDER BY dayOfWeek ASC, timeSlot ASC")
    fun getTimetable(): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableItem(item: TimetableEntity)

    @Query("DELETE FROM timetable WHERE id = :id")
    suspend fun deleteTimetableItem(id: Long)

    // User Stats
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity)

    // Blacklisted Apps
    @Query("SELECT * FROM blacklisted_apps")
    fun getBlacklistedApps(): Flow<List<BlacklistedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklistedApp(app: BlacklistedAppEntity)

    @Query("UPDATE blacklisted_apps SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateAppBlockStatus(packageName: String, isBlocked: Boolean)

    // Chest Rewards
    @Query("SELECT * FROM chest_rewards ORDER BY isOpened ASC, id DESC")
    fun getChestRewards(): Flow<List<ChestRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChestReward(chest: ChestRewardEntity)

    @Query("UPDATE chest_rewards SET isOpened = 1 WHERE id = :chestId")
    suspend fun openChest(chestId: Long)
}
