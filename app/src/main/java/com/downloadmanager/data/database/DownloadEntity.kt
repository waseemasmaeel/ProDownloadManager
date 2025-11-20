package com.downloadmanager.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.downloadmanager.domain.model.DownloadStatus
import com.downloadmanager.domain.model.DownloadTask

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val fileName: String,
    val filePath: String,
    val totalSize: Long,
    val downloadedSize: Long,
    val status: String,
    val createdAt: Long,
    val completedAt: Long?,
    val errorMessage: String?,
    val speed: Long,
    val mimeType: String?
) {
    fun toDomain(): DownloadTask {
        return DownloadTask(
            id = id,
            url = url,
            fileName = fileName,
            filePath = filePath,
            totalSize = totalSize,
            downloadedSize = downloadedSize,
            status = DownloadStatus.valueOf(status),
            createdAt = createdAt,
            completedAt = completedAt,
            errorMessage = errorMessage,
            speed = speed,
            mimeType = mimeType
        )
    }

    companion object {
        fun fromDomain(task: DownloadTask): DownloadEntity {
            return DownloadEntity(
                id = task.id,
                url = task.url,
                fileName = task.fileName,
                filePath = task.filePath,
                totalSize = task.totalSize,
                downloadedSize = task.downloadedSize,
                status = task.status.name,
                createdAt = task.createdAt,
                completedAt = task.completedAt,
                errorMessage = task.errorMessage,
                speed = task.speed,
                mimeType = task.mimeType
            )
        }
    }
}
