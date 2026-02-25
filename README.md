# Sticky Timer Playback

Sticky Timer Playback is a minimal Android utility for bedtime listening with Bluetooth earbuds/headphones.

## MVP behavior

- Sticky mode ON/OFF via Quick Settings tile or in-app toggle.
- Configurable session controls:
  - Session duration (default 5:00)
  - Fade-out length (default 10s)
  - Re-engagement window (default 30s)
  - Max active window (default 90m)
- While sticky mode is ON:
  - Playback resume arms/re-arms the timer.
  - Timer expiry performs best-effort fade, then pause.
  - Resume after stop re-arms automatically.
  - Max active window expiry fades/pauses if needed, then disables sticky mode.
- Foreground notification is shown only while sticky mode is active, with a `Stop` action.

## Runtime requirements

- Android 10+ (API 29+)
- Notification Access permission enabled for Sticky Timer Playback notification listener service.

## Testing checklist (manual)

- Resume playback from earbuds re-arms timer.
- Fade-out occurs before pause at session expiry.
- Resume within re-engagement window re-arms immediately.
- Sticky mode disables itself after max active window.
- Tile and notification state match actual sticky mode state.
- Reboot leaves sticky mode OFF.
