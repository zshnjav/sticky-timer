# Services And Config

## Build baseline

- App module: `app`
- Language/runtime: Kotlin on Android with Java/Kotlin target `17`
- UI stack: Jetpack Compose + Material 3
- SDKs: `minSdk 29`, `targetSdk 35`, `compileSdk 35`
- Current app version: `1.0.0` (`versionCode 1`)

## Android components

- `MainActivity`
  - Exported launcher activity for the settings screen.
- `StickyForegroundService`
  - Non-exported foreground service with `mediaPlayback` foreground-service type.
- `StickyTileService`
  - Exported Quick Settings tile service guarded by `android.permission.BIND_QUICK_SETTINGS_TILE`.
- `StickyNotificationListenerService`
  - Non-exported notification listener guarded by `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`.
- `BootCompletedReceiver`
  - Non-exported receiver for boot, locked boot, time change, and timezone change.
- `BedtimeAutoEnableReceiver`
  - Non-exported receiver for the app's bedtime auto-enable alarm.

## Declared permissions

- `android.permission.FOREGROUND_SERVICE`
- `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `android.permission.RECEIVE_BOOT_COMPLETED`
- `android.permission.POST_NOTIFICATIONS`

## Special access requirements

- Cross-app playback observation/control currently depends on notification-listener access.
- The settings screen routes users to `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` when that access is missing.
- The repo declares `POST_NOTIFICATIONS`, but there is no documented or obvious in-app request flow yet.

## Persistent settings

DataStore name: `sticky_settings`

Stored keys:

- `session_duration_sec`
  - Default `60`
  - Clamped to `60..1800`
- `max_active_window_min`
  - Default `90`
  - Clamped to `15..300`
- `auto_enable_time_minutes_of_day`
  - Default `-1` (`AUTO_ENABLE_DISABLED`)
  - Valid range `0..1439`
- `last_mode_state`
  - Boolean mirror of the latest enabled/disabled state

Computed but not persisted as user choices:

- Fade duration: fixed at `10s`
- Re-engagement window: fixed at `30s`

## Runtime constants and behavior

- Playback stability debounce: `1500ms`
- Resume rewind amount: `20000ms`
- Foreground notification channel id: `sticky_timer_active`
- Auto-enable alarms use `AlarmManager.setAndAllowWhileIdle()`

## Test coverage currently in repo

- `StickyTimerEngineTest` covers timer arming, fade/pause on expiry, quick re-engagement, and max-window expiry.
- There are no repo-local instrumentation tests or documented manual acceptance test runs for third-party media apps yet.
