package com.example.lupoaide.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class LupoScreen(val title: String) {
    HOME("Inicio"),
    TIMETABLE("Horario"),
    TASKS("Misiones"),
    LESSONS("Lecciones"),
    PROFILE("Perfil")
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
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            modifier = Modifier.testTag("nav_home_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.TIMETABLE,
            onClick = { onScreenSelected(LupoScreen.TIMETABLE) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Horario") },
            label = { Text("Horario") },
            modifier = Modifier.testTag("nav_timetable_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.TASKS,
            onClick = { onScreenSelected(LupoScreen.TASKS) },
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Misiones") },
            label = { Text("Misiones") },
            modifier = Modifier.testTag("nav_tasks_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.LESSONS,
            onClick = { onScreenSelected(LupoScreen.LESSONS) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Lecciones") },
            label = { Text("Lecciones") },
            modifier = Modifier.testTag("nav_lessons_btn")
        )
        NavigationBarItem(
            selected = currentScreen == LupoScreen.PROFILE,
            onClick = { onScreenSelected(LupoScreen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            modifier = Modifier.testTag("nav_profile_btn")
        )
    }
}
