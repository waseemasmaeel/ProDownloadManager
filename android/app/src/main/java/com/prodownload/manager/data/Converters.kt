package com.prodownload.manager.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String {
        return value.name
    }

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus {
        return DownloadStatus.valueOf(value)
    }

    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
}
