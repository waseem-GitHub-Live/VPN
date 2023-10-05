package com.xilli.stealthnet.Activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.xilli.stealthnet.R

class MyBackgroundService: Service() {

    override fun onCreate() {
        super.onCreate()
        // Create a notification channel (required on Android Oreo and above)
        createNotificationChannel()

        // Show a foreground notification to keep the service running
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Background Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Perform your background task here
        // For this example, we'll show a toast message

        Toast.makeText(this, "Background Service is running", Toast.LENGTH_SHORT).show()

        // Return START_STICKY to restart the service if it's killed by the system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "background_service_channel"
        private const val NOTIFICATION_ID = 101
    }
}