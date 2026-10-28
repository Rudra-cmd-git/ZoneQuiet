package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

class DndNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "zonequiet_status_channel"
        const val NOTIFICATION_ID_STATUS = 1001
        const val NOTIFICATION_ID_ALERT = 1002
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if the app has permission to change Do Not Disturb settings.
     */
    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Checks if DND is currently active on the device.
     * Any filter other than INTERRUPTION_FILTER_ALL means some level of DND is active.
     */
    fun isDndActive(): Boolean {
        val currentFilter = notificationManager.currentInterruptionFilter
        return currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    /**
     * Gets a human-readable string for the current DND state.
     */
    fun getDndStatusDescription(): String {
        return when (notificationManager.currentInterruptionFilter) {
            NotificationManager.INTERRUPTION_FILTER_ALL -> "DND Off (Normal)"
            NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "DND On (Priority Only)"
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> "DND On (Alarms Only)"
            NotificationManager.INTERRUPTION_FILTER_NONE -> "DND On (Total Silence)"
            NotificationManager.INTERRUPTION_FILTER_UNKNOWN -> "Unknown"
            else -> "Active"
        }
    }

    /**
     * Enables Do Not Disturb (Priority mode) if permission is granted.
     * Returns true if successful.
     */
    fun enableDnd(): Boolean {
        if (!hasNotificationPolicyAccess()) return false
        return try {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Disables Do Not Disturb (Restores all notifications) if permission is granted.
     * Returns true if successful.
     */
    fun disableDnd(): Boolean {
        if (!hasNotificationPolicyAccess()) return false
        return try {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Posts a status notification for geofence transitions.
     */
    fun postTransitionNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_STATUS, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS may not be granted
        }
    }
}
