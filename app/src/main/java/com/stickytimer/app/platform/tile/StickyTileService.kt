package com.stickytimer.app.platform.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.stickytimer.app.R
import com.stickytimer.app.StickyTimerApp
import com.stickytimer.app.core.timer.StickyModeSnapshot
import com.stickytimer.app.feature.sticky.StickyForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StickyTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listeningJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        if (listeningJob?.isActive == true) return
        val app = application as StickyTimerApp
        listeningJob = scope.launch {
            app.appContainer.statusStore.state.collectLatest { snapshot ->
                applySnapshotToTile(snapshot)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onDestroy() {
        listeningJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()
        val app = application as StickyTimerApp
        val snapshot = app.appContainer.statusStore.state.value
        val targetEnabled = !snapshot.isEnabled

        // Optimistic update to avoid stale tile state while async service/coordinator work completes.
        applyEnabledToTile(targetEnabled)

        if (snapshot.isEnabled) {
            StickyForegroundService.requestDisable(this)
        } else {
            StickyForegroundService.startEnable(this)
        }
    }

    private fun applySnapshotToTile(snapshot: StickyModeSnapshot) {
        applyEnabledToTile(snapshot.isEnabled)
    }

    private fun applyEnabledToTile(isEnabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.subtitle = if (isEnabled) getString(R.string.toggle_on) else getString(R.string.toggle_off)
        tile.updateTile()
    }
}
