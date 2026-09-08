package com.example.lupoaide.data.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.lupoaide.MainActivity
import com.example.lupoaide.R

object LupoNotificationHelper {

    const val CHANNEL_STUDY_ID = "lupo_study_reminders"
    const val CHANNEL_STUDY_NAME = "Recordatorios de Estudio y Racha"

    const val CHANNEL_EXAMS_ID = "lupo_exam_alerts"
    const val CHANNEL_EXAMS_NAME = "Alertas de Exámenes"

    const val CHANNEL_BACKPACK_ID = "lupo_backpack_alerts"
    const val CHANNEL_BACKPACK_NAME = "Mochila y Útiles Escolares"

    private const val NOTIF_ID_TEST = 1001
    private const val NOTIF_ID_STREAK = 1002
    private const val NOTIF_ID_EXAM = 1003
    private const val NOTIF_ID_BACKPACK = 1004

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Canal 1: Estudio
            val studyChannel = NotificationChannel(
                CHANNEL_STUDY_ID,
                CHANNEL_STUDY_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios pedagógicos y motivación diaria de Lupo"
                enableVibration(true)
            }

            // Canal 2: Exámenes
            val examChannel = NotificationChannel(
                CHANNEL_EXAMS_ID,
                CHANNEL_EXAMS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos importantes de evaluaciones y fechas límite"
                enableVibration(true)
            }

            // Canal 3: Mochila
            val backpackChannel = NotificationChannel(
                CHANNEL_BACKPACK_ID,
                CHANNEL_BACKPACK_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorio para empacar cuadernos y útiles antes de dormir"
            }

            notificationManager.createNotificationChannel(studyChannel)
            notificationManager.createNotificationChannel(examChannel)
            notificationManager.createNotificationChannel(backpackChannel)
        }
    }

    fun canSendNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendInstantTestNotification(context: Context, customMessage: String? = null): Boolean {
        initNotificationChannels(context)
        if (!canSendNotifications(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = customMessage ?: "🐺 ¡Auuu! Lupo está activo y listo para ayudarte a dominar tus materias hoy. ¡Vamos por esos 100 EXP!"

        val builder = NotificationCompat.Builder(context, CHANNEL_STUDY_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Lupo Tutor IA")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    fun sendExamAlert(context: Context, examTitle: String, subject: String, examDate: String): Boolean {
        initNotificationChannels(context)
        if (!canSendNotifications(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "⚠️ Mañana tienes examen de $subject ($examTitle). ¡Repasa tus flashcards hoy con Lupo para asegurar tu 10!"

        val builder = NotificationCompat.Builder(context, CHANNEL_EXAMS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Examen Próximo: $subject!")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        return try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_EXAM, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun sendBackpackAlert(context: Context, unfulfilledCount: Int): Boolean {
        initNotificationChannels(context)
        if (!canSendNotifications(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "🎒 Tienes $unfulfilledCount útiles pendientes en tu mochila para mañana. ¡Empácalos antes de dormir!"

        val builder = NotificationCompat.Builder(context, CHANNEL_BACKPACK_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Prepara tu Mochila Escolar")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        return try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_BACKPACK, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
