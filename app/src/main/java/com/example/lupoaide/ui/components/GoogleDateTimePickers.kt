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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun formatTimeTo12Hour(timeStr: String): String {
    val clean = timeStr.trim()
    if (clean.isBlank()) return "08:00 AM"

    // If already contains AM or PM
    if (clean.contains("AM", ignoreCase = true) || clean.contains("PM", ignoreCase = true)) {
        return clean.uppercase()
    }

    val parts = clean.split(":")
    val rawHour = parts.getOrNull(0)?.toIntOrNull() ?: return clean
    val rawMinute = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0

    val period = if (rawHour >= 12) "PM" else "AM"
    val hour12 = when {
        rawHour == 0 -> 12
        rawHour > 12 -> rawHour - 12
        else -> rawHour
    }
    val minStr = rawMinute.toString().padStart(2, '0')
    val hourStr = hour12.toString().padStart(2, '0')
    return "$hourStr:$minStr $period"
}

fun parseTimeTo24HourComponents(timeStr: String): Pair<Int, Int> {
    val clean = timeStr.trim().uppercase()
    val isPm = clean.contains("PM")
    val isAm = clean.contains("AM")

    val stripped = clean.replace("AM", "").replace("PM", "").trim()
    val parts = stripped.split(":")
    var hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

    if (isPm && hour < 12) {
        hour += 12
    } else if (isAm && hour == 12) {
        hour = 0
    }
    return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

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
    initialTime: String = "08:00 AM",
    onTimeSelected: (formattedTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    val (parsedHour, parsedMinute) = parseTimeTo24HourComponents(initialTime)

    val timePickerState = rememberTimePickerState(
        initialHour = parsedHour,
        initialMinute = parsedMinute,
        is24Hour = false // Habilita selector AM y PM oficial de Material 3
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Seleccionar Hora", style = MaterialTheme.typography.titleLarge)
                Text("Formato de 12 horas con AM / PM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawHour = timePickerState.hour
                    val minute = timePickerState.minute
                    val period = if (rawHour >= 12) "PM" else "AM"
                    val hour12 = when {
                        rawHour == 0 -> 12
                        rawHour > 12 -> rawHour - 12
                        else -> rawHour
                    }
                    val formatted = "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
                    onTimeSelected(formatted)
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
    val displayValue = if (selectedTime.isNotBlank()) formatTimeTo12Hour(selectedTime) else "08:00 AM"

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showModal = true }) {
                Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar hora con AM/PM")
            }
        },
        modifier = modifier
            .clickable { showModal = true },
        shape = RoundedCornerShape(12.dp)
    )

    if (showModal) {
        GoogleTimePickerModal(
            initialTime = displayValue,
            onTimeSelected = {
                onTimeSelected(it)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}
