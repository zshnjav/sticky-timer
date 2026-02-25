package com.stickytimer.app.core.timer

interface StickyEngineCallbacks {
    suspend fun fadeAndPause(fadeDurationMs: Long)
    suspend fun pauseNow()
    suspend fun onDisabled()
}
