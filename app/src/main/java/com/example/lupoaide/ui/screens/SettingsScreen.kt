package com.example.lupoaide.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.data.local.BlockedAppEntity
import com.example.lupoaide.data.local.UserProfileEntity
import com.example.lupoaide.data.service.BlockerPermissionHelper
import com.example.lupoaide.ui.components.LupoTimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profile: UserProfileEntity?,
    blockedApps: List<BlockedAppEntity> = emptyList(),
    isAiConnected: Boolean = true,
    aiTestResult: String? = null,
    isTestingAi: Boolean = false,
    onTestAiConnection: () -> Unit = {},
    onClearAiTestResult: () -> Unit = {},
    onUpdateNotificationPreferences: (
        notifications: Boolean,
        exams: Boolean,
        backpack: Boolean,
        streakReminders: Boolean,
        streakTime: String,
        backpackTime: String,
        examHoursBefore: Int
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onSendTestNotification: () -> Boolean = { true },
    onSendTestStreakNotification: (isUrgent: Boolean) -> Boolean = { false },
    onToggleAppBlocked: (BlockedAppEntity) -> Unit = {},
    onUpdateBlockedApp: (BlockedAppEntity) -> Unit = {},
    onDeleteBlockedApp: (Int) -> Unit = {},
    onAddBlockedApp: (
        packageName: String,
        appName: String,
        category: String,
        goalType: String,
        initialMinutes: Int,
        targetMinutes: Int,
        planDays: Int,
        motivation: String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onSelectThemeMode: (String) -> Unit = {},
    onPreviewSplash: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Soporte al botón atrás del sistema Android
    BackHandler { onBack() }

    var showAddBlockAppDialog by remember { mutableStateOf(false) }
    var editingBlockedApp by remember { mutableStateOf<BlockedAppEntity?>(null) }

    // Estados de permisos de bloqueo
    var hasUsageStats by remember { mutableStateOf(BlockerPermissionHelper.hasUsageStatsPermission(context)) }
    val isAccessibilityGranted = remember { BlockerPermissionHelper.isAccessibilityServiceEnabled(context) }

    // Notificaciones y Recordatorios Configurables
    var notificationsEnabled by remember(profile?.notificationsEnabled) { mutableStateOf(profile?.notificationsEnabled ?: true) }
    var examReminders by remember(profile?.examRemindersEnabled) { mutableStateOf(profile?.examRemindersEnabled ?: true) }
    var backpackReminders by remember(profile?.backpackRemindersEnabled) { mutableStateOf(profile?.backpackRemindersEnabled ?: true) }
    var streakReminders by remember(profile?.streakRemindersEnabled) { mutableStateOf(profile?.streakRemindersEnabled ?: true) }
    var streakTime by remember(profile?.streakReminderTime) { mutableStateOf(profile?.streakReminderTime ?: "19:00") }
    var backpackTime by remember(profile?.backpackReminderTime) { mutableStateOf(profile?.backpackReminderTime ?: "20:00") }
    var examHoursBefore by remember(profile?.examReminderHoursBefore) { mutableIntStateOf(profile?.examReminderHoursBefore ?: 24) }
    var testNotificationSentMsg by remember { mutableStateOf<String?>(null) }

    var showStreakTimeDialog by remember { mutableStateOf(false) }
    var showBackpackTimeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera con botón de Volver
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_from_settings_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Perfil",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tema, alertas y bloqueo de distracciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 1. TEMA Y APARIENCIA (MODO OSCURO / CLARO / SISTEMA)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_selection_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = "Tema de la app",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tema y Apariencia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Elige el modo de visualización de Lupo Aide",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val currentThemeMode = profile?.themeMode ?: "SYSTEM"

                    val themeOptions = listOf(
                        Triple("SYSTEM", "Por defecto del sistema", "Sigue la configuración de Android"),
                        Triple("LIGHT", "Modo Claro", "Blanco #FFFFFF • Secundario #b5bbc3 • Terciario #99c5ff"),
                        Triple("DARK", "Modo Oscuro", "Azul Marino #000c3d • Secundario #4f5058 • Terciario #99c5ff")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        themeOptions.forEach { (modeKey, title, subtitle) ->
                            val isSelected = currentThemeMode == modeKey
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                                } else {
                                    null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectThemeMode(modeKey) }
                                    .testTag("theme_option_$modeKey")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onSelectThemeMode(modeKey) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = when (modeKey) {
                                                        "LIGHT" -> Icons.Default.LightMode
                                                        "DARK" -> Icons.Default.DarkMode
                                                        else -> Icons.Default.BrightnessAuto
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = onPreviewSplash,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_splash_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver pantalla de carga de este modo")
                    }
                }
            }
        }

        // =========================================================================
        // 2. NOTIFICACIONES Y RECORDATORIOS
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Encabezado con Interruptor Maestro
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notificaciones y Recordatorios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                onUpdateNotificationPreferences(
                                    notificationsEnabled,
                                    examReminders,
                                    backpackReminders,
                                    streakReminders,
                                    streakTime,
                                    backpackTime,
                                    examHoursBefore
                                )
                            }
                        )
                    }

                    Text(
                        text = "Configura alertas inteligentes y personaliza los horarios exactos en los que deseas que Lupo te avise.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    // Racha Diaria
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recordatorio de Racha Diaria",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Aviso vespertino antes de medianoche",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = streakReminders && notificationsEnabled,
                                enabled = notificationsEnabled,
                                onCheckedChange = {
                                    streakReminders = it
                                    onUpdateNotificationPreferences(
                                        notificationsEnabled,
                                        examReminders,
                                        backpackReminders,
                                        streakReminders,
                                        streakTime,
                                        backpackTime,
                                        examHoursBefore
                                    )
                                }
                            )
                        }

                        if (streakReminders && notificationsEnabled) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏰ Horario de alerta: $streakTime",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    OutlinedButton(
                                        onClick = { showStreakTimeDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Cambiar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Mochila Inteligente
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alerta Nocturna de Mochila",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Recuérdame empacar útiles antes de dormir",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = backpackReminders && notificationsEnabled,
                                enabled = notificationsEnabled,
                                onCheckedChange = {
                                    backpackReminders = it
                                    onUpdateNotificationPreferences(
                                        notificationsEnabled,
                                        examReminders,
                                        backpackReminders,
                                        streakReminders,
                                        streakTime,
                                        backpackTime,
                                        examHoursBefore
                                    )
                                }
                            )
                        }

                        if (backpackReminders && notificationsEnabled) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏰ Horario de alerta: $backpackTime",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    OutlinedButton(
                                        onClick = { showBackpackTimeDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Cambiar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Recordatorios de Exámenes
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recordatorios de Exámenes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Avisos de repaso previo a evaluaciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = examReminders && notificationsEnabled,
                                enabled = notificationsEnabled,
                                onCheckedChange = {
                                    examReminders = it
                                    onUpdateNotificationPreferences(
                                        notificationsEnabled,
                                        examReminders,
                                        backpackReminders,
                                        streakReminders,
                                        streakTime,
                                        backpackTime,
                                        examHoursBefore
                                    )
                                }
                            )
                        }

                        if (examReminders && notificationsEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(12 to "12h antes", 24 to "24h antes", 48 to "48h antes").forEach { (hours, label) ->
                                    val isSel = examHoursBefore == hours
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                examHoursBefore = hours
                                                onUpdateNotificationPreferences(
                                                    notificationsEnabled,
                                                    examReminders,
                                                    backpackReminders,
                                                    streakReminders,
                                                    streakTime,
                                                    backpackTime,
                                                    hours
                                                )
                                            }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botones de prueba de notificación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSendTestNotification()
                                testNotificationSentMsg = "✅ Notificación de prueba enviada al sistema."
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Probar Alerta", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onSendTestStreakNotification(false)
                                testNotificationSentMsg = "🔥 Notificación de racha enviada."
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Probar Racha", fontSize = 12.sp)
                        }
                    }

                    testNotificationSentMsg?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 3. BLOQUEADOR DE APLICACIONES Y PLAN GRADUAL
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bloqueador de Apps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalButton(
                            onClick = { showAddBlockAppDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir App", fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "Reduce gradualmente el uso de aplicaciones que te distraen mientras estudias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Estado de permisos
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (hasUsageStats && isAccessibilityGranted) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasUsageStats && isAccessibilityGranted) "🛡️ Protección Activa" else "⚠️ Permisos Requeridos",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = if (hasUsageStats && isAccessibilityGranted) {
                                        "Acceso a uso y servicio de accesibilidad concedidos."
                                    } else {
                                        "Se requiere acceso de uso y accesibilidad para el bloqueo."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!hasUsageStats || !isAccessibilityGranted) {
                                OutlinedButton(
                                    onClick = {
                                        if (!hasUsageStats) BlockerPermissionHelper.openUsageStatsSettings(context)
                                        else BlockerPermissionHelper.openAccessibilitySettings(context)
                                        hasUsageStats = BlockerPermissionHelper.hasUsageStatsPermission(context)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Activar", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Lista de aplicaciones en el plan
                    if (blockedApps.isEmpty()) {
                        Text(
                            text = "No tienes aplicaciones en tu plan de bloqueo. Pulsa 'Añadir App' para comenzar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            blockedApps.forEach { app ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.PhoneAndroid,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = app.appName,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "${app.appCategory} • Límite: ${app.targetDailyLimitMinutes} min/día",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(
                                                checked = app.isBlocked,
                                                onCheckedChange = { onToggleAppBlocked(app) }
                                            )
                                            IconButton(
                                                onClick = { editingBlockedApp = app },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 4. ESTADO DE LUPO IA (GOOGLE GEMINI - CLAVE MAESTRA ACTIVA)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Inteligencia Artificial (Lupo IA)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Activa",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Banner de IA activa sin necesidad de clave
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "IA Lista",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Clave Maestra de Gemini Configurada",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Lupo IA está habilitado para tutorías, creación de cursos, quizzes interactivos y validación de tareas sin que tengas que ingresar nada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = onTestAiConnection,
                        enabled = !isTestingAi,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTestingAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comprobando conexión...")
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar conexión con Lupo IA")
                        }
                    }

                    aiTestResult?.let { result ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (result.startsWith("✅")) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onClearAiTestResult, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Selector de Hora de Racha
    if (showStreakTimeDialog) {
        LupoTimePickerDialog(
            title = "Horario de Alerta de Racha Diaria",
            initialTime = streakTime,
            presets = listOf("18:00", "19:00", "20:00", "20:30", "21:00", "21:30", "22:00", "22:30"),
            onDismiss = { showStreakTimeDialog = false },
            onTimeSelected = { newTime ->
                streakTime = newTime
                showStreakTimeDialog = false
                onUpdateNotificationPreferences(
                    notificationsEnabled,
                    examReminders,
                    backpackReminders,
                    streakReminders,
                    newTime,
                    backpackTime,
                    examHoursBefore
                )
                testNotificationSentMsg = "⏰ Horario de recordatorio de racha actualizado a las $newTime."
            }
        )
    }

    // Selector de Hora de Mochila
    if (showBackpackTimeDialog) {
        LupoTimePickerDialog(
            title = "Horario de Alerta Nocturna de Mochila",
            initialTime = backpackTime,
            presets = listOf("19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00"),
            onDismiss = { showBackpackTimeDialog = false },
            onTimeSelected = { newTime ->
                backpackTime = newTime
                showBackpackTimeDialog = false
                onUpdateNotificationPreferences(
                    notificationsEnabled,
                    examReminders,
                    backpackReminders,
                    streakReminders,
                    streakTime,
                    newTime,
                    examHoursBefore
                )
                testNotificationSentMsg = "🎒 Horario de alerta de mochila actualizado a las $newTime."
            }
        )
    }

    // Diálogo Añadir App a Bloquear
    if (showAddBlockAppDialog) {
        AddBlockedAppDialog(
            onDismiss = { showAddBlockAppDialog = false },
            onConfirm = { pkg, name, cat, goal, initM, targetM, days, mot ->
                onAddBlockedApp(pkg, name, cat, goal, initM, targetM, days, mot)
                showAddBlockAppDialog = false
            }
        )
    }

    // Diálogo Editar App Bloqueada
    editingBlockedApp?.let { app ->
        EditBlockedAppDialog(
            app = app,
            onDismiss = { editingBlockedApp = null },
            onUpdate = { updatedApp ->
                onUpdateBlockedApp(updatedApp)
                editingBlockedApp = null
            },
            onDelete = { id ->
                onDeleteBlockedApp(id)
                editingBlockedApp = null
            }
        )
    }
}

@Composable
private fun AddBlockedAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        packageName: String,
        appName: String,
        category: String,
        goalType: String,
        initialMinutes: Int,
        targetMinutes: Int,
        planDays: Int,
        motivation: String
    ) -> Unit
) {
    val context = LocalContext.current
    val availableApps = remember { BlockerPermissionHelper.getAvailableAppsToBlock(context) }

    var selectedApp by remember { mutableStateOf(availableApps.firstOrNull()) }
    var customAppName by remember { mutableStateOf("") }
    var planDurationDays by remember { mutableIntStateOf(21) }
    var targetMinutes by remember { mutableIntStateOf(0) }
    var initialMinutes by remember { mutableIntStateOf(60) }
    var motivation by remember { mutableStateOf("Quiero tener más tiempo para estudiar y pasar mis materias") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir App al Plan de Bloqueo") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Selecciona una aplicación sugerida:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(availableApps.take(10)) { app ->
                            FilterChip(
                                selected = selectedApp?.packageName == app.packageName,
                                onClick = {
                                    selectedApp = app
                                    customAppName = app.appName
                                    initialMinutes = app.defaultDailyMinutes
                                },
                                label = { Text(app.appName) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = customAppName.ifBlank { selectedApp?.appName ?: "" },
                        onValueChange = { customAppName = it },
                        label = { Text("Nombre de la aplicación") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Text("Duración del Plan Gradual:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(14 to "14 días", 21 to "21 días", 30 to "30 días").forEach { (d, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (planDurationDays == d) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { planDurationDays = d }
                            ) {
                                Text(
                                    text = label,
                                    color = if (planDurationDays == d) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (planDurationDays == d) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Límite diario deseado: $targetMinutes min", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = targetMinutes.toFloat(),
                        onValueChange = { targetMinutes = it.toInt() },
                        valueRange = 0f..120f,
                        steps = 23
                    )
                }

                item {
                    OutlinedTextField(
                        value = motivation,
                        onValueChange = { motivation = it },
                        label = { Text("¿Por qué quieres reducirla?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = customAppName.ifBlank { selectedApp?.appName ?: "App Bloqueada" }
                    val finalPkg = selectedApp?.packageName ?: "custom.${finalName.lowercase().replace(" ", "")}"
                    onConfirm(
                        finalPkg,
                        finalName,
                        selectedApp?.category ?: "Redes Sociales",
                        if (targetMinutes == 0) "QUIT_COMPLETELY" else "GRADUAL_REDUCTION",
                        initialMinutes,
                        targetMinutes,
                        planDurationDays,
                        motivation
                    )
                }
            ) {
                Text("Guardar en el Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditBlockedAppDialog(
    app: BlockedAppEntity,
    onDismiss: () -> Unit,
    onUpdate: (BlockedAppEntity) -> Unit,
    onDelete: (Int) -> Unit
) {
    var targetMinutes by remember { mutableIntStateOf(app.targetDailyLimitMinutes) }
    var isBlocked by remember { mutableStateOf<Boolean>(app.isBlocked) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar: ${app.appName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bloqueo activado:", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBlocked, onCheckedChange = { isBlocked = it })
                }

                Column {
                    Text("Meta diaria: $targetMinutes min", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = targetMinutes.toFloat(),
                        onValueChange = { targetMinutes = it.toInt() },
                        valueRange = 0f..180f,
                        steps = 17
                    )
                }

                val currentDay = BlockerPermissionHelper.calculatePlanDay(app.startTimestamp, app.planDurationDays)
                Text(
                    text = "Día $currentDay de ${app.planDurationDays} del plan gradual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        app.copy(
                            targetDailyLimitMinutes = targetMinutes,
                            isBlocked = isBlocked
                        )
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(app.id) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
