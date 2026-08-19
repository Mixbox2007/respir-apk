package app.respir

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = Prefs(context)
        if (!prefs.enabled) return
        showNotification(context)
        ReminderScheduler.schedule(context, prefs.intervalMinutes)
    }

    private fun showNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "respir_hourly"
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Rappels horaires", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Cette heure, tu ne fumes pas."
                enableVibration(true)
            },
        )
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = MESSAGES.random()
        val n = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle("Respir")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(1001, n)
    }

    companion object {
        val MESSAGES = listOf(
            "Cette heure, tu ne fumes pas.",
            "Pose le paquet. Respire.",
            "Une heure de plus. Tes poumons le sentent.",
            "Pas maintenant. Pas cette cigarette.",
            "L'envie passe. Toi, tu restes.",
            "Tu as déjà tenu jusqu'ici.",
        )
    }
}
