package com.stickytimer.app.core.timer

interface StickyController {
    fun enable()
    fun disable(reason: DisableReason = DisableReason.MANUAL_STOP)
    fun onPlaybackStateChanged(state: StickyPlaybackState, timestampMs: Long)
}
