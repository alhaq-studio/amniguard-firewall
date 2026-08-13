# Install AmnShield FireWall debug APK on connected Android device
# Prerequisites: USB debugging enabled, device connected

$ErrorActionPreference = "Stop"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$apkDir = Join-Path $PSScriptRoot "app\build\outputs\apk\debug"
$apk = Get-ChildItem -Path $apkDir -Filter "AmnShield FireWall-*-debug.apk" -ErrorAction SilentlyContinue | Select-Object -First 1

if (-not (Test-Path $adb)) {
    Write-Error "ADB not found at $adb. Install Android SDK platform-tools."
}
if (-not $apk) {
    Write-Error "APK not found in $apkDir. Run: .\gradlew.bat assembleDebug"
}
$apk = $apk.FullName

$devices = & $adb devices | Select-String "device$"
if (-not $devices) {
    Write-Error "No device connected. Connect device with USB debugging enabled."
}

Write-Host "Installing NetBlock..."
& $adb install -r "$apk"
if ($LASTEXITCODE -eq 0) {
    Write-Host "NetBlock installed successfully."
} else {
    exit $LASTEXITCODE
}
