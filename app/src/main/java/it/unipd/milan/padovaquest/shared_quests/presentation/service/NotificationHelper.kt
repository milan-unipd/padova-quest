package it.unipd.milan.padovaquest.shared_quests.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.presentation.BaseActivity


object NotificationHelper {

    private const val SERVICE_CHANNEL_ID = "padova_quest_service_channel"
    private const val NOTIFICATION_CHANNEL_ID = "padova_quest_notification_channel"
    private const val CHANNEL_NAME = "Padova Quest "

    const val SERVICE_NOTIFICATION_ID = 1
    const val WALK_NOTIFICATION_ID = 2
    const val TRIVIA_NOTIFICATION_ID = 3
    const val GROUP_QUEST_NOTIFICATION_ID = 4

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications for Padova Quest"
            }

            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Padova Quest Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service notifications without sound"
                enableLights(false)
                enableVibration(false)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun getServiceNotification(context: Context): NotificationCompat.Builder {

        val intent = Intent(context, BaseActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setContentTitle("Padova Quest")
            .setContentText("Your location is being tracked in the background.")
            .setSmallIcon(R.drawable.explore_24px)
            .setOngoing(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent to open BaseActivity
            .setAutoCancel(false) // Auto cancel the notification when tapped
    }

    fun notify(context: Context, content: String, notificationID: Int) {
        val intent = Intent(context, BaseActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT + PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Padova Quest")
            .setContentText(content)
            .setSmallIcon(R.drawable.explore_24px)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationID, notification)
    }

    fun cancelNotification(context: Context, notificationID: Int) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationID)

    }
}
