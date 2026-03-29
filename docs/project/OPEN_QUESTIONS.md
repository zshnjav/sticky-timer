# Open Questions

## Product questions

- Should v1 follow the `AGENTS.md` brief exactly:
  - default session around five minutes
  - configurable fade-out
  - configurable re-engagement window
  - no daily auto-enable schedule
- Or should v1 follow the current code path:
  - default session `60s`
  - fixed fade/re-engagement values
  - optional daily auto-enable schedule
- Should enabling sticky mode immediately pause active playback, or should it simply begin watching for the next stable play event?
- Which product name is canonical for release artifacts and user-facing copy: `Sticky Timer Playback` or `Bedtime Sticky Timer`?

## Technical risks

- The app declares `POST_NOTIFICATIONS`, but the repo does not yet show a clear request/degrade path for Android 13+.
- Fade-out currently changes global music stream volume; this needs validation across the target audiobook, podcast, and music apps.
- Resume rewind is hard-coded to `20s`; it is not yet clear whether that feels right for all supported media types.
- Notification-listener dependence needs real-device validation with the target Bluetooth bedtime scenario.

## Working defaults until changed

- Treat `docs/project/` as the canonical project-planning surface for this repo.
- Treat notification-listener access as required for current cross-app playback control.
- Treat sticky mode as foreground-service-backed only while enabled.
- Treat reboot as resetting sticky mode to off, while saved bedtime schedules may still be re-synced.
