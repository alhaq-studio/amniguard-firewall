Add-Type -AssemblyName System.Drawing

$legacyIconPath = "e:\HabiburRahman\Development\NetBlock\new_icon.png"
$foregroundIconPath = "e:\HabiburRahman\Development\NetBlock\new_icon_foreground.png"
$monochromeIconPath = "e:\HabiburRahman\Development\NetBlock\new_icon_monochrome.png"
$resPath = "e:\HabiburRahman\Development\NetBlock\app\src\main\res"
$iconSizes = @{"mipmap-mdpi"=48;"mipmap-hdpi"=72;"mipmap-xhdpi"=96;"mipmap-xxhdpi"=144;"mipmap-xxxhdpi"=192}

function Resize-And-Save($source,$path,$size,$grayscale){
    $bmp = New-Object System.Drawing.Bitmap $size,$size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    if($grayscale){
        $cm = New-Object Drawing.Imaging.ColorMatrix @(@(0.2126,0.2126,0.2126,0,0),@(0.7152,0.7152,0.7152,0,0),@(0.0722,0.0722,0.0722,0,0),@(0,0,0,1,0),@(0,0,0,0,1))
        $ia = New-Object Drawing.Imaging.ImageAttributes
        $ia.SetColorMatrix($cm)
        $rect = New-Object System.Drawing.Rectangle(0,0,$size,$size)
        $g.DrawImage($source,$rect,0,0,$source.Width,$source.Height,[System.Drawing.GraphicsUnit]::Pixel,$ia)
    } else {
        $g.DrawImage($source,0,0,$size,$size)
    }
    $bmp.Save($path,[System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose();$bmp.Dispose()
}

if(!(Test-Path $legacyIconPath)){Write-Host "missing new_icon.png" -ForegroundColor Red; exit 1}
if(!(Test-Path $foregroundIconPath)){Copy-Item $legacyIconPath $foregroundIconPath -Force}
if(!(Test-Path $monochromeIconPath)){ $useGray=$true } else { $useGray=$false }

$legacyImage=[System.Drawing.Image]::FromFile($legacyIconPath)
$foregroundImage=[System.Drawing.Image]::FromFile($foregroundIconPath)
$monoImage = $foregroundImage
if(!$useGray){ $monoImage=[System.Drawing.Image]::FromFile($monochromeIconPath) }

foreach($density in $iconSizes.Keys){
    $size=$iconSizes[$density]
    Resize-And-Save $legacyImage (Join-Path $resPath "$density/ic_launcher.png") $size $false
    Resize-And-Save $foregroundImage (Join-Path $resPath "$density/ic_launcher_foreground.png") $size $false
    Resize-And-Save $monoImage (Join-Path $resPath "$density/ic_launcher_monochrome.png") $size $useGray
    Write-Host "$density done ($size px)"
}

$legacyImage.Dispose();$foregroundImage.Dispose(); if($monoImage -ne $foregroundImage){$monoImage.Dispose()}
Write-Host "All icons updated" -ForegroundColor Green
