package com.example.lupoaide.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDatePickerModal(
    onDateSelected: (formattedDate: String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        val formatted = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        onDateSelected(formatted)
                    }
                    onDismiss()
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleTimePickerModal(
    initialTime: String = "08:00",
    onTimeSelected: (formattedTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = initialTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Hora") },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedHour = timePickerState.hour.toString().padStart(2, '0')
                    val formattedMinute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$formattedHour:$formattedMinute")
                    onDismiss()
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun GoogleDatePickerField(
    label: String = "Fecha de entrega",
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.ifBlank { "Seleccionar fecha..." },
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showModal = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showModal = true },
        shape = RoundedCornerShape(12.dp)
    )

    if (showModal) {
        GoogleDatePickerModal(
            onDateSelected = {
                onDateSelected(it)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun GoogleTimePickerField(
    label: String = "Hora",
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime.ifBlank { "08:00" },
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showModal = true }) {
                Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar hora")
            }
        },
        modifier = modifier
            .clickable { showModal = true },
        shape = RoundedCornerShape(12.dp)
    )

    if (showModal) {
        GoogleTimePickerModal(
            initialTime = selectedTime,
            onTimeSelected = {
                onTimeSelected(it)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}
