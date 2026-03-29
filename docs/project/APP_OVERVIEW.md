# App Overview

Sticky Timer Playback is a small Android utility for bedtime listening. The current app branding in code and strings is `Bedtime Sticky Timer`, and the core interaction is still the same: keep a short playback window "sticky" so a user can restart audio from earbuds without reaching for the phone.

## Current baseline

- Platform: Android app built with Kotlin, Jetpack Compose, and DataStore.
- Activation: sticky mode can be toggled from the Quick Settings tile or the in-app settings screen.
- Runtime model: a foreground service runs while sticky mode is enabled so timer behavior remains reliable under background limits.
- Playback detection: the app uses a notification-listener-backed active media session controller to observe third-party playback and issue pause/seek commands.
- Timer behavior: the timer engine debounces fresh playback for 1.5 seconds, arms a session timer, fades out near expiry, then pauses playback.
- Rearm behavior: if playback resumes during the recent-stop window, the timer rearms immediately and the coordinator rewinds 20 seconds before resuming the next session.
- Session cap: the first stable resume anchors the max active window; once it expires, sticky mode turns off.
- Persistence: user settings are stored locally in DataStore.

## Current configurable settings

- Session duration: persisted, default `60s`, adjustable from `1` to `30` minutes in one-minute steps.
- Max active window: persisted, default `90m`, adjustable from `15` to `300` minutes in five-minute steps.
- Auto-enable time: optional daily schedule stored as minutes-of-day.

## Current fixed behavior

- Fade-out length is fixed at `10s`.
- Re-engagement window is fixed at `30s`.
- Rewind-after-resume is fixed at `20s`.
- Reboot resets sticky mode to off, while saved auto-enable schedules are re-synced.

## Gaps between repo brief and current implementation

- `AGENTS.md` describes a default session of about five minutes; current code defaults to one minute.
- `AGENTS.md` lists configurable fade length and re-engagement window; current code intentionally fixes both values for a simpler settings UI.
- `AGENTS.md` keeps the MVP tight; the current codebase already includes an optional daily auto-enable schedule, which should be treated as an explicit scope decision.
- The current coordinator pauses active media immediately when sticky mode is enabled so bedtime starts from a clean baseline. That is stricter than the original product brief and needs explicit product confirmation.

## Validation status

- Unit coverage exists for the timer engine state machine and timing transitions.
- Cross-app acceptance testing called out in `AGENTS.md` is not yet captured in repo docs or automated tests.
