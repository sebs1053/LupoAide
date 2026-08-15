package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.CourseEntity
import com.example.data.local.CourseNodeEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoCyanNode
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoFlameOrange
import com.example.ui.theme.LupoPinkAccent
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoPurpleAccent
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoSurfaceVariant
import com.example.ui.theme.LupoTextPrimary
import com.example.ui.theme.LupoTextSecondary

@Composable
fun HomeScreen(
    courses: List<CourseEntity>,
    selectedCourseId: Long?,
    nodes: List<CourseNodeEntity>,
    onSelectCourse: (Long) -> Unit,
    onCompleteNode: (CourseNodeEntity) -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenBackpack: () -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenAddCourse: () -> Unit
) {
    var isQuickActionExpanded by remember { mutableStateOf(false) }
    var activeNodeModal by remember { mutableStateOf<CourseNodeEntity?>(null) }

    val activeCourse = courses.find { it.id == selectedCourseId } ?: courses.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LupoCanvasDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Action Menu Header Bar (Añadir tarea / Añadir curso)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Button(
                            onClick = { isQuickActionExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("quick_action_dropdown_button")
                        ) {
                            Text(
                                text = "Acción Rápida ⚡ ▼",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        DropdownMenu(
                            expanded = isQuickActionExpanded,
                            onDismissRequest = { isQuickActionExpanded = false },
                            modifier = Modifier.background(LupoSurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("📝 Añadir Tarea", color = Color.White) },
                                onClick = {
                                    isQuickActionExpanded = false
                                    onOpenAddTask()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🤖 Generar Curso con IA", color = Color.White) },
                                onClick = {
                                    isQuickActionExpanded = false
                                    onOpenAddCourse()
                                }
                            )
                        }
                    }

                    Text(
                        text = "LupoAide Motor IA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LupoCyanNode
                    )
                }
            }

            // Hero Mascot Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, LupoCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lupo_hero_1786137380216),
                            contentDescription = "Lupo Hero Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = "Compañero de Estudio IA 🐺",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LupoPrimaryGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "¡Avanza en tus nodos de aprendizaje y junta EXP!",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Course Selector Horizontal List
            item {
                Column {
                    Text(
                        text = "Tus Cursos de Estudio",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LupoTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(courses) { course ->
                            val isSelected = course.id == selectedCourseId
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { onSelectCourse(course.id) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) LupoPrimaryGold else LupoCardBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) LupoSurfaceVariant else LupoSurfaceDark
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = course.subject,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LupoCyanNode
                                    )
                                    Text(
                                        text = course.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { course.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = LupoPrimaryGold,
                                        trackColor = LupoCardBorder
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${course.completedNodes}/${course.totalNodes} Nodos completados",
                                        fontSize = 10.sp,
                                        color = LupoTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Active Course Interactive Node Graph Tree (Matching wireframe)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LupoCardBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Arbol de Nodos: ${activeCourse?.title ?: "Curso"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoPrimaryGold
                        )
                        Text(
                            text = "Toca cada nodo para estudiar la lección o realizar el examen",
                            fontSize = 11.sp,
                            color = LupoTextSecondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Visual connected node tree graph
                        for (index in nodes.indices) {
                            val node = nodes[index]
                            CourseNodeWidget(
                                node = node,
                                isLast = index == nodes.size - 1,
                                onClick = { activeNodeModal = node }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Buttons on right: AI Tutor Chat (🤖) & Backpack (🎒)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = onOpenBackpack,
                containerColor = LupoPurpleAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("floating_backpack_button")
            ) {
                Text("🎒", fontSize = 22.sp)
            }

            FloatingActionButton(
                onClick = onOpenAiChat,
                containerColor = LupoPrimaryGold,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("floating_ai_chat_button")
            ) {
                Text("🤖", fontSize = 26.sp)
            }
        }
    }

    // Node Lesson / Quiz Modal Dialog
    activeNodeModal?.let { node ->
        NodeDetailModal(
            node = node,
            onDismiss = { activeNodeModal = null },
            onComplete = {
                onCompleteNode(node)
                activeNodeModal = null
            }
        )
    }
}

@Composable
private fun CourseNodeWidget(
    node: CourseNodeEntity,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val nodeColor = when {
        node.isCompleted -> LupoEmeraldGreen
        node.nodeType == "QUIZ" -> LupoFlameOrange
        node.nodeType == "FLASHCARD" -> LupoPinkAccent
        else -> LupoCyanNode
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(nodeColor)
                    .border(2.dp, if (node.isCompleted) Color.White else LupoPrimaryGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (node.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completado",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = when (node.nodeType) {
                            "QUIZ" -> "❓"
                            "FLASHCARD" -> "🃏"
                            "SUMMARY" -> "📄"
                            else -> "📚"
                        },
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (node.isCompleted) "¡Nodo Completado! +60 EXP" else "Toca para abrir nodo",
                    fontSize = 11.sp,
                    color = if (node.isCompleted) LupoEmeraldGreen else LupoTextSecondary
                )
            }
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(nodeColor.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun NodeDetailModal(
    node: CourseNodeEntity,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var selectedOptionIndex by remember { mutableStateOf(-1) }
    var quizSubmitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LupoCanvasDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, LupoPrimaryGold, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LupoPrimaryGold
                    )
                    Text(
                        text = node.nodeType,
                        fontSize = 11.sp,
                        color = LupoCyanNode,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = node.contentText,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )

                // If Quiz Node, render options
                    if (node.nodeType == "QUIZ" && node.optionsJson.isNotBlank()) {
                        val options = node.optionsJson.split("|")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (index in options.indices) {
                                val option = options[index]
                                val isCorrect = index == node.correctAnswerIndex
                                val isSelected = index == selectedOptionIndex
                                val optionBg = when {
                                    quizSubmitted && isCorrect -> LupoEmeraldGreen.copy(alpha = 0.3f)
                                    quizSubmitted && isSelected && !isCorrect -> LupoFlameOrange.copy(alpha = 0.3f)
                                    isSelected -> LupoPrimaryGold.copy(alpha = 0.2f)
                                    else -> LupoSurfaceDark
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(optionBg)
                                        .border(1.dp, LupoCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { if (!quizSubmitted) selectedOptionIndex = index }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { if (!quizSubmitted) selectedOptionIndex = index },
                                        colors = RadioButtonDefaults.colors(selectedColor = LupoPrimaryGold)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (node.nodeType == "QUIZ" && !quizSubmitted) {
                                quizSubmitted = true
                            } else {
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                        modifier = Modifier.testTag("complete_node_modal_button")
                    ) {
                        Text(
                            text = if (node.nodeType == "QUIZ" && !quizSubmitted) "Verificar Respuesta" else "Marcar Nivel Completado ✔️",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
