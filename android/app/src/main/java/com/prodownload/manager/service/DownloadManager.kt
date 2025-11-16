package com.prodownload.manager.service

import android.content.Context
import android.util.Log
import com.prodownload.manager.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadManager private constructor(private val context: Context) {
    private val TAG = "DownloadManager"
    private val database = DownloadDatabase.getDatabase(context)
    private val downloadDao = database.downloadDao()
    
    private val downloadEngine = DownloadEngine(
        onProgressUpdate = { id, progress, downloadedSize, speed ->
            scope.launch {
                downloadDao.updateDownloadProgress(id, progress, downloadedSize, speed)
            }
        },
        onStatusChange = { id, status, errorMessage ->
            scope.launch {
                downloadDao.getDownloadById(id)?.let { download ->
                    val updatedDownload = download.copy(
                        status = status,
                        errorMessage = errorMessage,
                        completedAt = if (status == DownloadStatus.COMPLETED) System.currentTimeMillis() else null,
                        updatedAt = System.currentTimeMillis()
                    )
                    downloadDao.updateDownload(updatedDownload)
                }
                
                // Process queue after status change
                if (status == DownloadStatus.COMPLETED || status == DownloadStatus.FAILED) {
                    processQueue()
                }
            }
        }
    )
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val maxConcurrentDownloads = 3

    suspend fun addDownload(
        url: String,
        fileName: String,
        filePath: String,
        numberOfThreads: Int = 4,
        priority: Priority = Priority.NORMAL,
        wifiOnly: Boolean = false,
        scheduledTime: Long? = null
    ): String {
        val download = DownloadItem(
            url = url,
            fileName = fileName,
            filePath = filePath,
            numberOfThreads = numberOfThreads,
            priority = priority,
            wifiOnly = wifiOnly,
            scheduledTime = scheduledTime
        )
        
        downloadDao.insertDownload(download)
        Log.d(TAG, "Download added: ${download.id}")
        
        // Auto-start if not scheduled
        if (scheduledTime == null) {
            processQueue()
        }
        
        return download.id
    }

    suspend fun startDownload(downloadId: String) {
        val download = downloadDao.getDownloadById(downloadId) ?: return
        
        if (download.status != DownloadStatus.QUEUED && download.status != DownloadStatus.PAUSED) {
            Log.w(TAG, "Cannot start download in status: ${download.status}")
            return
        }
        
        // Check concurrent downloads limit
        val activeCount = downloadEngine.getActiveDownloadCount()
        if (activeCount >= maxConcurrentDownloads) {
            Log.d(TAG, "Max concurrent downloads reached, queuing: $downloadId")
            return
        }
        
        // Update status to downloading
        val updatedDownload = download.copy(
            status = DownloadStatus.DOWNLOADING,
            updatedAt = System.currentTimeMillis()
        )
        downloadDao.updateDownload(updatedDownload)
        
        // Start download
        scope.launch {
            try {
                downloadEngine.startDownload(updatedDownload)
            } catch (e: Exception) {
                Log.e(TAG, "Download failed: $downloadId", e)
                downloadDao.updateDownloadStatus(downloadId, DownloadStatus.FAILED)
            }
        }
    }

    suspend fun pauseDownload(downloadId: String) {
        downloadEngine.pauseDownload(downloadId)
        downloadDao.updateDownloadStatus(downloadId, DownloadStatus.PAUSED)
        processQueue()
    }

    suspend fun resumeDownload(downloadId: String) {
        startDownload(downloadId)
    }

    suspend fun cancelDownload(downloadId: String) {
        downloadEngine.cancelDownload(downloadId)
        downloadDao.updateDownloadStatus(downloadId, DownloadStatus.CANCELLED)
        processQueue()
    }

    suspend fun deleteDownload(downloadId: String, deleteFile: Boolean = false) {
        val download = downloadDao.getDownloadById(downloadId)
        
        if (download != null) {
            cancelDownload(downloadId)
            
            if (deleteFile) {
                val file = File(download.filePath, download.fileName)
                if (file.exists()) {
                    file.delete()
                }
            }
            
            downloadDao.deleteDownloadById(downloadId)
        }
    }

    private suspend fun processQueue() {
        val activeCount = downloadEngine.getActiveDownloadCount()
        if (activeCount >= maxConcurrentDownloads) return
        
        // Get queued downloads ordered by priority
        val queuedDownloads = downloadDao.getDownloadsByStatuses(
            listOf(DownloadStatus.QUEUED, DownloadStatus.PAUSED)
        )
        
        val downloadsToStart = queuedDownloads.take(maxConcurrentDownloads - activeCount)
        
        downloadsToStart.forEach { download ->
            startDownload(download.id)
        }
    }

    fun getAllDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getAllDownloads()
    }

    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>> {
        return downloadDao.getDownloadsByStatus(status)
    }

    suspend fun getDownloadById(id: String): DownloadItem? {
        return downloadDao.getDownloadById(id)
    }

    suspend fun getStatistics(): DownloadStatistics {
        val allDownloads = downloadDao.getDownloadsByStatuses(DownloadStatus.values().toList())
        val completed = allDownloads.filter { it.status == DownloadStatus.COMPLETED }
        val failed = allDownloads.filter { it.status == DownloadStatus.FAILED }
        
        val totalSize = completed.sumOf { it.fileSize }
        val averageSpeed = downloadDao.getAverageSpeed(DownloadStatus.DOWNLOADING) ?: 0.0
        
        val today = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val todayDownloads = downloadDao.getDownloadsBetween(today, System.currentTimeMillis())
        
        return DownloadStatistics(
            totalDownloads = allDownloads.size,
            completedDownloads = completed.size,
            failedDownloads = failed.size,
            totalSize = totalSize,
            averageSpeed = averageSpeed.toLong(),
            downloadsToday = todayDownloads.size
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadManager? = null

        fun getInstance(context: Context): DownloadManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DownloadManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

data class DownloadStatistics(
    val totalDownloads: Int,
    val completedDownloads: Int,
    val failedDownloads: Int,
    val totalSize: Long,
    val averageSpeed: Long,
    val downloadsToday: Int
) {
    val successRate: Float
        get() = if (totalDownloads > 0) {
            (completedDownloads.toFloat() / totalDownloads) * 100
        } else 0f
}
