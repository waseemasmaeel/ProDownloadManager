package com.downloadmanager.di

import android.content.Context
import androidx.room.Room
import com.downloadmanager.data.database.DownloadDao
import com.downloadmanager.data.database.DownloadDatabase
import com.downloadmanager.data.repository.DownloadRepositoryImpl
import com.downloadmanager.domain.repository.DownloadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DownloadDatabase {
        return Room.databaseBuilder(
            context,
            DownloadDatabase::class.java,
            "download_manager_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: DownloadDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
        okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao, okHttpClient, context)
    }

    @Provides
    @Singleton
    fun provideDownloadRepositoryImpl(
        downloadDao: DownloadDao,
        okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): DownloadRepositoryImpl {
        return DownloadRepositoryImpl(downloadDao, okHttpClient, context)
    }
}
