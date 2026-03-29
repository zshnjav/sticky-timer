package com.stickytimer.app.platform.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stickytimer.app.StickyTimerApp
import com.stickytimer.app.feature.sticky.BedtimeAutoEnableScheduler
import com.stickytimer.app.feature.sticky.StickyForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BedtimeAutoEnableReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != BedtimeAutoEnableScheduler.ACTION_AUTO_ENABLE_ALARM) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val app = context.applicationContext as StickyTimerApp
            val settings = app.appContainer.settingsRepository.settings.first()

            app.appContainer.autoEnableScheduler.sync(settings)
            if (settings.hasAutoEnableTime) {
                StickyForegroundService.startEnable(context)
            }

            pendingResult.finish()
        }
    }
}
