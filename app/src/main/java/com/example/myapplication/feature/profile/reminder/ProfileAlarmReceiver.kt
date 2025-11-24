package com.example.myapplication.feature.profile.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class ProfileAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fullName = intent.getStringExtra(EXTRA_FULL_NAME).orEmpty()
        val notificationManager = NotificationManagerCompat.from(context)
        createChannelIfNeeded(context)
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (fullName.isNotBlank()) {
            "Начинается пара у $fullName"
        } else {
            "Любимая пара начинается"
        }
        val message = "Самое время открыть приложение и быть вовремя."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания профиля",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о любимой паре"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_FULL_NAME = "extra_full_name"
        private const val CHANNEL_ID = "profile_reminders"
        private const val NOTIFICATION_ID = 5001
    }
}

