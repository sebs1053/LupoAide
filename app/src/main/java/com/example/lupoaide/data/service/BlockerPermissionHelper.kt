package com.example.lupoaide.data.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import java.util.concurrent.TimeUnit

data class SelectableAppInfo(
    val packageName: String,
    val appName: String,
    val category: String,
    val isInstalled: Boolean = true,
    val defaultDailyMinutes: Int = 60
)

object BlockerPermissionHelper {

    // Presets de las aplicaciones que más causan distracción en estudiantes
    val POPULAR_DISTRACTION_APPS = listOf(
        SelectableAppInfo("com.zhiliaoapp.musically", "TikTok", "Redes Sociales", defaultDailyMinutes = 60),
        SelectableAppInfo("com.instagram.android", "Instagram", "Redes Sociales", defaultDailyMinutes = 50),
        SelectableAppInfo("com.google.android.youtube", "YouTube", "Streaming / Video", defaultDailyMinutes = 60),
        SelectableAppInfo("com.facebook.katana", "Facebook", "Redes Sociales", defaultDailyMinutes = 45),
        SelectableAppInfo("com.twitter.android", "X (Twitter)", "Redes Sociales", defaultDailyMinutes = 30),
        SelectableAppInfo("com.snapchat.android", "Snapchat", "Redes Sociales", defaultDailyMinutes = 30),
        SelectableAppInfo("com.netflix.mediaclient", "Netflix", "Streaming / Series", defaultDailyMinutes = 90),
        SelectableAppInfo("tv.twitch.android.app", "Twitch", "Streaming / Juegos", defaultDailyMinutes = 60),
        SelectableAppInfo("com.reddit.frontpage", "Reddit", "Redes Sociales", defaultDailyMinutes = 45),
        SelectableAppInfo("com.discord", "Discord", "Mensajería / Gaming", defaultDailyMinutes = 45),
        SelectableAppInfo("com.roblox.client", "Roblox", "Juegos", defaultDailyMinutes = 60),
        SelectableAppInfo("com.dts.freefireth", "Free Fire", "Juegos", defaultDailyMinutes = 60),
        SelectableAppInfo("com.supercell.brawlstars", "Brawl Stars", "Juegos", defaultDailyMinutes = 45),
        SelectableAppInfo("com.supercell.clashroyale", "Clash Royale", "Juegos", defaultDailyMinutes = 40),
        SelectableAppInfo("com.whatsapp", "WhatsApp", "Mensajería", defaultDailyMinutes = 60)
    )

    /**
     * Obtiene la lista combinada de aplicaciones instaladas en el dispositivo
     * junto con los presets populares para fácil selección.
     */
    fun getAvailableAppsToBlock(context: Context): List<SelectableAppInfo> {
        val result = mutableListOf<SelectableAppInfo>()
        val seenPackages = mutableSetOf<String>()

        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in resolvedInfos) {
                val pkg = resolveInfo.activityInfo.packageName
                // Ignorar la propia app y configuraciones críticas
                if (pkg == context.packageName || pkg == "com.android.settings") continue

                val appName = resolveInfo.loadLabel(pm).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val category = guessCategory(pkg, appName, isSystem)
                seenPackages.add(pkg)
                result.add(
                    SelectableAppInfo(
                        packageName = pkg,
                        appName = appName,
                        category = category,
                        isInstalled = true
                    )
                )
            }
        } catch (e: Exception) {
            // Manejo seguro si queryIntentActivities falla
        }

        // Agregar presets populares que aún no estén en la lista (por si está en emulador o no instaladas)
        for (preset in POPULAR_DISTRACTION_APPS) {
            if (!seenPackages.contains(preset.packageName)) {
                result.add(preset.copy(isInstalled = false))
                seenPackages.add(preset.packageName)
            }
        }

        return result.sortedWith(compareBy({ !it.isInstalled }, { it.appName }))
    }

    private fun guessCategory(packageName: String, appName: String, isSystem: Boolean): String {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()

        return when {
            lowerPkg.contains("game") || lowerName.contains("juego") || lowerPkg.contains("supercell") || lowerPkg.contains("roblox") -> "Juegos"
            lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") || lowerPkg.contains("facebook") || lowerPkg.contains("twitter") || lowerPkg.contains("reddit") -> "Redes Sociales"
            lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("twitch") || lowerPkg.contains("video") || lowerPkg.contains("media") -> "Streaming / Video"
            lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("discord") || lowerPkg.contains("messenger") -> "Mensajería"
            lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("firefox") -> "Navegadores"
            isSystem -> "Sistema"
            else -> "Otras Apps"
        }
    }

    /**
     * Verifica si el servicio de accesibilidad de LupoAide está activo
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = "${context.packageName}/${LupoBlockerAccessibilityService::class.java.canonicalName}"
        val simpleExpected = LupoBlockerAccessibilityService::class.java.simpleName

        return enabledServices.any { service ->
            val id = service.id
            id.contains(context.packageName) && (id.contains(expectedServiceName) || id.contains(simpleExpected))
        }
    }

    /**
     * Abre los ajustes del sistema para que el usuario habilite el Servicio de Accesibilidad de LupoAide
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.createChooser(this, null).flags or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    // --- Helpers de cálculo para el Plan Gradual ---

    /**
     * Calcula qué día del plan se encuentra el usuario (ej. Día 3 de 21)
     */
    fun calculatePlanDay(startTimestamp: Long, planDurationDays: Int): Int {
        val elapsedMillis = (System.currentTimeMillis() - startTimestamp).coerceAtLeast(0)
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis).toInt() + 1
        return elapsedDays.coerceIn(1, planDurationDays)
    }

    /**
     * Calcula el límite diario actual en minutos según el progreso del plan gradual
     */
    fun calculateCurrentAllowedMinutes(
        initialDailyMinutes: Int,
        targetDailyMinutes: Int,
        planDurationDays: Int,
        startTimestamp: Long
    ): Int {
        val currentDay = calculatePlanDay(startTimestamp, planDurationDays)
        if (currentDay >= planDurationDays) return targetDailyMinutes

        val progressRatio = (currentDay - 1).toFloat() / planDurationDays.toFloat()
        val minutesToReduce = (initialDailyMinutes - targetDailyMinutes) * progressRatio
        return (initialDailyMinutes - minutesToReduce).toInt().coerceAtLeast(targetDailyMinutes)
    }

    /**
     * Calcula el porcentaje completado de reducción del plan gradual (0% a 100%)
     */
    fun calculateProgressPercentage(
        initialDailyMinutes: Int,
        targetDailyMinutes: Int,
        currentAllowedMinutes: Int
    ): Float {
        val totalReduction = (initialDailyMinutes - targetDailyMinutes).toFloat()
        if (totalReduction <= 0f) return 1f

        val currentReduced = (initialDailyMinutes - currentAllowedMinutes).toFloat()
        return (currentReduced / totalReduction).coerceIn(0f, 1f)
    }
}
