import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1024, 500
# Supersample at 2x (2048 x 1000) for pristine crisp text and graphics
SW, SH = W * 2, H * 2

img = Image.new("RGBA", (SW, SH), (0, 0, 0, 255))
draw = ImageDraw.Draw(img)

# 1. Background Gradient: Deep Midnight Slate #0A0F1D to #1E1B4B with subtle radial glows
for y in range(SH):
    t = y / SH
    r = int(10 * (1 - t) + 26 * t)
    g = int(15 * (1 - t) + 24 * t)
    b = int(29 * (1 - t) + 65 * t)
    draw.line([(0, y), (SW, y)], fill=(r, g, b, 255))

# Ambient glow spheres
glow = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
g_draw = ImageDraw.Draw(glow)

# Emerald glow on bottom left
for rad in range(600, 50, -30):
    alpha = int(25 * (1 - rad / 600))
    g_draw.ellipse([200 - rad, SH - 100 - rad, 200 + rad, SH - 100 + rad], fill=(16, 185, 129, alpha))

# Cyan/Indigo glow on top right
for rad in range(800, 100, -35):
    alpha = int(35 * (1 - rad / 800))
    g_draw.ellipse([SW - 400 - rad, 200 - rad, SW - 400 + rad, 200 + rad], fill=(99, 102, 241, alpha))

# Subtle background grid lines
grid_layer = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
grid_draw = ImageDraw.Draw(grid_layer)
for x in range(0, SW, 80):
    grid_draw.line([(x, 0), (x, SH)], fill=(255, 255, 255, 6), width=1)
for y in range(0, SH, 80):
    grid_draw.line([(0, y), (SW, y)], fill=(255, 255, 255, 6), width=1)

img = Image.alpha_composite(img, glow)
img = Image.alpha_composite(img, grid_layer)
draw = ImageDraw.Draw(img)

# 2. Fonts
font_bold_path = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
font_reg_path = "/System/Library/Fonts/Supplemental/Arial.ttf"

font_title = ImageFont.truetype(font_bold_path, 92)
font_subtitle = ImageFont.truetype(font_reg_path, 40)
font_pill = ImageFont.truetype(font_bold_path, 32)
font_card_title = ImageFont.truetype(font_bold_path, 34)
font_card_num = ImageFont.truetype(font_bold_path, 48)
font_card_sub = ImageFont.truetype(font_reg_path, 26)

# 3. Left Column: Branding, Title, Subtitle, Feature Badges
left_x = 120

# Top Logo icon preview (small 120x120)
try:
    icon_src = Image.open("store_assets/ic_launcher_512.png").convert("RGBA")
    icon_small = icon_src.resize((150, 150), Image.Resampling.LANCZOS)
    # Mask to rounded rect
    mask = Image.new("L", (150, 150), 0)
    m_draw = ImageDraw.Draw(mask)
    m_draw.rounded_rectangle([0, 0, 150, 150], radius=32, fill=255)
    img.paste(icon_small, (left_x, 140), mask)
except Exception as e:
    print("Icon load error:", e)

# App Title & Tag
title_y = 320
draw.text((left_x, title_y), "CoinBridge", font=font_title, fill=(255, 255, 255, 255))

# Glowing emerald dot next to title
draw.ellipse([left_x + 520, title_y + 35, left_x + 546, title_y + 61], fill=(16, 185, 129, 255))

# Subtitle
draw.text((left_x, title_y + 110), "Fast, Offline & Private Currency Exchange", font=font_subtitle, fill=(148, 163, 184, 255))

# Feature Pills
pills = [
    ("⚡ 100% Offline Rates & Instant Sync", (16, 185, 129)),
    ("🪙 170+ World Currencies & Crypto", (245, 158, 11)),
    ("🍽️ Travel Bill & Tip Splitter", (56, 189, 248)),
    ("🔒 Biometric Lock • Zero Ads", (168, 85, 247))
]

pill_start_y = title_y + 190
for i, (text, color) in enumerate(pills):
    py = pill_start_y + i * 72
    # Pill background
    bbox = draw.textbbox((0, 0), text, font=font_pill)
    pw = bbox[2] - bbox[0] + 50
    ph = 56
    draw.rounded_rectangle([left_x, py, left_x + pw, py + ph], radius=28, fill=(30, 41, 59, 180), outline=(*color, 160), width=2)
    draw.text((left_x + 25, py + 10), text, font=font_pill, fill=(241, 245, 249, 255))

# 4. Right Column: High-fidelity Mockup Cards
# Card 1: Main Currency Converter Card (Floating with drop shadow)
card1_w, card1_h = 760, 420
card1_x = SW - card1_w - 120
card1_y = 130

# Drop shadow
c_shadow = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
cs_draw = ImageDraw.Draw(c_shadow)
cs_draw.rounded_rectangle([card1_x - 10, card1_y + 15, card1_x + card1_w + 10, card1_y + card1_h + 35], radius=42, fill=(0, 0, 0, 160))
c_shadow = c_shadow.filter(ImageFilter.GaussianBlur(35))
img = Image.alpha_composite(img, c_shadow)
draw = ImageDraw.Draw(img)

# Card 1 Surface
card1_surf = Image.new("RGBA", (card1_w, card1_h), (0, 0, 0, 0))
c1_draw = ImageDraw.Draw(card1_surf)
c1_draw.rounded_rectangle([0, 0, card1_w, card1_h], radius=38, fill=(15, 23, 42, 230), outline=(56, 189, 248, 120), width=3)

# Inside Card 1:
# Header: Base Currency USD $1,250.00
c1_draw.rounded_rectangle([30, 30, card1_w - 30, 150], radius=24, fill=(30, 41, 59, 200))
c1_draw.text((60, 52), "🇺🇸 USD • US Dollar", font=font_card_sub, fill=(148, 163, 184))
c1_draw.text((60, 85), "$ 1,250.00", font=font_card_num, fill=(255, 255, 255))
c1_draw.rounded_rectangle([card1_w - 180, 55, card1_w - 60, 115], radius=16, fill=(16, 185, 129, 40), outline=(16, 185, 129), width=2)
c1_draw.text((card1_w - 160, 70), "BASE", font=font_card_sub, fill=(16, 185, 129))

# Target Rows
targets = [
    ("🇪🇺 EUR", "€ 1,114.68", "+0.14%", (16, 185, 129)),
    ("🇬🇧 GBP", "£ 931.26", "+0.08%", (16, 185, 129)),
    ("🇯🇵 JPY", "¥ 197,725", "-0.22%", (239, 68, 68)),
    ("🪙 BTC", "0.0195 ₿", "+2.45%", (245, 158, 11))
]

for idx, (t_code, t_val, t_chg, t_col) in enumerate(targets):
    ty = 175 + idx * 56
    c1_draw.text((60, ty), t_code, font=font_card_title, fill=(241, 245, 249))
    c1_draw.text((340, ty), t_val, font=font_card_title, fill=(255, 255, 255))
    c1_draw.text((card1_w - 180, ty), t_chg, font=font_card_title, fill=t_col)

img.paste(card1_surf, (card1_x, card1_y), card1_surf)

# Card 2: Floating Rate Trends & Bill Splitter Badge (overlapping lower right)
card2_w, card2_h = 720, 360
card2_x = SW - card2_w - 80
card2_y = card1_y + 320

c2_shadow = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
c2s_draw = ImageDraw.Draw(c2_shadow)
c2s_draw.rounded_rectangle([card2_x - 10, card2_y + 15, card2_x + card2_w + 10, card2_y + card2_h + 35], radius=38, fill=(0, 0, 0, 180))
c2_shadow = c2_shadow.filter(ImageFilter.GaussianBlur(30))
img = Image.alpha_composite(img, c2_shadow)

card2_surf = Image.new("RGBA", (card2_w, card2_h), (0, 0, 0, 0))
c2_draw = ImageDraw.Draw(card2_surf)
c2_draw.rounded_rectangle([0, 0, card2_w, card2_h], radius=34, fill=(30, 27, 75, 245), outline=(139, 92, 246, 180), width=3)

# Inside Card 2:
c2_draw.text((40, 30), "📈 Interactive Rate Trends (USD ➔ EUR)", font=font_card_title, fill=(255, 255, 255))
c2_draw.text((40, 75), "7D • 30D • 90D • 1Y  |  Low: 0.8876  Avg: 0.8931  High: 0.8985", font=font_card_sub, fill=(196, 181, 253))

# Draw mini line chart curve
chart_pts = [
    (50, 260), (120, 240), (200, 280), (280, 210), (360, 230),
    (440, 180), (520, 220), (600, 160), (670, 175)
]
for i in range(len(chart_pts) - 1):
    c2_draw.line([chart_pts[i], chart_pts[i+1]], fill=(16, 185, 129), width=5)

# Glowing dots on chart points
for pt in chart_pts:
    c2_draw.ellipse([pt[0] - 6, pt[1] - 6, pt[0] + 6, pt[1] + 6], fill=(56, 189, 248), outline=(255, 255, 255), width=2)

# Mini tip split badge inside card 2 bottom right
c2_draw.rounded_rectangle([400, 270, card2_w - 30, 335], radius=18, fill=(15, 23, 42, 220), outline=(16, 185, 129), width=2)
c2_draw.text((420, 285), "🍽️ Split: $13.28 / person (4 people • 18% tip)", font=font_card_sub, fill=(241, 245, 249))

img.paste(card2_surf, (card2_x, card2_y), card2_surf)

# Downsample to 1024 x 500 with LANCZOS
final_banner = img.resize((W, H), Image.Resampling.LANCZOS)

# Google play requires 24-bit PNG or JPEG without alpha
final_banner_rgb = Image.new("RGB", (W, H), (15, 23, 42))
final_banner_rgb.paste(final_banner, mask=final_banner.split()[3])
final_banner_rgb.save("store_assets/feature_graphic.png", "PNG", optimize=True)
print("Saved store_assets/feature_graphic.png: size 1024x500")
