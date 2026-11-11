import math
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# Android TV Banner Specification for Google Play Store Listing:
# Exact size: 1280 x 720 px (16:9 aspect ratio)
W, H = 1280, 720
# Supersample at 2x (2560 x 1440) for razor-sharp typography, vector curves, and anti-aliasing
SW, SH = W * 2, H * 2

img = Image.new("RGBA", (SW, SH), (0, 0, 0, 255))
draw = ImageDraw.Draw(img)

# 1. Background Cinematic Dark Navy Gradient (#080D1A -> #141B34 -> #1E1B4B)
for y in range(SH):
    t = y / SH
    r = int(8 * (1 - t) + 26 * t)
    g = int(13 * (1 - t) + 24 * t)
    b = int(26 * (1 - t) + 68 * t)
    draw.line([(0, y), (SW, y)], fill=(r, g, b, 255))

# 2. Ambient Colorful Glow Spheres (Emerald, Cyan, Indigo)
glow_layer = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
g_draw = ImageDraw.Draw(glow_layer)

# Emerald glow on center-left (behind bridge/logo)
for rad in range(900, 50, -40):
    alpha = int(28 * (1 - rad / 900))
    g_draw.ellipse([450 - rad, 720 - rad, 450 + rad, 720 + rad], fill=(16, 185, 129, alpha))

# Cyan / Indigo glow on right (behind TV preview card)
for rad in range(1000, 100, -45):
    alpha = int(32 * (1 - rad / 1000))
    g_draw.ellipse([SW - 600 - rad, 600 - rad, SW - 600 + rad, 600 + rad], fill=(99, 102, 241, alpha))

# Amber accent glow top center
for rad in range(600, 50, -35):
    alpha = int(20 * (1 - rad / 600))
    g_draw.ellipse([1100 - rad, 250 - rad, 1100 + rad, 250 + rad], fill=(245, 158, 11, alpha))

img = Image.alpha_composite(img, glow_layer)

# 3. Geometric Tech Grid Lines
grid_layer = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
grid_draw = ImageDraw.Draw(grid_layer)
for x in range(0, SW, 90):
    grid_draw.line([(x, 0), (x, SH)], fill=(255, 255, 255, 7), width=1)
for y in range(0, SH, 90):
    grid_draw.line([(0, y), (SW, y)], fill=(255, 255, 255, 7), width=1)
img = Image.alpha_composite(img, grid_layer)
draw = ImageDraw.Draw(img)

# 4. Fonts
font_bold_path = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
font_reg_path = "/System/Library/Fonts/Supplemental/Arial.ttf"

font_title = ImageFont.truetype(font_bold_path, 115)
font_subtitle = ImageFont.truetype(font_reg_path, 46)
font_badge = ImageFont.truetype(font_bold_path, 34)
font_card_head = ImageFont.truetype(font_bold_path, 42)
font_card_rate = ImageFont.truetype(font_bold_path, 60)
font_card_sub = ImageFont.truetype(font_reg_path, 30)
font_dpad_hint = ImageFont.truetype(font_bold_path, 28)

# 5. Left Section: Logo, Title, Subtitle, Feature Badges
left_x = 160

# Draw / Paste App Icon Emblem (240 x 240 in supersampled canvas)
icon_size = 220
try:
    if os.path.exists("store_assets/ic_launcher_512.png"):
        icon_src = Image.open("store_assets/ic_launcher_512.png").convert("RGBA")
        icon_resized = icon_src.resize((icon_size, icon_size), Image.Resampling.LANCZOS)
        
        # Rounded corner mask
        mask = Image.new("L", (icon_size, icon_size), 0)
        m_draw = ImageDraw.Draw(mask)
        m_draw.rounded_rectangle([0, 0, icon_size, icon_size], radius=48, fill=255)
        
        # Icon shadow
        icon_shadow = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
        is_draw = ImageDraw.Draw(icon_shadow)
        is_draw.rounded_rectangle(
            [left_x - 10, 220, left_x + icon_size + 10, 220 + icon_size + 20],
            radius=52, fill=(0, 0, 0, 160)
        )
        icon_shadow = icon_shadow.filter(ImageFilter.GaussianBlur(25))
        img = Image.alpha_composite(img, icon_shadow)
        draw = ImageDraw.Draw(img)
        
        img.paste(icon_resized, (left_x, 220), mask)
        
        # Border highlight around icon
        draw.rounded_rectangle(
            [left_x, 220, left_x + icon_size, 220 + icon_size],
            radius=48, outline=(56, 189, 248, 180), width=4
        )
except Exception as e:
    print("Icon loading error:", e)

# App Title & Brand
title_y = 480
draw.text((left_x, title_y), "CoinBridge", font=font_title, fill=(255, 255, 255, 255))

# Emerald glowing dot
draw.ellipse([left_x + 640, title_y + 40, left_x + 675, title_y + 75], fill=(16, 185, 129, 255))

# Subtitle
draw.text((left_x, title_y + 130), "Currency & Crypto Converter for Android TV", font=font_subtitle, fill=(203, 213, 225, 255))

# TV Feature Badges
tv_badges = [
    ("🎮 D-Pad Navigation", (16, 185, 129)),
    ("⚡ 100% Offline Rates", (56, 189, 248)),
    ("🪙 170+ Currencies & Crypto", (245, 158, 11)),
    ("📈 Interactive Trends", (168, 85, 247))
]

badge_start_y = title_y + 220
badge_x = left_x
for text, color in tv_badges:
    bbox = draw.textbbox((0, 0), text, font=font_badge)
    bw = bbox[2] - bbox[0] + 56
    bh = 68
    
    # Check if exceeds half width, wrap to next line
    if badge_x + bw > 1200:
        badge_start_y += 86
        badge_x = left_x
    
    draw.rounded_rectangle(
        [badge_x, badge_start_y, badge_x + bw, badge_start_y + bh],
        radius=34,
        fill=(15, 23, 42, 220),
        outline=(*color, 190),
        width=3
    )
    draw.text((badge_x + 28, badge_start_y + 14), text, font=font_badge, fill=(241, 245, 249, 255))
    badge_x += bw + 24

# 6. Right Section: Focused Android TV Leanback Card Showcase
card_w, card_h = 1040, 840
card_x = SW - card_w - 140
card_y = 260

# Card Drop Shadow
card_shadow = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
cs_draw = ImageDraw.Draw(card_shadow)
cs_draw.rounded_rectangle(
    [card_x - 15, card_y + 25, card_x + card_w + 15, card_y + card_h + 45],
    radius=54, fill=(0, 0, 0, 190)
)
card_shadow = card_shadow.filter(ImageFilter.GaussianBlur(40))
img = Image.alpha_composite(img, card_shadow)
draw = ImageDraw.Draw(img)

# Card Surface (Simulating Android TV D-Pad Focused Item with glowing Cyan border)
card_surf = Image.new("RGBA", (card_w, card_h), (0, 0, 0, 0))
c_draw = ImageDraw.Draw(card_surf)

# Card Background Fill
c_draw.rounded_rectangle(
    [0, 0, card_w, card_h],
    radius=48,
    fill=(15, 23, 42, 245),
    outline=(56, 189, 248, 240), # Vibrant D-pad focus outline
    width=6
)

# Active D-pad Focus Tag at top-right
c_draw.rounded_rectangle(
    [card_w - 260, 40, card_w - 40, 100],
    radius=20,
    fill=(16, 185, 129, 45),
    outline=(16, 185, 129, 255),
    width=3
)
c_draw.text((card_w - 235, 54), "● D-PAD ACTIVE", font=font_dpad_hint, fill=(16, 185, 129, 255))

# Primary Rate Pair Header: USD to EUR
c_draw.text((50, 48), "🇺🇸 USD  ⇄  🇪🇺 EUR", font=font_card_head, fill=(241, 245, 249, 255))
c_draw.text((50, 108), "1.00 USD  =  0.9248 EUR", font=font_card_rate, fill=(255, 255, 255, 255))

# Percentage Change Pill
c_draw.rounded_rectangle([680, 114, 820, 170], radius=16, fill=(16, 185, 129, 40), outline=(16, 185, 129), width=2)
c_draw.text((700, 126), "+0.38% ▲", font=font_dpad_hint, fill=(16, 185, 129, 255))

# Secondary Crypto Pair Box
c_draw.rounded_rectangle([50, 195, card_w - 50, 310], radius=24, fill=(30, 41, 59, 180), outline=(51, 65, 85, 150), width=2)
c_draw.text((80, 218), "🪙 Bitcoin (BTC)", font=font_card_sub, fill=(245, 158, 11))
c_draw.text((80, 256), "1 BTC = $ 64,820.00 USD", font=font_card_head, fill=(255, 255, 255))
c_draw.text((card_w - 240, 240), "+2.45% 🚀", font=font_card_head, fill=(16, 185, 129))

# Rate Trend Sparkline Chart
c_draw.text((50, 340), "30-Day Trend Overview", font=font_card_sub, fill=(148, 163, 184))
c_draw.text((card_w - 380, 340), "Min: 0.9120   Max: 0.9315", font=font_card_sub, fill=(148, 163, 184))

# Chart Grid & Trend Path
chart_pts = [
    (60, 560), (160, 530), (280, 580), (390, 490), (510, 520),
    (630, 440), (740, 470), (860, 400), (970, 420)
]

# Fill area under chart curve
poly_pts = [(60, 640)] + chart_pts + [(970, 640)]
c_draw.polygon(poly_pts, fill=(16, 185, 129, 30))

# Stroke chart line
for i in range(len(chart_pts) - 1):
    c_draw.line([chart_pts[i], chart_pts[i+1]], fill=(16, 185, 129, 255), width=6)

# Vertices
for pt in chart_pts:
    c_draw.ellipse([pt[0] - 8, pt[1] - 8, pt[0] + 8, pt[1] + 8], fill=(56, 189, 248), outline=(255, 255, 255), width=3)

# D-Pad Remote Navigation Bottom Control Bar
c_draw.rounded_rectangle([50, 680, card_w - 50, 780], radius=24, fill=(30, 27, 75, 220), outline=(139, 92, 246, 150), width=2)
c_draw.text(
    (80, 712),
    "🎮 Remote Controls:  [ ▲ / ▼ ] Select Currencies   [ OK ] Quick Swap   [ ◀ / ▶ ] Timeframe",
    font=font_dpad_hint,
    fill=(226, 232, 240)
)

img.paste(card_surf, (card_x, card_y), card_surf)

# 7. Downsample from 2560 x 1440 to final exact 1280 x 720 with LANCZOS
final_tv_banner = img.resize((W, H), Image.Resampling.LANCZOS)

# Google Play Store requires 24-bit RGB PNG without transparency
final_tv_banner_rgb = Image.new("RGB", (W, H), (15, 23, 42))
final_tv_banner_rgb.paste(final_tv_banner, mask=final_tv_banner.split()[3])

out_path = "store_assets/tv_banner.png"
final_tv_banner_rgb.save(out_path, "PNG", optimize=True)
print(f"Successfully generated {out_path} with size {W}x{H} (16:9 24-bit PNG).")
