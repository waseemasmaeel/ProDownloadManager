package com.prodownload.manager

import android.app.Application
import android.util.Log

class DownloadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("DownloadApplication", "Application started")
    }
}
