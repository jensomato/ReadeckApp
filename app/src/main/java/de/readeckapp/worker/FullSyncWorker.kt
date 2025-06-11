package de.readeckapp.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.readeckapp.MainActivity
import de.readeckapp.R
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.BookmarkRepository.SyncResult
import timber.log.Timber

@HiltWorker
class FullSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    val bookmarkRepository: BookmarkRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        try {
            Timber.d("Start Work")
            val syncResult = bookmarkRepository.performFullSyncV2()
            val workResult = when (syncResult) {
                is SyncResult.Error -> Result.failure()
                is SyncResult.NetworkError -> Result.retry()
                is SyncResult.Success -> Result.success(
                    Data.Builder().putInt(OUTPUT_DATA_COUNT, syncResult.countDeleted).build()
                )
            }
            showNotification(syncResult)
            return workResult
        } catch (e: Exception) {
            Timber.e(e, "Error performing full sync")
            return Result.failure()
        }
    }

    private val notificationChannelId = "FullSyncNotificationChannelId"


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                notificationChannelId,
                applicationContext.getString(R.string.auto_sync_notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            )

            val notificationManager: NotificationManager? =
                getSystemService(
                    applicationContext,
                    NotificationManager::class.java
                )

            notificationManager?.createNotificationChannel(
                notificationChannel
            )
        }
    }

    private fun showNotification(syncResult: SyncResult) {
        createNotificationChannel()

        val contentText = when (syncResult) {
            is SyncResult.Success -> {
                applicationContext.getString(R.string.auto_sync_notification_success, syncResult.countDeleted)
            }
            else -> {
                applicationContext.getString(R.string.auto_sync_notification_failure)
            }
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            notificationChannelId
        )
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(contentText)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(NOTIFICATION_ID, notification)
            }
        } else {
            Timber.w("No permission to show notification")
        }
    }


    override suspend fun getForegroundInfo(): ForegroundInfo {
        val mainActivityIntent = Intent(
            applicationContext,
            MainActivity::class.java
        )

        val mainActivityPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            notificationChannelId
        )
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(applicationContext.getString(R.string.auto_sync_notification_running))
            .setContentIntent(mainActivityPendingIntent)
            .setAutoCancel(true)
            .build()

        return ForegroundInfo(0, notification)
    }


    companion object {
        const val UNIQUE_NAME_AUTO = "auto_full_sync_work"
        const val UNIQUE_NAME_MANUAL = "manual_full_sync_work"
        const val TAG = "full_sync"
        const val OUTPUT_DATA_COUNT = "count"
        const val NOTIFICATION_ID = 0
    }
}
