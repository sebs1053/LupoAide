package com.example.lupoaide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.lupoaide.data.remote.QuizQuestion
import com.example.lupoaide.ui.components.*
import com.example.lupoaide.ui.screens.*
import com.example.lupoaide.ui.theme.LupoAideTheme
import com.example.lupoaide.ui.viewmodel.LupoViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LupoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LupoAideTheme {
                val profile by viewModel.userProfile.collectAsStateWithLifecycle()
                val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                val slots by viewModel.timetableSlots.collectAsStateWithLifecycle()
                val lessons by viewModel.lessons.collectAsStateWithLifecycle()
                val backpackMaterials by viewModel.backpackMaterials.collectAsStateWithLifecycle()
                val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
                val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
                val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
                val exams by viewModel.exams.collectAsStateWithLifecycle()
                val upcomingExams by viewModel.upcomingExams.collectAsStateWithLifecycle()

                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val isThinking by viewModel.isLupoThinking.collectAsStateWithLifecycle()
                val isGeneratingLesson by viewModel.isGeneratingLesson.collectAsStateWithLifecycle()
                val courses by viewModel.courses.collectAsStateWithLifecycle()
                val isGeneratingCourse by viewModel.isGeneratingCourse.collectAsStateWithLifecycle()
                val aiTestResult by viewModel.aiTestResult.collectAsStateWithLifecycle()
                val isTestingAi by viewModel.isTestingAi.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf(LupoScreen.HOME) }
                var showChatModal by remember { mutableStateOf(false) }
                var showBackpackModal by remember { mutableStateOf(false) }
                var showFlashcardsModal by remember { mutableStateOf(false) }
                var showExamsModal by remember { mutableStateOf(false) }
                var showShopModal by remember { mutableStateOf(false) }

                var activeQuizSubject by remember { mutableStateOf<String?>(null) }
                var activeQuizTopic by remember { mutableStateOf<String?>(null) }
                var activeQuizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
                var isQuizLoading by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                // Si es la primera vez (onboarding no completado), mostrar pantalla de configuración inicial
                if (profile != null && !profile!!.isOnboardingCompleted) {
                    OnboardingScreen(
                        onCompleteOnboarding = { username, country, educationLevel, grade, institution, additionalInfo, language ->
                            viewModel.completeOnboarding(username, country, educationLevel, grade, institution, additionalInfo, language)
                        },
                        onAddInitialScheduleSlot = { subject, day, start, end, room, teacher ->
                            viewModel.addTimetableSlot(subject, day, start, end, room, teacher)
                        },
                        onAddInitialBlockedApp = { packageName, appName, category, goalType, initialMinutes, targetMinutes, planDays, motivation ->
                            viewModel.addBlockedApp(packageName, appName, category, goalType, initialMinutes, targetMinutes, planDays, motivation)
                        }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            GamificationTopBar(
                                profile = profile,
                                currentDateFormatted = viewModel.currentDateFormatted,
                                onLupoClick = { showChatModal = true },
                                onOpenShop = { showShopModal = true }
                            )
                        },
                        bottomBar = {
                            LupoNavigationBar(
                                currentScreen = currentScreen,
                                onScreenSelected = { currentScreen = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                LupoScreen.HOME -> HomeScreen(
                                    profile = profile,
                                    tasks = tasks,
                                    upcomingExams = upcomingExams,
                                    flashcards = flashcards,
                                    tomorrowDay = viewModel.tomorrowSpanishDay,
                                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                                    onVerifyTask = { task, proof ->
                                        viewModel.verifyAndCompleteTask(task, proof)
                                    },
                                    onAddTask = { title, desc, sub, xp, coins, due, priority ->
                                        viewModel.addTask(title, desc, sub, xp, coins, due, priority)
                                    },
                                    onOpenLupoChat = { showChatModal = true },
                                    onOpenBackpack = { showBackpackModal = true },
                                    onOpenExams = { showExamsModal = true },
                                    onOpenFlashcards = { showFlashcardsModal = true },
                                    onOpenShop = { showShopModal = true },
                                    onActivateStreak = { viewModel.activateStreakToday() }
                                )
                                LupoScreen.TIMETABLE -> TimetableScreen(
                                    slots = slots,
                                    selectedDay = selectedDay,
                                    onSelectDay = { viewModel.setSelectedDay(it) },
                                    onAddMultipleSlots = { sub, daysSet, start, end, room, teacher ->
                                        viewModel.addMultipleTimetableSlots(sub, daysSet, start, end, room, teacher)
                                    },
                                    onDeleteSlot = { viewModel.deleteTimetableSlot(it) },
                                    onAddTaskForSubject = { title, desc, sub, xp, coins, due, priority ->
                                        viewModel.addTask(title, desc, sub, xp, coins, due, priority)
                                    }
                                )
                                LupoScreen.TASKS -> TasksScreen(
                                    tasks = tasks,
                                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                                    onVerifyTask = { task, proof ->
                                        viewModel.verifyAndCompleteTask(task, proof)
                                    },
                                    onDeleteTask = { viewModel.deleteTask(it) },
                                    onAddTask = { title, desc, sub, xp, coins, due, priority ->
                                        viewModel.addTask(title, desc, sub, xp, coins, due, priority)
                                    }
                                )
                                LupoScreen.LESSONS -> LessonsScreen(
                                    lessons = lessons,
                                    courses = courses,
                                    isAiConnected = viewModel.isAiConnected(),
                                    isGeneratingLesson = isGeneratingLesson,
                                    isGeneratingCourse = isGeneratingCourse,
                                    onAddLesson = { title, subject, summary, content, keyPoints ->
                                        viewModel.addLesson(title, subject, summary, content, keyPoints)
                                    },
                                    onGenerateAiLesson = { subject, topic ->
                                        viewModel.generateLessonWithAi(subject, topic)
                                    },
                                    onGenerateCourseWithAi = { subject, goal, level, weeks ->
                                        viewModel.generateCourseWithAi(subject, goal, level, weeks)
                                    },
                                    onAddManualCourse = { title, subject, desc, level, hours, modules ->
                                        viewModel.addCourse(title, subject, desc, level, hours, modules)
                                    },
                                    onCompleteCourseLesson = { course ->
                                        viewModel.completeCourseLesson(course)
                                    },
                                    onDeleteCourse = { courseId ->
                                        viewModel.deleteCourse(courseId)
                                    },
                                    onUpdateLesson = { viewModel.updateLesson(it) },
                                    onDeleteLesson = { viewModel.deleteLesson(it) },
                                    onAskLupoAboutLesson = { title, subject ->
                                        viewModel.sendMessageToLupo(
                                            messageText = "Explícame los conceptos clave y cómo estudiar mejor el tema: $title",
                                            subjectContext = subject
                                        )
                                        showChatModal = true
                                    },
                                    onCompleteStudySession = { lessonId, minutes ->
                                        viewModel.recordCompletedStudySession(lessonId, minutes)
                                    },
                                    onGenerateFlashcardsForLesson = { lesson ->
                                        viewModel.generateFlashcardsForLesson(lesson)
                                        showFlashcardsModal = true
                                    },
                                    onStartQuizForLesson = { lesson ->
                                        activeQuizSubject = lesson.subject
                                        activeQuizTopic = lesson.title
                                        isQuizLoading = true
                                        coroutineScope.launch {
                                            val questions = viewModel.getQuizForLesson(lesson)
                                            activeQuizQuestions = questions
                                            isQuizLoading = false
                                        }
                                    },
                                    onOpenAllFlashcards = { showFlashcardsModal = true }
                                )
                                LupoScreen.PROFILE -> ProfileScreen(
                                    profile = profile,
                                    blockedApps = blockedApps,
                                    isAiConnected = viewModel.isAiConnected(),
                                    aiTestResult = aiTestResult,
                                    isTestingAi = isTestingAi,
                                    onTestAiConnection = { viewModel.testAiConnection() },
                                    onClearAiTestResult = { viewModel.clearAiTestResult() },
                                    onSaveApiKey = { apiKey -> viewModel.updateCustomApiKey(apiKey) },
                                    onUpdateNotificationPreferences = { notif, exams, backpack, streakReminders, streakTime, backpackTime, examHours ->
                                        viewModel.updateNotificationPreferences(
                                            notificationsEnabled = notif,
                                            examRemindersEnabled = exams,
                                            backpackRemindersEnabled = backpack,
                                            streakRemindersEnabled = streakReminders,
                                            streakReminderTime = streakTime,
                                            backpackReminderTime = backpackTime,
                                            examReminderHoursBefore = examHours
                                        )
                                    },
                                    onSendTestNotification = { viewModel.sendTestNotification() },
                                    onSendTestStreakNotification = { isUrgent -> viewModel.sendTestStreakNotification(isUrgent) },
                                    onActivateStreakToday = { viewModel.activateStreakToday() },
                                    onUpdateProfile = { viewModel.completeOnboarding(it.username, it.country, it.educationLevel, it.grade, it.institution, it.additionalInfo, it.language) },
                                    onToggleAppBlocked = { viewModel.toggleAppBlocked(it) },
                                    onUpdateBlockedApp = { viewModel.updateBlockedApp(it) },
                                    onDeleteBlockedApp = { viewModel.deleteBlockedApp(it) },
                                    onAddBlockedApp = { pkg, name, cat, goal, initMin, targetMin, days, mot ->
                                        viewModel.addBlockedApp(pkg, name, cat, goal, initMin, targetMin, days, mot)
                                    },
                                    onOpenBackpack = { showBackpackModal = true },
                                    onOpenShop = { showShopModal = true }
                                )
                            }

                            if (showChatModal) {
                                LupoAiChatModal(
                                    messages = chatMessages,
                                    isThinking = isThinking,
                                    onSendMessage = { viewModel.sendMessageToLupo(it) },
                                    onDismiss = { showChatModal = false }
                                )
                            }

                            if (showBackpackModal) {
                                BackpackModal(
                                    materials = backpackMaterials,
                                    tomorrowDay = viewModel.tomorrowSpanishDay,
                                    onToggleMaterial = { viewModel.toggleMaterialPacked(it) },
                                    onAddMaterial = { name, subject, cat, day ->
                                        viewModel.addBackpackMaterial(name, subject, cat, day)
                                    },
                                    onDeleteMaterial = { viewModel.deleteBackpackMaterial(it) },
                                    onSuggestFromTimetable = { viewModel.suggestMaterialsFromTomorrowClasses() },
                                    onClearAll = { viewModel.clearAllMaterials() },
                                    onDismiss = { showBackpackModal = false }
                                )
                            }

                            if (showFlashcardsModal) {
                                FlashcardsModal(
                                    flashcards = flashcards,
                                    lessons = lessons,
                                    isGeneratingWithAi = isGeneratingLesson,
                                    onAddFlashcard = { sub, q, a, hint, lId ->
                                        viewModel.addFlashcard(sub, q, a, hint, lId)
                                    },
                                    onGenerateWithAi = { sub, topic ->
                                        viewModel.generateFlashcardsForTopic(sub, topic)
                                    },
                                    onStudyFlashcard = { card, mastered ->
                                        viewModel.recordFlashcardStudy(card, mastered)
                                    },
                                    onDeleteFlashcard = { viewModel.deleteFlashcard(it) },
                                    onDismiss = { showFlashcardsModal = false }
                                )
                            }

                            if (showExamsModal) {
                                ExamsModal(
                                    exams = exams,
                                    onAddExam = { sub, title, date, time, room, notes ->
                                        viewModel.addExam(sub, title, date, time, room, notes)
                                    },
                                    onToggleExamCompleted = { viewModel.toggleExamCompleted(it) },
                                    onDeleteExam = { viewModel.deleteExam(it) },
                                    onPrepareWithAi = { sub, title ->
                                        viewModel.sendMessageToLupo(
                                            messageText = "Ayúdame a crear un plan de repaso intensivo y tips de estudio para mi examen de '$title' en $sub.",
                                            subjectContext = sub
                                        )
                                        showExamsModal = false
                                        showChatModal = true
                                    },
                                    onStartExamQuiz = { sub, title ->
                                        activeQuizSubject = sub
                                        activeQuizTopic = title
                                        isQuizLoading = true
                                        coroutineScope.launch {
                                            val questions = viewModel.getQuizForTopic(sub, title)
                                            activeQuizQuestions = questions
                                            isQuizLoading = false
                                        }
                                    },
                                    onDismiss = { showExamsModal = false }
                                )
                            }

                            if (activeQuizSubject != null && activeQuizTopic != null) {
                                QuizExamModal(
                                    subject = activeQuizSubject!!,
                                    topic = activeQuizTopic!!,
                                    questions = activeQuizQuestions,
                                    isLoading = isQuizLoading,
                                    onCompleteQuiz = { score, total, subject ->
                                        viewModel.recordQuizCompleted(score, total, subject)
                                    },
                                    onDismiss = {
                                        activeQuizSubject = null
                                        activeQuizTopic = null
                                        activeQuizQuestions = emptyList()
                                        isQuizLoading = false
                                    }
                                )
                            }

                            if (showShopModal) {
                                LupoShopModal(
                                    profile = profile,
                                    onBuyAndEquip = { outfitId, cost ->
                                        viewModel.buyAndEquipOutfit(outfitId, cost)
                                    },
                                    onEquip = { outfitId ->
                                        viewModel.equipOutfit(outfitId)
                                    },
                                    onBuyStreakFreeze = {
                                        viewModel.buyStreakFreeze(cost = 50)
                                    },
                                    onDismiss = { showShopModal = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

