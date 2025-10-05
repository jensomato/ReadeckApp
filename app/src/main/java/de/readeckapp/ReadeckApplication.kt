package de.readeckapp

import android.app.Application
import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import de.readeckapp.util.createLogDir
import fr.bipi.treessence.context.GlobalContext.startTimber
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class ReadeckApplication : Application(), SingletonImageLoader.Factory {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override fun onCreate() {
        super.onCreate()
        initTimberLog()
        Thread.setDefaultUncaughtExceptionHandler(
            CustomExceptionHandler(this)
        )
    }
    
    override fun newImageLoader(context: Context): ImageLoader {
        return imageLoader
    }

    private fun initTimberLog() {
        val logDir = createLogDir(filesDir)
        startTimber {
            if (BuildConfig.DEBUG) {
                debugTree()
                logDir?.let {
                    fileTree {
                        level = Log.DEBUG
                        fileName = LOGFILE
                        dir = it.absolutePath
                        fileLimit = 2
                        appendToFile = true
                    }
                }
            } else {
                logDir?.let {
                    fileTree {
                        level = Log.INFO
                        fileName = LOGFILE
                        dir = it.absolutePath
                        fileLimit = 2
                        appendToFile = true
                    }
                }
            }
        }
    }
}

class CustomExceptionHandler(private val application: Application) :
    Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Timber.e(throwable, "CRASH: Uncaught exception")
        } catch (e: Exception) {
            // Handle any exceptions that occur during logging (e.g., file write errors)
            e.printStackTrace()
        } finally {
            // If there was a default handler, call it to let the system handle the crash
            defaultHandler?.uncaughtException(thread, throwable) ?: android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}

const val LOGFILE = "ReadeckAppLog"
const val LOGDIR = "logs"