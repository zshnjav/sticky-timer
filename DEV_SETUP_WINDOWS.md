# Dev Setup (Windows)

## Requirements
- JDK 17
- Android Studio (includes Android SDK tools)

## 1) Install and use JDK 17
- Install a JDK 17 distribution.
- Set `JAVA_HOME` to your JDK 17 folder.
- Add `%JAVA_HOME%\bin` to `Path`.
- Verify:
  ```powershell
  java -version
  ```
  It should report Java 17.

## 2) Add Android SDK platform-tools to PATH (`adb`)
- Locate your Android SDK folder (Android Studio default):
  - `%LOCALAPPDATA%\Android\Sdk`
- Add this folder to `Path`:
  - `%LOCALAPPDATA%\Android\Sdk\platform-tools`
- Verify:
  ```powershell
  adb version
  ```

## 3) Connect phone and verify
- On phone, enable Developer options and USB debugging.
- Connect phone by USB and accept the debugging prompt.
- Verify device detection:
  ```powershell
  adb devices -l
  ```
  You should see your device listed as `device`.

## 4) Build + install debug APK
From repo root:
```powershell
.\gradlew.bat :app:installDebug
```

## 5) Open Logcat in Android Studio
- Open this project in Android Studio.
- Go to `View` -> `Tool Windows` -> `Logcat`.
- Select your connected device and app package (`com.stickytimer.app`).
