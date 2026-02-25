package com.stickytimer.app.core.timer

import com.stickytimer.app.core.settings.StickySettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StickyTimerEngineTest {

    @Test
    fun firstResumeAnchorsMaxWindow() = runTest {
        val callbacks = FakeCallbacks()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val clock = TestSchedulerClock(testScheduler)
        val settings = StickySettings(
            sessionDurationSec = 300,
            fadeDurationSec = 10,
            reengagementWindowSec = 30,
            maxActiveWindowMin = 90
        )
        val engine = StickyTimerEngine(scope, clock, { settings }, callbacks)

        engine.enable()
        runCurrent()
        engine.onPlaybackStateChanged(StickyPlaybackState.PLAYING, clock.nowMs())
        runCurrent()
        advanceTimeBy(StickyTimerEngine.PLAYBACK_STABLE_DEBOUNCE_MS)
        runCurrent()

        val state = engine.state.value
        assertTrue(state.isEnabled)
        assertEquals(StickyPhase.SESSION_RUNNING, state.phase)
        assertTrue(state.maxWindowRemainingMs in (settings.maxActiveWindowMs - 5_000L)..settings.maxActiveWindowMs)
    }

    @Test
    fun sessionExpiryTriggersFadeAndPause() = runTest {
        val callbacks = FakeCallbacks()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val clock = TestSchedulerClock(testScheduler)
        val settings = StickySettings(
            sessionDurationSec = 10,
            fadeDurationSec = 5,
            reengagementWindowSec = 30,
            maxActiveWindowMin = 90
        )
        val engine = StickyTimerEngine(scope, clock, { settings }, callbacks)

        engine.enable()
        runCurrent()
        engine.onPlaybackStateChanged(StickyPlaybackState.PLAYING, clock.nowMs())
        runCurrent()
        advanceTimeBy(StickyTimerEngine.PLAYBACK_STABLE_DEBOUNCE_MS)
        runCurrent()

        advanceTimeBy(settings.sessionDurationMs)
        runCurrent()

        assertTrue(callbacks.fadeAndPauseCalls == 1)
        assertEquals(StickyPhase.STOPPED_RECENTLY, engine.state.value.phase)
    }

    @Test
    fun resumeInsideWindowIsImmediateOutsideWindowDebounced() = runTest {
        val callbacks = FakeCallbacks()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val clock = TestSchedulerClock(testScheduler)
        val settings = StickySettings(
            sessionDurationSec = 5,
            fadeDurationSec = 0,
            reengagementWindowSec = 30,
            maxActiveWindowMin = 90
        )
        val engine = StickyTimerEngine(scope, clock, { settings }, callbacks)

        engine.enable()
        runCurrent()
        engine.onPlaybackStateChanged(StickyPlaybackState.PLAYING, clock.nowMs())
        runCurrent()
        advanceTimeBy(StickyTimerEngine.PLAYBACK_STABLE_DEBOUNCE_MS)
        runCurrent()
        advanceTimeBy(settings.sessionDurationMs)
        runCurrent()

        engine.onPlaybackStateChanged(StickyPlaybackState.PLAYING, clock.nowMs())
        runCurrent()
        assertEquals(StickyPhase.SESSION_RUNNING, engine.state.value.phase)

        advanceTimeBy(settings.sessionDurationMs)
        runCurrent()

        advanceTimeBy(settings.reengagementWindowMs + 1_000L)
        engine.onPlaybackStateChanged(StickyPlaybackState.PLAYING, clock.nowMs())
        runCurrent()
        assertEquals(StickyPhase.STOPPED_RECENTLY, engine.state.value.phase)
        advanceTimeBy(StickyTimerEngine.PLAYBACK_STABLE_DEBOUNCE_MS)
        runCurrent()
        assertEquals(StickyPhase.SESSION_RUNNING, engine.state.value.phase)
    }

    @Test
    fun maxWindowExpiryWhilePlayingDisablesMode() = runTest {
        val callbacks = FakeCallbacks()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val clock = TestSchedulerClock(testScheduler)
        val settings = StickySettings(
            sessionDurationSec = 300,
            fadeDurationSec = 3,
            reengagementWindowSec = 30,
            maxActiveWindowMin = 1
        )
        val engine = StickyTimerEngine(scope, clock, { settings }, callbacks)

        engine.enable()
        runCurrent()
        engine.onPlaybackStateChanged(StickyPlaybackState.PLAYING, clock.nowMs())
        runCurrent()
        advanceTimeBy(StickyTimerEngine.PLAYBACK_STABLE_DEBOUNCE_MS)
        runCurrent()

        advanceTimeBy(settings.maxActiveWindowMs)
        runCurrent()

        assertFalse(engine.state.value.isEnabled)
        assertEquals(StickyPhase.OFF, engine.state.value.phase)
        assertTrue(callbacks.fadeAndPauseCalls >= 1)
    }

    private class FakeCallbacks : StickyEngineCallbacks {
        var fadeAndPauseCalls = 0
        var pauseCalls = 0
        var disabledCalls = 0

        override suspend fun fadeAndPause(fadeDurationMs: Long) {
            fadeAndPauseCalls += 1
        }

        override suspend fun pauseNow() {
            pauseCalls += 1
        }

        override suspend fun onDisabled() {
            disabledCalls += 1
        }
    }

    private class TestSchedulerClock(
        private val scheduler: kotlinx.coroutines.test.TestCoroutineScheduler
    ) : StickyTimeSource {
        override fun nowMs(): Long = scheduler.currentTime
    }
}
