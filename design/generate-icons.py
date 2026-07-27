#!/usr/bin/env python
"""
生成 SLC-Lovers 多分辨率图标
- Android: 5 个 mipmap 密度
- iOS: AppStore 1024x1024
- 预览: 256x256
"""
from PIL import Image, ImageDraw, ImageFilter
from pathlib import Path

SRC = Path("C:/Users/12558/WorkBuddy/2026-07-27-20-50-32/SLC-Lovers/design/icon-source.jpg")
OUT_BASE = Path("C:/Users/12558/WorkBuddy/2026-07-27-20-50-32/SLC-Lovers")

# Android 各分辨率
ANDROID_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# 打开源图
src = Image.open(SRC).convert("RGB")
print(f"源图尺寸: {src.size}")

# 给图标加圆角（iOS 风格）
def add_rounded_corners(img: Image.Image, radius: int) -> Image.Image:
    """给图片添加圆角蒙版"""
    mask = Image.new("L", img.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), img.size], radius=radius, fill=255)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    out.paste(img, mask=mask)
    return out

# 生成 Android 各分辨率图标
android_dir = OUT_BASE / "Android/app/src/main/res"
for folder, size in ANDROID_SIZES.items():
    out_dir = android_dir / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    # 缩放到目标尺寸
    resized = src.resize((size, size), Image.LANCZOS)
    # Android 不需要圆角（自适应图标由前景背景组成），但保留方形即可
    resized.save(out_dir / "ic_launcher.png", "PNG", optimize=True)
    print(f"  ✓ {folder}/ic_launcher.png ({size}x{size})")

# 生成 Android 自适应图标前景（保留完整图）
foreground_dir = android_dir / "mipmap-anydpi-v26"
foreground_dir.mkdir(parents=True, exist_ok=True)
# 前景图需要 108x108dp（外框 72x72 安全区），这里生成 432x432
fg_size = 432
fg = src.resize((fg_size, fg_size), Image.LANCZOS)
fg.save(foreground_dir / "ic_launcher_foreground.png", "PNG", optimize=True)
print(f"  ✓ mipmap-anydpi-v26/ic_launcher_foreground.png ({fg_size}x{fg_size})")

# 生成 iOS AppIcon
ios_dir = OUT_BASE / "iOS/SLC-Lovers/Resources/Assets.xcassets/AppIcon.appiconset"
ios_dir.mkdir(parents=True, exist_ok=True)
ios_1024 = src.resize((1024, 1024), Image.LANCZOS)
ios_1024.save(ios_dir / "icon-1024.png", "PNG", optimize=True)
print(f"  ✓ iOS AppIcon (1024x1024)")

# 生成 iOS 圆角预览（用于文档展示）
preview = add_rounded_corners(src.resize((512, 512), Image.LANCZOS), 100)
preview.save(OUT_BASE / "design/icon-preview-512.png", "PNG", optimize=True)

# 生成预览图（图标实际显示效果，120x120）
preview_120 = add_rounded_corners(src.resize((120, 120), Image.LANCZOS), 24)
preview_120.save(OUT_BASE / "design/icon-preview-120.png", "PNG", optimize=True)

# 生成 256 预览
preview_256 = add_rounded_corners(src.resize((256, 256), Image.LANCZOS), 50)
preview_256.save(OUT_BASE / "design/icon-256.png", "PNG", optimize=True)

# 保存一份到 README 同级供 README 引用
src.resize((128, 128), Image.LANCZOS).save(OUT_BASE / "design/icon-128.png", "PNG", optimize=True)

print("\n✅ 所有图标资源生成完毕")
print(f"   源图: {SRC}")
print(f"   输出: {OUT_BASE}")