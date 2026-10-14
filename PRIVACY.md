# AmniGuard Privacy Policy

**Effective Date:** August 29, 2026  
**Publisher:** Al-Haq Studio / Alhaq Initiative  
**Contact:** privacy@alhaq.uk  
**Website:** https://alhaq.uk/amniguard

---

## 1. Fundamental Principle: Zero-Knowledge & Local-Only Processing

AmniGuard Firewall & NetBlock is engineered under a strict **Zero-Knowledge Privacy Architecture**. The software operates 100% locally on your device. We do not collect, store, transmit, monetize, or inspect your personal data, network traffic, or browsing history.

---

## 2. Android `VpnService` Utilization

AmniGuard utilizes Android's local `VpnService` interface to create a virtual network adapter directly on your device.

- **No Remote VPN Servers:** Unlike commercial VPN services, AmniGuard **does not route your traffic through any remote VPN servers, third-party proxies, or cloud endpoints**.
- **Local On-Device Loopback:** All packet inspection, IP routing, domain resolution, and firewall rule enforcement occur exclusively in local device memory (`ServiceSinkhole` and native JNI C/C++ engine).
- **Zero Logging:** Packet payloads, destination IPs, and DNS query logs are stored in a private local SQLite database on your device and are never transmitted externally.

---

## 3. Domain Blocklists & Content Filtering

- **Asset Storage:** Adult and social media domain blocklists are bundled directly within the app APK as local assets.
- **Local Matching:** Domain lookups (DNS/SNI) are executed against volatile memory hash sets on-device. No DNS queries are forwarded to Al-Haq servers.

---

## 4. Cryptographic Licensing & Supporter Passes

- **Offline NIST P-256 Verification:** AmniGuard Pro / Supporter Pass activation utilizes offline cryptographic public-key verification (`SHA256withECDSA`).
- **No License Telemetry:** When activating a license key, the signature is verified locally against the bundled public key. The app never sends your license key, device ID, hardware identifiers, or email address to an activation server.

---

## 5. Third-Party Services & Telemetry

- **No Analytics SDKs:** AmniGuard contains zero third-party analytics SDKs (no Google Firebase Analytics, no Mixpanel, no Facebook SDK, etc.).
- **No Crash Trackers:** No automated crash telemetry is sent. Users may voluntarily report bug reports or stack traces on our public GitHub issue tracker.
- **No Advertising:** AmniGuard contains zero advertisement SDKs and will never inject, alter, or monetize user traffic.

---

## 6. Permissions & Usage

| Permission | Purpose |
| :--- | :--- |
| `android.permission.BIND_VPN_SERVICE` | Required by Android to create the local on-device firewall tunnel. |
| `android.permission.INTERNET` | Required by Android network stack for socket routing. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Optional: allows the firewall to start on device boot if enabled by the user. |
| `android.permission.FOREGROUND_SERVICE` | Required to maintain uninterrupted firewall enforcement in the background. |
| `android.permission.POST_NOTIFICATIONS` | Displays the active firewall status notification and per-app traffic alerts. |

---

## 7. Open Source & Transparency

AmniGuard is Free and Open-Source Software licensed under the **GNU General Public License v3.0 (GPL-3.0)**. The complete source code is publicly auditable at:
https://github.com/alhaq-studio/amniguard-firewall

---

## 8. Updates to this Policy

Any updates to this policy will be published in this repository with updated revision dates.

For questions regarding privacy or zero-knowledge compliance, contact:  
**Al-Haq Studio Privacy Team:** `privacy@alhaq.uk`
