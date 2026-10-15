# Building AmniGuard Firewall & NetBlock

This document provides step-by-step instructions for building **AmniGuard Firewall** from source across all product flavors.

For project architecture details, refer to [ARCHITECTURE.md](ARCHITECTURE.md) and [PROJECT.md](PROJECT.md).

---

## 1. Environment Requirements

- **Operating System:** Windows, macOS, or Linux
- **Java Development Kit:** JDK 17 (recommended: Eclipse Temurin or OpenJDK 17)
- **Gradle:** Version 8.14.5 (managed via `./gradlew`)
- **Android SDK:** Platform SDK API 34 / 35
- **Android NDK:** Version `25.2.9519653` (or higher)
- **CMake:** Version `3.22.1`+

---

## 2. Setting Up SDK & NDK

Create or update your `local.properties` file in the root directory:

```properties
sdk.dir=C:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
# Or on Linux / macOS:
# sdk.dir=/home/<user>/Android/Sdk
```

Ensure the NDK and CMake components are installed via Android SDK Manager:
```bash
sdkmanager --install "ndk;25.2.9519653" "cmake;3.22.1"
```

---

## 3. Build Tasks & Flavors

### Debug Build
Compiles the app with debugging symbols enabled:
```bash
./gradlew assembleDebug
```
Output location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
Compiles an optimized, R8-minified production build:
```bash
./gradlew assembleRelease
```
Output location: `app/build/outputs/apk/release/app-release.apk`

### F-Droid Release Build
Compiles the pure open-source variant stripped of Google Play Billing:
```bash
./gradlew :app:assembleFdroidRelease :app:lintFdroidRelease
```
Output location: `app/build/outputs/apk/fdroid/release/app-fdroid-release.apk`

See [FDROID_PUBLISHING_GUIDE.md](FDROID_PUBLISHING_GUIDE.md) for F-Droid submission metadata.

### Play Store Build
```bash
./gradlew assemblePlay
```
Output location: `app/build/outputs/apk/play/app-play.apk`

---

## 4. Native JNI Engine (C/C++)

The core packet filtering engine is written in C and compiled with CMake into native shared libraries:
- `app/src/main/jni/netblock/netblock.c` — Main packet processing loop & JNI bridge
- `app/src/main/jni/netblock/ip.c` — IP / TCP / UDP packet inspection & SNI extraction
- `app/src/main/jni/netblock/dns.c` — DNS query/response parsing & local domain sinkholing

Supported ABIs:
- `arm64-v8a` (Modern 64-bit Android)
- `armeabi-v7a` (Legacy 32-bit ARM)
- `x86_64` (64-bit Emulators & Chromebooks)
- `x86` (32-bit Emulators)

---

## 5. Related Documentation

- [Project Structure & Component Blueprint](PROJECT.md)
- [Architecture & Technical Specifications](ARCHITECTURE.md)
- [Testing & Scenario Guide](TESTING_GUIDE.md)
- [F-Droid Publishing Guide](FDROID_PUBLISHING_GUIDE.md)
