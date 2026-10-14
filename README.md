# AmniGuard Firewall & NetBlock

[![Android CI / CD](https://github.com/alhaq-studio/amniguard-firewall/actions/workflows/ci.yml/badge.svg)](https://github.com/alhaq-studio/amniguard-firewall/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%205.1%2B-green.svg)](https://android.com)
[![Privacy First](https://img.shields.io/badge/Privacy-100%25%20On--Device-success.svg)](PRIVACY.md)

**AmniGuard Firewall** is a high-performance, privacy-first, no-root firewall and local DNS content filter for Android. Developed by **Al-Haq Studio** as part of the **Amni Ecosystem**, AmniGuard provides granular network traffic control, content filtering, and offline-first zero-telemetry protection.

---

## Key Capabilities

### Local-First Domain & Content Blocker
- **80,000+ Explicit Adult Portal Protection:** Zero-latency, on-device DNS blocking for explicit portals and subdomains.
- **Social Media & Distraction Blocker:** 1,200+ major social networks and video platforms with customizable schedules.
- **Custom Blacklist & Whitelist:** Support for custom domains and wildcard rules (`*.example.com`) directly in-app.
- **Master Firewall Toggle:** Instant one-tap switch to enable or disable protection.
- **Encrypted DNS Enforcement:** Intercepts port 853 (DNS-over-TLS) to prevent apps from bypassing local filtering rules.

### Comprehensive Network & Application Control
- **No Root Required:** Built on Android's native `VpnService` interface.
- **Per-App Network Rules:** Individually allow or block Wi-Fi and mobile data access for every installed application.
- **Full Protocol Coverage:** TCP, UDP, and ICMP filtering for both IPv4 and IPv6.
- **Tethering & LAN Support:** Fine-grained controls for Wi-Fi hotspot and local network traffic.
- **Dynamic Underlying Networks:** Seamless network transitions across Wi-Fi and Cellular without dropped connections.

### Real-Time Diagnostics & Diagnostics (Pro)
- **Live Traffic Inspector:** View destination IP, port, UID, and resolved hostname in real-time.
- **PCAP Capture:** Export captured network packets for offline analysis in Wireshark.
- **Bandwidth Monitor:** Display active network speed graph in the Android status bar.

---

## Zero-Knowledge Privacy Architecture

AmniGuard operates under strict zero-knowledge privacy guarantees:
1. **100% On-Device Processing:** All packet analysis, routing, and DNS filtering occur strictly in local memory.
2. **Zero Remote Proxy Servers:** Traffic is never routed through external VPN servers or cloud proxies.
3. **No Analytics or Telemetry:** The app contains no third-party tracking SDKs, advertising libraries, or crash analytics.
4. **Offline Cryptographic Licensing:** Pro / Supporter Pass activation utilizes NIST P-256 ECDSA public-key verification without contacting external licensing servers.

See [PRIVACY.md](PRIVACY.md) for the complete privacy policy.

---

## Installation & Distribution

### Direct APK Downloads (GitHub Releases)
Pre-compiled release APKs are available on the [GitHub Releases](https://github.com/alhaq-studio/amniguard-firewall/releases) page:
- **`app-release.apk`**: Universal release APK supporting `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.
- **`app-play.apk`**: Standalone production flavor.

### Web Portal
Visit the official portal at [https://alhaq.uk/amniguard](https://alhaq.uk/amniguard) to download the latest builds or obtain Supporter Passes.

---

## Building from Source

### Prerequisites
- **JDK 17** (Eclipse Temurin or OpenJDK 17)
- **Android SDK** (API Level 34 / 35)
- **Android NDK** (`25.2.9519653` or higher)
- **CMake** (`3.22.1+`)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/alhaq-studio/amniguard-firewall.git
cd amniguard-firewall

# Compile Debug Build
./gradlew assembleDebug

# Compile Release Build
./gradlew assembleRelease
```

Compiled APK files will be located in:
- `app/build/outputs/apk/debug/`
- `app/build/outputs/apk/release/`

For detailed build instructions, see [BUILDING.md](BUILDING.md).

Build the F-Droid release variant without Google Play Billing:
```bash
./gradlew :app:assembleFdroidRelease :app:lintFdroidRelease
```

See [FDROID_PUBLISHING_GUIDE.md](FDROID_PUBLISHING_GUIDE.md) for the
fdroiddata metadata and release submission workflow.

---

## Documentation & Policies

- [Architecture & Technical Specifications](ARCHITECTURE.md)
- [Zero-Knowledge Privacy Policy](PRIVACY.md)
- [Terms of Service](TERMS.md)
- [Security & Vulnerability Disclosure](SECURITY.md)
- [Contributing Guidelines](CONTRIBUTING.md)
- [Changelog](CHANGELOG.md)

---

## License

AmniGuard Firewall & NetBlock is Free and Open-Source Software licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for the full license text.
