package com.example.lupoaide.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.lupoaide.data.local.UserProfileEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object LupoAlarmScheduler {

    const val ACTION_STREAK_REMINDER = "com.example.lupoaide.ACTION_STREAK_REMINDER"
    const val ACTION_BACKPACK_REMINDER = "com.example.lupoaide.ACTION_BACKPACK_REMINDER"

    private const val REQUEST_CODE_STREAK = 2001
    private const val REQUEST_CODE_BACKPACK = 2002

    fun scheduleAllReminders(context: Context, profile: UserProfileEntity?) {
        if (profile == null || !profile.notificationsEnabled) {
            cancelAllReminders(context)
            return
        }

        if (profile.streakRemindersEnabled) {
            scheduleStreakReminder(context, profile.streakReminderTime)
        } else {
            cancelStreakReminder(context)
        }

        if (profile.backpackRemindersEnabled) {
            scheduleBackpackReminder(context, profile.backpackReminderTime)
        } else {
            cancelBackpackReminder(context)
        }
    }

    fun scheduleStreakReminder(context: Context, timeStr: String) {
        val triggerMillis = calculateNextTriggerMillis(timeStr)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, LupoAlarmReceiver::class.java).apply {
            action = ACTION_STREAK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_STREAK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d("LupoAlarmScheduler", "Recordatorio de racha programado para: $timeStr (en $triggerMillis ms)")
        } catch (e: Exception) {
            Log.e("LupoAlarmScheduler", "Error al programar alarma de racha: ${e.message}")
        }
    }

    fun scheduleBackpackReminder(context: Context, timeStr: String) {
        val triggerMillis = calculateNextTriggerMillis(timeStr)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, LupoAlarmReceiver::class.java).apply {
            action = ACTION_BACKPACK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BACKPACK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d("LupoAlarmScheduler", "Recordatorio de mochila programado para: $timeStr (en $triggerMillis ms)")
        } catch (e: Exception) {
            Log.e("LupoAlarmScheduler", "Error al programar alarma de mochila: ${e.message}")
        }
    }

    fun cancelStreakReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LupoAlarmReceiver::class.java).apply {
            action = ACTION_STREAK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_STREAK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelBackpackReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LupoAlarmReceiver::class.java).apply {
            action = ACTION_BACKPACK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BACKPACK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllReminders(context: Context) {
        cancelStreakReminder(context)
        cancelBackpackReminder(context)
    }

    /**
     * Calcula la próxima hora de ejecución a partir de un string "HH:mm".
     * Si la hora ya pasó el día de hoy, programa para mañana a esa hora.
     */
    fun calculateNextTriggerMillis(timeStr: String): Long {
        val parts = timeStr.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0

        val now = LocalDateTime.now()
        var target = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute, 0))

        if (target.isBefore(now) || target.isEqual(now)) {
            target = target.plusDays(1)
        }

        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
