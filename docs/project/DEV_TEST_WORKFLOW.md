# Dev And Test Workflow

This document captures the established day-to-day workflow for this project:

1. Make code and doc changes in VS Code with Codex.
2. Run the key Gradle checks from the VS Code PowerShell terminal.
3. Open the same repo in Android Studio for sync, deploy, and Logcat.
4. Install to a connected Android phone and validate the bedtime-listening behavior on-device.

VS Code and Android Studio both point at the same working tree. There is no separate handoff repo or export step between them.

## One-time prerequisites

Complete the Windows environment setup in `docs/development/DEV_SETUP_WINDOWS.md` first.

Expected local tools:

- JDK 17
- Android Studio
- Android SDK / platform-tools
- `adb` available on `PATH`
- USB debugging enabled on the test phone

Useful verification commands:

```powershell
java -version
adb version
adb devices -l
```

## Standard daily loop

### 1. Open the project in VS Code

From the repo root:

```powershell
code .
```

Work in VS Code with Codex until the change is ready for a local build/test pass.

### 2. Run the fast terminal checks in VS Code

Use the VS Code PowerShell terminal from the repo root:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

If you want a fresh build artifact as part of the check:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected debug APK output after a successful build:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Use this when Gradle state feels stale:

```powershell
.\gradlew.bat clean
```

### 3. Open or refocus Android Studio on the same repo

Open Android Studio at the repo root:

```powershell
studio64 .
```

If `studio64` is not on `PATH`, open Android Studio manually and choose:

- `Open`
- `C:\Dev\projects\sticky-timer`

Then let Android Studio finish:

- Gradle sync
- indexing
- device detection

Use Android Studio as the visual deployment/debugging step after the VS Code terminal checks pass.

### 4. Connect the phone

On the phone:

- Enable Developer options
- Enable USB debugging
- Accept the computer authorization prompt

Verify the device from PowerShell:

```powershell
adb devices -l
```

The device should appear with status `device`.

### 5. Install and run on the phone

Preferred path in Android Studio:

1. Select the connected phone in the device chooser.
2. Make sure the `app` run configuration is selected.
3. Click `Run 'app'`.

CLI fallback from VS Code / PowerShell:

```powershell
.\gradlew.bat :app:installDebug
```

If you already built the APK and want a direct reinstall:

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

If you need to remove the app first:

```powershell
adb uninstall com.stickytimer.app
```

If you want to clear app data without uninstalling:

```powershell
adb shell pm clear com.stickytimer.app
```

### 6. Validate on the phone

Run the important manual checks after install:

- Open the app once and confirm it launches cleanly.
- Grant any required runtime/system access that the current build needs.
- Confirm Notification access is enabled for the app.
- Add the Quick Settings tile if it is part of the test pass.
- Start playback in a target media app.
- Turn sticky mode on.
- Confirm the timer arms when playback resumes.
- Confirm fade-out happens before pause.
- Confirm resume during the recent-stop window re-arms immediately.
- Confirm max active window eventually disables sticky mode.
- Confirm tile and foreground notification match the actual mode state.

Priority app/device coverage from `AGENTS.md`:

- Audible
- one major podcast app
- one major music app
- Bluetooth earbuds/headphones in the real bedtime flow

### 7. Use Android Studio Logcat during device testing

Open Logcat in Android Studio and filter for the app package:

```text
com.stickytimer.app
```

Useful shell fallback:

```powershell
adb logcat | Select-String "stickytimer"
```

Use Logcat while testing:

- sticky mode enable/disable
- notification listener behavior
- tile interactions
- foreground service lifecycle
- playback resume / pause timing

### 8. Repeat the loop

The normal iteration cycle is:

1. Change code in VS Code with Codex.
2. Run `:app:testDebugUnitTest`.
3. Run `:app:assembleDebug` when you want a new build artifact.
4. Switch to Android Studio.
5. Deploy to the phone.
6. Validate the real behavior on-device.
7. Check Logcat, adjust, and repeat.

## Command reference

```powershell
# Unit tests
.\gradlew.bat :app:testDebugUnitTest

# Build debug APK
.\gradlew.bat :app:assembleDebug

# Install debug build through Gradle
.\gradlew.bat :app:installDebug

# Clean build outputs
.\gradlew.bat clean

# Verify adb/device connection
adb devices -l

# Direct reinstall of built APK
adb install -r .\app\build\outputs\apk\debug\app-debug.apk

# Remove app from phone
adb uninstall com.stickytimer.app

# Clear app data
adb shell pm clear com.stickytimer.app

# Quick log filtering fallback
adb logcat | Select-String "stickytimer"
```

## Notes

- The debug/deploy workflow is Windows-first and assumes PowerShell.
- Android Studio is the best place for device deploys and Logcat, even if the code changes were made entirely in VS Code.
- The phone test pass is required for sticky-timer work because the core behavior depends on Android services, media-session behavior, and external playback apps.
