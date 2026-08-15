package com.example.data.repository

import com.example.data.local.BlacklistedAppEntity
import com.example.data.local.ChestRewardEntity
import com.example.data.local.CourseEntity
import com.example.data.local.CourseNodeEntity
import com.example.data.local.LupoDao
import com.example.data.local.TaskEntity
import com.example.data.local.TimetableEntity
import com.example.data.local.UserStatsEntity
import kotlinx.coroutines.flow.Flow

class LupoRepository(private val dao: LupoDao) {

    val allCourses: Flow<List<CourseEntity>> = dao.getAllCourses()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val timetable: Flow<List<TimetableEntity>> = dao.getTimetable()
    val userStats: Flow<UserStatsEntity?> = dao.getUserStats()
    val blacklistedApps: Flow<List<BlacklistedAppEntity>> = dao.getBlacklistedApps()
    val chestRewards: Flow<List<ChestRewardEntity>> = dao.getChestRewards()

    fun getCourseNodes(courseId: Long): Flow<List<CourseNodeEntity>> = dao.getNodesForCourse(courseId)

    suspend fun insertCourseWithNodes(
        course: CourseEntity,
        nodes: List<CourseNodeEntity>
    ) {
        val newCourseId = dao.insertCourse(course)
        val preparedNodes = nodes.map { it.copy(courseId = newCourseId) }
        dao.insertCourseNodes(preparedNodes)
    }

    suspend fun completeCourseNode(courseId: Long, nodeId: Long, currentCompletedCount: Int, totalNodes: Int) {
        dao.updateNodeCompletion(nodeId, true)
        val newCount = currentCompletedCount + 1
        val newProgress = (newCount.toFloat() / totalNodes.toFloat()).coerceIn(0f, 1f)
        dao.updateCourseProgress(courseId, newCount, newProgress)
        awardExpAndBones(50, 2)
    }

    suspend fun toggleTaskCompletion(taskId: Long, currentStatus: Boolean) {
        val newStatus = !currentStatus
        dao.updateTaskCompletion(taskId, newStatus)
        if (newStatus) {
            awardExpAndBones(60, 3)
        }
    }

    suspend fun addNewTask(task: TaskEntity) {
        dao.insertTask(task)
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTask(taskId)
    }

    suspend fun addTimetableItem(item: TimetableEntity) {
        dao.insertTimetableItem(item)
    }

    suspend fun deleteTimetableItem(id: Long) {
        dao.deleteTimetableItem(id)
    }

    suspend fun toggleAppBlock(packageName: String, currentBlocked: Boolean) {
        dao.updateAppBlockStatus(packageName, !currentBlocked)
    }

    suspend fun addBlacklistedApp(appName: String, packageName: String) {
        dao.insertBlacklistedApp(
            BlacklistedAppEntity(
                packageName = packageName,
                appName = appName,
                isBlocked = true
            )
        )
    }

    suspend fun openChestReward(chestId: Long, expBonus: Int, bonesBonus: Int) {
        dao.openChest(chestId)
        awardExpAndBones(expBonus, bonesBonus)
    }

    suspend fun awardExpAndBones(expGained: Int, bonesGained: Int) {
        // Retrieve current stats or default
        // We do a manual update on UserStatsEntity id=1
        // (In a real app, we flow or query single row)
    }

    suspend fun updateUserStats(stats: UserStatsEntity) {
        dao.insertOrUpdateUserStats(stats)
    }
}
