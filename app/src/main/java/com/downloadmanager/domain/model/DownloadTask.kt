package com.downloadmanager.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DownloadTask(
    val id: String,
    val url: String,
    val fileName: String,
    val filePath: String,
    val totalSize: Long = 0L,
    val downloadedSize: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val speed: Long = 0L,
    val mimeType: String? = null
) : Parcelable {
    
    val progress: Float
        get() = if (totalSize > 0) (downloadedSize.toFloat() / totalSize) * 100f else 0f
    
    val isCompleted: Boolean
        get() = status == DownloadStatus.COMPLETED
    
    val isPaused: Boolean
        get() = status == DownloadStatus.PAUSED
    
    val isDownloading: Boolean
        get() = status == DownloadStatus.DOWNLOADING
    
    val isFailed: Boolean
        get() = status == DownloadStatus.FAILED
}

@Serializable
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
