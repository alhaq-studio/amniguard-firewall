# AmniGuard Firewall Testing Guide

## Quick Start Testing

### Step 1: Build and Install
```powershell
# Build the app
.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace

# Install on connected device/emulator
adb install app\build\outputs\apk\debug\app-debug.apk
```

For the F-Droid distribution build, run:
```powershell
.\gradlew.bat :app:assembleFdroidRelease :app:lintFdroidRelease --no-daemon
```

### Step 2: Initial Setup

1. **Launch the App**
   - Open AmnShield from your app drawer
   - You should see the Blocks screen (empty initially)

2. **Navigate to Settings**
   - Tap the Settings icon in top-right corner
   - You'll see "Protection Services" section

---

## Test Scenario 1: VPN-Based Website Blocking

### Enable VPN Protection
1. In Settings, look for "VPN Protection" card
2. Should show "Inactive" in RED
3. Tap "Enable VPN" button
4. Android will show VPN permission dialog
5. Tap "OK" to grant permission
6. Card should turn GREEN showing "Active - Filtering network traffic"

### Test Website Blocking
1. Enable toggle "Block social media" in Settings
2. Open your browser (Chrome, Firefox, etc.)
3. Try to visit: `https://facebook.com`
4. **Expected:** DNS will fail to resolve, website won't load
5. Try to visit: `https://google.com`
6. **Expected:** Should work normally (not blocked)

### Verify Blocking
- VPN status should remain GREEN
- You should see a notification "AmnShield Protection Active"
- Notification shows packet statistics when you pull down notification shade

---

## Test Scenario 2: App Blocking

### Enable Accessibility Service
1. In Settings, look for "App Blocking" card
2. Should show "Inactive" in RED
3. Tap "Open Accessibility Settings"
4. Find "AmnShield Blocker" or "Accessibility Blocker"
5. Enable the service
6. Return to AmnShield app
7. Card should now show GREEN "Active - Blocking apps"

### Block an App
1. In Settings, tap "Manage blocked apps"
2. Search for an app (e.g., type "instagram")
3. Select checkbox next to Instagram
4. Select a few more apps if you want
5. Tap "Block X Apps" button
6. Dialog closes, apps are now in blocked list

### Test App Blocking
1. Exit AmnShield app (press home)
2. Try to open Instagram (or any blocked app)
3. **Expected:** 
   - App starts to open
   - Immediately redirects to home screen
   - Toast message: "App blocked by AmnShield"
4. Try again - same result with 2-second cooldown

---

## Test Scenario 3: Create Custom Blocks

### Create a Block
1. Go to Blocks screen (bottom nav)
2. Tap the "+" floating action button
3. Fill in block details:
   - **Name:** "Work Hours Block"
   - **Apps:** Use "Pick from installed" to select apps
   - **Websites:** Add domains like "youtube.com, reddit.com"
   - **Keywords:** Add words to block in URLs

### Add Websites in Bulk
1. In the Websites section, paste:
   ```
   youtube.com, reddit.com, twitter.com, netflix.com
   ```
2. Tap "Add" button
3. All valid domains should appear as chips below
4. Invalid ones will show error message

### Save and Verify
1. Scroll down and tap "Save Block" button
2. Block appears in Blocks screen
3. **Automatic:** VPN service configuration updates immediately
4. Test by visiting one of the blocked websites
5. **Expected:** Website won't load (DNS blocked)

---

## Test Scenario 4: Service Status Monitoring

### Watch Real-Time Updates
1. Open Settings screen
2. Note the service status cards
3. **Test VPN:**
   - Disable VPN (tap "Disable VPN")
   - Card turns RED within 2 seconds
   - Enable VPN again
   - Card turns GREEN within 2 seconds

4. **Test Accessibility:**
   - Go to Android Settings → Accessibility
   - Disable AmnShield Blocker
   - Return to app
   - Card turns RED
   - Re-enable service
   - Card turns GREEN

### Status Indicators
- ✅ **GREEN (Primary Container)** = Service Active
- ❌ **RED (Error Container)** = Service Inactive
- 🟢 **Online Icon** = Active
- 🔴 **Offline Icon** = Inactive

---

## Test Scenario 5: Settings Toggles

### Global Blocking Toggles
1. In Settings, you'll see toggles:
   - ✅ Enable blocking globally
   - ✅ Block harmful keywords
   - ✅ Block harmful websites
   - ✅ Block social media

2. **Test Toggle Integration:**
   - Turn ON "Block social media"
   - Try to visit facebook.com or instagram.com
   - Should be blocked
   - Turn OFF the toggle
   - Try again - should work
   - **Note:** Changes take effect immediately

---

## Troubleshooting Common Issues

### VPN Won't Enable
**Symptoms:** VPN permission dialog doesn't appear
**Fix:** 
- Check if another VPN is running
- Disable other VPNs first
- Grant permission when prompted

### App Blocking Not Working
**Symptoms:** Blocked apps still open
**Fix:**
1. Verify Accessibility Service is enabled:
   - Settings → Accessibility → AmnShield Blocker → ON
2. Check if app is in blocked list:
   - Settings → Blocked Apps section
3. Try blocking again from "Manage blocked apps"

### Website Not Blocked
**Symptoms:** Blocked website still loads
**Fix:**
1. Verify VPN is active (GREEN status)
2. Check if domain is in block list
3. Clear browser cache
4. Try in incognito/private mode
5. Remember: HTTPS sites may bypass some DNS filtering

### Status Cards Not Updating
**Symptoms:** Cards stay RED even when service is on
**Fix:**
- Wait 2-3 seconds (auto-refresh cycle)
- Close and reopen Settings screen
- Restart the app

---

## Expected Behavior Summary

| Feature | Expected Behavior |
|---------|------------------|
| VPN Blocking | Websites in block list won't resolve via DNS |
| App Blocking | Blocked apps redirect to home immediately |
| Service Status | Updates every 2 seconds, color-coded |
| Block Creation | Saves and updates VPN config automatically |
| Toggle Switches | Changes take effect immediately |
| App Picker | Shows all user apps, searchable |
| Notifications | Shows blocking events and service status |

---

## Performance Notes

- **VPN Impact:** Minimal - only DNS filtering, not inspecting all packets
- **Battery:** Low impact - services are optimized
- **Memory:** ~50MB RAM typical usage
- **Blocking Speed:** Instant for DNS, <100ms for apps
- **Status Updates:** 2-second polling interval

---

## Advanced Testing

### Test with adb logcat
```powershell
# Monitor VPN service logs
adb logcat | Select-String "BlockingVpnService"

# Monitor Accessibility service logs
adb logcat | Select-String "AccessibilityBlocker"

# Monitor all AmnShield logs
adb logcat | Select-String "AmnShield"
```

### Test Network Traffic
```powershell
# Check VPN interface
adb shell ip addr show tun0

# Monitor DNS queries (requires root)
adb shell tcpdump -i any port 53
```

---

## Known Limitations

1. **HTTPS/TLS:** Cannot inspect encrypted traffic content
2. **System Apps:** Cannot block pre-installed system apps via Accessibility
3. **VPN Bypass:** Some apps may detect and bypass VPN
4. **DNS Caching:** Browser may cache DNS, clear cache to test
5. **Root Required:** Some advanced features need root (not implemented)

---

## Success Criteria

✅ VPN enables without errors
✅ VPN status shows GREEN when active
✅ Social media sites blocked when toggle enabled
✅ Accessibility service enables in Android settings
✅ App blocking status shows GREEN when active
✅ Blocked apps redirect to home with toast
✅ Custom blocks save successfully
✅ Block websites become unreachable
✅ Settings toggles update in real-time
✅ App picker shows all user apps
✅ No crashes or ANR errors

---

## Reporting Issues

If you find bugs, please note:
1. **Steps to reproduce**
2. **Expected behavior**
3. **Actual behavior**
4. **Device model and Android version**
5. **Logcat output** (if available)
6. **Screenshots** (if UI issue)

---

**Happy Testing! 🛡️**
