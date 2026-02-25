package com.stickytimer.app.core.media

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.stickytimer.app.core.timer.StickyPlaybackState
import com.stickytimer.app.platform.notificationlistener.StickyNotificationListenerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ActiveMediaSessionController(context: Context) {

    private val appContext = context.applicationContext
    private val mediaSessionManager =
        appContext.getSystemService(MediaSessionManager::class.java)
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listenerComponent = ComponentName(appContext, StickyNotificationListenerService::class.java)
    private val volumeMutex = Mutex()

    private val _playbackUpdates = MutableSharedFlow<PlaybackStateUpdate>(extraBufferCapacity = 32)
    val playbackUpdates: SharedFlow<PlaybackStateUpdate> = _playbackUpdates.asSharedFlow()

    private var activeController: MediaController? = null
    private var isTracking = false
    private var preFadeVolume: Int? = null

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { sessions ->
        updateActiveController(sessions ?: emptyList())
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            emitPlaybackState(state)
        }

        override fun onSessionDestroyed() {
            refreshActiveSessions()
        }
    }

    @Synchronized
    fun startTracking() {
        if (isTracking) return
        isTracking = true
        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                sessionsChangedListener,
                listenerComponent,
                mainHandler
            )
        } catch (_: SecurityException) {
            isTracking = false
            return
        }
        refreshActiveSessions()
    }

    @Synchronized
    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        runCatching { mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener) }
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
    }

    fun refreshActiveSessions() {
        if (!isTracking) return
        val sessions = runCatching {
            mediaSessionManager.getActiveSessions(listenerComponent)
        }.getOrDefault(emptyList())
        updateActiveController(sessions)
    }

    fun isCurrentlyPlaying(): Boolean {
        val state = activeController?.playbackState?.state ?: return false
        return state == PlaybackState.STATE_PLAYING
    }

    suspend fun pauseWithRetry() {
        val controller = activeController ?: return
        controller.transportControls.pause()
        delay(700L)
        if (isCurrentlyPlaying()) {
            dispatchPauseMediaKey()
        }
    }

    suspend fun fadeAndPause(fadeDurationMs: Long) {
        val streamType = AudioManager.STREAM_MUSIC
        if (fadeDurationMs <= 0L) {
            pauseWithRetry()
            restoreVolumeIfNeeded()
            return
        }

        volumeMutex.withLock {
            val currentVolume = audioManager.getStreamVolume(streamType)
            val minVolume = audioManager.getStreamMinVolume(streamType)
            preFadeVolume = currentVolume

            if (currentVolume > minVolume) {
                val steps = (fadeDurationMs / 250L).coerceIn(1L, 20L).toInt()
                val stepDelay = (fadeDurationMs / steps).coerceAtLeast(1L)
                for (step in steps downTo 1) {
                    val target = minVolume + ((currentVolume - minVolume) * step / steps)
                    audioManager.setStreamVolume(streamType, target, 0)
                    delay(stepDelay)
                }
            } else {
                delay(fadeDurationMs)
            }
        }

        pauseWithRetry()
        restoreVolumeIfNeeded()
    }

    suspend fun restoreVolumeIfNeeded() {
        val streamType = AudioManager.STREAM_MUSIC
        volumeMutex.withLock {
            val originalVolume = preFadeVolume ?: return
            audioManager.setStreamVolume(streamType, originalVolume, 0)
            preFadeVolume = null
        }
    }

    @Synchronized
    private fun updateActiveController(controllers: List<MediaController>) {
        val selected = selectController(controllers)
        if (selected?.sessionToken == activeController?.sessionToken) return

        activeController?.unregisterCallback(controllerCallback)
        activeController = selected
        selected?.registerCallback(controllerCallback, mainHandler)
        emitPlaybackState(selected?.playbackState)
    }

    private fun selectController(controllers: List<MediaController>): MediaController? {
        return controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: controllers.maxByOrNull { it.playbackState?.lastPositionUpdateTime ?: 0L }
    }

    private fun emitPlaybackState(state: PlaybackState?) {
        _playbackUpdates.tryEmit(
            PlaybackStateUpdate(
                state = mapPlaybackState(state?.state),
                timestampMs = System.currentTimeMillis()
            )
        )
    }

    private fun mapPlaybackState(state: Int?): StickyPlaybackState {
        return when (state) {
            PlaybackState.STATE_PLAYING -> StickyPlaybackState.PLAYING
            PlaybackState.STATE_PAUSED -> StickyPlaybackState.PAUSED
            PlaybackState.STATE_STOPPED -> StickyPlaybackState.STOPPED
            PlaybackState.STATE_BUFFERING,
            PlaybackState.STATE_CONNECTING,
            PlaybackState.STATE_FAST_FORWARDING,
            PlaybackState.STATE_REWINDING -> StickyPlaybackState.BUFFERING

            else -> StickyPlaybackState.UNKNOWN
        }
    }

    private fun dispatchPauseMediaKey() {
        val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
        audioManager.dispatchMediaKeyEvent(down)
        audioManager.dispatchMediaKeyEvent(up)
    }
}
