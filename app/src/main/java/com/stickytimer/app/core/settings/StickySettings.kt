package com.stickytimer.app.core.settings

data class StickySettings(
    val sessionDurationSec: Int = DEFAULT_SESSION_DURATION_SEC,
    val fadeDurationSec: Int = DEFAULT_FADE_DURATION_SEC,
    val reengagementWindowSec: Int = DEFAULT_REENGAGEMENT_WINDOW_SEC,
    val maxActiveWindowMin: Int = DEFAULT_MAX_ACTIVE_WINDOW_MIN
) {
    val sessionDurationMs: Long = sessionDurationSec * 1000L
    val fadeDurationMs: Long = fadeDurationSec * 1000L
    val reengagementWindowMs: Long = reengagementWindowSec * 1000L
    val maxActiveWindowMs: Long = maxActiveWindowMin * 60_000L

    companion object {
        const val DEFAULT_SESSION_DURATION_SEC = 5 * 60
        const val DEFAULT_FADE_DURATION_SEC = 10
        const val DEFAULT_REENGAGEMENT_WINDOW_SEC = 30
        const val DEFAULT_MAX_ACTIVE_WINDOW_MIN = 90
    }
}
