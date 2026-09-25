package com.example.lupoaide.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.data.local.BlockedAppEntity
import com.example.lupoaide.data.local.UserProfileEntity
import com.example.lupoaide.ui.components.LUPO_OUTFITS
import com.example.lupoaide.ui.components.StreakCalendarHeatmapCard
import com.example.lupoaide.ui.components.WeeklyStudyStatsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    blockedApps: List<BlockedAppEntity> = emptyList(),
    isAiConnected: Boolean = true,
    aiTestResult: String? = null,
    isTestingAi: Boolean = false,
    onTestAiConnection: () -> Unit = {},
    onClearAiTestResult: () -> Unit = {},
    onSaveApiKey: (String) -> Unit = {},
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
    onActivateStreakToday: () -> Unit = {},
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
    onOpenBackpack: () -> Unit,
    onOpenShop: () -> Unit = {},
    onSelectThemeMode: (String) -> Unit = {},
    onPreviewSplash: () -> Unit = {}
) {
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    if (showSettingsScreen) {
        SettingsScreen(
            profile = profile,
            blockedApps = blockedApps,
            isAiConnected = isAiConnected,
            aiTestResult = aiTestResult,
            isTestingAi = isTestingAi,
            onTestAiConnection = onTestAiConnection,
            onClearAiTestResult = onClearAiTestResult,
            onUpdateNotificationPreferences = onUpdateNotificationPreferences,
            onSendTestNotification = onSendTestNotification,
            onSendTestStreakNotification = onSendTestStreakNotification,
            onToggleAppBlocked = onToggleAppBlocked,
            onUpdateBlockedApp = onUpdateBlockedApp,
            onDeleteBlockedApp = onDeleteBlockedApp,
            onAddBlockedApp = onAddBlockedApp,
            onSelectThemeMode = onSelectThemeMode,
            onPreviewSplash = onPreviewSplash,
            onBack = { showSettingsScreen = false }
        )
    } else {
        // =========================================================================
        // PERFIL PRINCIPAL LIMPIO Y ORGANIZADO
        // =========================================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("profile_clean_column"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera del Estudiante con botón destacado de Configuración
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Icono rápido de configuración en la esquina superior derecha
                        IconButton(
                            onClick = { showSettingsScreen = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .testTag("quick_settings_icon_btn")
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

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
                                    tint = MaterialTheme.colorScheme.tertiary,
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
                                text = "Nivel ${profile?.level ?: 1} • ${profile?.educationLevel ?: "Preparatoria"} (${profile?.grade ?: ""})",
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

                            Spacer(modifier = Modifier.height(18.dp))

                            // Botones de acción principales: Editar Perfil y Configuración
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showEditProfileDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Editar Perfil", fontSize = 13.sp)
                                }

                                Button(
                                    onClick = { showSettingsScreen = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_main_button")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Configuración", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Compañero Lupo (Skin Activa + Acceso a Tienda)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val activeOutfit = LUPO_OUTFITS.find { it.id == (profile?.activeOutfitId ?: "default") } ?: LUPO_OUTFITS.first()
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(activeOutfit.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (activeOutfit.imageRes != null) {
                                    Image(
                                        painter = painterResource(id = activeOutfit.imageRes),
                                        contentDescription = "Skin de Lupo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = activeOutfit.icon,
                                        contentDescription = "Skin de Lupo",
                                        tint = activeOutfit.color,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Lupo: ${activeOutfit.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${profile?.coins ?: 0} Monedas • ${activeOutfit.badge}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onOpenShop,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tienda")
                        }
                    }
                }
            }

            // Resumen de Métricas de Estudio
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
                    ProfileStatCard(
                        title = "Monedas",
                        value = "${profile?.coins ?: 0} 🪙",
                        icon = Icons.Default.Diamond,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
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
                    ProfileStatCard(
                        title = "Tiempo de Enfoque",
                        value = "${profile?.totalMinutesStudied ?: 0} min",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        title = "País / Idioma",
                        value = "${profile?.country ?: "México"}",
                        icon = Icons.Default.Public,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Calendario de Rachas (Heatmap mensual con escudos de congelación)
            item {
                StreakCalendarHeatmapCard(
                    profile = profile,
                    onOpenShop = onOpenShop
                )
            }

            // Estadísticas Semanales de Estudio y Concentración
            item {
                WeeklyStudyStatsCard(
                    profile = profile,
                    blockedApps = blockedApps
                )
            }

            // Acceso directo a Configuración al final
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSettingsScreen = true }
                        .testTag("settings_bottom_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Ajustes y Configuración",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tema claro/oscuro, alertas de racha y apps a bloquear",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Ir a configuración",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showEditProfileDialog && profile != null) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { updated ->
                onUpdateProfile(updated)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
