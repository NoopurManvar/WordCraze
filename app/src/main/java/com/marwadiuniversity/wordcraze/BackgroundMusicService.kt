package com.marwadiuniversity.wordcraze

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BackgroundMusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val CHANNEL_ID = "music_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create MediaPlayer if needed
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
            mediaPlayer?.isLooping = true
        }

        // Start playing if not already
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }

        // Build and show foreground notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Return START_STICKY to keep playing if the system kills the service
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null

        // Safely stop foreground mode (Android 15+ compatible)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WordCraze Music")
            .setContentText("Background music is playing 🎵")
            .setSmallIcon(R.drawable.applogo4) // Replace with your icon
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Music",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps WordCraze background music running"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
