import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def generate_clean_banner():
    width = 1024
    height = 500
    
    # 1. Base clean dark gradient background (sleek deep slate/navy)
    banner = Image.new("RGBA", (width, height), (15, 20, 32, 255)) # #0F1420
    draw = ImageDraw.Draw(banner)
    
    # Subtle smooth horizontal gradient from left to right
    for x in range(width):
        ratio = x / width
        r = int(14 + (18 - 14) * ratio)
        g = int(18 + (24 - 18) * ratio)
        b = int(30 + (42 - 30) * ratio)
        draw.line([(x, 0), (x, height)], fill=(r, g, b, 255))
        
    # Soft, subtle ambient glow behind the logo on the left
    glow_layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_layer)
    center_x, center_y = 260, 250
    for radius in range(240, 0, -10):
        alpha = int((1 - (radius / 240.0)) * 32)
        glow_draw.ellipse([center_x - radius, center_y - radius, center_x + radius, center_y + radius], fill=(14, 165, 233, alpha))
        
    banner = Image.alpha_composite(banner, glow_layer)
    
    # 2. Crop and paste transparent foreground logo
    icon_path = "d:/PROJECTS/Amni/AmniGuard-FireWall/app/src/assets/images/icon_foreground.png"
    if not os.path.exists(icon_path):
        icon_path = "d:/PROJECTS/Amni/AmniGuard-FireWall/new_icon_foreground.png"
        
    icon = Image.open(icon_path).convert("RGBA")
    bbox = icon.getbbox()
    if bbox:
        icon_cropped = icon.crop(bbox)
    else:
        icon_cropped = icon
        
    # Resize preserving aspect ratio (height: 330px)
    target_h = 330
    aspect = icon_cropped.width / icon_cropped.height
    target_w = int(target_h * aspect)
    icon_resized = icon_cropped.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Subtle drop shadow
    shadow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    pos_x = 100
    pos_y = (height - target_h) // 2
    shadow_draw.ellipse([pos_x + 20, pos_y + target_h - 30, pos_x + target_w - 20, pos_y + target_h + 20], fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(16))
    banner = Image.alpha_composite(banner, shadow)
    
    # Paste the transparent logo
    banner.paste(icon_resized, (pos_x, pos_y), icon_resized)
    
    # 3. Clean typography
    text_layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    text_draw = ImageDraw.Draw(text_layer)
    
    font_bold = "C:/Windows/Fonts/segoeuib.ttf"
    font_regular = "C:/Windows/Fonts/segoeui.ttf"
    font_semibold = "C:/Windows/Fonts/seguisb.ttf"
    
    tx = pos_x + target_w + 55
    ty = 115
    
    # App Title
    title_font = ImageFont.truetype(font_bold, 62)
    text_draw.text((tx, ty), "AmniGuard", font=title_font, fill=(255, 255, 255, 255))
    
    # Subtitle / Category
    sub_font = ImageFont.truetype(font_semibold, 28)
    text_draw.text((tx, ty + 78), "No-Root Firewall & Web Filter", font=sub_font, fill=(56, 189, 248, 255)) # Sky cyan #38BDF8
    
    # Description line
    desc_font = ImageFont.truetype(font_regular, 20)
    text_draw.text((tx, ty + 128), "Granular per-app network control and local DNS blocking", font=desc_font, fill=(148, 163, 184, 255)) # Slate #94A3B8
    
    # Key Highlights Divider Line & Badges
    text_draw.line([(tx, ty + 172), (tx + 460, ty + 172)], fill=(51, 65, 85, 180), width=1)
    
    highlight_font = ImageFont.truetype(font_semibold, 15)
    highlights = "100% ON-DEVICE  •  ZERO TELEMETRY  •  OPEN SOURCE"
    text_draw.text((tx, ty + 188), highlights, font=highlight_font, fill=(100, 116, 139, 255))
    
    banner = Image.alpha_composite(banner, text_layer)
    
    # 4. Save final high quality PNG
    final_output = banner.convert("RGB")
    output_path = "d:/PROJECTS/Amni/AmniGuard-FireWall/playstore/play.png"
    final_output.save(output_path, "PNG", quality=100)
    print(f"Generated clean feature banner: {output_path} (Size: {final_output.size})")

if __name__ == "__main__":
    generate_clean_banner()
