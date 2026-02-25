package com.stickytimer.app.core.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "sticky_settings"

private val Context.stickyDataStore by preferencesDataStore(name = DATASTORE_NAME)

class StickySettingsRepository(private val context: Context) {

    val settings: Flow<StickySettings> = context.stickyDataStore.data.map { prefs ->
        StickySettings(
            sessionDurationSec = prefs[Keys.SESSION_DURATION_SEC] ?: StickySettings.DEFAULT_SESSION_DURATION_SEC,
            fadeDurationSec = prefs[Keys.FADE_DURATION_SEC] ?: StickySettings.DEFAULT_FADE_DURATION_SEC,
            reengagementWindowSec = prefs[Keys.REENGAGEMENT_WINDOW_SEC] ?: StickySettings.DEFAULT_REENGAGEMENT_WINDOW_SEC,
            maxActiveWindowMin = prefs[Keys.MAX_ACTIVE_WINDOW_MIN] ?: StickySettings.DEFAULT_MAX_ACTIVE_WINDOW_MIN
        )
    }

    val lastModeEnabled: Flow<Boolean> = context.stickyDataStore.data.map { prefs ->
        prefs[Keys.LAST_MODE_STATE] ?: false
    }

    suspend fun updateSessionDurationSec(value: Int) {
        context.stickyDataStore.edit { it[Keys.SESSION_DURATION_SEC] = value.coerceIn(60, 1800) }
    }

    suspend fun updateFadeDurationSec(value: Int) {
        context.stickyDataStore.edit { it[Keys.FADE_DURATION_SEC] = value.coerceIn(0, 60) }
    }

    suspend fun updateReengagementWindowSec(value: Int) {
        context.stickyDataStore.edit { it[Keys.REENGAGEMENT_WINDOW_SEC] = value.coerceIn(5, 180) }
    }

    suspend fun updateMaxActiveWindowMin(value: Int) {
        context.stickyDataStore.edit { it[Keys.MAX_ACTIVE_WINDOW_MIN] = value.coerceIn(15, 300) }
    }

    suspend fun setLastModeEnabled(enabled: Boolean) {
        context.stickyDataStore.edit { it[Keys.LAST_MODE_STATE] = enabled }
    }

    private object Keys {
        val SESSION_DURATION_SEC: Preferences.Key<Int> = intPreferencesKey("session_duration_sec")
        val FADE_DURATION_SEC: Preferences.Key<Int> = intPreferencesKey("fade_duration_sec")
        val REENGAGEMENT_WINDOW_SEC: Preferences.Key<Int> = intPreferencesKey("reengagement_window_sec")
        val MAX_ACTIVE_WINDOW_MIN: Preferences.Key<Int> = intPreferencesKey("max_active_window_min")
        val LAST_MODE_STATE: Preferences.Key<Boolean> = booleanPreferencesKey("last_mode_state")
    }
}
