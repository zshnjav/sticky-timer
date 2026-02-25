package com.stickytimer.app.platform.notificationlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.stickytimer.app.StickyTimerApp

class StickyNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        (application as StickyTimerApp).appContainer.mediaController.refreshActiveSessions()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        (application as StickyTimerApp).appContainer.mediaController.refreshActiveSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        (application as StickyTimerApp).appContainer.mediaController.refreshActiveSessions()
    }
}
