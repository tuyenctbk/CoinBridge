import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# Canvas size: supersample at 4x (2048 x 2048) for ultra-smooth anti-aliasing
S = 2048
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# 1. Background Rounded Rect / Gradient Squircle or Full Canvas
# Google Play Store icons are submitted as 512x512 full bleed squares (Google Play automatically applies squircle mask with corner radius = 20%)
# So we fill the full 512x512 canvas with a rich midnight gradient.

for y in range(S):
    t = y / S
    # Radial + Linear blend: #0F172A (15, 23, 42) -> #1E1B4B (30, 27, 75)
    r = int(15 * (1 - t) + 30 * t)
    g = int(23 * (1 - t) + 27 * t)
    b = int(42 * (1 - t) + 75 * t)
    draw.line([(0, y), (S, y)], fill=(r, g, b, 255))

# Add ambient subtle radial glow in center
glow_center = (S // 2, S // 2)
glow_layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
glow_draw = ImageDraw.Draw(glow_layer)
for rad in range(700, 100, -25):
    alpha = int(20 * (1 - rad / 700))
    glow_draw.ellipse(
        [glow_center[0] - rad, glow_center[1] - rad, glow_center[0] + rad, glow_center[1] + rad],
        fill=(99, 102, 241, alpha) # Indigo subtle glow
    )
img = Image.alpha_composite(img, glow_layer)
draw = ImageDraw.Draw(img)

# 2. Concentric Orbit Rings
draw.ellipse([S * 0.12, S * 0.12, S * 0.88, S * 0.88], outline=(30, 41, 59, 200), width=int(S * 0.015))
draw.ellipse([S * 0.22, S * 0.22, S * 0.78, S * 0.78], outline=(51, 65, 85, 140), width=int(S * 0.01))

# 3. Bridge Arch in Center connecting both coins
# Coordinates in 2048 canvas:
# Left coin center ~ (660, 1024), Right coin center ~ (1388, 1024)
# Arch connects from (580, 1200) curving up to (1024, 880) down to (1468, 1200)

arch_points = []
for deg in range(0, 181, 2):
    rad = math.radians(deg)
    # Parametric arch
    x = 1024 - 440 * math.cos(rad)
    y = 1220 - 320 * math.sin(rad)
    arch_points.append((x, y))

# Draw arch glow
for i in range(len(arch_points) - 1):
    draw.line([arch_points[i], arch_points[i+1]], fill=(16, 185, 129, 60), width=int(S * 0.07))
# Draw main arch
for i in range(len(arch_points) - 1):
    draw.line([arch_points[i], arch_points[i+1]], fill=(16, 185, 129, 255), width=int(S * 0.04))

# Arch deck highlights
for deg in range(30, 151, 15):
    rad = math.radians(deg)
    x = 1024 - 440 * math.cos(rad)
    y = 1220 - 320 * math.sin(rad)
    draw.line([(x, y), (x, 1220)], fill=(16, 185, 129, 120), width=int(S * 0.012))

# 4. Swap Exchange Arrows
# Top arrow: left to right (Cyan/Indigo)
arrow_y1 = int(S * 0.35)
arrow_x1 = int(S * 0.42)
arrow_x2 = int(S * 0.58)
draw.line([(arrow_x1, arrow_y1), (arrow_x2, arrow_y1)], fill=(56, 189, 248, 240), width=int(S * 0.022))
# Head
draw.line([(arrow_x2 - int(S * 0.04), arrow_y1 - int(S * 0.035)), (arrow_x2, arrow_y1)], fill=(56, 189, 248, 240), width=int(S * 0.022))
draw.line([(arrow_x2 - int(S * 0.04), arrow_y1 + int(S * 0.035)), (arrow_x2, arrow_y1)], fill=(56, 189, 248, 240), width=int(S * 0.022))

# Bottom arrow: right to left (Indigo)
arrow_y2 = int(S * 0.65)
draw.line([(arrow_x2, arrow_y2), (arrow_x1, arrow_y2)], fill=(99, 102, 241, 240), width=int(S * 0.022))
# Head
draw.line([(arrow_x1 + int(S * 0.04), arrow_y2 - int(S * 0.035)), (arrow_x1, arrow_y2)], fill=(99, 102, 241, 240), width=int(S * 0.022))
draw.line([(arrow_x1 + int(S * 0.04), arrow_y2 + int(S * 0.035)), (arrow_x1, arrow_y2)], fill=(99, 102, 241, 240), width=int(S * 0.022))

# 5. Coins
coin_radius = int(S * 0.16) # ~ 328 px
left_cx, left_cy = int(S * 0.33), int(S * 0.50)
right_cx, right_cy = int(S * 0.67), int(S * 0.50)

# Function to draw a 3D styled coin
def draw_coin(cx, cy, radius, base_col, ring_col, symbol_text, text_col):
    # Outer drop shadow
    shadow_layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.ellipse([cx - radius - 20, cy - radius - 10, cx + radius + 20, cy + radius + 40], fill=(0, 0, 0, 140))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(30))
    img.paste(Image.alpha_composite(Image.new("RGBA", (S, S), (0, 0, 0, 0)), shadow_layer), (0, 0), shadow_layer)
    
    c_draw = ImageDraw.Draw(img)
    # Coin base
    c_draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], fill=base_col)
    # Inner rim
    c_draw.ellipse([cx - radius + 20, cy - radius + 20, cx + radius - 20, cy + radius - 20], outline=ring_col, width=int(S * 0.015))
    
    # Highlight crescent
    c_draw.arc([cx - radius + 10, cy - radius + 10, cx + radius - 10, cy + radius - 10], 190, 350, fill=(255, 255, 255, 120), width=int(S * 0.012))

    # Symbol text
    try:
        font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Bold.ttf", int(radius * 1.15))
    except:
        font = ImageFont.load_default()
    
    bbox = c_draw.textbbox((0, 0), symbol_text, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    tx = cx - tw / 2 - bbox[0]
    ty = cy - th / 2 - bbox[1]
    
    # Symbol shadow
    c_draw.text((tx + 4, ty + 6), symbol_text, fill=(0, 0, 0, 90), font=font)
    # Symbol fill
    c_draw.text((tx, ty), symbol_text, fill=text_col, font=font)

# Left Coin: Emerald $ (Fiat)
draw_coin(left_cx, left_cy, coin_radius, (16, 185, 129), (5, 150, 105), "$", (255, 255, 255))

# Right Coin: Gold ₿ (Crypto)
draw_coin(right_cx, right_cy, coin_radius, (245, 158, 11), (217, 119, 6), "₿", (255, 255, 255))

# Downsample to 512 x 512 with LANCZOS for pristine quality
final_icon = img.resize((512, 512), Image.Resampling.LANCZOS)
final_icon.save("store_assets/ic_launcher_512.png", "PNG", optimize=True)
print("Saved store_assets/ic_launcher_512.png: size 512x512")
