# Decisions Log

## 2026-03-22 (backfilled)

Decision: Use a notification-listener-backed media session controller for third-party playback detection and control.

Why: This keeps the app focused on standard Android media controls and avoids Accessibility-based automation while still working across other media apps.

Implications: The app depends on user-granted notification-listener access, and media behavior is constrained by what the active media session exposes.

## 2026-03-22 (backfilled)

Decision: Keep the foreground service active only while sticky mode is enabled.

Why: Sticky mode needs reliable background execution when armed, but the app should stay idle when the mode is off.

Implications: Tile, notification, service, and coordinator state must remain synchronized; disabling sticky mode should tear the service down promptly.

## 2026-03-22 (backfilled)

Decision: Fix fade-out to `10s` and the re-engagement window to `30s` instead of exposing those settings in the UI.

Why: Current repository code comments describe this as a simpler bedtime UX.

Implications: The implementation currently differs from the original MVP brief in `AGENTS.md`, which still lists those values as configurable.

## 2026-03-22 (backfilled)

Decision: Include an optional daily auto-enable schedule in the current codebase.

Why: The existing implementation supports a recurring bedtime routine without requiring manual activation every night.

Implications: Alarm scheduling, boot/time-change handling, and additional UX copy are now part of the active design surface and need explicit MVP confirmation.

## 2026-03-22 (backfilled)

Decision: Pause currently playing media once when sticky mode is enabled.

Why: The current coordinator implementation and root README both describe a "clean bedtime start" behavior.

Implications: This may surprise users who toggle sticky mode mid-playback, and it should be treated as a conscious product decision rather than incidental behavior.

## 2026-03-28

Decision: Use a VS Code + Codex editing loop, then Android Studio + connected-phone deployment/testing as the standard development workflow.

Why: This matches the team's actual day-to-day flow and keeps fast code iteration separate from device deployment, Logcat inspection, and real playback validation.

Implications: The canonical process lives in `docs/project/DEV_TEST_WORKFLOW.md`, and changes are not considered fully validated until they pass the phone test loop.

## 2026-03-28

Decision: Keep project-planning docs in `docs/project/` and development/setup docs in `docs/development/`.

Why: This keeps the repo root focused on build/config entry points while giving project and environment docs a predictable home.

Implications: New setup or workflow guides should go under `docs/development/`, and root-level docs should stay limited to high-signal entry documents such as `README.md` and `AGENTS.md`.
