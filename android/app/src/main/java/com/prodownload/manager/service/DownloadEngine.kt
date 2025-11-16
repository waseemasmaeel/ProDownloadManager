package com.prodownload.manager.service

import android.util.Log
import com.prodownload.manager.data.DownloadItem
import com.prodownload.manager.data.DownloadStatus
import com.prodownload.manager.utils.NetworkUtils
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class DownloadEngine(
    private val onProgressUpdate: (String, Int, Long, Long) -> Unit,
    private val onStatusChange: (String, DownloadStatus, String?) -> Unit
) {
    private val TAG = "DownloadEngine"
    private val activeDownloads = ConcurrentHashMap<String, DownloadJob>()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    data class DownloadJob(
        val job: Job,
        val downloadedBytes: AtomicLong,
        val startTime: Long,
        var isPaused: Boolean = false
    )

    suspend fun startDownload(downloadItem: DownloadItem) = withContext(Dispatchers.IO) {
        if (activeDownloads.containsKey(downloadItem.id)) {
            Log.w(TAG, "Download already active: ${downloadItem.id}")
            return@withContext
        }

        val job = launch {
            try {
                onStatusChange(downloadItem.id, DownloadStatus.DOWNLOADING, null)
                
                // Get file info first
                val fileInfo = getFileInfo(downloadItem.url)
                val fileSize = fileInfo.first
                val supportsRange = fileInfo.second
                
                val file = File(downloadItem.filePath, downloadItem.fileName)
                file.parentFile?.mkdirs()
                
                // Determine number of threads
                val threadsToUse = if (supportsRange && fileSize > 1024 * 1024) {
                    minOf(downloadItem.numberOfThreads, 16)
                } else {
                    1 // Single thread if range not supported or small file
                }
                
                val downloadedBytes = AtomicLong(downloadItem.downloadedSize)
                val downloadJob = DownloadJob(
                    job = coroutineContext[Job]!!,
                    downloadedBytes = downloadedBytes,
                    startTime = System.currentTimeMillis()
                )
                activeDownloads[downloadItem.id] = downloadJob
                
                if (threadsToUse > 1) {
                    downloadMultiPart(downloadItem, file, fileSize, threadsToUse, downloadedBytes, downloadJob)
                } else {
                    downloadSinglePart(downloadItem, file, downloadedBytes, downloadJob)
                }
                
                // Download completed
                if (!downloadJob.isPaused) {
                    onStatusChange(downloadItem.id, DownloadStatus.COMPLETED, null)
                    onProgressUpdate(downloadItem.id, 100, fileSize, 0)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Download failed: ${downloadItem.id}", e)
                onStatusChange(downloadItem.id, DownloadStatus.FAILED, e.message)
            } finally {
                activeDownloads.remove(downloadItem.id)
            }
        }
    }

    private suspend fun downloadMultiPart(
        downloadItem: DownloadItem,
        file: File,
        fileSize: Long,
        threads: Int,
        downloadedBytes: AtomicLong,
        downloadJob: DownloadJob
    ) = coroutineScope {
        val chunkSize = fileSize / threads
        val jobs = mutableListOf<Deferred<Unit>>()
        
        val randomAccessFile = RandomAccessFile(file, "rw")
        randomAccessFile.setLength(fileSize)
        
        for (i in 0 until threads) {
            val start = i * chunkSize
            val end = if (i == threads - 1) fileSize - 1 else (i + 1) * chunkSize - 1
            
            val job = async(Dispatchers.IO) {
                downloadChunk(downloadItem, start, end, randomAccessFile, downloadedBytes, downloadJob)
            }
            jobs.add(job)
        }
        
        // Monitor progress
        val progressJob = launch {
            while (isActive && !downloadJob.isPaused) {
                val downloaded = downloadedBytes.get()
                val progress = ((downloaded * 100) / fileSize).toInt()
                val speed = calculateSpeed(downloadedBytes.get(), downloadJob.startTime)
                
                onProgressUpdate(downloadItem.id, progress, downloaded, speed)
                delay(500) // Update every 500ms
            }
        }
        
        // Wait for all chunks
        jobs.awaitAll()
        progressJob.cancel()
        randomAccessFile.close()
    }

    private suspend fun downloadChunk(
        downloadItem: DownloadItem,
        start: Long,
        end: Long,
        randomAccessFile: RandomAccessFile,
        downloadedBytes: AtomicLong,
        downloadJob: DownloadJob
    ) {
        var currentStart = start
        var retries = 3
        
        while (currentStart <= end && !downloadJob.isPaused) {
            try {
                val request = Request.Builder()
                    .url(downloadItem.url)
                    .addHeader("Range", "bytes=$currentStart-$end")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("HTTP ${response.code}")
                    }
                    
                    response.body?.byteStream()?.use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        
                        synchronized(randomAccessFile) {
                            randomAccessFile.seek(currentStart)
                        }
                        
                        while (input.read(buffer).also { bytesRead = it } != -1 && !downloadJob.isPaused) {
                            synchronized(randomAccessFile) {
                                randomAccessFile.write(buffer, 0, bytesRead)
                            }
                            currentStart += bytesRead
                            downloadedBytes.addAndGet(bytesRead.toLong())
                        }
                    }
                }
                break // Success
                
            } catch (e: Exception) {
                retries--
                if (retries <= 0 || downloadJob.isPaused) throw e
                Log.w(TAG, "Chunk download failed, retrying... ($retries left)", e)
                delay(1000)
            }
        }
    }

    private suspend fun downloadSinglePart(
        downloadItem: DownloadItem,
        file: File,
        downloadedBytes: AtomicLong,
        downloadJob: DownloadJob
    ) {
        val request = Request.Builder()
            .url(downloadItem.url)
            .build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
            
            val fileSize = response.body?.contentLength() ?: 0L
            
            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastUpdate = System.currentTimeMillis()
                    
                    while (input.read(buffer).also { bytesRead = it } != -1 && !downloadJob.isPaused) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes.addAndGet(bytesRead.toLong())
                        
                        // Update progress periodically
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate >= 500) {
                            val downloaded = downloadedBytes.get()
                            val progress = if (fileSize > 0) ((downloaded * 100) / fileSize).toInt() else 0
                            val speed = calculateSpeed(downloaded, downloadJob.startTime)
                            
                            onProgressUpdate(downloadItem.id, progress, downloaded, speed)
                            lastUpdate = now
                        }
                    }
                }
            }
        }
    }

    private suspend fun getFileInfo(url: String): Pair<Long, Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .head()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    val fileSize = response.header("Content-Length")?.toLongOrNull() ?: 0L
                    val acceptRanges = response.header("Accept-Ranges") == "bytes"
                    Pair(fileSize, acceptRanges)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get file info", e)
                Pair(0L, false)
            }
        }
    }

    private fun calculateSpeed(downloadedBytes: Long, startTime: Long): Long {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        return if (elapsedTime > 0) (downloadedBytes / elapsedTime).toLong() else 0L
    }

    fun pauseDownload(downloadId: String) {
        activeDownloads[downloadId]?.let { downloadJob ->
            downloadJob.isPaused = true
            downloadJob.job.cancel()
            activeDownloads.remove(downloadId)
        }
    }

    fun cancelDownload(downloadId: String) {
        activeDownloads[downloadId]?.let { downloadJob ->
            downloadJob.job.cancel()
            activeDownloads.remove(downloadId)
        }
    }

    fun isDownloadActive(downloadId: String): Boolean {
        return activeDownloads.containsKey(downloadId)
    }
    
    fun getActiveDownloadCount(): Int {
        return activeDownloads.size
    }
}
