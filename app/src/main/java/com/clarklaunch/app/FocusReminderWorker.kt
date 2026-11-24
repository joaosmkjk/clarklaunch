package com.clarklaunch.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class FocusReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val appLabel = inputData.getString("app_label") ?: "Aplicativo"
        val minutes = inputData.getInt("minutes", 5)
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "clark_focus_sessions"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes do Modo Foco",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos quando uma sessão de uso terminar" }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Sua sessão terminou")
            .setContentText("O tempo de $minutes minutos para $appLabel acabou. Que tal fazer uma pausa?")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Você reservou $minutes minutos para $appLabel. Sua sessão terminou. Se quiser, volte ao ClarkLaunch e escolha o que fazer agora."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(applicationContext)
                .notify(("focus_$appLabel").hashCode(), notification)
            Result.success()
        } catch (_: SecurityException) {
            // Notifications may be disabled by the user or not yet authorized.
            Result.success()
        }
    }
}
