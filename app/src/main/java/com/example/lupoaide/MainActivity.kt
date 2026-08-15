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
import com.example.lupoaide.ui.components.*
import com.example.lupoaide.ui.screens.*
import com.example.lupoaide.ui.theme.LupoAideTheme
import com.example.lupoaide.ui.viewmodel.LupoViewModel

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

                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val isThinking by viewModel.isLupoThinking.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf(LupoScreen.HOME) }
                var showChatModal by remember { mutableStateOf(false) }
                var showBackpackModal by remember { mutableStateOf(false) }

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
                                onLupoClick = { showChatModal = true }
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
                                    tomorrowDay = viewModel.tomorrowSpanishDay,
                                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                                    onOpenLupoChat = { showChatModal = true },
                                    onOpenBackpack = { showBackpackModal = true }
                                )
                                LupoScreen.TIMETABLE -> TimetableScreen(
                                    slots = slots,
                                    selectedDay = selectedDay,
                                    onSelectDay = { viewModel.setSelectedDay(it) },
                                    onAddSlot = { sub, day, start, end, room, teacher ->
                                        viewModel.addTimetableSlot(sub, day, start, end, room, teacher)
                                    },
                                    onDeleteSlot = { viewModel.deleteTimetableSlot(it) }
                                )
                                LupoScreen.TASKS -> TasksScreen(
                                    tasks = tasks,
                                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                                    onDeleteTask = { viewModel.deleteTask(it) },
                                    onAddTask = { title, desc, sub, xp, coins, due, priority ->
                                        viewModel.addTask(title, desc, sub, xp, coins, due, priority)
                                    }
                                )
                                LupoScreen.LESSONS -> LessonsScreen(
                                    lessons = lessons,
                                    onAddLesson = { title, subject, summary, content, keyPoints ->
                                        viewModel.addLesson(title, subject, summary, content, keyPoints)
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
                                    }
                                )
                                LupoScreen.PROFILE -> ProfileScreen(
                                    profile = profile,
                                    blockedApps = blockedApps,
                                    onUpdateProfile = { viewModel.completeOnboarding(it.username, it.country, it.educationLevel, it.grade, it.institution, it.additionalInfo, it.language) },
                                    onToggleAppBlocked = { viewModel.toggleAppBlocked(it) },
                                    onUpdateBlockedApp = { viewModel.updateBlockedApp(it) },
                                    onDeleteBlockedApp = { viewModel.deleteBlockedApp(it) },
                                    onAddBlockedApp = { pkg, name, cat, goal, initMin, targetMin, days, mot ->
                                        viewModel.addBlockedApp(pkg, name, cat, goal, initMin, targetMin, days, mot)
                                    },
                                    onOpenBackpack = { showBackpackModal = true }
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
                        }
                    }
                }
            }
        }
    }
}
