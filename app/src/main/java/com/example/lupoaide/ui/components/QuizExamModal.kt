package com.example.lupoaide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lupoaide.data.remote.QuizQuestion

@Composable
fun QuizExamModal(
    subject: String,
    topic: String,
    questions: List<QuizQuestion>,
    isLoading: Boolean,
    onCompleteQuiz: (score: Int, total: Int, subject: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("quiz_exam_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Simulacro de Examen",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$subject • $topic",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🐺 Lupo IA está preparando tu examen...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Diseñando preguntas pedagógicas de opción múltiple...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (questions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No se pudieron generar preguntas para este tema.")
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onDismiss) { Text("Cerrar") }
                        }
                    }
                } else if (isQuizCompleted) {
                    // Pantalla de Resultados
                    val total = questions.size
                    val percentage = (correctAnswersCount.toFloat() / total.toFloat() * 100).toInt()
                    val earnedXp = (correctAnswersCount * 15) + 10
                    val earnedCoins = (correctAnswersCount * 5) + 5

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (percentage >= 70) "🎉" else "🐺",
                            fontSize = 54.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (percentage >= 80) "¡Excelente Dominio!"
                            else if (percentage >= 50) "¡Buen Intento, Sigue Así!"
                            else "¡Repaso Necesario!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Calificación: $correctAnswersCount / $total ($percentage%)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tarjeta de Recompensas
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚡ +$earnedXp XP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Experiencia", fontSize = 11.sp)
                                }
                                Divider(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(1.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🪙 +$earnedCoins Monedas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    Text("Para la tienda", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Consejo de Lupo
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("🐺", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (percentage >= 80)
                                        "¡Estás más que listo para tu evaluación escolar! Mantén el repaso ligero el día anterior."
                                    else
                                        "Te recomiendo crear tarjetas de repaso (Flashcards) o hacer una sesión Pomodoro sobre los conceptos que fallaste.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Finalizar y Guardar Resultados")
                        }
                    }
                } else if (currentQuestion != null) {
                    // Pregunta Activa
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Barra de progreso
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pregunta ${currentQuestionIndex + 1} de ${questions.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aciertos: $correctAnswersCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1).toFloat() / questions.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Enunciado
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentQuestion.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Opciones A, B, C, D
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            val isSelected = selectedOptionIndex == index
                            val isCorrect = index == currentQuestion.correctIndex

                            val backgroundColor = when {
                                !isAnswerSubmitted && isSelected -> MaterialTheme.colorScheme.primaryContainer
                                isAnswerSubmitted && isCorrect -> Color(0xFF10B981).copy(alpha = 0.2f)
                                isAnswerSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val borderColor = when {
                                !isAnswerSubmitted && isSelected -> MaterialTheme.colorScheme.primary
                                isAnswerSubmitted && isCorrect -> Color(0xFF10B981)
                                isAnswerSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }

                            val optionLetter = when (index) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                3 -> "D"
                                else -> "${index + 1}"
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = backgroundColor,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !isAnswerSubmitted) {
                                        selectedOptionIndex = index
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(borderColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = optionLetter,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = borderColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswerSubmitted) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        // Explicación didáctica si ya se contestó
                        if (isAnswerSubmitted) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Explicación de Lupo IA:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = currentQuestion.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón de acción (Comprobar o Siguiente)
                    if (!isAnswerSubmitted) {
                        Button(
                            onClick = {
                                isAnswerSubmitted = true
                                if (selectedOptionIndex == currentQuestion.correctIndex) {
                                    correctAnswersCount++
                                }
                            },
                            enabled = selectedOptionIndex != null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Comprobar Respuesta")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOptionIndex = null
                                    isAnswerSubmitted = false
                                } else {
                                    isQuizCompleted = true
                                    onCompleteQuiz(correctAnswersCount, questions.size, subject)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (currentQuestionIndex < questions.size - 1) "Siguiente Pregunta ➡️"
                                else "Ver Resultados 🎉"
                            )
                        }
                    }
                }
            }
        }
    }
}
