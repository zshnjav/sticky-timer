package com.stickytimer.app.feature.sticky

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import com.stickytimer.app.core.media.ActiveMediaSessionController
import com.stickytimer.app.core.settings.NotificationAccessManager
import com.stickytimer.app.core.settings.StickySettings
import com.stickytimer.app.core.settings.StickySettingsRepository
import com.stickytimer.app.core.timer.DisableReason
import com.stickytimer.app.core.timer.StickyEngineCallbacks
import com.stickytimer.app.core.timer.StickyTimerEngine
import com.stickytimer.app.core.timer.SystemStickyTimeSource
import com.stickytimer.app.platform.tile.StickyTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StickyModeCoordinator(
    context: Context,
    private val settingsRepository: StickySettingsRepository,
    private val accessManager: NotificationAccessManager,
    private val mediaController: ActiveMediaSessionController,
    private val statusStore: StickyModeStatusStore
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var settingsSnapshot: StickySettings = StickySettings()

    private var playbackCollectionStarted = false
    private var lastPersistedModeEnabled: Boolean? = null

    private val engine = StickyTimerEngine(
        scope = scope,
        timeSource = SystemStickyTimeSource,
        settingsProvider = { settingsSnapshot },
        callbacks = object : StickyEngineCallbacks {
            override suspend fun fadeAndPause(fadeDurationMs: Long) {
                mediaController.fadeAndPause(fadeDurationMs)
            }

            override suspend fun pauseNow() {
                mediaController.pauseWithRetry()
            }

            override suspend fun onDisabled() {
                mediaController.restoreVolumeIfNeeded()
                mediaController.stopTracking()
                playbackCollectionStarted = false
            }
        }
    )

    init {
        scope.launch {
            settingsRepository.settings.collectLatest { settings ->
                settingsSnapshot = settings
            }
        }
        scope.launch {
            engine.state.collectLatest { snapshot ->
                statusStore.publish(snapshot)
                if (lastPersistedModeEnabled != snapshot.isEnabled) {
                    lastPersistedModeEnabled = snapshot.isEnabled
                    settingsRepository.setLastModeEnabled(snapshot.isEnabled)
                }
                TileService.requestListeningState(
                    appContext,
                    ComponentName(appContext, StickyTileService::class.java)
                )
            }
        }
    }

    fun enable(): Boolean {
        if (!accessManager.hasAccess()) {
            statusStore.publish(com.stickytimer.app.core.timer.StickyModeSnapshot.off())
            scope.launch { settingsRepository.setLastModeEnabled(false) }
            return false
        }
        mediaController.startTracking()
        if (!playbackCollectionStarted) {
            playbackCollectionStarted = true
            scope.launch {
                mediaController.playbackUpdates.collectLatest { update ->
                    engine.onPlaybackStateChanged(update.state, update.timestampMs)
                }
            }
        }
        engine.enable()
        return true
    }

    fun disable(reason: DisableReason = DisableReason.MANUAL_STOP) {
        engine.disable(reason)
    }
}
