# AmniGuard Firewall & NetBlock Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-08-29

### Added
- **Offline Cryptographic ECDSA Licensing:** Implemented `LicenseValidator` using NIST P-256 (`SHA256withECDSA`) public key validation for 100% offline Pro activation.
- **Publication Documentation Suite:** Added comprehensive `PRIVACY.md`, `TERMS.md`, `SECURITY.md`, `CONTRIBUTING.md`, `BUILDING.md`, and `ARCHITECTURE.md`.
- **Debounced Command Queue:** Added a 200ms debounce buffer in `CommandHandler` to prevent rapid tunnel restarts during transient network flaps.
- **Dynamic Underlying Networks:** Enabled `setUnderlyingNetworks` for Android M+ (API 23+) in `startVPN()`, `onAvailable()`, and `onCapabilitiesChanged()`, allowing seamless handovers across Wi-Fi and Cellular networks.
- **Material 3 Pro Screen:** Redesigned `ActivityPro` with dedicated license activation, status inspection, and zero telemetry indicators.

### Fixed
- **DNS SVCB / HTTPS Type Parser:** Fixed `dns.c` handling of `DNS_SVCB` (type 64) and `DNS_HTTPS` (type 65) queries to prevent zeroing answer counts on concurrent `A` / `AAAA` responses.
- **JNI Fast Path Lock Contention:** Converted domain blocklists in `ServiceSinkhole` to `volatile Set<String>` immutable snapshots, eliminating packet loop lock contention.
- **Android 14+ FGS Compatibility:** Added `startForegroundCompat` with `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` to prevent foreground service start exceptions.
- **Asynchronous Connectivity Checks:** Offloaded network validation socket connections to the background executor thread.

---

## [0.0.1_Alpha] - 2026-08-25

### Added
- Initial public alpha release of AmniGuard Firewall & NetBlock.
- On-device local DNS sinkholing with 80,000+ adult domain blocklist.
- Social media and distraction domain filter with custom whitelist/blacklist rules.
- Per-application Wi-Fi and mobile data firewall toggles.
- Port 853 DoT bypass prevention.
- Adaptive icons with monochrome dynamic color theming.
- Multi-architecture NDK builds (arm64-v8a, armeabi-v7a, x86, x86_64).
