# AmniGuard Firewall 🛡️

[![Android CI / CD](https://github.com/alhaq-studio/amnishield-firewall/actions/workflows/ci.yml/badge.svg)](https://github.com/alhaq-studio/amnishield-firewall/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%205.1%2B-green.svg)](https://android.com)
[![Privacy First](https://img.shields.io/badge/Privacy-100%25%20On--Device-success.svg)](README.md)

**AmniGuard Firewall** is a high-performance, privacy-first, no-root firewall and local DNS content filter for Android. Developed by **Al-Haq Initiative** as part of the **AmnShield Ecosystem**, AmniGuard empowers users with granular control over network traffic, privacy protection, and zero-latency local content blocking.

---

## ✨ Features

### 🛡️ Local-First Website & Domain Blocker
- **80,000+ Explicit Adult Portal Protection**: Zero-latency, on-device DNS blocking for adult portals and all their subdomains.
- **Social Media & Distraction Blocker**: 1,200+ major social networks and video platforms with flexible scheduling.
- **Custom Blacklist & Whitelist**: Add custom domains and wildcard rules (`*.example.com`) directly in-app.
- **Master Switch**: One-tap global toggle to enable or disable domain filtering instantly.
- **Encrypted DNS Bypass Prevention**: Intercepts unauthorized DoT (DNS-over-TLS on port 853) to ensure strict adherence to local protection rules.

### 🌐 Comprehensive App & Network Control
- **No Root Required**: Built on Android's native `VpnService` interface.
- **Per-App Network Rules**: Individually allow or block Wi-Fi and mobile data access for any installed app.
- **IPv4 & IPv6 Support**: Full TCP, UDP, and ICMP protocol filtering.
- **Tethering & LAN Support**: Fine-grained controls for Wi-Fi hotspot and local network traffic.
- **Screen-On & Roaming Filters**: Restrict background data when the screen is locked or while roaming.

### 📊 Real-Time Diagnostics & Logging (PRO)
- **Live Traffic Inspector**: Log outgoing connection attempts with destination IP, port, and resolved hostname.
- **PCAP Export**: Export captured packets for deep inspection in Wireshark.
- **Bandwidth Monitor**: Display real-time network speed graph in the notification bar.

---

## 🔒 Privacy & Security Guarantee

- **100% Local Processing**: All packet analysis and DNS filtering happen entirely on your device.
- **Zero Analytics / Telemetry**: No third-party trackers, no telemetry SDKs, no external analytics.
- **Zero Cloud Dependence**: Blocklists are stored locally in the app assets for offline-first protection.

---

## 🛠️ Building from Source

### Prerequisites
- **JDK 17** (Eclipse Adoptium or OpenJDK)
- **Android SDK** (API Level 35)
- **Android NDK** (`25.2.9519653`)
- **CMake** (`3.22.1+`)

### Build Commands

Clone the repository:
```bash
git clone https://github.com/alhaq-studio/amnishield-firewall.git
cd amnshield-firewall
```

Build debug and standalone APKs:
```bash
./gradlew assembleDebug assemblePlay
```

The compiled APKs will be located in:
- `app/build/outputs/apk/debug/`
- `app/build/outputs/apk/play/`

Build the F-Droid release variant without Google Play Billing:
```bash
./gradlew :app:assembleFdroidRelease :app:lintFdroidRelease
```

See [FDROID_PUBLISHING_GUIDE.md](FDROID_PUBLISHING_GUIDE.md) for the
fdroiddata metadata and release submission workflow.

---

## 🚀 CI / CD & Releases

Automated builds and releases are managed via **GitHub Actions**:
- Every push and pull request to `main` is validated and built automatically.
- Tagging a release (`git tag v0.0.1 && git push --tags`) automatically packages and publishes release APKs to GitHub Releases.

---

## 📄 License

AmniGuard Firewall is free and open-source software licensed under the **GNU General Public License v3.0 (GPLv3)**. See [LICENSE](LICENSE) for details.
