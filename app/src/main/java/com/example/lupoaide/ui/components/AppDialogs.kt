package com.example.lupoaide.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.data.local.TaskEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    initialSubject: String = "",
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, subject: String, xp: Int, coins: Int, dueDate: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(initialSubject) }
    var priority by remember { mutableStateOf("Media") }
    var dueDate by remember { mutableStateOf("Mañana") }

    var expandedPriority by remember { mutableStateOf(false) }
    val priorities = listOf("Baja", "Media", "Alta")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Misión o Tarea", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea *") },
                    placeholder = { Text("Ej. Ejercicios de Cálculo...") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción o requisitos") },
                    placeholder = { Text("Ej. Páginas 24 a 28, ejercicios impares") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia / Asignatura") },
                    placeholder = { Text("Ej. Matemáticas, Historia...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Selector de Fecha estándar estilo Google
                GoogleDatePickerField(
                    label = "Fecha de entrega",
                    selectedDate = dueDate,
                    onDateSelected = { dueDate = it }
                )

                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prioridad y Recompensa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorities.forEach { p ->
                            val rewardText = when (p) {
                                "Alta" -> "+40 EXP / +20 🪙"
                                "Media" -> "+25 EXP / +10 🪙"
                                else -> "+15 EXP / +5 🪙"
                            }
                            DropdownMenuItem(
                                text = { Text("$p ($rewardText)") },
                                onClick = {
                                    priority = p
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val xpReward = when (priority) {
                            "Alta" -> 40
                            "Media" -> 25
                            else -> 15
                        }
                        val coinReward = when (priority) {
                            "Alta" -> 20
                            "Media" -> 10
                            else -> 5
                        }
                        onAdd(title, desc, subject.ifBlank { "General" }, xpReward, coinReward, dueDate, priority)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_add_task_btn")
            ) {
                Text("Crear Misión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTimetableSlotDialog(
    currentDay: String,
    onDismiss: () -> Unit,
    onAddMultipleDays: (subject: String, selectedDays: Set<String>, start: String, end: String, room: String, teacher: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf(currentDay)) }
    var start by remember { mutableStateOf("08:00 AM") }
    var end by remember { mutableStateOf("09:30 AM") }
    var room by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }

    val daysList = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Clase al Horario", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Nombre de la Materia *") },
                    placeholder = { Text("Ej. Cálculo Diferencial, Química...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Días en que se imparte (puedes elegir varios):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysList.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) {
                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(day, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }

                Text(
                    text = "Horario de la clase (Selector estándar de Google con AM/PM):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoogleTimePickerField(
                        label = "Hora Inicio",
                        selectedTime = start,
                        onTimeSelected = { start = it },
                        modifier = Modifier.weight(1f)
                    )
                    GoogleTimePickerField(
                        label = "Hora Fin",
                        selectedTime = end,
                        onTimeSelected = { end = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Salón / Laboratorio (Opcional)") },
                    placeholder = { Text("Ej. Aula 204, Lab 2...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Profesor / Docente (Opcional)") },
                    placeholder = { Text("Ej. Prof. García...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && selectedDays.isNotEmpty()) {
                        onAddMultipleDays(subject, selectedDays, start, end, room, teacher)
                        onDismiss()
                    }
                }
            ) {
                Text(if (selectedDays.size > 1) "Guardar en ${selectedDays.size} días" else "Guardar Clase")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo interactivo para comprobar si el estudiante realmente realizó la tarea con Inteligencia Artificial Lupo.
 */
@Composable
fun TaskVerificationDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onVerifyAndClaim: (proofText: String) -> Unit
) {
    var proofText by remember { mutableStateOf("") }
    var isAnalyzingWithAi by remember { mutableStateOf(false) }
    var aiFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var bonusXpAwarded by remember { mutableStateOf(15) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comprobación con Lupo IA", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tarea: ${task.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Materia: ${task.subject}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instrucciones: ${task.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Describe brevemente tu procedimiento, resumen o resultados. Lupo IA analizará tu evidencia para validar tu aprendizaje y otorgarte EXP:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = proofText,
                    onValueChange = {
                        proofText = it
                        // Reset previous feedback when text changes
                        if (aiFeedbackMessage != null) {
                            aiFeedbackMessage = null
                        }
                    },
                    label = { Text("Tu evidencia o procedimiento de la tarea *") },
                    placeholder = { Text("Ej. Resolví los problemas del 1 al 10 usando la regla de la cadena y comprobé los signos...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("task_proof_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Tarjeta de Evaluación de Lupo IA en tiempo real
                if (isAnalyzingWithAi) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🐺 Lupo IA está analizando tu evidencia...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Comprobando comprensión pedagógica...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (aiFeedbackMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "¡Evaluación Aprobada por Lupo IA! ✨",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = aiFeedbackMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "+${task.xpReward + bonusXpAwarded} EXP Totales (+${bonusXpAwarded} Bono IA) / +${task.coinReward} 🪙",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recompensa base + Bono IA:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "+${task.xpReward} EXP / +${task.coinReward} 🪙 (+15 Bono IA)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (aiFeedbackMessage != null) {
                Button(
                    onClick = {
                        onVerifyAndClaim(proofText.trim())
                    },
                    modifier = Modifier.testTag("claim_verified_task_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reclamar y Terminar")
                }
            } else {
                Button(
                    onClick = {
                        if (proofText.trim().length >= 3) {
                            isAnalyzingWithAi = true
                            coroutineScope.launch {
                                val service = com.example.lupoaide.data.remote.GeminiStudyService()
                                val result = service.verifyTaskWithAi(
                                    taskTitle = task.title,
                                    subject = task.subject,
                                    studentProof = proofText.trim()
                                )
                                isAnalyzingWithAi = false
                                aiFeedbackMessage = result.feedbackMessage
                                bonusXpAwarded = result.bonusXp
                            }
                        }
                    },
                    enabled = proofText.trim().length >= 3 && !isAnalyzingWithAi,
                    modifier = Modifier.testTag("verify_task_confirm_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comprobar con IA")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para generar automáticamente una lección completa con IA
 */
@Composable
fun GenerateAiLessonDialog(
    isAiConnected: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (subject: String, topic: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Lección con Lupo IA", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAiConnected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isAiConnected) Icons.Default.CheckCircle else Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = if (isAiConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAiConnected) "Lupo IA Conectada (Gemini API Activa)"
                            else "Modo Asistente Lupo (Generador Estructurado)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Ingresa el tema y Lupo generará automáticamente una guía completa con resumen, explicación detallada, puntos clave y quiz:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia *") },
                    placeholder = { Text("Ej. Física, Biología, Historia, Matemáticas...") },
                    modifier = Modifier.fillMaxWidth().testTag("ai_lesson_subject_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Tema o concepto a estudiar *") },
                    placeholder = { Text("Ej. Leyes de Kepler, Fotosíntesis, Segunda Guerra Mundial...") },
                    modifier = Modifier.fillMaxWidth().testTag("ai_lesson_topic_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onGenerate(subject.ifBlank { "General" }, topic)
                        onDismiss()
                    }
                },
                enabled = topic.isNotBlank(),
                modifier = Modifier.testTag("confirm_generate_ai_lesson_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generar Lección")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
