# AmniGuard Firewall & NetBlock — Project Blueprint & Structure Specification

This document provides a comprehensive structural and technical logic map of the **AmniGuard Firewall** codebase, detailing directory layouts, software architecture, native JNI components, and data flow pipelines.

---

## 1. Project Directory Layout

```
AmniGuard-FireWall/
├── .github/                     # GitHub Actions CI/CD workflows & Copilot rules
├── app/                         # Primary Android application module
│   ├── build.gradle             # App-level Gradle build configuration, dependencies & flavors
│   ├── proguard-rules.pro       # Obfuscation & R8 minification keep rules
│   └── src/
│       ├── fdroid/              # F-Droid specific product flavor overrides
│       │   └── AndroidManifest.xml
│       ├── main/                # Production source tree
│       │   ├── AndroidManifest.xml # Core application manifest & permissions
│       │   ├── assets/          # Static assets & bundled domain blocklists
│       │   │   └── blocklists/  # Adult & social media domain lists
│       │   ├── java/com/alhaq/amnishield/netblock/ # Java source code
│       │   ├── jni/netblock/   # Native C C++ packet filtering engine
│       │   │   ├── CMakeLists.txt
│       │   │   ├── dns.c / dns.h   # Local DNS parsing & sinkholing
│       │   │   ├── ip.c / ip.h     # TCP/UDP packet inspection & SNI extraction
│       │   │   ├── netblock.c / .h # JNI bridge & epoll event loop
│       │   │   └── util.c / util.h # Native memory & string utilities
│       │   └── res/             # Application resources (layouts, drawables, values)
│       └── play/                # Google Play product flavor overrides
│           └── AndroidManifest.xml
├── fastlane/                    # Play Store store listing metadata
├── playstore/                   # Localized app store descriptions & media graphics
├── tools/                       # Build & graphics helper scripts
├── ARCHITECTURE.md              # Technical logic specification & packet lifecycle
├── BUILDING.md                  # Comprehensive build & toolchain requirements
├── CHANGELOG.md                 # Version release history
├── CONTRIBUTING.md              # Guidelines for open-source contributors
├── FDROID_PUBLISHING_GUIDE.md   # F-Droid build instructions & fdroiddata metadata
├── LICENSE                      # GNU General Public License v3.0 text
├── PRIVACY.md                   # Zero-knowledge privacy policy
├── README.md                    # Project overview & quick start guide
├── SECURITY.md                  # Vulnerability disclosure & PGP security contact
├── TERMS.md                     # Terms of service
└── TESTING_GUIDE.md             # Scenario testing guide for firewall & app blocking
```

---

## 2. Component Blueprint & Package Mapping

All production Java source files are organized under `com.alhaq.amnishield.netblock`:

### Core Engine & VPN Service
- **`ServiceSinkhole.java`**: Central `VpnService` implementation. Creates and manages the `/dev/tun` virtual tunnel descriptor, handles network state callbacks (`ConnectivityManager`), coordinates packet routing with the native C engine via JNI, and enforces per-application Wi-Fi/Mobile firewall rules.
- **`ServiceExternal.java`**: Background worker service handling external blocklist downloads (e.g., StevenBlack hosts, custom adult blocklists).
- **`ReceiverAutostart.java`**: Listens for system boot (`BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`) to restore firewall protection automatically upon device startup.
- **`ReceiverPackageRemoved.java`**: Listens for package uninstallation events to clean up associated firewall rules in SQLite.

### Native C Engine (JNI Bridge)
- **`netblock.c`**: Non-blocking `epoll` packet reader that bridges raw IP frames between `TunFD` and Java memory.
- **`ip.c`**: Deep packet inspector. Extracts IP headers, TCP flags, UDP ports, and parses TLS Client Hello Server Name Indication (SNI).
- **`dns.c`**: Local DNS sinkhole engine. Evaluates DNS queries on port 53 against lock-free blocklists and generates synthetic `NXDOMAIN` or `REFUSED` responses for blocked domains.

### UI Architecture (Navigation & Fragments)
- **`ActivityMain.java`**: Main entry activity hosting the Material 3 `BottomNavigationView` and Navigation Drawer.
- **`FragmentApps.java`**: Renders the per-application firewall control list with search and filtering chips (All, No Internet, Disabled, User, System).
- **`FragmentDomains.java`**: Manages the local DNS domain blocker toggles (Adult Portal Blocker, Social Media Blocker, Custom Whitelist/Blacklist).
- **`FragmentLogs.java`**: Real-time connection log viewer displaying destination IP, port, UID, resolved domain name, and packet status.
- **`FragmentSettingsHub.java`**: Settings hub for configuring DNS servers, themes, notification preferences, and backup/restore.

### Cryptographic Licensing & Security
- **`LicenseValidator.java`**: 100% on-device NIST P-256 (secp256r1) ECDSA cryptographic license verifier (`SHA256withECDSA`).
- **`LicensePayload.java`**: JSON parser and model representing offline supporter key payloads.
- **`IAB.java`**: Google Play Billing Library 7.x integration for one-time lifetime supporter purchases.

### Data Storage & Helpers
- **`DatabaseHelper.java`**: Thread-safe SQLite database manager storing application firewall rules, custom domain blocklists, and connection traffic logs.
- **`Rule.java`**: Data model representing an application's network access rules across Wi-Fi, Mobile Data, Roaming, and Screen-On states.
- **`Packet.java`**: Lightweight data model representing captured network packet telemetry.

---

## 3. Key Technical Workflows

### Packet Filtering Pipeline
```
[Application Socket] 
      │
      ▼
[Android TunFD] ──► [netblock.c (epoll)] ──► [ip.c (SNI) / dns.c (QNAME)]
                                                    │
                                                    ▼
                                          [Blocklist Evaluation]
                                         ┌──────────┴──────────┐
                                         ▼                     ▼
                                    [ALLLOWED]             [BLOCKED]
                                         │                     │
                                         ▼                     ▼
                                  [Forward to Net]      [TCP RST / NXDOMAIN]
```

### Zero-Knowledge Offline Licensing Workflow
```
[User Pastes ECDSA Key] ──► [Base64 & JSON Decode] ──► [P1363 to DER Conversion]
                                                               │
                                                               ▼
[Activated Pro Features] ◄── [Valid Signature] ◄── [NIST P-256 ECDSA Verify]
```

---

## 4. Related Technical Documentation

- [Building & Toolchain Guide](BUILDING.md)
- [Architecture & Technical Specifications](ARCHITECTURE.md)
- [Zero-Knowledge Privacy Policy](PRIVACY.md)
- [F-Droid Publishing Guide](FDROID_PUBLISHING_GUIDE.md)
- [Testing Guide](TESTING_GUIDE.md)
