package de.readeckapp.io.rest.auth

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.res.ResourcesCompat
import de.readeckapp.MainActivity
import de.readeckapp.R

object NotificationUtil {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showUnauthorizedNotification(context: Context, notificationManager: NotificationManagerCompat) {
        val notificationId = 1 // Unique ID for the notification
        val channelId = "synchronization_channel" // Unique channel ID

        // Create an explicit intent for an Activity in your app
        val resultIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigateToAccountSettings", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_sync_name)
            val descriptionText = context.getString(R.string.notification_channel_sync_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationService: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationService.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.notification_authentication_error_title))
            .setContentText(context.getString(R.string.notification_authentication_error_message))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification
        notificationManager.notify(notificationId, builder.build())
    }
}
