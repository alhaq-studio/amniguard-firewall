# AmnShield AmnShield FireWall - AI Coding Agent Instructions

## Project Overview
**AmnShield AmnShield FireWall** is part of the **AmnShield Ecosystem** - a comprehensive protection suite for parental control, individual protection, and organizational-level content management, focused on serving the Muslim community and wider world.

### Current Ecosystem Status (January 24, 2026)
- **AmnShield App:** v1.1.0 published, Guardian integration Phase 6-8 planned
- **Guardian:** Phase 1 auth 60% complete, integration design complete
- **AmnShield FireWall:** Planned for Phase 9-10 integration (February 2026)
- **Integration Model:** Optional federation with permission-based linking

### AmnShield Ecosystem Architecture
- **AmnShield Guardian** (Kotlin/Compose) - Central **protection hub and manager** for parental control, individual protection, and organizational-level content blocking. Provides centralized configuration and management interface. **Status:** Phase 1 development
- **AmnShield AmnShield FireWall** (This Project) - **Individual app-level internet/WiFi controller and access blocker**. No-root Android firewall for per-app/per-address network access control. **Standalone app** that can optionally pair with Guardian for centralized management. **Status:** Ready for Phase 9-10 integration
- **AmnShield App** (Main On-Device Protection) - Primary online protection, content filtering, blurring, and productivity features. Handles real-time content-level filtering and immoral content protection. **Standalone app** that can optionally pair with Guardian. **Status:** v1.1.0 published, Phase 6-8 integration ready

### Integration Architecture
- **Standalone Mode:** Each app (AmnShield, AmnShield FireWall) functions independently with full features
- **Guardian-Paired Mode:** Apps can optionally connect to Guardian for centralized management, parental control, and organizational policies
- **User Choice:** Integration is optional and user-driven. Guardian is **recommended but not forced** - users decide their protection level

### Ecosystem Consistency Goals
The AmnShield Ecosystem must be:
- **Fully Consistent** - Unified UX, branding, and interaction patterns across all apps
- **Fully Functional** - Every feature works reliably across Guardian, AmnShield FireWall, and AmnShield App
- **Accessible to All** - Premium protection available regardless of financial situation

### Compassionate Access Program ("I Can't Afford to Pay")
**Islamic Principle:** Rahmah (Mercy) - Protection should not be denied due to financial hardship.

#### User Flow
1. **Premium Screen Link:** Below premium purchase options, display:
   > *"I can't afford to pay"* (clickable link/note)

2. **Honesty & Trust Screen:** When clicked, user sees:
   - Clear message: *"This program is for those who are truly in financial need."*
   - Reminder to be truthful (amanah) - Allah knows our circumstances
   - Explanation that this helps us serve those who genuinely need it

3. **Simple Registration:**
   - **Name:** Required (for personalization)
   - **Email:** Optional (for account recovery/support)
   - **App ID:** Auto-generated (for verification)

4. **Proceed Button:** After pressing "Proceed":
   - Grant **1-year free access** to ALL AmnShield platforms (Guardian, App, AmnShield FireWall premium features)
   - Show confirmation with access details
   - No payment required, no verification, trust-based

5. **Additional Support Information:**
   Display after granting access:
   > *"If you need further assistance or have questions:"*
   > - Visit: **alhaq-initiative.org/AmnShield/premium** for more information
   > - Visit: **alhaq-initiative.org/AmnShield/pay** if your situation changes and you wish to contribute
   > - Email: **support@alhaq-initiative.org** with your App ID, email, and name for direct support

#### Implementation Requirements
- **All Apps:** Must implement this flow in their premium/purchase screens
- **Shared Access:** Grant applies to entire ecosystem, not just one app
- **Trust-Based:** No income verification - rely on user honesty (Islamic trust model)
- **Renewal:** After 1 year, user can re-apply if still in need
- **Tracking:** Store compassionate access grants locally + optional anonymized count for reporting

### AmnShield FireWall Component Variants
Two active implementations in this workspace:
- **AmnShield FireWall** (Java-based, mature) - Legacy but feature-complete firewall with advanced routing, AIDL communication
- **AmnShield Guardian** (Kotlin/Compose-based, modern) - Contemporary rebuild with VPN-based blocking, serves as both central hub AND enhanced AmnShield FireWall replacement

## Architecture & Key Components

### AmnShield FireWall (Java - Legacy)
**Core Engine:**
- `ServiceSinkhole` - Network packet interception via VPN service; routes ALL traffic through `0.0.0.0/0`
- `Rule` - Blocking rule storage (SQL-backed); contains app/domain/IP filtering logic
- `Schedule` - Time-based enforcement; integrates with `ScheduleManager`
- AIDL-based inter-process communication for external app integration

**UI/Persistence:**
- Traditional XML preferences & SQLite for rule storage
- Widget-based quick toggles (`WidgetMain`, `WidgetAdmin`, `WidgetLockdown`)
- Quick-settings tiles (`ServiceTile*` classes)

### AmnShield Guardian (Kotlin/Compose - Modern)
**Architecture:** MVVM with Hilt DI, Compose UI, Room database
- **BlockViewModel** - Central state management; sends configuration to services via Intent
- **BlockingVpnService** - Hilt-injected VPN service; packet inspection with `ContentFilter` & `PacketParser`
- **AccessibilityBlocker** - App-level blocking; monitors accessibility events for forbidden apps
- **BlockRepository** - Room-based persistence for Block configurations

**Data Flow:**
```
User Input (Compose UI) → BlockViewModel 
  → Intent to BlockingVpnService (config update) + AccessibilityBlocker (app list)
  → Real-time enforcement (packet inspection + app interception)
```

## Critical Developer Workflows

### Build & Test
```powershell
# AmnShield FireWall (Java)
cd e:\HabiburRahman\Development\NetBlock
.\gradlew.bat assembleDebug --console=plain

# AmnShield Guardian (Kotlin)
cd e:\HabiburRahman\Development\DeenSheild-Guardian
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace

# Install via ADB
adb install app\build\outputs\apk\debug\app-debug.apk
```

**Debugging:** Both use Android Studio debugger. AmnShield FireWall is older (Java 1.8 target), Guardian uses Java 17 (Kotlin 2.0).

### Key Integration Points
1. **VPN Service Configuration** - Guardian only: VPN service listens for `ACTION_UPDATE_CONFIG` Intents with domain/keyword lists
2. **Accessibility Service** - Guardian: Must be explicitly enabled in device Settings; cooldown (2s) prevents block notification spam
3. **Native Libraries** - AmnShield FireWall uses JNI for packet parsing (CMakeLists.txt, NativeCode.cpp); Guardian uses pure Kotlin/Java

## Project-Specific Patterns

### Configuration Management (Guardian)
- **Thread-Safe Updates:** ContentFilter uses `@Volatile` fields + `@Synchronized` methods
- **Service Communication:** ViewModel sends Intent with extras: `EXTRA_BLOCKED_DOMAINS`, `EXTRA_BLOCK_SOCIAL_MEDIA`, etc.
- **Immediate Effect:** No polling; configuration propagates to services immediately

### Blocking Enforcement
- **VPN-level (Deep Packet Inspection):** Guardian intercepts all IP packets; checks DNS responses, HTTP headers
- **App-level (Accessibility):** When forbidden app launched, AccessibilityService redirects to Home with Toast + 2s cooldown
- **Scheduling:** Both support time-based rules; AmnShield FireWall integrates `ScheduleManager`, Guardian has `schedule` field in Block model

### Error Handling & Resource Cleanup
- **Guardian VPN:** Implements proper `onDestroy()` cleanup (closes FileDescriptors, cancels coroutines, stops foreground notification)
- **AmnShield FireWall:** Uses traditional Android lifecycle; critical fix: prevent VPN loops via `addDisallowedApplication()`

## Important Known Issues & Fixes

### Guardian - Critical Fixes Applied
1. **VPN Loop Prevention:** Changed `!isActive.not()` → `isActive` in packet processing loop (line ~146 BlockingVpnService.kt)
2. **Foreground Service Type:** Only `dataSync` allowed (Android 14+), not `location` (see CRITICAL_FIX_FOREGROUND_SERVICE.md)
3. **Configuration Sync:** VPN service must receive Intent updates from ViewModel to enforce custom blocks
4. **Accessibility Permissions:** Device requires explicit enabling via Settings > Accessibility > AccessibilityBlocker

## Testing Checklist (Guardian)
1. Build APK: `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`
2. Enable VPN in app Settings → service should start (green status card)
3. Enable Accessibility in device Settings
4. Add block rule (app/website/keyword) → should appear in Blocks screen
5. Verify blocking: open blocked app (should redirect) or visit blocked website (VPN intercepts)
6. Check status cards: both should show GREEN when active

## File Structure & Key Paths
- **Guardian Core:** `app/src/main/java/com/AmnShield/blocker/{service,viewmodel,ui,data}/`
- **Guardian Config:** App-level dependencies in `app/build.gradle.kts` (Hilt, Room, Compose)
- **AmnShield FireWall Legacy:** `app/src/main/java/netblock/` (Java packages); native code in `app/jni/`
- **Shared:** Both use same application ID `com.alhaq.AmnShield.*`; separate build types (debug/release/play)

## Critical Safety Guidelines

### Data Protection & Local-Only Processing
- ⚠️ **User Privacy:** VPN service has access to ALL network traffic. NEVER log, persist, or transmit user traffic data without explicit user consent
- ⚠️ **Local-Only Traffic:** ALL traffic routing and packet inspection MUST occur locally on device. NEVER send traffic through external servers, proxies, or third-party services
- ⚠️ **No Third-Party Forwarding:** Block all attempts to forward, relay, or analyze traffic through third-party infrastructure. All packet processing must be in-process
- ⚠️ **DNS Leak Prevention:** Ensure DNS queries NEVER leak to external servers. DNS proxy must intercept and resolve locally or block entirely
- ⚠️ **No External Analytics:** NEVER collect, aggregate, or send telemetry about user traffic patterns, blocked domains, or device network state
- ⚠️ **Sensitive Data:** Block lists may contain user preferences about restricted content. Protect with encryption at rest
- ⚠️ **Room Database:** Ensure database queries don't expose personal information. Validate all SQL operations. Keep database files local and secure
- ⚠️ **Backup Safety:** When implementing backup/export features, encrypt exported block configurations. NEVER upload to cloud storage or remote servers
- ⚠️ **VPN Lifecycle:** Always verify VPN stops cleanly. Orphaned VPN connections can intercept traffic after app uninstall

### Operational Safety
- ⚠️ **Configuration Rollback:** Before making breaking changes to Block model/schema, ensure migration path exists
- ⚠️ **Service State Validation:** Never assume services are running. Always check `isVpnServiceActive()` before sending Intents
- ⚠️ **Database Migrations:** Changes to Room entity schema MUST include proper version bumps and migration strategies
- ⚠️ **Intent Extras:** Configuration Intents are not encrypted. Never put sensitive data in extras; use secure storage instead
- ⚠️ **Network Access Audit:** Verify no network calls exist in VPN service/ContentFilter. All processing must be CPU-local with NO outbound connections
- ⚠️ **TUN Interface Safety:** Ensure TUN interface processes packets locally only. NO packet forwarding to external gateways or cloud servers
- ⚠️ **Certificate Pinning:** If any HTTPS inspection is needed, use local certificate store only. NEVER download certificates from external authorities

### Testing Requirements Before Merge
1. **Uninstall Test:** After blocking is active, uninstall app → verify VPN stops completely (check Settings > VPN)
2. **Device Restart:** Create blocks, restart device → blocks must persist (Room DB integrity check)
3. **Permission Denial:** Deny VPN permission in device settings → app must handle gracefully without crash
4. **Storage Full:** Fill device storage → ensure database operations fail gracefully without corruption

## What NOT to Do
- ❌ Remove `addDisallowedApplication(packageName)` from VPN (causes blocking loop or traffic interception after uninstall)
- ❌ Change foreground service type to `location` on Android 14+ (causes SecurityException)
- ❌ Access VPN/Accessibility service state without checking device permissions first
- ❌ Block notifications indefinitely (use cooldown mechanism like 2s in AccessibilityBlocker)
- ❌ Log user traffic data or network destinations to logcat or persistent storage without consent
- ❌ Modify Block entity fields without creating Room migration (causes crash on update)
- ❌ Create blocking rules that intercept own VPN control traffic (causes infinite loops)
- ❌ Persist authentication tokens or API keys in SharedPreferences unencrypted
- ❌ Send block list updates via unencrypted HTTP (blocks may contain sensitive user data)
- ❌ Modify Intent extras structure without versioning (breaks inter-process communication)
- ❌ **Forward traffic to external DNS servers, proxies, or VPN providers (all traffic must be local)**
- ❌ **Send traffic packets, DNS queries, or network metadata to analytics/telemetry servers**
- ❌ **Add dependencies on cloud APIs, cloud storage, or third-party services for traffic analysis**
- ❌ **Implement device fingerprinting or tracking that requires external servers**
- ❌ **Cache or persist DNS resolution history to external storage or cloud backup**
- ❌ **Enable packet capture export to remote servers or cloud providers**
- ❌ **Use third-party ad networks, SDKs, or tracking libraries in VPN/blocking services**
- ❌ **Create background jobs that transmit traffic patterns, block statistics, or device state**
- ❌ **Download updated block lists from external CDNs without verification of source**
- ❌ **Implement device control or remote management features that depend on external servers**
