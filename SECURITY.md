# AmniGuard Security & Vulnerability Policy

**Publisher:** Al-Haq Studio / Alhaq Initiative  
**Security Contact:** security@alhaq.uk  
**PGP / Encrypted Inquiries:** Available upon request via `security@alhaq.uk`

---

## 1. Zero-Knowledge Security Principles

AmniGuard is designed with security, privacy, and zero telemetry as core architectural pillars:
- **Local Isolation:** All packet handling and DNS filtering execute 100% on-device in local unmanaged memory and the Linux kernel network stack.
- **No External Exfiltration:** Under no circumstances are network packets, source/destination IPs, or DNS queries uploaded to remote servers.
- **Cryptographic Independence:** Pro / Supporter Pass licensing verification uses offline NIST P-256 ECDSA signatures. Activation does not contact any validation servers.

---

## 2. Reporting a Vulnerability

We take the security and integrity of AmniGuard seriously. If you discover a security vulnerability, memory safety issue, or bypass flaw:

1. **Do NOT open a public GitHub issue** with exploit details or PoC code.
2. Email your findings to: **`security@alhaq.uk`** with the subject `[SECURITY] AmniGuard Vulnerability Report`.
3. Include:
   - Detailed description of the vulnerability.
   - Steps to reproduce or proof-of-concept (PoC).
   - Impact assessment (e.g. DNS bypass, local privilege escalation, packet leak).
   - Affected versions and device architecture (e.g., ARM64-v8a, Android 14).

---

## 3. Vulnerability Response Timeline

- **Initial Acknowledgment:** Within **24 to 48 hours**.
- **Assessment & Triage:** Within **5 business days**.
- **Fix & Disclosure:** Coordinated release with patched APK and release advisory.

---

## 4. Scope

The following components are in scope:
- **JNI / C Engine:** Packet routing, TCP/UDP/ICMP filtering, DNS interception (`netblock.c`, `ip.c`, `dns.c`).
- **Android Service:** `ServiceSinkhole.java`, intent filters, local broadcast receivers.
- **Cryptographic Licensing:** `LicenseValidator.java` NIST P-256 ECDSA verification.
