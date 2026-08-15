package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ChestRewardEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoFlameOrange
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, category: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("General") }
    var category by remember { mutableStateOf("TAREA") } // TAREA, PROYECTO, MATERIAL_MAÑANA
    var priority by remember { mutableStateOf("MEDIA") }

    var isCatExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LupoSurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, LupoPrimaryGold, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Añadir Tarea / Material 📝",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LupoPrimaryGold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la actividad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia (ej. Biología, Matemáticas)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_subject_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = isCatExpanded,
                    onExpandedChange = { isCatExpanded = !isCatExpanded }
                ) {
                    OutlinedTextField(
                        value = when (category) {
                            "PROYECTO" -> "Proyecto Escolar"
                            "MATERIAL_MAÑANA" -> "Material para Mañana 🎒"
                            else -> "Tarea Diaria"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCatExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LupoPrimaryGold,
                            unfocusedBorderColor = LupoCardBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isCatExpanded,
                        onDismissRequest = { isCatExpanded = false },
                        modifier = Modifier.background(LupoCanvasDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tarea Diaria", color = Color.White) },
                            onClick = { category = "TAREA"; isCatExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Proyecto Escolar", color = Color.White) },
                            onClick = { category = "PROYECTO"; isCatExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Material para Mañana 🎒", color = Color.White) },
                            onClick = { category = "MATERIAL_MAÑANA"; isCatExpanded = false }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, subject, category, priority)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                        modifier = Modifier.testTag("confirm_add_task_button")
                    ) {
                        Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (subject: String, grade: String, notes: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Preparatoria") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LupoSurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, LupoPrimaryGold, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Generar Curso con IA (Gemini) 🤖",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LupoPrimaryGold
                )

                Text(
                    text = "LupoAide creará lecciones y simulacros adaptados a tu grado escolar.",
                    fontSize = 12.sp,
                    color = LupoTextPrimary
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia o Tema (ej. Historia de México, Física)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_course_subject_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grado Escolar") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Material de Clase / Apuntes (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                onConfirm(subject, grade, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                        modifier = Modifier.testTag("confirm_create_course_button")
                    ) {
                        Text("Generar Curso ⚡", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChestRewardDialog(
    chest: ChestRewardEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LupoCanvasDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, LupoFlameOrange, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🎉 ¡COFRE DESBLOQUEADO! 🎉",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LupoPrimaryGold
                )

                Text(
                    text = "🎁",
                    fontSize = 64.sp
                )

                Text(
                    text = chest.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = chest.rewardText,
                    fontSize = 14.sp,
                    color = LupoEmeraldGreen,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_chest_reward_button")
                ) {
                    Text("¡Reclamar Premios! 🦴⚡", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
