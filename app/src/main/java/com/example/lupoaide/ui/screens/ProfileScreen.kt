package com.example.lupoaide.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lupoaide.data.local.BlockedAppEntity
import com.example.lupoaide.data.local.UserProfileEntity
import com.example.lupoaide.data.service.BlockerPermissionHelper
import com.example.lupoaide.ui.components.LUPO_OUTFITS
import com.example.lupoaide.ui.components.LupoTimePickerDialog
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
    onOpenShop: () -> Unit = {}
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddBlockAppDialog by remember { mutableStateOf(false) }
    var editingBlockedApp by remember { mutableStateOf<BlockedAppEntity?>(null) }

    // Estado de la clave API
    var apiKeyInput by remember(profile?.customApiKey) { mutableStateOf(profile?.customApiKey ?: "") }
    var isApiKeySavedMessageVisible by remember { mutableStateOf(false) }

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

        // Estado del plan y protección sin alertas de privacidad
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasUsageStats) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ),
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (hasUsageStats) Icons.Default.VerifiedUser else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (hasUsageStats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Modo de Bloqueo Sin Alerta de Privacidad",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasUsageStats) "✅ Permiso de Bienestar Digital Oficial Activo"
                                else "⚠️ Requiere Permiso Estándar de Acceso a Uso",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasUsageStats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Text(
                        text = "Para evitar que Android muestre la alerta roja invasiva de 'Accesibilidad' al instalar la app, Lupo utiliza el estándar oficial 'Acceso a Datos de Uso' (Digital Wellbeing). Este método es 100% seguro, respeta tu privacidad y no lee tus contraseñas ni pantalla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                BlockerPermissionHelper.openUsageStatsSettings(context)
                                hasUsageStats = BlockerPermissionHelper.hasUsageStatsPermission(context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (hasUsageStats) "Ajustes de Uso (Configurado)" else "Activar Acceso de Uso Seguro", fontSize = 12.sp)
                        }
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

        // Lupo Wardrobe & Shop
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

        // =========================================================================
        // CONFIGURACIÓN DE INTELIGENCIA ARTIFICIAL (GEMINI 3.5 FLASH)
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
                                tint = MaterialTheme.colorScheme.primary,
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
                            color = if (isAiConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (isAiConnected) "Conectado" else "Sin Clave",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAiConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Lupo utiliza modelos Gemini de Google (con fallback automático a Gemini 3.5 Flash, Gemini Flash Latest y Gemini 2.5 Flash) para generar cursos, explicaciones, cuestionarios y flashcards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Seguridad GitHub",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔒 Seguridad GitHub: Puedes poner tu clave en el archivo local '.env' (en la raíz del proyecto) o ingresarla aquí en tu teléfono. El archivo .env y tus datos locales están en .gitignore y NUNCA se subirán a tu repositorio público.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Campo para ingresar API Key personalizada si lo desea
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            isApiKeySavedMessageVisible = false
                        },
                        label = { Text("Clave API de Google AI Studio (Opcional)") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        trailingIcon = {
                            if (apiKeyInput.isNotBlank()) {
                                IconButton(onClick = { apiKeyInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Borrar clave")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveApiKey(apiKeyInput)
                                isApiKeySavedMessageVisible = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar Clave")
                        }

                        FilledTonalButton(
                            onClick = onTestAiConnection,
                            enabled = !isTestingAi,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isTestingAi) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Probar IA")
                            }
                        }
                    }

                    if (isApiKeySavedMessageVisible) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ Clave API guardada en tu perfil local.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Resultado de la prueba de conexión
                    if (aiTestResult != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (aiTestResult.startsWith("✅")) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = aiTestResult,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onClearAiTestResult, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // NOTIFICACIONES Y RECORDATORIOS
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
                                tint = MaterialTheme.colorScheme.primary,
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

                    // Estado actual de la racha hoy
                    val isStreakActive = profile?.isStreakActiveToday == true
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isStreakActive) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = if (isStreakActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isStreakActive) "🔥 Racha de hoy: ACTIVADA (${profile?.studyStreak ?: 1} días)" else "⚡ Racha de hoy: PENDIENTE",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isStreakActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }

                                if (!isStreakActive) {
                                    Button(
                                        onClick = {
                                            onActivateStreakToday()
                                            testNotificationSentMsg = "🎉 ¡Racha activada con éxito! +25 EXP otorgados."
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Activar (+25 XP)", fontSize = 12.sp)
                                    }
                                }
                            }

                            Text(
                                text = if (isStreakActive) {
                                    "¡Gran trabajo! Ya estudiaste hoy. Las alertas de racha permanecerán silenciadas hasta mañana para no interrumpirte."
                                } else {
                                    "Aún no has activado tu racha hoy. Lupo te enviará alertas a las $streakTime hasta que ingreses a estudiar o hagas check-in."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider()

                    // ==========================================
                    // 1. RECORDATORIOS DE RACHA ("ACTIVA TU RACHA")
                    // ==========================================
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recordatorios de Racha Diaria",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Se envían hasta que actives tu racha para proteger tus días seguidos",
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
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⏰ Horario de aviso: $streakTime",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        OutlinedButton(
                                            onClick = { showStreakTimeDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Modificar Horario", fontSize = 12.sp)
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                val sent = onSendTestStreakNotification(false)
                                                testNotificationSentMsg = if (sent) "🔥 Notificación de racha enviada con éxito." else "⚠️ Habilita permisos de notificación en el sistema."
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text("🔥 Probar Alerta de Racha", fontSize = 11.sp, maxLines = 1)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val sent = onSendTestStreakNotification(true)
                                                testNotificationSentMsg = if (sent) "⚠️ Alerta urgente nocturna enviada con éxito." else "⚠️ Permiso no disponible."
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text("⚠️ Alerta Urgente", fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // ==========================================
                    // 2. ALERTA NOCTURNA DE MOCHILA
                    // ==========================================
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Backpack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Alerta Nocturna de Mochila",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Avisa qué cuadernos y útiles empacar según tus clases de mañana",
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
                                shape = RoundedCornerShape(12.dp),
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
                                        Text("Modificar Horario", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // ==========================================
                    // 3. RECORDATORIOS DE EXÁMENES
                    // ==========================================
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recordatorios de Exámenes",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Avisos para repasar antes de fechas de evaluación",
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Anticipación:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                listOf(12, 24, 48).forEach { hours ->
                                    val isSelected = examHoursBefore == hours
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
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
                                            testNotificationSentMsg = "📅 Anticipación de exámenes fijada en $hours horas antes."
                                        },
                                        label = { Text("${hours}h antes", fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Botón de prueba general
                    FilledTonalButton(
                        onClick = {
                            val sent = onSendTestNotification()
                            testNotificationSentMsg = if (sent) "✅ Notificación de prueba general enviada al sistema." else "⚠️ Permiso de notificaciones deshabilitado en el sistema."
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar Notificación de Prueba General")
                    }

                    if (testNotificationSentMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testNotificationSentMsg!!,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
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

    // Diálogo: Selector de Horario para Recordatorio de Racha
    if (showStreakTimeDialog) {
        LupoTimePickerDialog(
            title = "Horario de Recordatorio de Racha",
            initialTime = streakTime,
            presets = listOf("17:00", "18:00", "19:00", "20:00", "21:00", "22:00"),
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

    // Diálogo: Selector de Horario para Alerta Nocturna de Mochila
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
