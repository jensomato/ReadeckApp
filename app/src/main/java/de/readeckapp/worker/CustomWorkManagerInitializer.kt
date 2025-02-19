package de.readeckapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class CustomWorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val workerFactory = getWorkerFactory(appContext = context.applicationContext)
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    private fun getWorkerFactory(appContext: Context): HiltWorkerFactory {
        val workManagerEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WorkManagerInitializerEntryPoint::class.java
        )
        return workManagerEntryPoint.hiltWorkerFactory()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface WorkManagerInitializerEntryPoint {
        fun hiltWorkerFactory(): HiltWorkerFactory
    }
}