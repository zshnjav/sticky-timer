package com.stickytimer.app.core.media

import com.stickytimer.app.core.timer.StickyPlaybackState

data class PlaybackStateUpdate(
    val state: StickyPlaybackState,
    val timestampMs: Long
)
