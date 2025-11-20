package com.downloadmanager.data.repository

import android.content.Context
import android.os.Environment
import com.downloadmanager.data.database.DownloadDao
import com.downloadmanager.data.database.DownloadEntity
import com.downloadmanager.domain.model.DownloadStatus
import com.downloadmanager.domain.model.DownloadTask
import com.downloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient,
    private val context: Context
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<DownloadTask>> {
        return downloadDao.getAllDownloads().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDownloadById(id: String): Flow<DownloadTask?> {
        return downloadDao.getDownloadById(id).map { it?.toDomain() }
    }

    override fun getActiveDownloads(): Flow<List<DownloadTask>> {
        return downloadDao.getActiveDownloads().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedDownloads(): Flow<List<DownloadTask>> {
        return downloadDao.getCompletedDownloads().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addDownload(task: DownloadTask) {
        downloadDao.insertDownload(DownloadEntity.fromDomain(task))
    }

    override suspend fun updateDownload(task: DownloadTask) {
        downloadDao.updateDownload(DownloadEntity.fromDomain(task))
    }

    override suspend fun deleteDownload(id: String) {
        downloadDao.deleteDownload(id)
    }

    override suspend fun deleteAllDownloads() {
        downloadDao.deleteAllDownloads()
    }

    override suspend fun startDownload(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.DOWNLOADING.name)
    }

    override suspend fun pauseDownload(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.PAUSED.name)
    }

    override suspend fun resumeDownload(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.DOWNLOADING.name)
    }

    override suspend fun cancelDownload(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.CANCELLED.name)
    }

    suspend fun performDownload(task: DownloadTask): Result<DownloadTask> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(task.url).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response"))
            val totalSize = body.contentLength()
            
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadDir, task.fileName)
            
            var downloadedSize = 0L
            val buffer = ByteArray(8192)
            
            FileOutputStream(file).use { output ->
                body.byteStream().use { input ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        
                        downloadDao.updateProgress(task.id, downloadedSize, 0)
                    }
                }
            }
            
            val completedTask = task.copy(
                status = DownloadStatus.COMPLETED,
                totalSize = totalSize,
                downloadedSize = downloadedSize,
                completedAt = System.currentTimeMillis(),
                filePath = file.absolutePath
            )
            
            updateDownload(completedTask)
            Result.success(completedTask)
            
        } catch (e: Exception) {
            val failedTask = task.copy(
                status = DownloadStatus.FAILED,
                errorMessage = e.message
            )
            updateDownload(failedTask)
            Result.failure(e)
        }
    }

    fun createDownloadTask(url: String, fileName: String? = null): DownloadTask {
        val actualFileName = fileName ?: url.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: "download_${System.currentTimeMillis()}"
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        
        return DownloadTask(
            id = UUID.randomUUID().toString(),
            url = url,
            fileName = actualFileName,
            filePath = File(downloadDir, actualFileName).absolutePath
        )
    }
}
