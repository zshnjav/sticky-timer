package com.stickytimer.app.core.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.stickytimer.app.platform.notificationlistener.StickyNotificationListenerService

class NotificationAccessManager(private val context: Context) {

    private val componentName = ComponentName(context, StickyNotificationListenerService::class.java)

    fun hasAccess(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.split(':').any { flattened ->
            ComponentName.unflattenFromString(flattened) == componentName
        }
    }

    fun accessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
