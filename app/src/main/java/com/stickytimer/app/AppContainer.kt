package com.stickytimer.app

import android.content.Context
import com.stickytimer.app.core.media.ActiveMediaSessionController
import com.stickytimer.app.core.settings.NotificationAccessManager
import com.stickytimer.app.core.settings.StickySettingsRepository
import com.stickytimer.app.feature.sticky.StickyModeCoordinator
import com.stickytimer.app.feature.sticky.StickyModeStatusStore

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsRepository = StickySettingsRepository(appContext)
    val notificationAccessManager = NotificationAccessManager(appContext)
    val statusStore = StickyModeStatusStore()
    val mediaController = ActiveMediaSessionController(appContext)
    val coordinator = StickyModeCoordinator(
        context = appContext,
        settingsRepository = settingsRepository,
        accessManager = notificationAccessManager,
        mediaController = mediaController,
        statusStore = statusStore
    )
}
