# Contributing to AmniGuard Firewall

Thank you for your interest in contributing to **AmniGuard Firewall & NetBlock**!

AmniGuard is an open-source, privacy-first firewall and local content filter built for the global Android community.

---

## 1. Code of Conduct & Core Standards

### Strict Zero-Emoji Policy
To maintain a clean, professional, and accessible codebase across all platforms, **do NOT use UTF-8 emojis or emoji unicode characters** anywhere in:
- Source code (Kotlin / Java / C / C++)
- UI layouts, XML strings, or menu titles
- Log messages or exception descriptions
- Commit messages and PR descriptions

Use Material Symbols, vector drawables (`ic_*.xml`), or clean plain-text tags (`[INFO]`, `[SUCCESS]`, `[ERROR]`) instead.

### Zero-Knowledge Guarantee
Never introduce telemetry, remote analytics, third-party advertising SDKs, or unencrypted external network requests. All packet inspection and filtering logic must remain 100% on-device.

---

## 2. Development Setup

### Prerequisites
- Android Studio Ladybug (or newer) / IntelliJ IDEA
- Android SDK (API 34 / 35)
- Android NDK (Version `25.2.9519653` or higher)
- CMake `3.22.1`+
- JDK 17+

### Building Locally
```bash
# Clone the repository
git clone https://github.com/alhaq/AmniGuard-FireWall.git
cd AmniGuard-FireWall

# Build Debug APK
./gradlew assembleDebug

# Run Unit & Instrumentation Tests
./gradlew test
```

---

## 3. Pull Request Guidelines

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Ensure your changes compile cleanly without warnings or lint errors.
3. Test thoroughly on an Android emulator or physical device.
4. Open a descriptive Pull Request with clear steps to test.
