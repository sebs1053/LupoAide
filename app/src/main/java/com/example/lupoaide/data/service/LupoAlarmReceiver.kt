package com.example.lupoaide.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lupoaide.data.local.LupoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class LupoAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("LupoAlarmReceiver", "Alarma recibida con acción: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = LupoDatabase.getDatabase(context)
                val dao = db.lupoDao()
                val profile = dao.getUserProfile().firstOrNull()

                when (action) {
                    LupoAlarmScheduler.ACTION_STREAK_REMINDER -> {
                        if (profile != null && profile.notificationsEnabled && profile.streakRemindersEnabled) {
                            val today = LocalDate.now().toString()
                            // Verificar si el usuario ya activó su racha hoy
                            if (profile.lastStreakActivatedDate != today) {
                                LupoNotificationHelper.sendStreakReminderAlert(
                                    context = context,
                                    streakDays = profile.studyStreak,
                                    isUrgent = false
                                )
                            }
                            // Reprogramar para el día siguiente a la hora configurada por el usuario
                            LupoAlarmScheduler.scheduleStreakReminder(context, profile.streakReminderTime)
                        }
                    }

                    LupoAlarmScheduler.ACTION_BACKPACK_REMINDER -> {
                        if (profile != null && profile.notificationsEnabled && profile.backpackRemindersEnabled) {
                            val materials = dao.getAllBackpackMaterials().firstOrNull() ?: emptyList()
                            val unfulfilled = materials.count { !it.isPacked }
                            LupoNotificationHelper.sendBackpackAlert(
                                context = context,
                                unfulfilledCount = if (unfulfilled > 0) unfulfilled else 3
                            )
                            // Reprogramar para el día siguiente a la hora configurada por el usuario
                            LupoAlarmScheduler.scheduleBackpackReminder(context, profile.backpackReminderTime)
                        }
                    }

                    Intent.ACTION_BOOT_COMPLETED -> {
                        if (profile != null) {
                            LupoAlarmScheduler.scheduleAllReminders(context, profile)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LupoAlarmReceiver", "Error en LupoAlarmReceiver: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
