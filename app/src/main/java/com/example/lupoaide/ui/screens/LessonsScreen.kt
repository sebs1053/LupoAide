package com.example.lupoaide.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.data.local.LessonEntity
import kotlinx.coroutines.delay

@Composable
fun LessonsScreen(
    lessons: List<LessonEntity>,
    onAddLesson: (title: String, subject: String, summary: String, content: String, keyPoints: String) -> Unit,
    onUpdateLesson: (LessonEntity) -> Unit,
    onDeleteLesson: (Int) -> Unit,
    onAskLupoAboutLesson: (lessonTitle: String, subject: String) -> Unit,
    onCompleteStudySession: (lessonId: Int, minutes: Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("Todas") }
    var viewingLesson by remember { mutableStateOf<LessonEntity?>(null) }
    var activeStudyTimerLesson by remember { mutableStateOf<LessonEntity?>(null) }

    val subjects = remember(lessons) {
        listOf("Todas") + lessons.map { it.subject }.filter { it.isNotBlank() }.distinct()
    }

    val filteredLessons = if (selectedSubjectFilter == "Todas") {
        lessons
    } else {
        lessons.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_lesson_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Lección")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Lecciones y Temas de Estudio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Guarda tus apuntes, temas clave y repasa con Lupo para ganar experiencia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filtros de materia
            if (subjects.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { subj ->
                        val isSelected = subj == selectedSubjectFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectFilter = subj },
                            label = { Text(subj) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredLessons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tienes lecciones guardadas aún",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Presiona el botón + para registrar tus temas, fórmulas o apuntes de clase.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredLessons) { lesson ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingLesson = lesson }
                                .testTag("lesson_card_${lesson.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = lesson.subject,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (lesson.reviewStatus) {
                                            "Dominado" -> MaterialTheme.colorScheme.tertiaryContainer
                                            "En progreso" -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = lesson.reviewStatus,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                if (lesson.summary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = lesson.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { activeStudyTimerLesson = lesson },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Estudiar (Timer)")
                                    }

                                    OutlinedButton(
                                        onClick = { onAskLupoAboutLesson(lesson.title, lesson.subject) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tutor IA")
                                    }

                                    IconButton(onClick = { onDeleteLesson(lesson.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para crear lección
        if (showAddDialog) {
            var title by remember { mutableStateOf("") }
            var subject by remember { mutableStateOf("") }
            var summary by remember { mutableStateOf("") }
            var content by remember { mutableStateOf("") }
            var keyPoints by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nueva Lección / Apunte") },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Título de la lección *") },
                                placeholder = { Text("Ej. Leyes de Newton, Factorización...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("Materia / Asignatura *") },
                                placeholder = { Text("Ej. Física, Matemáticas, Historia...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = summary,
                                onValueChange = { summary = it },
                                label = { Text("Resumen breve") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = keyPoints,
                                onValueChange = { keyPoints = it },
                                label = { Text("Puntos clave / Fórmulas importantes") },
                                placeholder = { Text("• F = m*a\n• Tercera ley: Acción y reacción") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = { Text("Apuntes y desarrollo del tema") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddLesson(title, subject.ifBlank { "General" }, summary, content, keyPoints)
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Guardar Lección")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Modal de detalle de Lección
        viewingLesson?.let { lesson ->
            AlertDialog(
                onDismissRequest = { viewingLesson = null },
                title = {
                    Column {
                        Text(text = lesson.subject, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(text = lesson.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (lesson.summary.isNotBlank()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = lesson.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        if (lesson.keyPoints.isNotBlank()) {
                            item {
                                Text(text = "📌 Puntos Clave:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = lesson.keyPoints, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        if (lesson.content.isNotBlank()) {
                            item {
                                Text(text = "📝 Apuntes Completos:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = lesson.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        item {
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Estado: ${lesson.reviewStatus}", style = MaterialTheme.typography.bodySmall)
                                TextButton(
                                    onClick = {
                                        val nextStatus = when (lesson.reviewStatus) {
                                            "Por repasar" -> "En progreso"
                                            "En progreso" -> "Dominado"
                                            else -> "Por repasar"
                                        }
                                        onUpdateLesson(lesson.copy(reviewStatus = nextStatus))
                                        viewingLesson = lesson.copy(reviewStatus = nextStatus)
                                    }
                                ) {
                                    Text("Cambiar estado")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewingLesson = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        // Temporizador de Estudio Verificado (Otorga EXP legítima sin bugs)
        activeStudyTimerLesson?.let { lesson ->
            StudyTimerModal(
                lesson = lesson,
                onDismiss = { activeStudyTimerLesson = null },
                onFinishSession = { minutes ->
                    onCompleteStudySession(lesson.id, minutes)
                    activeStudyTimerLesson = null
                }
            )
        }
    }
}

@Composable
fun StudyTimerModal(
    lesson: LessonEntity,
    onDismiss: () -> Unit,
    onFinishSession: (minutes: Int) -> Unit
) {
    var secondsLeft by remember { mutableStateOf(1500) } // 25 min default Pomodoro
    var isRunning by remember { mutableStateOf(false) }
    var totalSecondsCompleted by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
            totalSecondsCompleted += 1
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sesión de Enfoque: ${lesson.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Materia: ${lesson.subject}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isRunning) "🔥 ¡Concéntrate en tu tema! Lupo está estudiando contigo."
                           else "Presiona Iniciar para arrancar el temporizador de estudio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isRunning) {
                    Button(onClick = { isRunning = true }) {
                        Text("Iniciar")
                    }
                } else {
                    Button(onClick = { isRunning = false }) {
                        Text("Pausar")
                    }
                }

                Button(
                    onClick = {
                        val minutesStudied = (totalSecondsCompleted / 60).coerceAtLeast(1)
                        onFinishSession(minutesStudied)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Finalizar (+EXP)")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Salir")
            }
        }
    )
}
