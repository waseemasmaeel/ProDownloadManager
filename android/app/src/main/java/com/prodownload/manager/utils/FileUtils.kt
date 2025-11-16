package com.prodownload.manager.utils

import java.net.URL

object FileUtils {
    fun getFileNameFromUrl(url: String): String {
        return try {
            val urlObj = URL(url)
            val path = urlObj.path
            val fileName = path.substringAfterLast('/')
            
            if (fileName.isNotEmpty() && fileName.contains('.')) {
                fileName
            } else {
                "download_${System.currentTimeMillis()}"
            }
        } catch (e: Exception) {
            "download_${System.currentTimeMillis()}"
        }
    }
    
    fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
    
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    fun formatSpeed(bytesPerSecond: Long): String {
        return formatBytes(bytesPerSecond) + "/s"
    }
    
    fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "mp4", "avi", "mkv" -> "video/*"
            "mp3", "wav", "flac" -> "audio/*"
            "jpg", "jpeg", "png", "gif" -> "image/*"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "zip", "rar" -> "application/zip"
            "apk" -> "application/vnd.android.package-archive"
            else -> "*/*"
        }
    }
}
