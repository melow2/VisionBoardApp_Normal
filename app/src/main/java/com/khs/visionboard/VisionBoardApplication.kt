package com.khs.visionboard

import android.content.Context
import androidx.multidex.MultiDexApplication
import timber.log.Timber

class VisionBoardApplication: MultiDexApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Timber.plant(Timber.DebugTree())
        Timber.d("attachBaseContext()")
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")
    }
}