package com.example.lupoaide.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lupoaide.data.local.*
import com.example.lupoaide.data.remote.GeminiStudyService
import com.example.lupoaide.data.repository.LupoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "User" or "Lupo"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LupoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LupoRepository
    private val geminiService = GeminiStudyService()

    val tasks: StateFlow<List<TaskEntity>>
    val timetableSlots: StateFlow<List<TimetableSlotEntity>>
    val contracts: StateFlow<List<StudyContractEntity>>
    val backpackItems: StateFlow<List<BackpackItemEntity>>
    val userProfile: StateFlow<UserProfileEntity?>

    private val _selectedDay = MutableStateFlow("Mon")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "Lupo",
                message = "¡Auuu! ¡Hola! Soy Lupo, tu compañero de estudio. ¿En qué tema quieres que te ayude hoy?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isLupoThinking = MutableStateFlow(false)
    val isLupoThinking: StateFlow<Boolean> = _isLupoThinking.asStateFlow()

    init {
        val database = LupoDatabase.getDatabase(application)
        repository = LupoRepository(database.lupoDao())

        tasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        timetableSlots = repository.allSlots.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        contracts = repository.allContracts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        backpackItems = repository.backpackItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        userProfile = repository.userProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
    }

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.completeTask(task, userProfile.value)
        }
    }

    fun addTask(title: String, description: String, subject: String, xp: Int, coins: Int, dueDate: String, priority: String) {
        viewModelScope.launch {
            repository.addTask(
                TaskEntity(
                    title = title,
                    description = description,
                    subject = subject,
                    xpReward = xp,
                    coinReward = coins,
                    dueDate = dueDate,
                    priority = priority
                )
            )
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun addTimetableSlot(subject: String, day: String, start: String, end: String, room: String, teacher: String) {
        viewModelScope.launch {
            repository.addSlot(
                TimetableSlotEntity(
                    subject = subject,
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    room = room,
                    teacher = teacher
                )
            )
        }
    }

    fun deleteTimetableSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch {
            repository.deleteSlot(slot)
        }
    }

    fun addContract(title: String, goalMinutes: Int, targetDays: Int, penalty: String, rewardCoins: Int, rewardXp: Int) {
        viewModelScope.launch {
            repository.addContract(
                StudyContractEntity(
                    title = title,
                    goalMinutes = goalMinutes,
                    targetDays = targetDays,
                    penaltyDescription = penalty,
                    rewardCoins = rewardCoins,
                    rewardXp = rewardXp
                )
            )
        }
    }

    fun useItem(item: BackpackItemEntity) {
        viewModelScope.launch {
            repository.useItem(item, userProfile.value)
        }
    }

    fun sendMessageToLupo(messageText: String, subjectContext: String = "") {
        if (messageText.isBlank()) return

        val userMsg = ChatMessage(sender = "User", message = messageText)
        _chatMessages.update { it + userMsg }
        _isLupoThinking.value = true

        viewModelScope.launch {
            val response = geminiService.askLupo(messageText, subjectContext)
            val lupoMsg = ChatMessage(sender = "Lupo", message = response)
            _chatMessages.update { it + lupoMsg }
            _isLupoThinking.value = false
        }
    }
}
