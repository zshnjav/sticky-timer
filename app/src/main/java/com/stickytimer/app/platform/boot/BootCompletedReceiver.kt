package com.stickytimer.app.platform.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stickytimer.app.StickyTimerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val app = context.applicationContext as StickyTimerApp
            app.appContainer.settingsRepository.setLastModeEnabled(false)
            app.appContainer.statusStore.publish(com.stickytimer.app.core.timer.StickyModeSnapshot.off())
            pendingResult.finish()
        }
    }
}
