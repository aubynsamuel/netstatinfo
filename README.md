# NetStatInfo

NetStatInfo is a small Android app for viewing per-app network usage on your device.

## Features

- Shows mobile, Wi-Fi, and total data usage by app.
- Supports multiple time ranges for usage filtering.
- Uses Android Usage Access permission to read local usage stats.
- Keeps usage data on the device.

## Screenshots

Add final screenshots to `docs/screenshots/` using the filenames below.

| Permission | Dashboard |
| --- | --- |
| ![Permission screen placeholder](docs/screenshots/permission-screen.png) | ![Dashboard screen placeholder](docs/screenshots/dashboard-screen.png) |

## Requirements

- Android Studio
- Android SDK
- JDK 11 or newer

## Build

```powershell
.\gradlew.bat assembleDebug
```

If Gradle cannot find the Android SDK, update `local.properties` with your local `sdk.dir`.
