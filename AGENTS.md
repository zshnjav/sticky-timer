# AGENTS.md — Sticky Timer Playback

This repo contains an Android utility app: **Sticky Timer Playback**.

## 1) Project objective (product brief)
Build a **minimal Android utility** that enables a **sticky sleep timer** for bedtime listening.

Primary scenario:
- User is in bed wearing **Bluetooth earbuds/headphones**
- Phone is **out of reach** (charging on a nightstand)
- User starts playback (audiobook/podcast/music)
- App runs playback for a short window (default ~5 min), then **gently fades out** and **pauses**
- If the user taps **Play** on earbuds (or other resume controls), playback resumes and the timer **auto re-arms**
- Repeat until a **max active window** expires, then sticky mode turns **off**

This is a small, non-invasive utility. It should feel like a missing OS feature.

## 2) Non-goals (scope guardrails)
- Not a general automation app
- Not a media player replacement
- No analytics, no ads, no accounts
- No invasive permissions (avoid Accessibility unless absolutely necessary)
- No “feature bloat” (Wi-Fi toggles, DND controls, etc.) for v1

## 3) Defining features (MVP)
- Sticky mode ON/OFF control via **Quick Settings Tile**
- Configurable:
  - Session duration (default 5:00)
  - Fade-out length (default 10s)
  - Re-engagement window after stop (default 30s)
  - Max active window (default 90 min)
- When sticky mode is ON:
  - Detect playback **start/resume**
  - Arm/re-arm countdown
  - Fade-out near end, then **pause**
  - If playback resumes during re-engagement window, re-arm immediately
  - Auto-disable sticky mode when max active window expires
- Persistent preferences (DataStore or equivalent)
- Foreground notification while active (minimal, includes Stop action)

## 4) UX principles
- Screenless-first (tile + earbuds)
- Predictable behavior; no surprises outside sticky mode
- Minimal UI: settings screen only for essentials
- Battery-conscious; do nothing when sticky mode OFF

## 5) Implementation guidance (high-level)
- Prefer Kotlin and modern Android tooling.
- Prefer robust, standard Android media control mechanisms (MediaSession/MediaController).
- Use a foreground service ONLY while sticky mode is active (to be reliable under Doze).
- Add debounce/filters to avoid false re-arms from buffering blips.
- If a player ignores pause/duck, retry once; do not loop aggressively.

## 6) Testing expectations (acceptance checks)
Must work reliably on at least:
- Audible + one major podcast app + one major music app
Core checks:
- Timer arms on resume (including earbud play)
- Fade-out occurs then pause triggers at expiry
- Resume within re-engagement window re-arms instantly
- Sticky mode auto-disables after max active window
- Tile/notification state always matches real mode state

## 7) Repo workflow & output artifacts
- Keep the repo readable: small modules, clear names, comments for tricky lifecycle/Android bits.
- Prefer incremental commits tied to milestones.
- Release distribution: **GitHub Releases** with a **signed APK** (no Play Store required for v1).

## 8) How Codex should work in this repo
When making changes:
- Keep scope tight to the MVP and bedtime Bluetooth scenario.
- If a request adds complexity or permissions, propose a simpler alternative first.
- Provide small, reviewable diffs and explain behavior changes briefly.
- Update docs when requirements/behavior change (especially timer/fade/re-engagement rules).