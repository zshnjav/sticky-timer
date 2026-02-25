package com.stickytimer.app.core.timer

import com.stickytimer.app.core.settings.StickySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StickyTimerEngine(
    private val scope: CoroutineScope,
    private val timeSource: StickyTimeSource,
    private val settingsProvider: () -> StickySettings,
    private val callbacks: StickyEngineCallbacks
) : StickyController {

    companion object {
        const val PLAYBACK_STABLE_DEBOUNCE_MS = 1_500L
    }

    private val mutex = Mutex()
    private val _state = MutableStateFlow(StickyModeSnapshot.off())

    val state: StateFlow<StickyModeSnapshot> = _state.asStateFlow()

    private var enabled = false
    private var phase = StickyPhase.OFF
    private var latestPlaybackState = StickyPlaybackState.UNKNOWN
    private var lastStoppedAtMs: Long? = null
    private var sessionEndMs: Long? = null
    private var maxWindowEndMs: Long? = null

    private var sessionJob: Job? = null
    private var maxWindowJob: Job? = null
    private var debounceJob: Job? = null
    private var tickerJob: Job? = null

    override fun enable() {
        scope.launch {
            mutex.withLock {
                if (enabled) return@withLock
                enabled = true
                phase = StickyPhase.ON_IDLE
                latestPlaybackState = StickyPlaybackState.UNKNOWN
                lastStoppedAtMs = null
                sessionEndMs = null
                maxWindowEndMs = null
                clearTimerJobsLocked()
                ensureTickerLocked()
                emitStateLocked()
            }
        }
    }

    override fun disable(reason: DisableReason) {
        scope.launch { disableInternal(reason) }
    }

    override fun onPlaybackStateChanged(state: StickyPlaybackState, timestampMs: Long) {
        scope.launch {
            var shouldScheduleDebounce = false
            var shouldArmImmediately = false
            var shouldCancelDebounce = false
            var shouldHandleMaxExpiry = false

            mutex.withLock {
                if (!enabled) return@withLock
                val previousPlaybackState = latestPlaybackState
                latestPlaybackState = state
                val now = timeSource.nowMs()

                if (maxWindowEndMs != null && now >= maxWindowEndMs!!) {
                    shouldHandleMaxExpiry = true
                    return@withLock
                }

                when (state) {
                    StickyPlaybackState.PLAYING -> {
                        if (phase == StickyPhase.SESSION_RUNNING || phase == StickyPhase.FADING) {
                            return@withLock
                        }
                        if (previousPlaybackState == StickyPlaybackState.PLAYING) {
                            return@withLock
                        }
                        val withinReengagement = isWithinReengagementWindowLocked(now)
                        if (withinReengagement) {
                            shouldArmImmediately = true
                            shouldCancelDebounce = true
                        } else {
                            shouldScheduleDebounce = true
                        }
                    }

                    else -> {
                        shouldCancelDebounce = true
                    }
                }
            }

            if (shouldCancelDebounce) {
                debounceJob?.cancel()
            }
            if (shouldArmImmediately) {
                armSessionNow()
            } else if (shouldScheduleDebounce) {
                schedulePlayDebounce()
            }
            if (shouldHandleMaxExpiry) {
                handleMaxWindowExpiry()
            }
        }
    }

    private fun schedulePlayDebounce() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(PLAYBACK_STABLE_DEBOUNCE_MS)
            armSessionNow()
        }
    }

    private suspend fun armSessionNow() {
        mutex.withLock {
            if (!enabled || latestPlaybackState != StickyPlaybackState.PLAYING) return@withLock
            val settings = settingsProvider()
            val now = timeSource.nowMs()

            if (maxWindowEndMs == null) {
                maxWindowEndMs = now + settings.maxActiveWindowMs
                scheduleMaxWindowExpiryLocked(maxWindowEndMs!! - now)
            }
            if (maxWindowEndMs != null && now >= maxWindowEndMs!!) return@withLock

            phase = StickyPhase.SESSION_RUNNING
            sessionEndMs = now + settings.sessionDurationMs
            scheduleSessionExpiryLocked(settings.sessionDurationMs)
            emitStateLocked()
        }
    }

    private fun scheduleSessionExpiryLocked(delayMs: Long) {
        sessionJob?.cancel()
        sessionJob = scope.launch {
            delay(delayMs.coerceAtLeast(0L))
            handleSessionExpiry()
        }
    }

    private fun scheduleMaxWindowExpiryLocked(delayMs: Long) {
        maxWindowJob?.cancel()
        maxWindowJob = scope.launch {
            delay(delayMs.coerceAtLeast(0L))
            handleMaxWindowExpiry()
        }
    }

    private suspend fun handleSessionExpiry() {
        val fadeMs = settingsProvider().fadeDurationMs
        val shouldRunPause = mutex.withLock {
            if (!enabled) return
            if (phase != StickyPhase.SESSION_RUNNING) return
            phase = if (fadeMs > 0L) StickyPhase.FADING else StickyPhase.STOPPED_RECENTLY
            emitStateLocked()
            true
        }
        if (!shouldRunPause) return

        if (fadeMs > 0L) {
            callbacks.fadeAndPause(fadeMs)
        } else {
            callbacks.pauseNow()
        }

        var shouldHandleMaxExpiry = false
        mutex.withLock {
            if (!enabled) return
            latestPlaybackState = StickyPlaybackState.PAUSED
            lastStoppedAtMs = timeSource.nowMs()
            sessionEndMs = null
            phase = StickyPhase.STOPPED_RECENTLY
            emitStateLocked()
            shouldHandleMaxExpiry = maxWindowEndMs?.let { timeSource.nowMs() >= it } == true
        }

        if (shouldHandleMaxExpiry) {
            handleMaxWindowExpiry()
        }
    }

    private suspend fun handleMaxWindowExpiry() {
        val shouldPause = mutex.withLock {
            if (!enabled) return
            phase = StickyPhase.EXPIRED
            emitStateLocked()
            latestPlaybackState == StickyPlaybackState.PLAYING
        }

        if (shouldPause) {
            val fadeMs = settingsProvider().fadeDurationMs
            if (fadeMs > 0L) {
                callbacks.fadeAndPause(fadeMs)
            } else {
                callbacks.pauseNow()
            }
        }
        disableInternal(DisableReason.MAX_WINDOW_EXPIRED)
    }

    private suspend fun disableInternal(reason: DisableReason) {
        val shouldDisable = mutex.withLock {
            if (!enabled && phase == StickyPhase.OFF) return
            enabled = false
            phase = StickyPhase.OFF
            latestPlaybackState = StickyPlaybackState.UNKNOWN
            lastStoppedAtMs = null
            sessionEndMs = null
            maxWindowEndMs = null
            clearTimerJobsLocked()
            emitStateLocked()
            true
        }
        if (!shouldDisable) return
        callbacks.onDisabled()
    }

    private fun clearTimerJobsLocked() {
        sessionJob?.cancel()
        maxWindowJob?.cancel()
        debounceJob?.cancel()
        tickerJob?.cancel()
        sessionJob = null
        maxWindowJob = null
        debounceJob = null
        tickerJob = null
    }

    private fun ensureTickerLocked() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (isActive) {
                delay(1_000L)
                mutex.withLock {
                    if (!enabled) return@withLock
                    emitStateLocked()
                }
            }
        }
    }

    private fun isWithinReengagementWindowLocked(nowMs: Long): Boolean {
        val stoppedAt = lastStoppedAtMs ?: return false
        return nowMs - stoppedAt <= settingsProvider().reengagementWindowMs
    }

    private fun emitStateLocked() {
        val now = timeSource.nowMs()
        val remainingSession = (sessionEndMs?.minus(now) ?: 0L).coerceAtLeast(0L)
        val remainingMaxWindow = (maxWindowEndMs?.minus(now) ?: 0L).coerceAtLeast(0L)
        _state.value = StickyModeSnapshot(
            isEnabled = enabled,
            phase = phase,
            remainingSessionMs = remainingSession,
            maxWindowRemainingMs = remainingMaxWindow
        )
    }
}
