package com.prodownload.manager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY priority DESC, createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>
    
    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadItem?
    
    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY priority DESC, createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>>
    
    @Query("SELECT * FROM downloads WHERE status IN (:statuses)")
    suspend fun getDownloadsByStatuses(statuses: List<DownloadStatus>): List<DownloadItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadItem)
    
    @Update
    suspend fun updateDownload(download: DownloadItem)
    
    @Delete
    suspend fun deleteDownload(download: DownloadItem)
    
    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)
    
    @Query("UPDATE downloads SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: DownloadStatus, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE downloads SET progress = :progress, downloadedSize = :downloadedSize, speed = :speed, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDownloadProgress(
        id: String,
        progress: Int,
        downloadedSize: Long,
        speed: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    @Query("SELECT COUNT(*) FROM downloads WHERE status = :status")
    suspend fun getDownloadCountByStatus(status: DownloadStatus): Int
    
    @Query("SELECT SUM(fileSize) FROM downloads WHERE status = :status")
    suspend fun getTotalSizeByStatus(status: DownloadStatus): Long?
    
    @Query("SELECT AVG(speed) FROM downloads WHERE status = :status")
    suspend fun getAverageSpeed(status: DownloadStatus): Double?
    
    @Query("SELECT * FROM downloads WHERE createdAt >= :startTime AND createdAt < :endTime")
    suspend fun getDownloadsBetween(startTime: Long, endTime: Long): List<DownloadItem>
}
