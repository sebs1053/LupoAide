package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BlacklistedAppEntity
import com.example.data.local.ChestRewardEntity
import com.example.data.local.CourseEntity
import com.example.data.local.CourseNodeEntity
import com.example.data.local.LupoDatabase
import com.example.data.local.TaskEntity
import com.example.data.local.TimetableEntity
import com.example.data.local.UserStatsEntity
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiStudyService
import com.example.data.repository.LupoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LupoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LupoDatabase.getDatabase(application, viewModelScope)
    private val repository = LupoRepository(db.lupoDao())

    val userStatsState: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val coursesState: StateFlow<List<CourseEntity>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasksState: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableState: StateFlow<List<TimetableEntity>> = repository.timetable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklistedAppsState: StateFlow<List<BlacklistedAppEntity>> = repository.blacklistedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chestRewardsState: StateFlow<List<ChestRewardEntity>> = repository.chestRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Course for Node Tree View
    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    val selectedCourseId: StateFlow<Long?> = _selectedCourseId.asStateFlow()

    private val _courseNodesState = MutableStateFlow<List<CourseNodeEntity>>(emptyList())
    val courseNodesState: StateFlow<List<CourseNodeEntity>> = _courseNodesState.asStateFlow()

    // Study Contract & Focus Timer State
    private val _isContractActive = MutableStateFlow(false)
    val isContractActive: StateFlow<Boolean> = _isContractActive.asStateFlow()

    private val _contractMinutes = MutableStateFlow(25)
    val contractMinutes: StateFlow<Int> = _contractMinutes.asStateFlow()

    private val _contractRemainingSeconds = MutableStateFlow(25 * 60)
    val contractRemainingSeconds: StateFlow<Int> = _contractRemainingSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Gemini Chat Tutor State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                messageText = "¡Hola! 🐺 Soy Lupo, tu compañero de estudio. ¿Qué materia o tema repasaremos hoy para ganarle a la procrastinación?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // UI Overlays & Dialogs
    val isAiChatOpen = MutableStateFlow(false)
    val isBackpackOpen = MutableStateFlow(false)
    val isAddTaskDialogOpen = MutableStateFlow(false)
    val isAddCourseDialogOpen = MutableStateFlow(false)
    val isAddClassDialogOpen = MutableStateFlow(false)
    val activeOpenedChest = MutableStateFlow<ChestRewardEntity?>(null)

    init {
        // Automatically select first course if available
        viewModelScope.launch {
            coursesState.collect { courses ->
                if (courses.isNotEmpty() && _selectedCourseId.value == null) {
                    selectCourse(courses.first().id)
                }
            }
        }
    }

    fun selectCourse(courseId: Long) {
        _selectedCourseId.value = courseId
        viewModelScope.launch {
            repository.getCourseNodes(courseId).collect { nodes ->
                _courseNodesState.value = nodes
            }
        }
    }

    fun completeNode(node: CourseNodeEntity) {
        if (node.isCompleted) return
        val courseId = node.courseId
        val currentCourse = coursesState.value.find { it.id == courseId } ?: return
        viewModelScope.launch {
            repository.completeCourseNode(
                courseId = courseId,
                nodeId = node.id,
                currentCompletedCount = currentCourse.completedNodes,
                totalNodes = currentCourse.totalNodes
            )
            rewardUser(expGained = 60, bonesGained = 2)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task.id, task.isCompleted)
            if (!task.isCompleted) {
                rewardUser(expGained = task.expReward, bonesGained = 2)
            }
        }
    }

    fun addTask(title: String, subject: String, category: String, priority: String) {
        viewModelScope.launch {
            repository.addNewTask(
                TaskEntity(
                    title = title,
                    subject = subject,
                    category = category,
                    priority = priority,
                    expReward = if (priority == "ALTA") 100 else 50
                )
            )
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun addTimetableClass(dayOfWeek: Int, timeSlot: String, subject: String, room: String, colorHex: String) {
        viewModelScope.launch {
            repository.addTimetableItem(
                TimetableEntity(
                    dayOfWeek = dayOfWeek,
                    timeSlot = timeSlot,
                    subject = subject,
                    room = room,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteTimetableClass(id: Long) {
        viewModelScope.launch {
            repository.deleteTimetableItem(id)
        }
    }

    fun toggleAppBlock(packageName: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleAppBlock(packageName, currentStatus)
        }
    }

    fun addCustomBlockedApp(appName: String) {
        if (appName.isBlank()) return
        val pkg = "com.custom." + appName.lowercase().replace(" ", "")
        viewModelScope.launch {
            repository.addBlacklistedApp(appName, pkg)
        }
    }

    // Study Contract Timer Logic
    fun setContractDuration(minutes: Int) {
        if (_isContractActive.value) return
        _contractMinutes.value = minutes
        _contractRemainingSeconds.value = minutes * 60
    }

    fun startStudyContract() {
        if (_isContractActive.value) return
        _isContractActive.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_contractRemainingSeconds.value > 0 && _isContractActive.value) {
                delay(1000L)
                _contractRemainingSeconds.value -= 1
            }
            if (_contractRemainingSeconds.value <= 0) {
                finishStudyContractSuccess()
            }
        }
    }

    fun cancelStudyContract() {
        timerJob?.cancel()
        _isContractActive.value = false
        _contractRemainingSeconds.value = _contractMinutes.value * 60
    }

    private fun finishStudyContractSuccess() {
        _isContractActive.value = false
        _contractRemainingSeconds.value = _contractMinutes.value * 60
        viewModelScope.launch {
            rewardUser(expGained = 200, bonesGained = 8)
            // Add a new unopened chest
            db.lupoDao().insertChestReward(
                ChestRewardEntity(
                    name = "Cofre del Contrato de ${_contractMinutes.value} min",
                    rarity = "ÉPICO",
                    isOpened = false,
                    expBonus = 180,
                    bonesBonus = 6,
                    rewardText = "¡Cumpliste exitosamente tu sesión de estudio sin distracciones! 🐺⚡"
                )
            )
        }
    }

    // Gemini AI Chat
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(isUser = true, messageText = text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = GeminiStudyService.askLupoTutor(_chatMessages.value, text)
            val aiMsg = ChatMessage(isUser = false, messageText = response)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    // Create New Course via Gemini AI
    fun createCourseWithAi(subject: String, gradeLevel: String, notesMaterial: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val (desc, drafts) = GeminiStudyService.generateCourseTopic(subject, gradeLevel, notesMaterial)
            val newCourse = CourseEntity(
                title = "Curso: $subject",
                subject = subject,
                gradeLevel = gradeLevel,
                description = desc,
                totalNodes = drafts.size,
                completedNodes = 0
            )
            val nodeEntities = drafts.map { draft ->
                CourseNodeEntity(
                    courseId = 0,
                    title = draft.title,
                    nodeType = draft.nodeType,
                    contentText = draft.contentText,
                    optionsJson = draft.optionsJson,
                    correctAnswerIndex = draft.correctAnswerIndex
                )
            }
            repository.insertCourseWithNodes(newCourse, nodeEntities)
            _isAiLoading.value = false
            isAddCourseDialogOpen.value = false
        }
    }

    // Open Chest
    fun openChest(chest: ChestRewardEntity) {
        if (chest.isOpened) return
        activeOpenedChest.value = chest
        viewModelScope.launch {
            repository.openChestReward(chest.id, chest.expBonus, chest.bonesBonus)
            rewardUser(chest.expBonus, chest.bonesBonus)
        }
    }

    private suspend fun rewardUser(expGained: Int, bonesGained: Int) {
        val currentStats = userStatsState.value ?: UserStatsEntity()
        var newExp = currentStats.expPoints + expGained
        var newLevel = currentStats.level
        val expNeeded = newLevel * 200

        if (newExp >= expNeeded) {
            newLevel += 1
            newExp -= expNeeded
        }

        val updated = currentStats.copy(
            expPoints = newExp,
            level = newLevel,
            bonesCount = currentStats.bonesCount + bonesGained,
            streaksCount = currentStats.streaksCount,
            totalFocusMinutes = currentStats.totalFocusMinutes + 15
        )
        repository.updateUserStats(updated)
    }
}
