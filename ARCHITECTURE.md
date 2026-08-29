# AmniGuard Architecture & Technical Specifications

This document outlines the system design, packet lifecycle, and cryptographic licensing subsystem of **AmniGuard Firewall & NetBlock**.

---

## 1. System Architecture Diagram

```
+-------------------------------------------------------------------+
|                        Android OS Layer                           |
|  Apps / Sockets ---> VpnService Interface (TunFD: /dev/tun)      |
+---------------------------------+---------------------------------+
                                  |
                                  v
+---------------------------------+---------------------------------+
|                    ServiceSinkhole.java (Service)                 |
|  - Manages VpnService lifecycle & Network Callbacks               |
|  - Dynamic underlying networks (setUnderlyingNetworks)            |
|  - Debounced command handler & reload coordinator                 |
+---------------------------------+---------------------------------+
                                  |
                                  v
+---------------------------------+---------------------------------+
|                   Native JNI Engine (C / NDK)                     |
|                                                                   |
|   +-----------------------+     +-------------------------------+ |
|   |  netblock.c           |     |  ip.c                         | |
|   |  - epoll event loop   |     |  - TCP / UDP / ICMP parser    | |
|   |  - tun device read    |     |  - TLS SNI inspection         | |
|   |  - UID resolution     |     |  - Port 853 DoT interception  | |
|   +-----------+-----------+     +---------------+---------------+ |
|               |                                 |                 |
|               +----------------+----------------+                 |
|                                |                                  |
|                                v                                  |
|                 +-------------------------------+                 |
|                 |  dns.c                        |                 |
|                 |  - DNS query / response parser|                 |
|                 |  - SVCB / HTTPS type handler  |                 |
|                 |  - On-device sinkholing       |                 |
|                 +---------------+---------------+                 |
|                                 |                                 |
+---------------------------------+---------------------------------+
                                  |
                                  v  (Lock-free volatile hash sets)
+---------------------------------+---------------------------------+
|                     Domain Blocking Subsystem                     |
|  - 80,000+ Adult domain blocklist (volatile Set<String>)          |
|  - Social media domain blocklist (volatile Set<String>)           |
|  - Custom User Whitelist & Blacklist                              |
|  - O(1) Lock-free thread-safe evaluation                          |
+-------------------------------------------------------------------+
```

---

## 2. Packet Flow & Filtering Pipeline

1. **Traffic Capture:** Android OS directs outgoing application packets to the virtual `TunFD` created by `VpnService.Builder`.
2. **Native Dispatch:** `netblock.c` reads raw IP frames from `TunFD` and assigns them to the non-blocking `epoll` event loop.
3. **Protocol Demuxing:**
   - **DNS (UDP/TCP Port 53):** Handled by `dns.c`. Query domains (`qname`) are evaluated against `isDomainBlocked()`. Blocked domains are immediately sinkholed with synthetic `NXDOMAIN` / `REFUSED` responses. Standard `A` / `AAAA` responses are preserved cleanly even when modern `DNS_SVCB` (type 64) or `DNS_HTTPS` (type 65) queries are present.
   - **Encrypted DNS Enforcement (Port 853):** Port 853 (DNS over TLS) is intercepted and blocked to prevent applications from bypassing local DNS filtering.
   - **HTTPS / TLS (Port 443):** Handled by `ip.c`. The TLS Client Hello Server Name Indication (SNI) is parsed. If the target host matches a blocked domain, a `TCP RST` packet is returned, immediately terminating the connection.
   - **Per-App Wi-Fi / Mobile Rules:** Per-application UID firewall rules configured in the UI are evaluated before socket creation.

---

## 3. Cryptographic Licensing Engine (`LicenseValidator.java`)

AmniGuard implements the Amni Ecosystem's **Zero-Knowledge Cryptographic Offline Licensing Standard**:

- **Key Pair:** Standard NIST P-256 (secp256r1) elliptic curve.
- **Verification Algorithm:** `SHA256withECDSA`.
- **Signature Formats Supported:**
  - ASN.1 DER format (`30 || len || 02 || r_len || r || 02 || s_len || s`)
  - IEEE P1363 64-byte raw signature (`r[32] || s[32]`) from modern WebCrypto API.
- **Zero Server Communication:** All license verification occurs on-device against the bundled public key:
  ```
  MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7EFR1qxpfZTMeR52M1+04+tPb6ItmVmhPbRCIJYje3jtglTdBbcct+/xvc1D1NZtXuvSb4Egtdqm/EJ6H67fEA==
  ```

---

## 4. Performance & Concurrency Model

- **Lock-Free Fast Path:** Blocklists use `volatile Set<String>` immutable snapshots, eliminating read/write lock contention between JNI native packet loop workers and UI threads.
- **Reload Debouncing:** Multiple consecutive network state changes (`onAvailable`, `onCapabilitiesChanged`, `onLinkPropertiesChanged`) are debounced with a 200ms buffer, preventing rapid tunnel restarts.
- **Dynamic Underlying Networks:** Seamless handoff across Wi-Fi and mobile data networks via `setUnderlyingNetworks()` without dropping active TCP/UDP sockets.
