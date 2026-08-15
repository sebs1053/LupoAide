package com.example.lupoaide.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.data.local.BlockedAppEntity
import com.example.lupoaide.data.local.UserProfileEntity
import com.example.lupoaide.data.service.BlockerPermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    blockedApps: List<BlockedAppEntity> = emptyList(),
    onUpdateProfile: (UserProfileEntity) -> Unit,
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
    onOpenBackpack: () -> Unit
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddBlockAppDialog by remember { mutableStateOf(false) }
    var editingBlockedApp by remember { mutableStateOf<BlockedAppEntity?>(null) }

    val isPermissionGranted = remember { BlockerPermissionHelper.isAccessibilityServiceEnabled(context) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera de Perfil
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile?.username?.ifBlank { "Estudiante" } ?: "Estudiante",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nivel ${profile?.level ?: 1} • ${profile?.educationLevel ?: "Preparatoria"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (profile?.institution?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🏫 ${profile.institution}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Estadísticas de Estudio y Gamificación
        item {
            Text(
                text = "Estadísticas de Estudio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Monedas",
                    value = "${profile?.coins ?: 0} 🪙",
                    icon = Icons.Default.Diamond,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Racha de Estudio",
                    value = "${profile?.studyStreak ?: 1} días 🔥",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Tiempo de Enfoque",
                    value = "${profile?.totalMinutesStudied ?: 0} min",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "País / Idioma",
                    value = "${profile?.country ?: "México"}",
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // =========================================================================
        // RESUMEN DEL PLAN GRADUAL Y BLOQUEADOR DE APPS (Requerimiento explícito)
        // =========================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Plan Gradual de Bloqueo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = { showAddBlockAppDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_blocked_app_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir App", fontSize = 13.sp)
                }
            }
        }

        // Estado del plan y protección
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Control Gradual de Enfoque Activo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lupo calcula tus límites y registra tu constancia para vencer las distracciones.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Lista de Apps en Plan Gradual
        if (blockedApps.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AppBlocking,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin aplicaciones en plan de bloqueo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toca en «Añadir App» para configurar metas de reducción gradual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(blockedApps) { app ->
                val planDay = BlockerPermissionHelper.calculatePlanDay(app.startTimestamp, app.planDurationDays)
                val currentAllowed = BlockerPermissionHelper.calculateCurrentAllowedMinutes(
                    initialDailyMinutes = app.initialDailyLimitMinutes,
                    targetDailyMinutes = app.targetDailyLimitMinutes,
                    planDurationDays = app.planDurationDays,
                    startTimestamp = app.startTimestamp
                )
                val progressFloat = (planDay.toFloat() / app.planDurationDays.toFloat()).coerceIn(0f, 1f)

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${app.appCategory} • ${if (app.targetDailyLimitMinutes == 0) "Meta: 0 min (Dejar)" else "Meta: ${app.targetDailyLimitMinutes} min"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = app.isBlocked,
                                    onCheckedChange = { onToggleAppBlocked(app) }
                                )
                            }
                        }

                        // Barra de progreso del plan gradual
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Día $planDay de ${app.planDurationDays} (${(progressFloat * 100).toInt()}% cumplido)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Límite hoy: $currentAllowed min",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        // Motivación
                        if (app.motivationReason.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "«${app.motivationReason}»",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Botones de editar y borrar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { editingBlockedApp = app },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar Meta", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDeleteBlockedApp(app.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Información académica registrada
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Datos Académicos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "• Grado/Semestre: ${profile?.grade ?: "1° Semestre"}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "• Idioma: ${profile?.language ?: "Español"}", style = MaterialTheme.typography.bodyMedium)
                    if (profile?.additionalInfo?.isNotBlank() == true) {
                        Text(text = "• Metas / Notas: ${profile.additionalInfo}", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Datos de Perfil")
                    }
                }
            }
        }

        // Acciones
        item {
            Button(
                onClick = onOpenBackpack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_open_backpack_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Backpack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Revisar Mochila para Mañana")
            }
        }
    }

    // =========================================================================
    // DIÁLOGOS DE EDICIÓN Y AÑADIR APPS
    // =========================================================================

    // Diálogo: Añadir App al Plan de Bloqueo
    if (showAddBlockAppDialog) {
        AddBlockedAppDialog(
            onDismiss = { showAddBlockAppDialog = false },
            onConfirm = { pkg, name, cat, goal, initMin, targetMin, days, mot ->
                onAddBlockedApp(pkg, name, cat, goal, initMin, targetMin, days, mot)
                showAddBlockAppDialog = false
            }
        )
    }

    // Diálogo: Editar App y Metas Existentes
    if (editingBlockedApp != null) {
        val app = editingBlockedApp!!
        EditBlockedAppGoalDialog(
            app = app,
            onDismiss = { editingBlockedApp = null },
            onConfirm = { updated ->
                onUpdateBlockedApp(updated)
                editingBlockedApp = null
            }
        )
    }

    // Diálogo: Editar Perfil de Estudiante
    if (showEditProfileDialog && profile != null) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { updatedProfile ->
                onUpdateProfile(updatedProfile)
                showEditProfileDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        title = { Text("Añadir App al Plan Gradual") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Selecciona una aplicación sugerida o ingresa una:", style = MaterialTheme.typography.labelMedium)
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
                        listOf(14 to "14 días", 21 to "21 días (Ideal)", 30 to "30 días").forEach { (d, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (planDurationDays == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { planDurationDays = d }
                            ) {
                                Text(
                                    text = label,
                                    color = if (planDurationDays == d) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (planDurationDays == d) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Meta Final: Dejar por completo", style = MaterialTheme.typography.labelMedium)
                            Text(if (targetMinutes == 0) "0 min / día (Bloqueo total)" else "$targetMinutes min / día", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = targetMinutes == 0,
                            onCheckedChange = { leaveCompletely ->
                                targetMinutes = if (leaveCompletely) 0 else 15
                            }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = motivation,
                        onValueChange = { motivation = it },
                        label = { Text("¿Por qué quieres reducirla? (Motivación)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val appToSave = selectedApp
                    val appName = customAppName.ifBlank { appToSave?.appName ?: "App" }
                    val pkg = appToSave?.packageName ?: "com.app.${appName.lowercase().replace(" ", "")}"
                    val cat = appToSave?.category ?: "Redes y Ocio"

                    onConfirm(pkg, appName, cat, "Plan Gradual", initialMinutes, targetMinutes, planDurationDays, motivation)
                }
            ) {
                Text("Crear Plan")
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
private fun EditBlockedAppGoalDialog(
    app: BlockedAppEntity,
    onDismiss: () -> Unit,
    onConfirm: (BlockedAppEntity) -> Unit
) {
    var planDurationDays by remember { mutableIntStateOf(app.planDurationDays) }
    var targetMinutes by remember { mutableIntStateOf(app.targetDailyLimitMinutes) }
    var initialMinutes by remember { mutableIntStateOf(app.initialDailyLimitMinutes) }
    var motivation by remember { mutableStateOf(app.motivationReason) }
    var isBlocked by remember { mutableStateOf(app.isBlocked) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Meta: ${app.appName}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bloqueo Activo", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBlocked, onCheckedChange = { isBlocked = it })
                }

                Text("Duración del Plan Gradual:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(14 to "14 días", 21 to "21 días", 30 to "30 días").forEach { (d, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (planDurationDays == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { planDurationDays = d }
                        ) {
                            Text(
                                text = label,
                                color = if (planDurationDays == d) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dejar por completo (Meta 0 min)", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = targetMinutes == 0,
                        onCheckedChange = { leaveCompletely ->
                            targetMinutes = if (leaveCompletely) 0 else 15
                        }
                    )
                }

                OutlinedTextField(
                    value = motivation,
                    onValueChange = { motivation = it },
                    label = { Text("Motivo / Recordatorio de Lupo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        app.copy(
                            planDurationDays = planDurationDays,
                            targetDailyLimitMinutes = targetMinutes,
                            initialDailyLimitMinutes = initialMinutes,
                            motivationReason = motivation,
                            isBlocked = isBlocked
                        )
                    )
                }
            ) {
                Text("Actualizar Meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    profile: UserProfileEntity,
    onDismiss: () -> Unit,
    onConfirm: (UserProfileEntity) -> Unit
) {
    var editUsername by remember { mutableStateOf(profile.username) }
    var editEdu by remember { mutableStateOf(profile.educationLevel) }
    var editGrade by remember { mutableStateOf(profile.grade) }
    var editInstitution by remember { mutableStateOf(profile.institution) }
    var editInfo by remember { mutableStateOf(profile.additionalInfo) }
    var expandedEdu by remember { mutableStateOf(false) }
    var expandedGrade by remember { mutableStateOf(false) }

    val educationLevels = listOf(
        "Preparatoria / Bachillerato",
        "Universidad / Licenciatura / Ingeniería",
        "Secundaria / ESO",
        "Carrera Técnica / Formación Profesional",
        "Maestría / Posgrado",
        "Primaria"
    )
    val grades = listOf(
        "1° Semestre / Año",
        "2° Semestre / Año",
        "3° Semestre / Año",
        "4° Semestre / Año",
        "5° Semestre / Año",
        "6° Semestre / Año",
        "7° Semestre o superior",
        "1° Grado",
        "2° Grado",
        "3° Grado"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil de Estudiante") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Nombre o Apodo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedEdu,
                        onExpandedChange = { expandedEdu = !expandedEdu },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editEdu,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Nivel de Estudios") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEdu) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEdu,
                            onDismissRequest = { expandedEdu = false }
                        ) {
                            educationLevels.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        editEdu = item
                                        expandedEdu = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedGrade,
                        onExpandedChange = { expandedGrade = !expandedGrade },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editGrade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grado o Semestre") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGrade) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGrade,
                            onDismissRequest = { expandedGrade = false }
                        ) {
                            grades.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        editGrade = item
                                        expandedGrade = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = editInstitution,
                        onValueChange = { editInstitution = it },
                        label = { Text("Escuela / Universidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = editInfo,
                        onValueChange = { editInfo = it },
                        label = { Text("Notas / Metas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        profile.copy(
                            username = editUsername.ifBlank { "Estudiante" },
                            educationLevel = editEdu,
                            grade = editGrade,
                            institution = editInstitution,
                            additionalInfo = editInfo
                        )
                    )
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
