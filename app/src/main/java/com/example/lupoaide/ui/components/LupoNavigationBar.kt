package com.example.lupoaide.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class LupoScreen(val title: String) {
    HOME("Home"),
    TIMETABLE("Timetable"),
    TASKS("Tasks"),
    CONTRACTS("Contracts"),
    PROFILE("Profile")
}

@Composable
fun LupoNavigationBar(
    currentScreen: LupoScreen,
    onScreenSelected: (LupoScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("lupo_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == LupoScreen.HOME,
            onClick = { onScreenSelected(LupoScreen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            modifier = Modifier.testTag("nav_home_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.TIMETABLE,
            onClick = { onScreenSelected(LupoScreen.TIMETABLE) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Timetable") },
            label = { Text("Schedule") },
            modifier = Modifier.testTag("nav_timetable_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.TASKS,
            onClick = { onScreenSelected(LupoScreen.TASKS) },
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
            label = { Text("Quests") },
            modifier = Modifier.testTag("nav_tasks_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.CONTRACTS,
            onClick = { onScreenSelected(LupoScreen.CONTRACTS) },
            icon = { Icon(Icons.Default.Gavel, contentDescription = "Contracts") },
            label = { Text("Contracts") },
            modifier = Modifier.testTag("nav_contracts_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.PROFILE,
            onClick = { onScreenSelected(LupoScreen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            modifier = Modifier.testTag("nav_profile_btn")
        )
    }
}
