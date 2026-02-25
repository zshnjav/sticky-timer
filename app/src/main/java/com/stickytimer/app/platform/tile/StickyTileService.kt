package com.stickytimer.app.platform.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.stickytimer.app.R
import com.stickytimer.app.StickyTimerApp
import com.stickytimer.app.feature.sticky.StickyForegroundService

class StickyTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val app = application as StickyTimerApp
        val isEnabled = app.appContainer.statusStore.state.value.isEnabled
        if (isEnabled) {
            StickyForegroundService.requestDisable(this)
        } else {
            StickyForegroundService.startEnable(this)
        }
        refreshTile()
    }

    private fun refreshTile() {
        val app = application as StickyTimerApp
        val snapshot = app.appContainer.statusStore.state.value
        val tile = qsTile ?: return
        tile.state = if (snapshot.isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.subtitle = if (snapshot.isEnabled) getString(R.string.toggle_on) else getString(R.string.toggle_off)
        tile.updateTile()
    }
}
