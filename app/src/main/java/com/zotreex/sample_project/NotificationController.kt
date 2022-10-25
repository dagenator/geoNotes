package com.zotreex.sample_project

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class NotificationController(private val context: Context) {

    private val defMessage = "Scanning for Beacons"

    private val notificationBuilder = Notification.Builder(context).also {
        it.setSmallIcon(com.google.android.material.R.drawable.ic_clock_black_24dp)
    }

    private val notificationManager =
        context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationForService(message: String? = null) = notificationBuilder.also {
        it.setContentTitle(message ?: defMessage)

        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            else PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        it.setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "Is Bacon Id",
                "is Beacon background", NotificationManager.IMPORTANCE_HIGH
            )

            channel.description = "Work in background"
            notificationManager.createNotificationChannel(channel)
            it.setChannelId(channel.id)
        }
    }

    fun notify(mes: String) {
        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, createNotificationForService(mes).build())
        }
    }

    companion object {
        const val notificationID = 345
    }

}
