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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.TaskEntity
import com.example.data.local.TimetableEntity
import com.example.ui.theme.LupoCanvasDark
import com.example.ui.theme.LupoCardBorder
import com.example.ui.theme.LupoCyanNode
import com.example.ui.theme.LupoEmeraldGreen
import com.example.ui.theme.LupoPrimaryGold
import com.example.ui.theme.LupoSurfaceDark
import com.example.ui.theme.LupoTextPrimary
import com.example.ui.theme.LupoTextSecondary

@Composable
fun TimetableScreen(
    timetableList: List<TimetableEntity>,
    tomorrowMaterials: List<TaskEntity>,
    onAddClass: (day: Int, time: String, subject: String, room: String, color: String) -> Unit,
    onDeleteClass: (Long) -> Unit,
    onToggleMaterialTask: (TaskEntity) -> Unit
) {
    var selectedDayOfWeek by remember { mutableStateOf(1) } // 1 = Lunes
    var isAddClassModalOpen by remember { mutableStateOf(false) }

    val days = listOf(
        1 to "Lunes",
        2 to "Martes",
        3 to "Miércoles",
        4 to "Jueves",
        5 to "Viernes"
    )

    val currentDayClasses = timetableList.filter { it.dayOfWeek == selectedDayOfWeek }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LupoCanvasDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "Horario de Clases 📅",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LupoPrimaryGold
                        )
                        Text(
                            text = "Sincronización de materias y materiales",
                            fontSize = 12.sp,
                            color = LupoTextSecondary
                        )
                    }

                    Button(
                        onClick = { isAddClassModalOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_class_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clase", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Days Selector Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(days) { (dayNum, dayName) ->
                        val isSelected = dayNum == selectedDayOfWeek
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) LupoPrimaryGold else LupoSurfaceDark)
                                .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp))
                                .clickable { selectedDayOfWeek = dayNum }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // Schedule Items List
            if (currentDayClasses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin clases programadas para este día",
                                fontSize = 14.sp,
                                color = LupoTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(currentDayClasses) { classItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LupoCardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            try {
                                                Color(android.graphics.Color.parseColor(classItem.colorHex))
                                            } catch (e: Exception) {
                                                LupoCyanNode
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = classItem.subject,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Hora",
                                            tint = LupoPrimaryGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${classItem.timeSlot} • ${classItem.room}",
                                            fontSize = 12.sp,
                                            color = LupoTextSecondary
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { onDeleteClass(classItem.id) }) {
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

            // Material para mañana Checklist (Matching Wireframe tab 2/4)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, LupoCyanNode, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LupoSurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎒", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Material para Mañana",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LupoCyanNode
                            )
                        }
                        Text(
                            text = "Lista de útiles, batas o calculadoras necesarias:",
                            fontSize = 11.sp,
                            color = LupoTextSecondary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        if (tomorrowMaterials.isEmpty()) {
                            Text(
                                text = "✔️ No hay útiles pendientes registrados.",
                                fontSize = 12.sp,
                                color = LupoEmeraldGreen,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            for (task in tomorrowMaterials) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { onToggleMaterialTask(task) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = LupoEmeraldGreen,
                                            uncheckedColor = LupoPrimaryGold
                                        )
                                    )
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        color = if (task.isCompleted) LupoEmeraldGreen else Color.White,
                                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (isAddClassModalOpen) {
        AddClassModal(
            dayOfWeek = selectedDayOfWeek,
            onDismiss = { isAddClassModalOpen = false },
            onConfirm = { day, time, subj, room, color ->
                onAddClass(day, time, subj, room, color)
                isAddClassModalOpen = false
            }
        )
    }
}

@Composable
private fun AddClassModal(
    dayOfWeek: Int,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, time: String, subject: String, room: String, color: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("08:00 - 09:30") }
    var room by remember { mutableStateOf("Aula 101") }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Añadir Clase al Horario 📅",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LupoPrimaryGold
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Nombre de la Materia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_class_subject_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Horario (ej. 08:00 - 09:30)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Aula / Laboratorio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LupoPrimaryGold,
                        unfocusedBorderColor = LupoCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                onConfirm(dayOfWeek, timeSlot, subject, room, "#2A9D8F")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LupoPrimaryGold),
                        modifier = Modifier.testTag("confirm_add_class_button")
                    ) {
                        Text("Guardar Clase", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
