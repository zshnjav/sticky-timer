# Architecture

## System shape

The app is organized around a small set of long-lived components:

- `StickyTimerApp` creates a single `AppContainer` on process start.
- `AppContainer` wires settings, permission checks, media control, status storage, auto-enable scheduling, and the sticky-mode coordinator.
- `StickyModeCoordinator` is the orchestration layer between Android entry points, playback updates, and the timer engine.
- `StickyTimerEngine` owns sticky-mode state transitions, countdowns, debounce timing, recent-stop behavior, and max-window expiry.
- `ActiveMediaSessionController` talks to Android media sessions, controls playback, and performs volume fade / restore.
- `StickyModeStatusStore` publishes the current snapshot for the tile, UI, and foreground service.

## Runtime flow

1. A user enables sticky mode from the Quick Settings tile, the in-app switch, or the daily auto-enable alarm.
2. `StickyForegroundService` starts and calls `StickyModeCoordinator.enable()`.
3. The coordinator verifies notification-listener access, starts media tracking, enables the timer engine, and currently pauses any already-playing media once to establish a clean bedtime start.
4. `ActiveMediaSessionController` emits playback-state updates from the selected active media session.
5. `StickyTimerEngine` debounces stable playback, starts the session countdown, and anchors the max active window on the first valid play event.
6. On session expiry, the engine moves to `FADING` or `STOPPED_RECENTLY`, then asks the media controller to fade and pause.
7. If playback resumes inside the recent-stop window, the coordinator rewinds `20s` and the engine rearms immediately.
8. Manual stop or max-window expiry disables sticky mode, stops media tracking, and collapses the foreground service.

## State model

The timer engine publishes `StickyModeSnapshot` values with these phases:

- `OFF`
- `ON_IDLE`
- `SESSION_RUNNING`
- `FADING`
- `STOPPED_RECENTLY`
- `EXPIRED`

The snapshot also carries remaining session time and remaining max-window time for UI and notification rendering.

## Android entry points

- `MainActivity` hosts the Compose settings screen.
- `StickyTileService` toggles sticky mode and mirrors state into Quick Settings.
- `StickyForegroundService` owns the active notification lifecycle and provides the notification stop action.
- `StickyNotificationListenerService` refreshes active media sessions when notifications change.
- `BootCompletedReceiver` resets the mode state after boot and re-synchronizes alarms.
- `BedtimeAutoEnableReceiver` re-schedules the next alarm and enables sticky mode at the configured time.

## Persistence and scheduling

- `StickySettingsRepository` stores user settings in DataStore preferences.
- `BedtimeAutoEnableScheduler` maps the saved bedtime time-of-day to an `AlarmManager.setAndAllowWhileIdle()` alarm.
- `StickyModeStatusStore` is in-memory only and is rebuilt on process restart.

## Notable constraints

- Active session selection prefers a currently playing controller and otherwise falls back to the most recently updated session.
- Fade-out uses the device music stream volume and restores the previous level after pause.
- The architecture assumes notification-listener access is the cross-app integration point instead of Accessibility.
