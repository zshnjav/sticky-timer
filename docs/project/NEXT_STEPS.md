# Next Steps

## Current priorities

1. Reconcile the product spec with the actual implementation.
   - Confirm whether v1 should follow `AGENTS.md` exactly or keep the current simplified settings model and daily auto-enable feature.
2. Validate sticky behavior on real target apps and earbuds.
   - Run the acceptance checks against Audible, one major podcast app, and one major music app.
3. Harden permission and onboarding behavior.
   - Confirm how notification-listener access and `POST_NOTIFICATIONS` should be explained, requested, and degraded when denied.
4. Lock the release path.
   - Define the signed APK workflow and GitHub Releases checklist for v1.

## Ready-now tasks

- Expand automated coverage beyond `StickyTimerEngineTest`, especially around coordinator behavior, tile/service state sync, and alarm-driven enablement.
- Capture a repeatable manual test matrix for boot, timezone change, auto-enable alarms, notification access revocation, and Bluetooth-earbud resume.
- Reconcile product naming in docs and UX copy: the repo refers to `Sticky Timer Playback`, while current strings/UI say `Bedtime Sticky Timer`.
- Decide whether the root `README.md` should be updated to reflect the current 1-minute default and auto-enable support once product decisions are settled.

## Blocked or waiting on decisions

- Whether auto-enable schedule belongs in the MVP.
- Whether fade duration and re-engagement window remain fixed or become user-configurable.
- Whether enabling sticky mode should immediately pause active playback.
- Real-device confirmation on third-party app behavior and any app-specific compatibility gaps.

## Near-term milestones

- Milestone 1: Spec reconciliation and docs alignment.
- Milestone 2: Device/app validation across audiobook, podcast, and music scenarios.
- Milestone 3: Permission/onboarding hardening and UX copy cleanup.
- Milestone 4: Release-signing workflow and first GitHub Release candidate.
