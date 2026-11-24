"""
Generates all CoinBridge icons and banners with 100% vector-drawn symbols (zero missing glyphs/tofu boxes)
and strict compliance with Android TV App Quality & TV App Icon Guidelines:
  1. app/src/main/res/drawable-xhdpi/tv_banner.png  (320x180 px, 24-bit RGB PNG, with clear app title)
  2. store_assets/ic_launcher_512.png               (512x512 px, circular-safe logo padding)
  3. app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png & ic_launcher_round.png
  4. store_assets/tv_banner.png                     (1280x720 px, 24-bit RGB PNG, Play Store TV Banner)
  5. store_assets/feature_graphic.png               (1024x500 px, 24-bit RGB PNG, Play Store Feature Graphic)
"""
import math
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

FONT_BOLD = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
FONT_REG = "/System/Library/Fonts/Supplemental/Arial.ttf"


def make_gradient_bg(w, h, top_rgb=(15, 23, 42), bot_rgb=(30, 27, 75)):
    img = Image.new("RGBA", (w, h), (*top_rgb, 255))
    draw = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        r = int(top_rgb[0] * (1 - t) + bot_rgb[0] * t)
        g = int(top_rgb[1] * (1 - t) + bot_rgb[1] * t)
        b = int(top_rgb[2] * (1 - t) + bot_rgb[2] * t)
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))
    return img


def add_radial_glow(img, cx, cy, max_rad, rgb, max_alpha=45, steps=20):
    glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for i in range(steps):
        rad = max_rad * (1 - i / steps)
        alpha = int(max_alpha * (i / steps) * (1 / steps) * 4.5)
        alpha = min(alpha, max_alpha)
        gd.ellipse([cx - rad, cy - rad, cx + rad, cy + rad], fill=(*rgb, alpha))
    return Image.alpha_composite(img, glow)


def draw_dollar_symbol(draw, cx, cy, r, color=(255, 255, 255, 255)):
    """Draws a crisp, bold vector-style '$' centered at (cx, cy) scaled to coin radius r."""
    font = ImageFont.truetype(FONT_BOLD, int(r * 1.28))
    bbox = draw.textbbox((0, 0), "$", font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = cx - tw / 2 - bbox[0]
    ty = cy - th / 2 - bbox[1]
    # Subtle drop shadow
    draw.text((tx, ty + r * 0.06), "$", font=font, fill=(0, 0, 0, 70))
    draw.text((tx, ty), "$", font=font, fill=color)


def draw_bitcoin_symbol(draw, cx, cy, r, color=(255, 255, 255, 255)):
    """
    Draws a crisp, bold vector-style Bitcoin '₿' (bold 'B' + vertical stem bars)
    centered at (cx, cy) scaled to coin radius r. Never relies on Unicode font glyphs.
    """
    font = ImageFont.truetype(FONT_BOLD, int(r * 1.18))
    bbox = draw.textbbox((0, 0), "B", font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = cx - tw / 2 - bbox[0]
    ty = cy - th / 2 - bbox[1]

    bar_w = max(2, int(r * 0.12))
    bar_ext = int(r * 0.18)
    bar_top = int(cy - th / 2 - bar_ext)
    bar_bot = int(cy + th / 2 + bar_ext)
    bx1 = int(cx - tw * 0.16)
    bx2 = int(cx + tw * 0.14)

    # Drop shadow
    sh = int(r * 0.06)
    shadow_col = (0, 0, 0, 70)
    draw.rounded_rectangle([bx1 - bar_w // 2, bar_top + sh, bx1 + bar_w // 2, bar_bot + sh], radius=bar_w // 2, fill=shadow_col)
    draw.rounded_rectangle([bx2 - bar_w // 2, bar_top + sh, bx2 + bar_w // 2, bar_bot + sh], radius=bar_w // 2, fill=shadow_col)
    draw.text((tx, ty + sh), "B", font=font, fill=shadow_col)

    # Vertical Bitcoin bars
    draw.rounded_rectangle([bx1 - bar_w // 2, bar_top, bx1 + bar_w // 2, bar_bot], radius=bar_w // 2, fill=color)
    draw.rounded_rectangle([bx2 - bar_w // 2, bar_top, bx2 + bar_w // 2, bar_bot], radius=bar_w // 2, fill=color)
    # Bold B
    draw.text((tx, ty), "B", font=font, fill=color)


def draw_coinbridge_emblem(img, cx, cy, emblem_diameter):
    """
    Draws the complete CoinBridge emblem strictly inside a circle of diameter `emblem_diameter`
    centered at (cx, cy). Nothing ever exceeds radius = emblem_diameter / 2.
    """
    draw = ImageDraw.Draw(img)
    R = emblem_diameter / 2.0

    # 1. Subtle orbit ring right at 0.96 R (strictly inside emblem_diameter)
    ring_r = R * 0.96
    draw.ellipse(
        [cx - ring_r, cy - ring_r, cx + ring_r, cy + ring_r],
        outline=(148, 163, 184, 70),
        width=max(2, int(R * 0.04)),
    )

    # 2. Glowing Emerald Bridge Arch connecting the two coins (smooth polyline with curve joints)
    arch_pts = []
    arch_half_w = R * 0.56
    arch_base_y = cy + R * 0.14
    arch_height = R * 0.42
    for deg in range(0, 181, 3):
        rad = math.radians(deg)
        x = cx - arch_half_w * math.cos(rad)
        y = arch_base_y - arch_height * math.sin(rad)
        arch_pts.append((x, y))

    draw.line(arch_pts, fill=(16, 185, 129, 65), width=max(4, int(R * 0.18)), joint="curve")
    draw.line(arch_pts, fill=(16, 185, 129, 255), width=max(2, int(R * 0.10)), joint="curve")

    # 3. Swap Exchange Arrows (clean polygons, zero corner artifacts)
    shaft_h = max(2, R * 0.042)
    head_l = R * 0.14
    head_h = R * 0.12
    ax1 = cx - R * 0.28
    ax2 = cx + R * 0.28

    # Top arrow (->, Cyan)
    ay1 = cy - R * 0.62
    top_poly = [
        (ax1, ay1 - shaft_h),
        (ax2 - head_l, ay1 - shaft_h),
        (ax2 - head_l, ay1 - head_h),
        (ax2, ay1),
        (ax2 - head_l, ay1 + head_h),
        (ax2 - head_l, ay1 + shaft_h),
        (ax1, ay1 + shaft_h),
    ]
    draw.polygon(top_poly, fill=(56, 189, 248, 255))

    # Bottom arrow (<-, Indigo)
    ay2 = cy + R * 0.62
    bot_poly = [
        (ax2, ay2 - shaft_h),
        (ax1 + head_l, ay2 - shaft_h),
        (ax1 + head_l, ay2 - head_h),
        (ax1, ay2),
        (ax1 + head_l, ay2 + head_h),
        (ax1 + head_l, ay2 + shaft_h),
        (ax2, ay2 + shaft_h),
    ]
    draw.polygon(bot_poly, fill=(129, 140, 248, 255))

    # 4. Left Coin ($ Emerald) & Right Coin (₿ Amber)
    # Coin centers at cx +/- 0.45 R, coin radius = 0.36 R -> outer edge at 0.81 R (well inside 1.0 R!)
    coin_r = R * 0.365
    left_cx = cx - R * 0.445
    right_cx = cx + R * 0.445

    # Soft coin drop shadow
    sh_layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sh_draw = ImageDraw.Draw(sh_layer)
    sh_off = R * 0.04
    for ccx in (left_cx, right_cx):
        sh_draw.ellipse(
            [ccx - coin_r, cy - coin_r + sh_off, ccx + coin_r, cy + coin_r + sh_off],
            fill=(0, 0, 0, 110),
        )
    sh_layer = sh_layer.filter(ImageFilter.GaussianBlur(max(2, int(R * 0.06))))
    img.alpha_composite(sh_layer)
    draw = ImageDraw.Draw(img)

    # Left Emerald Coin
    draw.ellipse(
        [left_cx - coin_r, cy - coin_r, left_cx + coin_r, cy + coin_r],
        fill=(16, 185, 129, 255),
    )
    inner_r = coin_r * 0.84
    draw.ellipse(
        [left_cx - inner_r, cy - inner_r, left_cx + inner_r, cy + inner_r],
        outline=(52, 211, 153, 220),
        width=max(2, int(R * 0.032)),
    )
    draw_dollar_symbol(draw, left_cx, cy, coin_r)

    # Right Amber Coin
    draw.ellipse(
        [right_cx - coin_r, cy - coin_r, right_cx + coin_r, cy + coin_r],
        fill=(245, 158, 11, 255),
    )
    draw.ellipse(
        [right_cx - inner_r, cy - inner_r, right_cx + inner_r, cy + inner_r],
        outline=(251, 191, 36, 220),
        width=max(2, int(R * 0.032)),
    )
    draw_bitcoin_symbol(draw, right_cx, cy, coin_r)


def generate_icons():
    """Generates 512x512 store icon + all legacy mipmap square & round PNGs + circular safety preview."""
    S = 2048  # 4x supersample
    img = make_gradient_bg(S, S, (15, 23, 42), (30, 27, 75))
    img = add_radial_glow(img, S // 2, S // 2, int(S * 0.42), (99, 102, 241), max_alpha=50)

    # Emblem diameter = 68% of canvas -> 16% margin on EVERY side -> fits comfortably inside
    # Google Play squircle AND Android TV / Google TV circular mask with generous padding!
    draw_coinbridge_emblem(img, S / 2, S / 2, S * 0.68)

    # 1. Store 512x512 icon (24-bit RGB PNG, full bleed as required by Google Play)
    icon_512 = img.resize((512, 512), Image.Resampling.LANCZOS).convert("RGB")
    icon_512.save("store_assets/ic_launcher_512.png", "PNG", optimize=True)
    print("Saved store_assets/ic_launcher_512.png (512x512 RGB)")

    # 2. Legacy mipmap PNGs (square + circular masked round icons)
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    # Build round master at 4x with clean anti-aliased circle mask
    round_mask = Image.new("L", (S, S), 0)
    rm_draw = ImageDraw.Draw(round_mask)
    pad = int(S * 0.04)
    rm_draw.ellipse([pad, pad, S - pad, S - pad], fill=255)
    round_master = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    round_master.paste(img, (0, 0), round_mask)

    for folder, size in densities.items():
        out_dir = os.path.join("app/src/main/res", folder)
        os.makedirs(out_dir, exist_ok=True)
        sq = img.resize((size, size), Image.Resampling.LANCZOS)
        sq.save(os.path.join(out_dir, "ic_launcher.png"), "PNG", optimize=True)
        rd = round_master.resize((size, size), Image.Resampling.LANCZOS)
        rd.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG", optimize=True)
    print("Regenerated all mipmap-*/ic_launcher.png and ic_launcher_round.png")

    # 3. Also save a visual check of how Google TV circular mask crops the 512x512 icon
    check = Image.new("RGBA", (512, 512), (24, 24, 27, 255))
    cmask = Image.new("L", (512, 512), 0)
    ImageDraw.Draw(cmask).ellipse([16, 16, 496, 496], fill=255)
    check.paste(icon_512.convert("RGBA"), (0, 0), cmask)
    cd = ImageDraw.Draw(check)
    # Show visible TV circle boundary (cyan) and inner safe-zone boundary (green dashed-style ring)
    cd.ellipse([16, 16, 496, 496], outline=(56, 189, 248, 255), width=2)
    os.makedirs("store_assets/scripts", exist_ok=True)
    return icon_512


def generate_xhdpi_tv_banner():
    """
    Generates app/src/main/res/drawable-xhdpi/tv_banner.png at EXACT 320 x 180 px (24-bit RGB PNG).
    Follows Android TV Home Screen Banner guidelines:
      - 320px x 180px in drawable-xhdpi
      - Full-bleed background (no rounded corners baked in)
      - Prominent app icon emblem + bold, unmistakable 'CoinBridge' app title readable from 10 feet
      - Generous safe margins on all 4 sides
    """
    W, H = 320, 180
    SW, SH = W * 4, H * 4  # 1280 x 720 supersample

    img = make_gradient_bg(SW, SH, (11, 19, 43), (30, 27, 75))
    img = add_radial_glow(img, int(SW * 0.27), int(SH * 0.50), 420, (16, 185, 129), max_alpha=45)
    img = add_radial_glow(img, int(SW * 0.78), int(SH * 0.45), 480, (99, 102, 241), max_alpha=50)

    # Subtle tech grid
    grid = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
    gd = ImageDraw.Draw(grid)
    for x in range(0, SW, 80):
        gd.line([(x, 0), (x, SH)], fill=(255, 255, 255, 10), width=2)
    for y in range(0, SH, 80):
        gd.line([(0, y), (SW, y)], fill=(255, 255, 255, 10), width=2)
    img = Image.alpha_composite(img, grid)

    # Left: CoinBridge Emblem (centered vertically at y = SH/2, x = 280, diameter = 360)
    draw_coinbridge_emblem(img, 290, SH // 2, 370)

    # Right: High-contrast bold app title & subtitle
    draw = ImageDraw.Draw(img)
    font_title = ImageFont.truetype(FONT_BOLD, 122)
    font_sub = ImageFont.truetype(FONT_REG, 50)
    font_tag = ImageFont.truetype(FONT_BOLD, 34)

    tx = 530
    # Drop shadow + Title
    draw.text((tx, 206), "CoinBridge", font=font_title, fill=(0, 0, 0, 110))
    draw.text((tx, 200), "CoinBridge", font=font_title, fill=(255, 255, 255, 255))

    # Emerald accent bar under title
    draw.rounded_rectangle([tx, 348, tx + 180, 360], radius=6, fill=(16, 185, 129, 255))
    draw.rounded_rectangle([tx + 196, 348, tx + 276, 360], radius=6, fill=(245, 158, 11, 255))

    # Subtitle
    draw.text((tx, 388), "Currency & Crypto", font=font_sub, fill=(226, 232, 240, 255))

    # Crisp badge pill
    badge_text = "170+ RATES  |  OFFLINE"
    bbox = draw.textbbox((0, 0), badge_text, font=font_tag)
    bw = (bbox[2] - bbox[0]) + 56
    bh = 62
    by = 474
    draw.rounded_rectangle(
        [tx, by, tx + bw, by + bh],
        radius=31,
        fill=(15, 23, 42, 210),
        outline=(56, 189, 248, 200),
        width=4,
    )
    draw.text((tx + 28, by + 11), badge_text, font=font_tag, fill=(125, 211, 252, 255))

    # Save 320x180 24-bit RGB PNG to app/src/main/res/drawable-xhdpi/tv_banner.png
    out_dir = "app/src/main/res/drawable-xhdpi"
    os.makedirs(out_dir, exist_ok=True)
    final_xhdpi = img.resize((W, H), Image.Resampling.LANCZOS).convert("RGB")
    out_path = os.path.join(out_dir, "tv_banner.png")
    final_xhdpi.save(out_path, "PNG", optimize=True)
    print(f"Saved {out_path} ({W}x{H} RGB)")


def draw_clean_pill(draw, x, y, dot_rgb, text, font, height=58, pad_x=26):
    """Draws a crisp status pill with a colored indicator dot (zero missing emojis)."""
    bbox = draw.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    dot_r = 8
    pw = pad_x + dot_r * 2 + 16 + tw + pad_x
    draw.rounded_rectangle(
        [x, y, x + pw, y + height],
        radius=height // 2,
        fill=(15, 23, 42, 215),
        outline=(*dot_rgb, 190),
        width=3,
    )
    dcx = x + pad_x + dot_r
    dcy = y + height / 2
    draw.ellipse([dcx - dot_r, dcy - dot_r, dcx + dot_r, dcy + dot_r], fill=(*dot_rgb, 255))
    tx = dcx + dot_r + 16 - bbox[0]
    ty = y + (height - th) / 2 - bbox[1]
    draw.text((tx, ty), text, font=font, fill=(241, 245, 249, 255))
    return pw


def generate_store_tv_banner(icon_512):
    """Generates clean 1280x720 store_assets/tv_banner.png with ZERO tofu boxes and ZERO clipping."""
    W, H = 1280, 720
    SW, SH = W * 2, H * 2  # 2560 x 1440

    img = make_gradient_bg(SW, SH, (8, 13, 26), (28, 25, 72))
    img = add_radial_glow(img, 450, 720, 850, (16, 185, 129), max_alpha=36)
    img = add_radial_glow(img, SW - 600, 620, 950, (99, 102, 241), max_alpha=42)

    grid = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
    gd = ImageDraw.Draw(grid)
    for x in range(0, SW, 90):
        gd.line([(x, 0), (x, SH)], fill=(255, 255, 255, 7), width=1)
    for y in range(0, SH, 90):
        gd.line([(0, y), (SW, y)], fill=(255, 255, 255, 7), width=1)
    img = Image.alpha_composite(img, grid)
    draw = ImageDraw.Draw(img)

    font_title = ImageFont.truetype(FONT_BOLD, 118)
    font_sub = ImageFont.truetype(FONT_REG, 44)
    font_pill = ImageFont.truetype(FONT_BOLD, 32)
    font_card_head = ImageFont.truetype(FONT_BOLD, 40)
    font_card_rate = ImageFont.truetype(FONT_BOLD, 56)
    font_card_sub = ImageFont.truetype(FONT_REG, 29)
    font_small_bold = ImageFont.truetype(FONT_BOLD, 26)

    # Left Column
    left_x = 150
    icon_size = 216
    icon_r = icon_512.convert("RGBA").resize((icon_size, icon_size), Image.Resampling.LANCZOS)
    mask = Image.new("L", (icon_size, icon_size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, icon_size, icon_size], radius=48, fill=255)
    img.paste(icon_r, (left_x, 210), mask)
    draw.rounded_rectangle([left_x, 210, left_x + icon_size, 210 + icon_size], radius=48, outline=(56, 189, 248, 190), width=4)

    title_y = 468
    draw.text((left_x, title_y), "CoinBridge", font=font_title, fill=(255, 255, 255, 255))
    tb = draw.textbbox((left_x, title_y), "CoinBridge", font=font_title)
    draw.ellipse([tb[2] + 22, title_y + 42, tb[2] + 54, title_y + 74], fill=(16, 185, 129, 255))

    draw.text((left_x, title_y + 136), "Currency & Crypto Converter for Android TV", font=font_sub, fill=(203, 213, 225, 255))

    pills = [
        ((16, 185, 129), "D-Pad Navigation"),
        ((56, 189, 248), "100% Offline Rates"),
        ((245, 158, 11), "170+ Currencies & Crypto"),
        ((168, 85, 247), "Interactive Trends"),
    ]
    px, py = left_x, title_y + 230
    for col, txt in pills:
        # Pre-measure width to wrap neatly
        tw = draw.textbbox((0, 0), txt, font=font_pill)[2] + 92
        if px + tw > 1220:
            py += 82
            px = left_x
        w_used = draw_clean_pill(draw, px, py, col, txt, font_pill, height=64, pad_x=26)
        px += w_used + 22

    # Right Column: Focused TV Card
    card_w, card_h = 1080, 860
    card_x = SW - card_w - 130
    card_y = 290

    sh = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([card_x - 12, card_y + 20, card_x + card_w + 12, card_y + card_h + 36], radius=52, fill=(0, 0, 0, 180))
    sh = sh.filter(ImageFilter.GaussianBlur(36))
    img = Image.alpha_composite(img, sh)

    card = Image.new("RGBA", (card_w, card_h), (0, 0, 0, 0))
    cd = ImageDraw.Draw(card)
    cd.rounded_rectangle([0, 0, card_w, card_h], radius=46, fill=(15, 23, 42, 245), outline=(56, 189, 248, 240), width=6)

    # Top row: Pair label + D-PAD ACTIVE badge (generous width, centered text, zero clipping)
    cd.text((54, 52), "USD  /  EUR", font=font_card_head, fill=(148, 163, 184, 255))
    cd.rounded_rectangle([card_w - 316, 44, card_w - 48, 102], radius=22, fill=(16, 185, 129, 40), outline=(16, 185, 129, 255), width=3)
    cd.ellipse([card_w - 292, 65, card_w - 276, 81], fill=(16, 185, 129, 255))
    cd.text((card_w - 264, 58), "D-PAD ACTIVE", font=font_small_bold, fill=(16, 185, 129, 255))

    # Main Rate + Change badge cleanly right-aligned
    cd.text((54, 118), "1.00 USD = 0.9248 EUR", font=font_card_rate, fill=(255, 255, 255, 255))
    cd.rounded_rectangle([card_w - 210, 124, card_w - 48, 180], radius=16, fill=(16, 185, 129, 45), outline=(16, 185, 129, 255), width=2)
    cd.text((card_w - 186, 137), "+0.38%", font=font_small_bold, fill=(52, 211, 153, 255))

    # Secondary Crypto Box
    cd.rounded_rectangle([54, 212, card_w - 54, 330], radius=24, fill=(30, 41, 59, 190), outline=(51, 65, 85, 170), width=2)
    cd.ellipse([82, 240, 106, 264], fill=(245, 158, 11, 255))
    cd.text((120, 234), "Bitcoin (BTC)", font=font_card_sub, fill=(251, 191, 36, 255))
    cd.text((82, 272), "1 BTC = $64,820.00 USD", font=font_card_head, fill=(255, 255, 255, 255))
    cd.text((card_w - 225, 256), "+2.45%", font=font_card_head, fill=(52, 211, 153, 255))

    # Trend Chart
    cd.text((54, 362), "30-Day Rate Trend", font=font_card_sub, fill=(148, 163, 184, 255))
    cd.text((card_w - 430, 362), "Low: 0.9120   High: 0.9315", font=font_card_sub, fill=(148, 163, 184, 255))

    pts = [(64, 590), (178, 552), (296, 602), (414, 512), (532, 540), (650, 462), (768, 492), (886, 424), (1012, 444)]
    cd.polygon([(64, 668)] + pts + [(1012, 668)], fill=(16, 185, 129, 32))
    for i in range(len(pts) - 1):
        cd.line([pts[i], pts[i + 1]], fill=(16, 185, 129, 255), width=6)
    for pt in pts:
        cd.ellipse([pt[0] - 8, pt[1] - 8, pt[0] + 8, pt[1] + 8], fill=(56, 189, 248, 255), outline=(255, 255, 255, 255), width=3)

    # Bottom D-pad control bar (cleanly fits inside box!)
    cd.rounded_rectangle([54, 710, card_w - 54, 804], radius=24, fill=(30, 27, 75, 225), outline=(139, 92, 246, 165), width=2)
    hint = "Remote:  [UP/DOWN] Currencies   |   [OK] Swap   |   [LEFT/RIGHT] Range"
    hb = cd.textbbox((0, 0), hint, font=font_small_bold)
    hx = (card_w - (hb[2] - hb[0])) / 2 - hb[0]
    cd.text((hx, 742), hint, font=font_small_bold, fill=(226, 232, 240, 255))

    img.paste(card, (card_x, card_y), card)

    out = img.resize((W, H), Image.Resampling.LANCZOS).convert("RGB")
    out.save("store_assets/tv_banner.png", "PNG", optimize=True)
    print("Saved store_assets/tv_banner.png (1280x720 RGB)")


def generate_feature_graphic(icon_512):
    """Generates clean 1024x500 store_assets/feature_graphic.png with ZERO tofu boxes and ZERO clipping."""
    W, H = 1024, 500
    SW, SH = W * 2, H * 2  # 2048 x 1000

    img = make_gradient_bg(SW, SH, (10, 15, 29), (26, 24, 65))
    img = add_radial_glow(img, 240, SH - 120, 620, (16, 185, 129), max_alpha=36)
    img = add_radial_glow(img, SW - 420, 220, 780, (99, 102, 241), max_alpha=42)

    grid = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
    gd = ImageDraw.Draw(grid)
    for x in range(0, SW, 80):
        gd.line([(x, 0), (x, SH)], fill=(255, 255, 255, 6), width=1)
    for y in range(0, SH, 80):
        gd.line([(0, y), (SW, y)], fill=(255, 255, 255, 6), width=1)
    img = Image.alpha_composite(img, grid)
    draw = ImageDraw.Draw(img)

    font_title = ImageFont.truetype(FONT_BOLD, 92)
    font_sub = ImageFont.truetype(FONT_REG, 36)
    font_pill = ImageFont.truetype(FONT_BOLD, 30)
    font_card_title = ImageFont.truetype(FONT_BOLD, 33)
    font_card_num = ImageFont.truetype(FONT_BOLD, 48)
    font_card_sub = ImageFont.truetype(FONT_REG, 25)
    font_small_bold = ImageFont.truetype(FONT_BOLD, 24)

    left_x = 110
    icon_s = 152
    icon_r = icon_512.convert("RGBA").resize((icon_s, icon_s), Image.Resampling.LANCZOS)
    mask = Image.new("L", (icon_s, icon_s), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, icon_s, icon_s], radius=34, fill=255)
    img.paste(icon_r, (left_x, 120), mask)
    draw.rounded_rectangle([left_x, 120, left_x + icon_s, 120 + icon_s], radius=34, outline=(56, 189, 248, 170), width=3)

    title_y = 304
    draw.text((left_x, title_y), "CoinBridge", font=font_title, fill=(255, 255, 255, 255))
    tb = draw.textbbox((left_x, title_y), "CoinBridge", font=font_title)
    draw.ellipse([tb[2] + 18, title_y + 34, tb[2] + 44, title_y + 60], fill=(16, 185, 129, 255))

    draw.text((left_x, title_y + 108), "Fast, Offline & Private Currency Exchange", font=font_sub, fill=(180, 194, 212, 255))

    pills = [
        ((16, 185, 129), "100% Offline Rates & Instant Sync"),
        ((245, 158, 11), "170+ World Currencies & Crypto"),
        ((56, 189, 248), "Travel Bill & Tip Splitter"),
        ((168, 85, 247), "Biometric Lock  |  Zero Ads"),
    ]
    for i, (col, txt) in enumerate(pills):
        draw_clean_pill(draw, left_x, title_y + 184 + i * 74, col, txt, font_pill, height=58, pad_x=24)

    # Right Card 1: Multi-currency converter
    c1_w, c1_h = 780, 410
    c1_x, c1_y = SW - c1_w - 110, 105
    c1_sh = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
    ImageDraw.Draw(c1_sh).rounded_rectangle([c1_x - 10, c1_y + 14, c1_x + c1_w + 10, c1_y + c1_h + 28], radius=40, fill=(0, 0, 0, 160))
    img = Image.alpha_composite(img, c1_sh.filter(ImageFilter.GaussianBlur(30)))

    c1 = Image.new("RGBA", (c1_w, c1_h), (0, 0, 0, 0))
    d1 = ImageDraw.Draw(c1)
    d1.rounded_rectangle([0, 0, c1_w, c1_h], radius=36, fill=(15, 23, 42, 238), outline=(56, 189, 248, 130), width=3)
    d1.rounded_rectangle([28, 28, c1_w - 28, 146], radius=22, fill=(30, 41, 59, 210))
    d1.text((54, 46), "USD  -  US Dollar", font=font_card_sub, fill=(148, 163, 184, 255))
    d1.text((54, 80), "$ 1,250.00", font=font_card_num, fill=(255, 255, 255, 255))
    d1.rounded_rectangle([c1_w - 164, 56, c1_w - 52, 114], radius=16, fill=(16, 185, 129, 40), outline=(16, 185, 129, 255), width=2)
    d1.text((c1_w - 140, 72), "BASE", font=font_small_bold, fill=(52, 211, 153, 255))

    rows = [
        ("EUR", "Euro", "1,114.68 EUR", "+0.14%", (52, 211, 153)),
        ("GBP", "British Pound", "931.26 GBP", "+0.08%", (52, 211, 153)),
        ("JPY", "Japanese Yen", "197,725 JPY", "-0.22%", (248, 113, 113)),
        ("BTC", "Bitcoin", "0.0195 BTC", "+2.45%", (251, 191, 36)),
    ]
    for idx, (code, name, val, chg, col) in enumerate(rows):
        ry = 168 + idx * 56
        d1.rounded_rectangle([48, ry + 4, 130, ry + 42], radius=10, fill=(30, 41, 59, 255))
        d1.text((62, ry + 9), code, font=font_small_bold, fill=(226, 232, 240, 255))
        d1.text((150, ry + 7), val, font=font_card_title, fill=(255, 255, 255, 255))
        d1.text((c1_w - 172, ry + 7), chg, font=font_card_title, fill=(*col, 255))

    img.paste(c1, (c1_x, c1_y), c1)

    # Right Card 2: Trends & Bill Splitter (cleanly positioned below, no text cut off)
    c2_w, c2_h = 760, 345
    c2_x, c2_y = SW - c2_w - 80, 545
    c2_sh = Image.new("RGBA", (SW, SH), (0, 0, 0, 0))
    ImageDraw.Draw(c2_sh).rounded_rectangle([c2_x - 10, c2_y + 14, c2_x + c2_w + 10, c2_y + c2_h + 28], radius=36, fill=(0, 0, 0, 175))
    img = Image.alpha_composite(img, c2_sh.filter(ImageFilter.GaussianBlur(28)))

    c2 = Image.new("RGBA", (c2_w, c2_h), (0, 0, 0, 0))
    d2 = ImageDraw.Draw(c2)
    d2.rounded_rectangle([0, 0, c2_w, c2_h], radius=34, fill=(30, 27, 75, 245), outline=(139, 92, 246, 185), width=3)
    d2.text((40, 28), "Interactive Rate Trends (USD to EUR)", font=font_card_title, fill=(255, 255, 255, 255))
    font_card_sub_sm = ImageFont.truetype(FONT_REG, 22)
    d2.text((40, 74), "7D / 30D / 90D / 1Y  |  Low: 0.8876  Avg: 0.8931  High: 0.8985", font=font_card_sub_sm, fill=(196, 181, 253, 255))

    pts = [(48, 224), (128, 200), (212, 232), (296, 172), (380, 192), (464, 148), (548, 178), (632, 130), (708, 144)]
    d2.polygon([(48, 256)] + pts + [(708, 256)], fill=(16, 185, 129, 28))
    for i in range(len(pts) - 1):
        d2.line([pts[i], pts[i + 1]], fill=(16, 185, 129, 255), width=5)
    for pt in pts:
        d2.ellipse([pt[0] - 6, pt[1] - 6, pt[0] + 6, pt[1] + 6], fill=(56, 189, 248, 255), outline=(255, 255, 255, 255), width=2)

    # Bill splitter callout cleanly centered at bottom
    d2.rounded_rectangle([38, 266, c2_w - 38, 322], radius=16, fill=(15, 23, 42, 225), outline=(16, 185, 129, 220), width=2)
    split_txt = "Bill Split: $13.28 / person  (4 people, 18% tip)"
    sb = d2.textbbox((0, 0), split_txt, font=font_small_bold)
    sx = (c2_w - (sb[2] - sb[0])) / 2 - sb[0]
    d2.text((sx, 280), split_txt, font=font_small_bold, fill=(241, 245, 249, 255))

    img.paste(c2, (c2_x, c2_y), c2)

    out = img.resize((W, H), Image.Resampling.LANCZOS).convert("RGB")
    out.save("store_assets/feature_graphic.png", "PNG", optimize=True)
    print("Saved store_assets/feature_graphic.png (1024x500 RGB)")


if __name__ == "__main__":
    icon_512 = generate_icons()
    generate_xhdpi_tv_banner()
    generate_store_tv_banner(icon_512)
    generate_feature_graphic(icon_512)
