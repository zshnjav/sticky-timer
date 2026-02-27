package com.stickytimer.app.feature.sticky

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.stickytimer.app.MainActivity
import com.stickytimer.app.R
import com.stickytimer.app.StickyTimerApp
import com.stickytimer.app.core.timer.DisableReason
import com.stickytimer.app.core.timer.StickyModeSnapshot
import com.stickytimer.app.core.timer.StickyPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class StickyForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var app: StickyTimerApp

    private var notificationStarted = false
    private var hasEverBeenEnabled = false
    private var stopIssued = false

    override fun onCreate() {
        super.onCreate()
        app = application as StickyTimerApp
        createNotificationChannel()
        scope.launch {
            app.appContainer.statusStore.state.collectLatest { snapshot ->
                if (snapshot.isEnabled) {
                    hasEverBeenEnabled = true
                    ensureForeground(snapshot)
                } else if (hasEverBeenEnabled && !stopIssued) {
                    stopIssued = true
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else if (notificationStarted) {
                    updateNotification(snapshot)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            StickyServiceActions.ACTION_ENABLE -> {
                ensureForeground(app.appContainer.statusStore.state.value)
                val enabled = app.appContainer.coordinator.enable()
                if (!enabled) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }

            StickyServiceActions.ACTION_DISABLE,
            StickyServiceActions.ACTION_NOTIFICATION_STOP -> {
                stopIssued = true
                app.appContainer.coordinator.disable(DisableReason.MANUAL_STOP)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureForeground(snapshot: StickyModeSnapshot) {
        if (!notificationStarted) {
            startForeground(NOTIFICATION_ID, buildNotification(snapshot))
            notificationStarted = true
        } else {
            updateNotification(snapshot)
        }
    }

    private fun updateNotification(snapshot: StickyModeSnapshot) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(snapshot))
    }

    private fun buildNotification(snapshot: StickyModeSnapshot): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            100,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            101,
            Intent(this, StickyForegroundService::class.java).setAction(StickyServiceActions.ACTION_NOTIFICATION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_sticky)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(statusText(snapshot))
            .setContentIntent(contentIntent)
            .setOngoing(snapshot.isEnabled)
            .addAction(0, getString(R.string.notification_stop), stopIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun statusText(snapshot: StickyModeSnapshot): String {
        return when (snapshot.phase) {
            StickyPhase.OFF -> getString(R.string.notification_text_idle)
            StickyPhase.ON_IDLE -> getString(R.string.notification_text_idle)
            StickyPhase.SESSION_RUNNING -> getString(
                R.string.notification_text_running_with_remaining,
                formatRemainingSession(snapshot.remainingSessionMs)
            )
            StickyPhase.FADING -> getString(R.string.notification_text_fading)
            StickyPhase.STOPPED_RECENTLY -> getString(R.string.notification_text_recent_stop)
            StickyPhase.EXPIRED -> getString(R.string.notification_text_expired)
        }
    }

    private fun formatRemainingSession(remainingMs: Long): String {
        val totalSeconds = (remainingMs.coerceAtLeast(0L) + 999L) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "sticky_timer_active"
        private const val NOTIFICATION_ID = 0x5147

        fun startEnable(context: Context) {
            val intent = Intent(context, StickyForegroundService::class.java)
                .setAction(StickyServiceActions.ACTION_ENABLE)
            ContextCompat.startForegroundService(context, intent)
        }

        fun requestDisable(context: Context) {
            val intent = Intent(context, StickyForegroundService::class.java)
                .setAction(StickyServiceActions.ACTION_DISABLE)
            context.startService(intent)
        }
    }
}
