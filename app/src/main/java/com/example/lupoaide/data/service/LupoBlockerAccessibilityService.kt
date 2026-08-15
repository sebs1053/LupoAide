package com.example.lupoaide.data.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.lupoaide.data.local.LupoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LupoBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastBlockedPackage = ""
    private var lastBlockTimestamp = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignorar nuestra propia app, launchers o ajustes para no bloquear el sistema
        if (packageName == applicationContext.packageName ||
            packageName == "com.android.settings" ||
            packageName == "com.google.android.apps.nexuslauncher" ||
            packageName == "com.android.systemui"
        ) {
            return
        }

        // Throttle para evitar llamadas repetidas en el mismo segundo
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTimestamp) < 2500) {
            return
        }

        serviceScope.launch {
            try {
                val db = LupoDatabase.getDatabase(applicationContext)
                val blockedApp = db.lupoDao().getBlockedAppByPackage(packageName)

                if (blockedApp != null && blockedApp.isBlocked) {
                    val currentAllowed = BlockerPermissionHelper.calculateCurrentAllowedMinutes(
                        initialDailyMinutes = blockedApp.initialDailyLimitMinutes,
                        targetDailyMinutes = blockedApp.targetDailyLimitMinutes,
                        planDurationDays = blockedApp.planDurationDays,
                        startTimestamp = blockedApp.startTimestamp
                    )

                    val planDay = BlockerPermissionHelper.calculatePlanDay(
                        blockedApp.startTimestamp,
                        blockedApp.planDurationDays
                    )

                    // Si el límite calculado es 0 o el usuario tiene la app en bloqueo activo
                    lastBlockedPackage = packageName
                    lastBlockTimestamp = now

                    // Lanzar la pantalla de bloqueo y motivación de Lupo
                    val intent = Intent(applicationContext, BlockOverlayActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, blockedApp.packageName)
                        putExtra(BlockOverlayActivity.EXTRA_APP_NAME, blockedApp.appName)
                        putExtra(BlockOverlayActivity.EXTRA_MOTIVATION, blockedApp.motivationReason)
                        putExtra(BlockOverlayActivity.EXTRA_PLAN_DAY, planDay)
                        putExtra(BlockOverlayActivity.EXTRA_TOTAL_DAYS, blockedApp.planDurationDays)
                        putExtra(BlockOverlayActivity.EXTRA_CURRENT_LIMIT, currentAllowed)
                        putExtra(BlockOverlayActivity.EXTRA_TARGET_LIMIT, blockedApp.targetDailyLimitMinutes)
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("LupoBlockerService", "Error verificando app bloqueada", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.d("LupoBlockerService", "Servicio de bloqueo interrumpido")
    }
}
