package com.example.lupoaide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
        setContent {
            LupoAideTheme {
                var currentScreen by remember { mutableStateOf(LupoScreen.HOME) }
                var showChatModal by remember { mutableStateOf(false) }
                var showBackpackModal by remember { mutableStateOf(false) }

                val profile by viewModel.userProfile.collectAsStateWithLifecycle()
                val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                val slots by viewModel.timetableSlots.collectAsStateWithLifecycle()
                val contracts by viewModel.contracts.collectAsStateWithLifecycle()
                val backpackItems by viewModel.backpackItems.collectAsStateWithLifecycle()
                val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()

                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val isThinking by viewModel.isLupoThinking.collectAsStateWithLifecycle()

                Scaffold(
                    topBar = {
                        GamificationTopBar(
                            profile = profile,
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
                            LupoScreen.CONTRACTS -> StudyContractScreen(
                                contracts = contracts,
                                onAddContract = { title, goalMins, targetDays, penalty, rewardCoins, rewardXp ->
                                    viewModel.addContract(title, goalMins, targetDays, penalty, rewardCoins, rewardXp)
                                }
                            )
                            LupoScreen.PROFILE -> ProfileScreen(
                                profile = profile,
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
                                items = backpackItems,
                                onUseItem = { viewModel.useItem(it) },
                                onDismiss = { showBackpackModal = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
