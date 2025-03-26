package de.readeckapp.util

import android.util.Log
import de.readeckapp.BuildConfig
import de.readeckapp.LOGDIR
import fr.bipi.treessence.file.FileLoggerTree
import timber.log.Timber
import java.io.File

fun getLatestLogFile(): File? {
    val tree = Timber.forest().firstOrNull { it is FileLoggerTree } as FileLoggerTree?
    return tree?.files?.sortedBy { it.name }?.firstOrNull()
}

fun createLogDir(parentDir: File): File? {
    val logdir = File(parentDir, LOGDIR)
    return if (logdir.isDirectory) {
        Log.i("LOGDIR", "logdir $logdir already exists")
        logdir
    } else {
        logdir.mkdirs().let {
            if (it) {
                Log.i("LOGDIR", "logdir $logdir created")
                logdir
            } else {
                Log.w("LOGDIR", "logdir $logdir not created")
                null
            }
        }
    }
}

fun logAppInfo() {
    Timber.tag("APP-INFO")
    Timber.i("versionName=${BuildConfig.VERSION_NAME}")
    Timber.tag("APP-INFO")
    Timber.i("versionCode=${BuildConfig.VERSION_CODE}")
    Timber.tag("APP-INFO")
    Timber.i("flavor=${BuildConfig.FLAVOR}")
}