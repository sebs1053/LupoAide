package com.example.lupoaide.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.lupoaide.data.service.BlockerPermissionHelper
import com.example.lupoaide.data.service.SelectableAppInfo

data class OnboardingBlockedAppPlan(
    val packageName: String,
    val appName: String,
    val category: String,
    var goalType: String = "Plan Gradual",
    var initialMinutes: Int = 60,
    var targetMinutes: Int = 0,
    var planDays: Int = 21,
    var motivation: String = "Quiero concentrarme en mis estudios y no procrastinar"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (
        username: String,
        country: String,
        educationLevel: String,
        grade: String,
        institution: String,
        additionalInfo: String,
        language: String
    ) -> Unit,
    onAddInitialScheduleSlot: ((subject: String, day: String, start: String, end: String, room: String, teacher: String) -> Unit)? = null,
    onAddInitialBlockedApp: ((packageName: String, appName: String, category: String, goalType: String, initialMinutes: Int, targetMinutes: Int, planDays: Int, motivation: String) -> Unit)? = null
) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Español (Latinoamérica)") }
    var country by remember { mutableStateOf("México") }
    var educationLevel by remember { mutableStateOf("Preparatoria / Bachillerato") }
    var grade by remember { mutableStateOf("1° Semestre / Año") }
    var institution by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }

    // Control de menús desplegables
    var expandedLang by remember { mutableStateOf(false) }
    var expandedCountry by remember { mutableStateOf(false) }
    var expandedEdu by remember { mutableStateOf(false) }
    var expandedGrade by remember { mutableStateOf(false) }

    // Opciones
    val languages = listOf("Español (Latinoamérica)", "Español (España)", "English")
    val countries = listOf("México", "España", "Colombia", "Argentina", "Chile", "Perú", "Ecuador", "Guatemala", "Costa Rica", "Estados Unidos", "Otro")
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

    // Sección de horario inicial
    var showAddInitialSlot by remember { mutableStateOf(false) }
    var initialSubject by remember { mutableStateOf("") }
    var initialDay by remember { mutableStateOf("Lunes") }
    var initialStart by remember { mutableStateOf("08:00") }
    var initialEnd by remember { mutableStateOf("09:30") }
    var initialRoom by remember { mutableStateOf("") }
    var initialTeacher by remember { mutableStateOf("") }
    var expandedDay by remember { mutableStateOf(false) }
    val daysList = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    // Sección de Plan Gradual de Bloqueo de Apps
    var wantsToBlockApps by remember { mutableStateOf(false) }
    val availableApps = remember { BlockerPermissionHelper.getAvailableAppsToBlock(context) }
    val selectedAppPlans = remember { mutableStateListOf<OnboardingBlockedAppPlan>() }
    var isPermissionActive by remember { mutableStateOf(BlockerPermissionHelper.isAccessibilityServiceEnabled(context)) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Bienvenida y Avatar de Lupo
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Lupo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "¡Bienvenido a LupoAide!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configura tu perfil de estudio para personalizar tu experiencia, metas y horarios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Nombre / Apodo
            item {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Tu Nombre o Apodo de Estudiante *") },
                    placeholder = { Text("Ej. Alex, Sofía, Mike...") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_username_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Idioma (Desplegable)
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedLang,
                    onExpandedChange = { expandedLang = !expandedLang },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = language,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Idioma") },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLang) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLang,
                        onDismissRequest = { expandedLang = false }
                    ) {
                        languages.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    language = item
                                    expandedLang = false
                                }
                            )
                        }
                    }
                }
            }

            // País (Desplegable)
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedCountry,
                    onExpandedChange = { expandedCountry = !expandedCountry },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("País") },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountry) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCountry,
                        onDismissRequest = { expandedCountry = false }
                    ) {
                        countries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    country = item
                                    expandedCountry = false
                                }
                            )
                        }
                    }
                }
            }

            // Nivel de Estudios (Desplegable)
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedEdu,
                    onExpandedChange = { expandedEdu = !expandedEdu },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = educationLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nivel o Grado de Estudios") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEdu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEdu,
                        onDismissRequest = { expandedEdu = false }
                    ) {
                        educationLevels.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    educationLevel = item
                                    expandedEdu = false
                                }
                            )
                        }
                    }
                }
            }

            // Grado / Semestre / Año (Desplegable)
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedGrade,
                    onExpandedChange = { expandedGrade = !expandedGrade },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grado o Semestre actual") },
                        leadingIcon = { Icon(Icons.Default.Grade, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGrade) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGrade,
                        onDismissRequest = { expandedGrade = false }
                    ) {
                        grades.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    grade = item
                                    expandedGrade = false
                                }
                            )
                        }
                    }
                }
            }

            // Escuela / Institución
            item {
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Escuela o Universidad (Opcional)") },
                    placeholder = { Text("Ej. UNAM, Preparatoria #5, Tec...") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Espacio para añadir más información
            item {
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    label = { Text("Información adicional o metas de estudio") },
                    placeholder = { Text("Ej. Carrera de Ingeniería, enfocado en Matemáticas, preparar examen de admisión...") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // ==========================================
            // SECCIÓN: PLAN GRADUAL & BLOQUEO DE APPS
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Plan Gradual de Bloqueo de Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = wantsToBlockApps,
                                onCheckedChange = { wantsToBlockApps = it },
                                modifier = Modifier.testTag("onboarding_block_apps_switch")
                            )
                        }

                        Text(
                            text = "¿Quieres dejar de utilizar o reducir el tiempo en alguna app que te distraiga? Lupo te ayuda a crear un plan gradual para lograrlo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        AnimatedVisibility(visible = wantsToBlockApps) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Distraction guard security badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Protección de Enfoque y Privacidad Segura",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "LupoAide calcula tus metas diarias y te recompensa con EXP al superar las distracciones.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Selecciona las apps que quieres reducir o dejar:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Carrusel de apps sugeridas / instaladas
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(availableApps.take(12)) { app ->
                                        val isSelected = selectedAppPlans.any { it.packageName == app.packageName }
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelected) {
                                                    selectedAppPlans.removeAll { it.packageName == app.packageName }
                                                } else {
                                                    selectedAppPlans.add(
                                                        OnboardingBlockedAppPlan(
                                                            packageName = app.packageName,
                                                            appName = app.appName,
                                                            category = app.category,
                                                            initialMinutes = app.defaultDailyMinutes,
                                                            targetMinutes = 0,
                                                            planDays = 21
                                                        )
                                                    )
                                                }
                                            },
                                            label = { Text(app.appName) },
                                            leadingIcon = {
                                                Icon(
                                                    if (isSelected) Icons.Default.Check else Icons.Default.Block,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }

                                // Detalle y personalización de planes seleccionados
                                if (selectedAppPlans.isNotEmpty()) {
                                    Text(
                                        text = "Configuración del Plan Gradual (${selectedAppPlans.size} app${if (selectedAppPlans.size > 1) "s" else ""}):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    selectedAppPlans.forEachIndexed { index, plan ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.AppBlocking, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = plan.appName,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { selectedAppPlans.removeAt(index) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                                                    }
                                                }

                                                // Duración del Plan
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    listOf(14 to "14 días", 21 to "21 días (Ideal)", 30 to "30 días").forEach { (days, label) ->
                                                        ChoiceChip(
                                                            selected = plan.planDays == days,
                                                            onClick = {
                                                                selectedAppPlans[index] = plan.copy(planDays = days)
                                                            },
                                                            label = label
                                                        )
                                                    }
                                                }

                                                // Meta: Dejar por completo o Límite
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Dejar por completo (Meta 0 min)",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Switch(
                                                        checked = plan.targetMinutes == 0,
                                                        onCheckedChange = { leaveCompletely ->
                                                            selectedAppPlans[index] = plan.copy(
                                                                targetMinutes = if (leaveCompletely) 0 else 15
                                                            )
                                                        }
                                                    )
                                                }

                                                // Motivación
                                                OutlinedTextField(
                                                    value = plan.motivation,
                                                    onValueChange = {
                                                        selectedAppPlans[index] = plan.copy(motivation = it)
                                                    },
                                                    label = { Text("¿Por qué quieres reducirla?") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    singleLine = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Añadir Horario Escolar Inicial (Opcional)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Horario de Clases",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = { showAddInitialSlot = !showAddInitialSlot }) {
                                Text(if (showAddInitialSlot) "Ocultar" else "+ Añadir una clase")
                            }
                        }

                        Text(
                            text = "Puedes registrar tus materias ahora o gestionarlas más tarde en la pestaña de Horario.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        AnimatedVisibility(visible = showAddInitialSlot) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = initialSubject,
                                    onValueChange = { initialSubject = it },
                                    label = { Text("Materia") },
                                    placeholder = { Text("Ej. Cálculo, Historia...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedDay,
                                    onExpandedChange = { expandedDay = !expandedDay },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = initialDay,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Día") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedDay,
                                        onDismissRequest = { expandedDay = false }
                                    ) {
                                        daysList.forEach { d ->
                                            DropdownMenuItem(
                                                text = { Text(d) },
                                                onClick = {
                                                    initialDay = d
                                                    expandedDay = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = initialStart,
                                        onValueChange = { initialStart = it },
                                        label = { Text("Inicio") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = initialEnd,
                                        onValueChange = { initialEnd = it },
                                        label = { Text("Fin") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = initialRoom,
                                    onValueChange = { initialRoom = it },
                                    label = { Text("Aula o Salón (Opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Botones de acción: Comenzar / Omitir
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        // Guardar horario inicial si se ingresó
                        if (showAddInitialSlot && initialSubject.isNotBlank()) {
                            onAddInitialScheduleSlot?.invoke(
                                initialSubject,
                                initialDay,
                                initialStart,
                                initialEnd,
                                initialRoom,
                                initialTeacher
                            )
                        }

                        // Guardar apps seleccionadas para el plan gradual
                        if (wantsToBlockApps) {
                            selectedAppPlans.forEach { plan ->
                                onAddInitialBlockedApp?.invoke(
                                    plan.packageName,
                                    plan.appName,
                                    plan.category,
                                    plan.goalType,
                                    plan.initialMinutes,
                                    plan.targetMinutes,
                                    plan.planDays,
                                    plan.motivation
                                )
                            }
                        }

                        onCompleteOnboarding(
                            username.ifBlank { "Estudiante" },
                            country,
                            educationLevel,
                            grade,
                            institution,
                            additionalInfo,
                            language
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("complete_onboarding_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar y Comenzar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        onCompleteOnboarding(
                            username.ifBlank { "Estudiante" },
                            country,
                            educationLevel,
                            grade,
                            institution,
                            additionalInfo,
                            language
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("skip_schedule_onboarding_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Omitir horario y bloqueo por ahora")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

