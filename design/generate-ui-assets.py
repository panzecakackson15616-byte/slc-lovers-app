#!/usr/bin/env python
"""
生成 UI 占位资源：
- 默认头像（他 / 她）
- 示例相册图片
- 心情图标背景
"""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path
import math

OUT = Path("C:/Users/12558/WorkBuddy/2026-07-27-20-50-32/SLC-Lovers/design/ui-assets")
OUT.mkdir(parents=True, exist_ok=True)

# 配色（取自设计令牌）
CREAM = (245, 241, 232)
CREAM_LIGHT = (250, 247, 240)
HIM = (26, 26, 26)
HER = (201, 169, 97)
HER_DEEP = (184, 149, 106)
HER_SOFT = (232, 212, 160)
TEXT_PRIMARY = (44, 40, 38)
TEXT_SECONDARY = (107, 101, 96)

# 字体（优先使用系统字体）
def get_font(size):
    candidates = [
        "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/msyh.ttc",
    ]
    for p in candidates:
        try:
            return ImageFont.truetype(p, size)
        except:
            pass
    return ImageFont.load_default()

def get_serif_font(size):
    candidates = [
        "C:/Windows/Fonts/Georgia.ttf",
        "C:/Windows/Fonts/times.ttf",
        "C:/Windows/Fonts/cambria.ttc",
    ]
    for p in candidates:
        try:
            return ImageFont.truetype(p, size)
        except:
            pass
    return get_font(size)

# ============ 默认头像 ============
def make_avatar(name, color, size=400):
    img = Image.new("RGB", (size, size), CREAM_LIGHT)
    draw = ImageDraw.Draw(img)
    # 圆环背景
    draw.ellipse([10, 10, size-10, size-10], fill=color)
    # 首字母
    f = get_serif_font(size // 2)
    bbox = draw.textbbox((0, 0), name, font=f)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    draw.text(((size-w)/2, (size-h)/2 - size*0.05), name,
              fill=CREAM if color != CREAM_LIGHT else HIM, font=f)
    return img

make_avatar("S", HIM, 400).save(OUT / "avatar-him.png", "PNG")
make_avatar("L", HER_DEEP, 400).save(OUT / "avatar-her.png", "PNG")
print(f"  ✓ 头像 (他/她)")

# ============ 示例相册图片 ============
def make_photo(text, bg_color, size=800):
    """生成一张带文字和装饰的示例照片"""
    img = Image.new("RGB", (size, size), bg_color)
    draw = ImageDraw.Draw(img)
    # 渐变叠加
    for y in range(size):
        alpha = int(20 * (y / size))
        draw.line([(0, y), (size, y)], fill=(0, 0, 0, alpha))
    # 装饰圆形
    draw.ellipse([size*0.3, size*0.3, size*0.7, size*0.7],
                 fill=(255, 255, 255, 30))
    # 文字
    f = get_serif_font(size // 8)
    bbox = draw.textbbox((0, 0), text, font=f)
    w = bbox[2] - bbox[0]
    draw.text(((size-w)/2, size*0.45), text, fill=(255, 255, 255, 220), font=f)
    return img

# 6 张示例相册图
photo_palettes = [
    ("Spring", (200, 220, 200)),
    ("Sunset", (240, 180, 130)),
    ("Ocean", (140, 180, 200)),
    ("Coffee", (160, 130, 100)),
    ("Night", (60, 70, 90)),
    ("Bloom", (220, 180, 190)),
]
for i, (name, color) in enumerate(photo_palettes, 1):
    img = make_photo(name, color)
    img.save(OUT / f"sample-photo-{i}.jpg", "JPEG", quality=85)
print(f"  ✓ {len(photo_palettes)} 张示例照片")

# ============ 心情图标（圆形+emoji占位） ============
def make_mood_bg(color, size=200):
    img = Image.new("RGB", (size, size), color)
    draw = ImageDraw.Draw(img)
    return img

moods = [
    ("happy", (220, 200, 150)),
    ("peaceful", (180, 210, 200)),
    ("missing", (210, 180, 200)),
    ("excited", (230, 180, 130)),
    ("sad", (170, 180, 200)),
    ("angry", (220, 150, 140)),
]
for name, color in moods:
    img = make_mood_bg(color)
    img.save(OUT / f"mood-{name}.png", "PNG")
print(f"  ✓ {len(moods)} 种心情背景")

# ============ 启动页背景 ============
def make_splash(width=1080, height=1920):
    img = Image.new("RGB", (width, height), CREAM)
    draw = ImageDraw.Draw(img)
    # 顶部装饰
    draw.line([(width*0.2, height*0.15), (width*0.8, height*0.15)],
              fill=HER_SOFT, width=2)
    # 文字
    f1 = get_serif_font(80)
    f2 = get_font(28)
    draw.text((width/2 - 60, height*0.4), "S & LC", fill=HIM, font=f1)
    draw.text((width/2 - 130, height*0.5), "Just for the two of us",
              fill=TEXT_SECONDARY, font=f2)
    return img

make_splash().save(OUT / "splash.png", "PNG")
print(f"  ✓ 启动页背景")

print("\n✅ UI 资源生成完毕")
print(f"   输出目录: {OUT}")