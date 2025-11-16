package com.prodownload.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long = 0L,
    val downloadedSize: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Int = 0,
    val speed: Long = 0L, // bytes per second
    val numberOfThreads: Int = 4,
    val priority: Priority = Priority.NORMAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val wifiOnly: Boolean = false,
    val scheduledTime: Long? = null,
    val resumeData: String? = null, // JSON data for resuming
    val mimeType: String? = null
) {
    val remainingSize: Long
        get() = fileSize - downloadedSize

    val eta: Long // Estimated time in seconds
        get() = if (speed > 0) remainingSize / speed else 0L
    
    fun formatSpeed(): String {
        return formatBytes(speed) + "/s"
    }
    
    fun formatSize(): String {
        return formatBytes(fileSize)
    }
    
    fun formatDownloaded(): String {
        return formatBytes(downloadedSize)
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    fun formatETA(): String {
        if (eta == 0L) return "--"
        
        val hours = eta / 3600
        val minutes = (eta % 3600) / 60
        val seconds = eta % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> "${seconds}s"
        }
    }
    
    fun getFileExtension(): String {
        return fileName.substringAfterLast('.', "")
    }
    
    fun getFileType(): FileType {
        val ext = getFileExtension().lowercase()
        return when (ext) {
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> FileType.VIDEO
            "mp3", "wav", "flac", "aac", "m4a", "ogg" -> FileType.AUDIO
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> FileType.DOCUMENT
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp" -> FileType.IMAGE
            "zip", "rar", "7z", "tar", "gz" -> FileType.ARCHIVE
            "apk" -> FileType.APK
            else -> FileType.OTHER
        }
    }
}

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class Priority(val value: Int) {
    LOW(1),
    NORMAL(5),
    HIGH(10)
}

enum class FileType {
    VIDEO,
    AUDIO,
    DOCUMENT,
    IMAGE,
    ARCHIVE,
    APK,
    OTHER
}
