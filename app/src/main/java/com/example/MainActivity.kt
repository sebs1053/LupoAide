package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AddCourseDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.BackpackModal
import com.example.ui.components.ChestRewardDialog
import com.example.ui.components.GamificationTopBar
import com.example.ui.components.LupoAiChatModal
import com.example.ui.components.LupoNavigationBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StudyContractScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.LupoAideTheme
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.viewmodel.LupoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LupoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LupoAideTheme {
                LupoAideApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LupoAideApp(viewModel: LupoViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }

    val userStats by viewModel.userStatsState.collectAsState()
    val courses by viewModel.coursesState.collectAsState()
    val tasks by viewModel.tasksState.collectAsState()
    val timetable by viewModel.timetableState.collectAsState()
    val blacklistedApps by viewModel.blacklistedAppsState.collectAsState()
    val chestRewards by viewModel.chestRewardsState.collectAsState()

    val selectedCourseId by viewModel.selectedCourseId.collectAsState()
    val courseNodes by viewModel.courseNodesState.collectAsState()

    val isContractActive by viewModel.isContractActive.collectAsState()
    val contractMinutes by viewModel.contractMinutes.collectAsState()
    val contractRemainingSeconds by viewModel.contractRemainingSeconds.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val isAiChatOpen by viewModel.isAiChatOpen.collectAsState()
    val isBackpackOpen by viewModel.isBackpackOpen.collectAsState()
    val isAddTaskOpen by viewModel.isAddTaskDialogOpen.collectAsState()
    val isAddCourseOpen by viewModel.isAddCourseDialogOpen.collectAsState()
    val activeChest by viewModel.activeOpenedChest.collectAsState()

    Scaffold(
        topBar = {
            GamificationTopBar(
                stats = userStats,
                onProfileClick = { currentTab = 4 }
            )
        },
        bottomBar = {
            LupoNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        containerColor = LupoCanvasDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LupoCanvasDark)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    courses = courses,
                    selectedCourseId = selectedCourseId,
                    nodes = courseNodes,
                    onSelectCourse = { viewModel.selectCourse(it) },
                    onCompleteNode = { viewModel.completeNode(it) },
                    onOpenAiChat = { viewModel.isAiChatOpen.value = true },
                    onOpenBackpack = { viewModel.isBackpackOpen.value = true },
                    onOpenAddTask = { viewModel.isAddTaskDialogOpen.value = true },
                    onOpenAddCourse = { viewModel.isAddCourseDialogOpen.value = true }
                )

                1 -> TimetableScreen(
                    timetableList = timetable,
                    tomorrowMaterials = tasks.filter { it.category == "MATERIAL_MAÑANA" },
                    onAddClass = { day, time, subj, room, color ->
                        viewModel.addTimetableClass(day, time, subj, room, color)
                    },
                    onDeleteClass = { viewModel.deleteTimetableClass(it) },
                    onToggleMaterialTask = { viewModel.toggleTask(it) }
                )

                2 -> StudyContractScreen(
                    blacklistedApps = blacklistedApps,
                    isContractActive = isContractActive,
                    contractMinutes = contractMinutes,
                    remainingSeconds = contractRemainingSeconds,
                    onSetDuration = { viewModel.setContractDuration(it) },
                    onStartContract = { viewModel.startStudyContract() },
                    onCancelContract = { viewModel.cancelStudyContract() },
                    onToggleAppBlock = { pkg, status -> viewModel.toggleAppBlock(pkg, status) },
                    onAddCustomApp = { appName -> viewModel.addCustomBlockedApp(appName) }
                )

                3 -> TasksScreen(
                    tasks = tasks,
                    onToggleTask = { viewModel.toggleTask(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onOpenAddTask = { viewModel.isAddTaskDialogOpen.value = true }
                )

                4 -> ProfileScreen(
                    stats = userStats,
                    chests = chestRewards,
                    onOpenChest = { viewModel.openChest(it) }
                )
            }
        }
    }

    // Overlays & Dialogs
    if (isAiChatOpen) {
        LupoAiChatModal(
            messages = chatMessages,
            isLoading = isAiLoading,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onDismiss = { viewModel.isAiChatOpen.value = false }
        )
    }

    if (isBackpackOpen) {
        BackpackModal(
            onDismiss = { viewModel.isBackpackOpen.value = false }
        )
    }

    if (isAddTaskOpen) {
        AddTaskDialog(
            onDismiss = { viewModel.isAddTaskDialogOpen.value = false },
            onConfirm = { title, subject, category, priority ->
                viewModel.addTask(title, subject, category, priority)
                viewModel.isAddTaskDialogOpen.value = false
            }
        )
    }

    if (isAddCourseOpen) {
        AddCourseDialog(
            onDismiss = { viewModel.isAddCourseDialogOpen.value = false },
            onConfirm = { subject, grade, notes ->
                viewModel.createCourseWithAi(subject, grade, notes)
            }
        )
    }

    activeChest?.let { chest ->
        ChestRewardDialog(
            chest = chest,
            onDismiss = { viewModel.activeOpenedChest.value = null }
        )
    }
}
