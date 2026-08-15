package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoCyanNode
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoFlameOrange
import com.example.ui.theme.LupoPinkAccent
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoTextPrimary
import com.example.ui.theme.LupoTextSecondary

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onOpenAddTask: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("TODAS") }

    val categories = listOf(
        "TODAS" to "Todas",
        "TAREA" to "Tareas",
        "PROYECTO" to "Proyectos",
        "MATERIAL_MAÑANA" to "Materiales 🎒"
    )

    val filteredTasks = when (selectedCategoryFilter) {
        "TODAS" -> tasks
        else -> tasks.filter { it.category == selectedCategoryFilter }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LupoCanvasDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gestor Académico de Tareas ✔️",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoPrimaryGold
                        )
                        Text(
                            text = "Completa tus metas escolares y suma EXP",
                            fontSize = 12.sp,
                            color = LupoTextSecondary
                        )
                    }
                }
            }

            // Category Filter Tabs
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (catKey, catLabel) ->
                        val isSelected = catKey == selectedCategoryFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) LupoPrimaryGold else LupoSurfaceDark)
                                .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp))
                                .clickable { selectedCategoryFilter = catKey }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "¡No hay tareas en esta categoría!",
                                fontSize = 14.sp,
                                color = LupoEmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) LupoSurfaceDark.copy(alpha = 0.6f) else LupoSurfaceDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onToggleTask(task) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = LupoEmeraldGreen,
                                        uncheckedColor = LupoPrimaryGold
                                    ),
                                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.subject,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LupoCyanNode
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        PriorityBadge(priority = task.priority)
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = task.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) Color.Gray else Color.White
                                    )

                                    Text(
                                        text = if (task.isCompleted) "¡Completada! +${task.expReward} EXP" else "Recompensa: +${task.expReward} EXP ⚡",
                                        fontSize = 11.sp,
                                        color = if (task.isCompleted) LupoEmeraldGreen else LupoPrimaryGold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteTask(task.id) },
                                modifier = Modifier.testTag("delete_task_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Task Floating Action Button (+)
        FloatingActionButton(
            onClick = onOpenAddTask,
            containerColor = LupoPrimaryGold,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
                .testTag("add_task_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir Tarea",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun PriorityBadge(priority: String) {
    val (bg, label) = when (priority) {
        "ALTA" -> LupoFlameOrange to "Urgente"
        "BAJA" -> LupoEmeraldGreen to "Baja"
        else -> LupoPrimaryGold to "Media"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = bg
        )
    }
}
