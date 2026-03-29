# Bedtime Sticky Timer

Bedtime Sticky Timer is a minimal Android utility for bedtime listening with Bluetooth earbuds/headphones.

## MVP behavior

- Bedtime Timer ON/OFF via Quick Settings tile or in-app toggle.
- Configurable controls:
  - Play time before pause (default 1m, 1-minute intervals)
  - Bedtime timer limit (default 90m)
  - Auto turn on time (optional daily schedule)
- While Bedtime Timer is ON:
  - Turning Bedtime Timer on pauses any currently playing media.
  - Playback resume arms/re-arms the timer.
  - Timer expiry performs a fixed 10s fade, then pause.
  - Resume after stop re-arms automatically.
- Bedtime timer limit expiry fades/pauses if needed, then disables Bedtime Timer.
- Foreground notification is shown only while Bedtime Timer is active, with a `Stop` action.

## Runtime requirements

- Android 10+ (API 29+)
- Notification Access permission enabled for Bedtime Sticky Timer notification listener service.

## Testing checklist (manual)

- Resume playback from earbuds re-arms timer.
- Fade-out occurs before pause at session expiry.
- Resume within quick restart window re-arms immediately.
- Bedtime Timer disables itself after bedtime timer limit.
- Tile and notification state match actual Bedtime Timer state.
- Reboot leaves Bedtime Timer OFF.
