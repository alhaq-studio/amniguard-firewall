# Building AmniGuard Firewall & NetBlock

This document provides step-by-step instructions for building **AmniGuard Firewall** from source.

---

## 1. Environment Requirements

- **Operating System:** Windows, macOS, or Linux
- **Java Development Kit:** JDK 17 (recommended: Eclipse Temurin or OpenJDK 17)
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

Ensure the NDK is installed via Android SDK Manager:
```bash
sdkmanager --install "ndk;25.2.9519653" "cmake;3.22.1"
```

---

## 3. Build Tasks

### Debug Build
Compiles the app with debugging symbols enabled:
```bash
./gradlew assembleDebug
```
Output location:
`app/build/outputs/apk/debug/app-debug.apk`

### Release Build
Compiles an optimized, R8-minified production build:
```bash
./gradlew assembleRelease
```
Output location:
`app/build/outputs/apk/release/app-release.apk`

### Play Store Build (Alternative Flavor)
```bash
./gradlew assemblePlay
```
Output location:
`app/build/outputs/apk/play/app-play.apk`

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
