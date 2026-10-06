# PowerShell script to resize and update Android app icons
# Save assets in project root:
#   - Main adaptive icon: new_icon.png (full-color)
#   - Foreground (preferred for adaptive/round): new_icon_foreground.png (usually flat/white)
#   - Monochrome (optional; if missing, a grayscale version will be auto-generated): new_icon_monochrome.png

$legacyIconPath     = "e:\HabiburRahman\Development\NetBlock\new_icon.png"
$foregroundIconPath = "e:\HabiburRahman\Development\NetBlock\new_icon_foreground.png"
$monochromeIconPath = "e:\HabiburRahman\Development\NetBlock\new_icon_monochrome.png"
$resPath            = "e:\HabiburRahman\Development\NetBlock\app\src\main\res"

# Define icon sizes for each density
$iconSizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

function Resize-And-Save {
    param(
        [System.Drawing.Image]$source,
        [string]$path,
        [int]$size,
        [bool]$grayscale = $false
    )

    $newBitmap = New-Object System.Drawing.Bitmap $size, $size
    $graphics  = [System.Drawing.Graphics]::FromImage($newBitmap)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode   = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    if ($grayscale) {
        $colorMatrixElements = @(
            @(0.2126, 0.2126, 0.2126, 0, 0),
            @(0.7152, 0.7152, 0.7152, 0, 0),
            @(0.0722, 0.0722, 0.0722, 0, 0),
            @(0, 0, 0, 1, 0),
            @(0, 0, 0, 0, 1)
        )
        $colorMatrix = New-Object Drawing.Imaging.ColorMatrix($colorMatrixElements)
        $imageAttributes = New-Object Drawing.Imaging.ImageAttributes
        $imageAttributes.SetColorMatrix($colorMatrix)
        $rect = New-Object System.Drawing.Rectangle(0, 0, $size, $size)
        $graphics.DrawImage($source, $rect, 0, 0, $source.Width, $source.Height, [System.Drawing.GraphicsUnit]::Pixel, $imageAttributes)
    } else {
        $graphics.DrawImage($source, 0, 0, $size, $size)
    }

    $newBitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $newBitmap.Dispose()
}

if (-not (Test-Path $legacyIconPath)) {
    Write-Host "ERROR: Please save the main icon as 'new_icon.png' in the project root first!" -ForegroundColor Red
    Write-Host "Expected: $legacyIconPath" -ForegroundColor Yellow
    exit 1
}

Write-Host "Starting icon update process..." -ForegroundColor Green
Add-Type -AssemblyName System.Drawing

try {
    $legacyImage = [System.Drawing.Image]::FromFile($legacyIconPath)
    Write-Host "Loaded main icon: $($legacyImage.Width)x$($legacyImage.Height)" -ForegroundColor Cyan

    if (-not (Test-Path $foregroundIconPath)) {
        Write-Host "Foreground icon not found; reusing main icon for foreground." -ForegroundColor Yellow
        Copy-Item $legacyIconPath $foregroundIconPath -Force
    }
    $foregroundImage = [System.Drawing.Image]::FromFile($foregroundIconPath)
    Write-Host "Loaded foreground icon: $($foregroundImage.Width)x$($foregroundImage.Height)" -ForegroundColor Cyan

    $monochromeImage = $null
    if (Test-Path $monochromeIconPath) {
        $monochromeImage = [System.Drawing.Image]::FromFile($monochromeIconPath)
        Write-Host "Loaded monochrome icon: $($monochromeImage.Width)x$($monochromeImage.Height)" -ForegroundColor Cyan
    } else {
        Write-Host "Monochrome icon not found; will auto-generate grayscale from foreground." -ForegroundColor Yellow
        $monochromeImage = $foregroundImage
    }

    foreach ($density in $iconSizes.Keys) {
        $size       = $iconSizes[$density]
        $legacyOut  = Join-Path $resPath "$density\ic_launcher.png"
        $fgOut      = Join-Path $resPath "$density\ic_launcher_foreground.png"
        $monoOut    = Join-Path $resPath "$density\ic_launcher_monochrome.png"

        Write-Host "Creating $density assets (${size}x${size})..." -ForegroundColor Yellow
        Resize-And-Save -source $legacyImage -path $legacyOut -size $size
        Resize-And-Save -source $foregroundImage -path $fgOut -size $size
        $useGrayscale = ($monochromeImage -eq $foregroundImage)
        Resize-And-Save -source $monochromeImage -path $monoOut -size $size -grayscale:$useGrayscale
        Write-Host "  ✓ Saved legacy: $legacyOut" -ForegroundColor Green
        Write-Host "  ✓ Saved foreground: $fgOut" -ForegroundColor Green
        Write-Host "  ✓ Saved monochrome: $monoOut" -ForegroundColor Green
    }

    $legacyImage.Dispose()
    $foregroundImage.Dispose()
    if ($monochromeImage -and ($monochromeImage -ne $foregroundImage)) {
        $monochromeImage.Dispose()
    }

    Write-Host "`n✓ Icon update completed successfully!" -ForegroundColor Green
    Write-Host "All launcher icons, foregrounds, and monochrome layers updated in res/mipmap-* folders." -ForegroundColor Cyan

} catch {
    Write-Host "ERROR: Failed to process icon - $_" -ForegroundColor Red
    exit 1
}
