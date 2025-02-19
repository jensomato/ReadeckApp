package de.readeckapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant


@HiltAndroidApp
class ReadeckApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        } else {
            // TODO add production tree
        }
    }
}
