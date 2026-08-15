package com.example.lupoaide.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lupoaide.data.local.*
import com.example.lupoaide.data.remote.GeminiStudyService
import com.example.lupoaide.data.repository.LupoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ChatMessage(
    val sender: String, // "Usuario" o "Lupo"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LupoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LupoRepository
    private val geminiService = GeminiStudyService()

    val tasks: StateFlow<List<TaskEntity>>
    val timetableSlots: StateFlow<List<TimetableSlotEntity>>
    val lessons: StateFlow<List<LessonEntity>>
    val backpackMaterials: StateFlow<List<BackpackMaterialEntity>>
    val userProfile: StateFlow<UserProfileEntity?>
    val blockedApps: StateFlow<List<BlockedAppEntity>>
    val activeBlockedApps: StateFlow<List<BlockedAppEntity>>

    // Fecha y día real actual del sistema
    private val todaySpanishDay = getTodayDayOfWeekSpanish()
    private val _selectedDay = MutableStateFlow(todaySpanishDay)
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    val currentDateFormatted: String
        get() {
            return try {
                val now = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
                now.format(formatter).replaceFirstChar { it.uppercase() }
            } catch (e: Exception) {
                "Hoy"
            }
        }

    val tomorrowSpanishDay: String
        get() = getTomorrowDayOfWeekSpanish()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "Lupo",
                message = "¡Auuu! ¡Hola! Soy Lupo, tu compañero y tutor de estudio con IA. ¿En qué materia o tema te gustaría enfocarte hoy?"
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

        lessons = repository.allLessons.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        backpackMaterials = repository.backpackMaterials.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        userProfile = repository.userProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        blockedApps = repository.allBlockedApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeBlockedApps = repository.activeBlockedApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // Configuración Inicial / Onboarding
    fun completeOnboarding(
        username: String,
        country: String,
        educationLevel: String,
        grade: String,
        institution: String,
        additionalInfo: String,
        language: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = (current ?: UserProfileEntity()).copy(
                username = username.ifBlank { "Estudiante" },
                country = country,
                educationLevel = educationLevel,
                grade = grade,
                institution = institution,
                additionalInfo = additionalInfo,
                language = language,
                isOnboardingCompleted = true,
                lupoMood = "¡Listo para estudiar!",
                level = 1,
                currentXp = 0,
                targetXp = 100,
                coins = 20
            )
            repository.updateProfile(updated)
        }
    }

    fun setSelectedDay(day: String) {
        _selectedDay.value = day
    }

    // Tareas / Misiones (Control estricto anti-bugs de EXP)
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task, userProfile.value)
        }
    }

    fun addTask(title: String, description: String, subject: String, xp: Int, coins: Int, dueDate: String, priority: String) {
        viewModelScope.launch {
            repository.addTask(
                TaskEntity(
                    title = title,
                    description = description,
                    subject = subject.ifBlank { "General" },
                    xpReward = xp.coerceAtLeast(10),
                    coinReward = coins.coerceAtLeast(5),
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

    // Horario escolar
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

    // Lecciones / Apuntes
    fun addLesson(title: String, subject: String, summary: String, content: String, keyPoints: String) {
        viewModelScope.launch {
            repository.addLesson(
                LessonEntity(
                    title = title,
                    subject = subject.ifBlank { "General" },
                    summary = summary,
                    content = content,
                    keyPoints = keyPoints,
                    reviewStatus = "Por repasar"
                )
            )
        }
    }

    fun updateLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            repository.updateLesson(lesson)
        }
    }

    fun deleteLesson(id: Int) {
        viewModelScope.launch {
            repository.deleteLesson(id)
        }
    }

    fun recordCompletedStudySession(lessonId: Int, minutesStudied: Int) {
        viewModelScope.launch {
            repository.recordStudySession(lessonId, minutesStudied, userProfile.value)
        }
    }

    // Mochila: Materiales para el día siguiente
    fun addBackpackMaterial(name: String, subject: String, category: String, targetDay: String) {
        viewModelScope.launch {
            repository.addBackpackMaterial(
                BackpackMaterialEntity(
                    name = name,
                    subject = subject,
                    category = category,
                    targetDay = targetDay.ifBlank { tomorrowSpanishDay }
                )
            )
        }
    }

    fun toggleMaterialPacked(material: BackpackMaterialEntity) {
        viewModelScope.launch {
            repository.toggleMaterialPacked(material, userProfile.value)
        }
    }

    fun deleteBackpackMaterial(id: Int) {
        viewModelScope.launch {
            repository.deleteBackpackMaterial(id)
        }
    }

    fun suggestMaterialsFromTomorrowClasses() {
        viewModelScope.launch {
            val tomorrow = tomorrowSpanishDay
            val slotsForTomorrow = timetableSlots.value.filter { it.dayOfWeek.equals(tomorrow, ignoreCase = true) }
            val existing = backpackMaterials.value.map { it.name.lowercase() }

            for (slot in slotsForTomorrow) {
                val materialName = "Cuaderno y Libro de ${slot.subject}"
                if (!existing.contains(materialName.lowercase())) {
                    repository.addBackpackMaterial(
                        BackpackMaterialEntity(
                            name = materialName,
                            subject = slot.subject,
                            category = "Libro / Libreta",
                            targetDay = tomorrow,
                            isPacked = false
                        )
                    )
                }
            }
        }
    }

    fun clearAllMaterials() {
        viewModelScope.launch {
            repository.clearAllMaterials()
        }
    }

    // Chat con Lupo AI
    fun sendMessageToLupo(messageText: String, subjectContext: String = "") {
        if (messageText.isBlank()) return

        val userMsg = ChatMessage(sender = "Usuario", message = messageText)
        _chatMessages.update { it + userMsg }
        _isLupoThinking.value = true

        viewModelScope.launch {
            val response = geminiService.askLupo(
                userQuery = messageText,
                subjectContext = subjectContext,
                userProfile = userProfile.value
            )
            val lupoMsg = ChatMessage(sender = "Lupo", message = response)
            _chatMessages.update { it + lupoMsg }
            _isLupoThinking.value = false
        }
    }

    // Bloqueador de Apps & Plan Gradual
    fun addBlockedApp(
        packageName: String,
        appName: String,
        category: String,
        goalType: String,
        initialMinutes: Int,
        targetMinutes: Int,
        planDays: Int,
        motivation: String
    ) {
        viewModelScope.launch {
            repository.addBlockedApp(
                BlockedAppEntity(
                    packageName = packageName,
                    appName = appName,
                    appCategory = category,
                    isBlocked = true,
                    goalType = goalType,
                    initialDailyLimitMinutes = initialMinutes,
                    targetDailyLimitMinutes = targetMinutes,
                    currentDailyLimitMinutes = initialMinutes,
                    planDurationDays = planDays,
                    startTimestamp = System.currentTimeMillis(),
                    motivationReason = motivation.ifBlank { "Quiero concentrarme en mis estudios y no procrastinar" }
                )
            )
        }
    }

    fun updateBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            repository.updateBlockedApp(app)
        }
    }

    fun toggleAppBlocked(app: BlockedAppEntity) {
        viewModelScope.launch {
            repository.updateBlockedApp(app.copy(isBlocked = !app.isBlocked))
        }
    }

    fun deleteBlockedApp(id: Int) {
        viewModelScope.launch {
            repository.deleteBlockedApp(id)
        }
    }

    fun rewardResistingDistraction(appName: String) {
        viewModelScope.launch {
            repository.rewardResistingDistraction(appName, userProfile.value)
        }
    }

    companion object {
        fun getTodayDayOfWeekSpanish(): String {
            return when (LocalDate.now().dayOfWeek) {
                DayOfWeek.MONDAY -> "Lunes"
                DayOfWeek.TUESDAY -> "Martes"
                DayOfWeek.WEDNESDAY -> "Miércoles"
                DayOfWeek.THURSDAY -> "Jueves"
                DayOfWeek.FRIDAY -> "Viernes"
                DayOfWeek.SATURDAY -> "Sábado"
                DayOfWeek.SUNDAY -> "Domingo"
                null -> "Lunes"
            }
        }

        fun getTomorrowDayOfWeekSpanish(): String {
            return when (LocalDate.now().plusDays(1).dayOfWeek) {
                DayOfWeek.MONDAY -> "Lunes"
                DayOfWeek.TUESDAY -> "Martes"
                DayOfWeek.WEDNESDAY -> "Miércoles"
                DayOfWeek.THURSDAY -> "Jueves"
                DayOfWeek.FRIDAY -> "Viernes"
                DayOfWeek.SATURDAY -> "Sábado"
                DayOfWeek.SUNDAY -> "Domingo"
                null -> "Martes"
            }
        }
    }
}
