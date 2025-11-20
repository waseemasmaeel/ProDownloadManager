package com.downloadmanager.domain.repository

import com.downloadmanager.domain.model.DownloadTask
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadTask>>
    fun getDownloadById(id: String): Flow<DownloadTask?>
    fun getActiveDownloads(): Flow<List<DownloadTask>>
    fun getCompletedDownloads(): Flow<List<DownloadTask>>
    
    suspend fun addDownload(task: DownloadTask)
    suspend fun updateDownload(task: DownloadTask)
    suspend fun deleteDownload(id: String)
    suspend fun deleteAllDownloads()
    
    suspend fun startDownload(id: String)
    suspend fun pauseDownload(id: String)
    suspend fun resumeDownload(id: String)
    suspend fun cancelDownload(id: String)
}
