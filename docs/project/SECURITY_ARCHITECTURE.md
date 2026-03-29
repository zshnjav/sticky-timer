# Security Architecture

## Security posture

The app is intentionally local-first and narrow in scope:

- No backend or network integrations are present in the repo.
- No analytics, ads, or account systems are present in the repo.
- No secrets or credentials are stored in project docs or app settings.

## Sensitive capabilities

- Notification listener access
  - Used to discover active media sessions across other apps.
  - This is the broadest user-facing permission in the current design and should be clearly explained in UX copy.
- Media transport and volume control
  - The app can pause playback, seek backward, and temporarily change the music stream volume during fade-out.
- Foreground execution
  - The foreground service keeps the sticky timer alive while mode is enabled and exposes an always-visible notification.
- Alarm and boot handling
  - Optional bedtime auto-enable uses `AlarmManager`, and boot/time change receivers keep that schedule synchronized.

## Data handling

- User data stays on-device in DataStore preferences.
- Persisted values are limited to timer preferences, bedtime schedule, and last known mode state.
- The app does not store tokens, credentials, contacts, media libraries, or notification contents.

## Exposure surface

- Exported components are limited to the launcher activity and the Quick Settings tile service.
- The tile service is protected by the system Quick Settings bind permission.
- The notification listener and both receivers are non-exported.
- The foreground service is non-exported.

## Current risks and mitigations

- Notification-listener access can feel invasive.
  - Mitigation: the implementation uses it for media session discovery instead of broader automation techniques such as Accessibility.
  - Follow-up: keep rationale clear in user-facing copy and onboarding.
- Fade-out changes the global music stream volume.
  - Mitigation: `ActiveMediaSessionController` stores the pre-fade volume and restores it after pause.
  - Follow-up: validate this behavior with the target audiobook, podcast, and music apps.
- Notification posting on Android 13+ may fail or degrade if permission is denied.
  - Current state: permission is declared, but the request/education flow is not yet captured in the app docs.
- Auto-enable alarms and boot receivers can wake app logic unexpectedly.
  - Mitigation: sticky mode itself resets to off after boot, and alarms are only maintained when a bedtime time is configured.

## Security boundaries

- This project does not attempt device administration, Accessibility-based automation, or cross-device sync.
- The current design boundary is "only the permissions needed to monitor media sessions, show a notification, and re-arm a bedtime timer."
