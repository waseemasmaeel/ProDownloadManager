package com.prodownload.manager.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.prodownload.manager.MainActivity
import com.prodownload.manager.R
import com.prodownload.manager.data.*
import kotlinx.coroutines.*
import kotlin.math.max

class DownloadService : Service() {
    private val TAG = "DownloadService"
    private val CHANNEL_ID = "download_channel"
    private val NOTIFICATION_ID = 1001
    
    private lateinit var downloadManager: DownloadManager
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        downloadManager = DownloadManager.getInstance(applicationContext)
        
        startForeground(NOTIFICATION_ID, createNotification("Download Service", "Ready"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { startDownload(it) }
            }
            ACTION_PAUSE_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { pauseDownload(it) }
            }
            ACTION_RESUME_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { resumeDownload(it) }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { cancelDownload(it) }
            }
        }
        
        return START_STICKY
    }

    private fun startDownload(downloadId: String) {
        serviceScope.launch {
            downloadManager.startDownload(downloadId)
        }
    }

    private fun pauseDownload(downloadId: String) {
        serviceScope.launch {
            downloadManager.pauseDownload(downloadId)
        }
    }

    private fun resumeDownload(downloadId: String) {
        serviceScope.launch {
            downloadManager.resumeDownload(downloadId)
        }
    }

    private fun cancelDownload(downloadId: String) {
        serviceScope.launch {
            downloadManager.cancelDownload(downloadId)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows download progress"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, text: String, progress: Int = -1): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (progress >= 0) {
            builder.setProgress(100, progress, false)
        }

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_DOWNLOAD = "com.prodownload.manager.START_DOWNLOAD"
        const val ACTION_PAUSE_DOWNLOAD = "com.prodownload.manager.PAUSE_DOWNLOAD"
        const val ACTION_RESUME_DOWNLOAD = "com.prodownload.manager.RESUME_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.prodownload.manager.CANCEL_DOWNLOAD"
        const val EXTRA_DOWNLOAD_ID = "download_id"

        fun startDownload(context: Context, downloadId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseDownload(context: Context, downloadId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_PAUSE_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            context.startService(intent)
        }

        fun resumeDownload(context: Context, downloadId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_RESUME_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancelDownload(context: Context, downloadId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            context.startService(intent)
        }
    }
}
