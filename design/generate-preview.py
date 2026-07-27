#!/usr/bin/env python
"""生成 SLC-Lovers 高保真设计预览页"""
import base64
from pathlib import Path
from PIL import Image
import io

OUT = Path("C:/Users/12558/WorkBuddy/2026-07-27-20-50-32/SLC-Lovers/design")
PREVIEW_DIR = OUT / "preview"

PREVIEW_DIR.mkdir(parents=True, exist_ok=True)

# 读取图标并转 base64
icon_path = OUT / "icon-source.jpg"
with open(icon_path, "rb") as f:
    icon_b64 = base64.b64encode(f.read()).decode()
icon_data_uri = f"data:image/jpeg;base64,{icon_b64}"

# 生成 1024 预览（圆角）
img = Image.open(icon_path).convert("RGB")
img_1024 = img.resize((1024, 1024), Image.LANCZOS)
buf = io.BytesIO()
img_1024.save(buf, "JPEG", quality=92)
icon_1024_b64 = base64.b64encode(buf.getvalue()).decode()
icon_1024_data_uri = f"data:image/jpeg;base64,{icon_1024_b64}"

# 生成 6 张示例照片（Base64）
def make_sample_photo(color, name):
    img = Image.new("RGB", (400, 600), color)
    return img

sample_palettes = [
    ((220, 200, 180), "Spring"),
    ((240, 200, 160), "Sunset"),
    ((180, 200, 200), "Ocean"),
    ((200, 170, 140), "Coffee"),
    ((100, 110, 130), "Night"),
    ((230, 200, 200), "Bloom"),
]
photo_uris = []
for color, name in sample_palettes:
    img = make_sample_photo(color, name)
    buf = io.BytesIO()
    img.save(buf, "JPEG", quality=85)
    photo_uris.append(f"data:image/jpeg;base64,{base64.b64encode(buf.getvalue()).decode()}")

HTML = f'''<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>SLC-Lovers · 高保真设计预览</title>
<style>
* {{ margin: 0; padding: 0; box-sizing: border-box; }}

:root {{
  --cream: #F5F1E8;
  --cream-light: #FAF7F0;
  --cream-deep: #EDE6D5;
  --him: #1A1A1A;
  --him-soft: #3A3A3A;
  --her: #C9A961;
  --her-deep: #B8956A;
  --her-soft: #E8D4A0;
  --text-primary: #2C2826;
  --text-secondary: #6B6560;
  --text-tertiary: #9C958E;
  --success: #7A9B6E;
  --warning: #D4A574;
  --danger: #C46B5A;
  --info: #8FA4B5;
}}

body {{
  background: var(--cream);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  color: var(--text-primary);
  line-height: 1.6;
  padding: 40px 24px;
  max-width: 1400px;
  margin: 0 auto;
}}

.serif {{
  font-family: "Didot", "Georgia", "Times New Roman", "Noto Serif SC", serif;
}}

/* ========== Header ========== */
.hero {{
  text-align: center;
  padding: 80px 24px;
  position: relative;
}}

.hero-icon {{
  width: 140px;
  height: 140px;
  margin: 0 auto 32px;
  border-radius: 30%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16px 48px rgba(26, 26, 26, 0.12);
  overflow: hidden;
}}

.hero-icon img {{
  width: 100%;
  height: 100%;
  object-fit: cover;
}}

.hero h1 {{
  font-family: "Didot", "Georgia", serif;
  font-size: 72px;
  font-weight: 300;
  letter-spacing: 2px;
  color: var(--him);
  margin-bottom: 16px;
}}

.hero .tagline {{
  font-size: 14px;
  letter-spacing: 4px;
  color: var(--text-secondary);
  text-transform: uppercase;
  margin-bottom: 32px;
}}

.hero .subtitle {{
  font-size: 22px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
}}

.hero .description {{
  font-size: 15px;
  color: var(--text-secondary);
  letter-spacing: 2px;
}}

.hero-divider {{
  width: 60px;
  height: 1px;
  background: var(--her-soft);
  margin: 32px auto;
}}

/* ========== Sections ========== */
.section {{
  margin: 80px 0;
}}

.section-title {{
  font-family: "Didot", "Georgia", serif;
  font-size: 36px;
  font-weight: 300;
  text-align: center;
  margin-bottom: 16px;
  color: var(--him);
}}

.section-subtitle {{
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 48px;
  font-size: 15px;
  letter-spacing: 1px;
}}

/* ========== Color Palette ========== */
.color-grid {{
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
  margin: 32px 0;
}}

.color-card {{
  background: var(--cream-light);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: transform 0.2s;
}}

.color-card:hover {{
  transform: translateY(-2px);
}}

.color-swatch {{
  height: 100px;
}}

.color-info {{
  padding: 12px 16px;
}}

.color-name {{
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
}}

.color-hex {{
  font-size: 11px;
  font-family: "SF Mono", Monaco, monospace;
  color: var(--text-secondary);
}}

/* ========== Phone Frames ========== */
.phones {{
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 40px;
  margin: 40px 0;
}}

.phone {{
  width: 320px;
  background: var(--him);
  border-radius: 48px;
  padding: 12px;
  box-shadow: 0 24px 60px rgba(0,0,0,0.18), 0 4px 12px rgba(0,0,0,0.08);
}}

.phone-screen {{
  background: var(--cream);
  border-radius: 36px;
  height: 640px;
  overflow: hidden;
  position: relative;
}}

.notch {{
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 24px;
  background: var(--him);
  border-radius: 0 0 16px 16px;
  z-index: 10;
}}

.status-bar {{
  height: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 600;
  color: var(--him);
}}

.status-icons {{
  display: flex;
  gap: 6px;
}}

.phone-content {{
  padding: 0 20px;
  height: calc(100% - 40px);
  overflow-y: auto;
}}

/* ========== Home Screen ========== */
.home-header {{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 24px;
}}

.greeting-text {{
  font-size: 13px;
  color: var(--text-secondary);
}}

.user-name {{
  font-family: "Didot", "Georgia", serif;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}}

.profile-circle {{
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--him);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cream);
  font-family: "Didot", serif;
  font-size: 18px;
}}

.together-card {{
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(180deg, var(--cream-light) 0%, var(--cream) 100%);
  border-radius: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}}

.together-label {{
  font-size: 14px;
  letter-spacing: 4px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}}

.together-number {{
  font-family: "Didot", "Georgia", serif;
  font-size: 96px;
  font-weight: 200;
  color: var(--him);
  line-height: 1;
}}

.together-unit {{
  font-size: 28px;
  font-weight: 300;
  color: var(--him);
  vertical-align: bottom;
}}

.together-date {{
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 8px;
}}

.card {{
  background: var(--cream-light);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}}

.card-row {{
  display: flex;
  align-items: center;
  gap: 12px;
}}

.card-icon {{
  font-size: 32px;
}}

.card-title {{
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}}

.card-subtitle {{
  font-size: 13px;
  color: var(--text-secondary);
}}

.section-header {{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  margin: 16px 4px 12px;
}}

.section-title-sm {{
  font-size: 18px;
  font-weight: 600;
}}

.section-action {{
  font-size: 14px;
  color: var(--her-deep);
}}

.todo-row {{
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--cream-light);
  border-radius: 12px;
  margin-bottom: 8px;
}}

.todo-circle {{
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid var(--text-tertiary);
}}

.todo-title {{
  flex: 1;
  font-size: 16px;
}}

.todo-badge {{
  font-size: 10px;
  background: var(--him);
  color: var(--cream);
  padding: 2px 8px;
  border-radius: 999px;
}}

/* ========== Chat Screen ========== */
.chat-header {{
  text-align: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--cream-deep);
}}

.chat-name {{
  font-size: 16px;
  font-weight: 600;
}}

.chat-status {{
  font-size: 11px;
  color: var(--success);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}}

.chat-status::before {{
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: var(--success);
  border-radius: 50%;
}}

.chat-messages {{
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}}

.message {{
  display: flex;
  gap: 8px;
  align-items: flex-end;
  max-width: 80%;
}}

.message.her {{
  align-self: flex-start;
}}

.message.him {{
  align-self: flex-end;
  flex-direction: row-reverse;
}}

.avatar {{
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cream);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}}

.avatar.her {{
  background: var(--her);
}}

.bubble {{
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 15px;
  max-width: 240px;
  line-height: 1.4;
}}

.bubble.her {{
  background: var(--cream-deep);
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
}}

.bubble.him {{
  background: var(--him);
  color: var(--cream);
  border-bottom-right-radius: 4px;
}}

.message-time {{
  font-size: 10px;
  color: var(--text-tertiary);
  margin-top: 4px;
}}

.chat-input {{
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--cream-light);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
}}

.chat-input-field {{
  flex: 1;
  background: var(--cream);
  padding: 8px 14px;
  border-radius: 20px;
  font-size: 15px;
  color: var(--text-secondary);
}}

.send-btn {{
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--him);
  color: var(--cream);
  display: flex;
  align-items: center;
  justify-content: center;
}}

/* ========== Photo Gallery ========== */
.photo-grid {{
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
  padding: 16px 4px;
}}

.photo-cell {{
  aspect-ratio: 1;
  border-radius: 4px;
  background-size: cover;
  background-position: center;
}}

/* ========== Capsule ========== */
.capsule-card {{
  background: var(--cream-light);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}}

.capsule-header {{
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}}

.capsule-icon {{
  font-size: 32px;
}}

.capsule-title {{
  font-size: 17px;
  font-weight: 600;
}}

.capsule-meta {{
  font-size: 12px;
  color: var(--text-secondary);
}}

.capsule-status {{
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: var(--her-soft);
  color: var(--her-deep);
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
}}

/* ========== Sticky Notes ========== */
.notes-grid {{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 8px;
}}

.note {{
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  min-height: 100px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transform: rotate(var(--rotate, 0deg));
}}

.note.cream {{ background: var(--cream); color: var(--text-primary); --rotate: -2deg; }}
.note.gold {{ background: var(--her-soft); color: var(--text-primary); --rotate: 1deg; }}
.note.blush {{ background: #F0D5C8; color: var(--text-primary); --rotate: 2deg; }}
.note.sage {{ background: #C8D4C0; color: var(--text-primary); --rotate: -1deg; }}

/* ========== Hobbies ========== */
.hobby-chip {{
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--cream-light);
  border-radius: 12px;
  margin: 4px;
  border: 1px solid rgba(0,0,0,0.05);
}}

.hobby-emoji {{
  font-size: 16px;
}}

.hobby-title {{
  font-size: 14px;
  font-weight: 500;
}}

.hobby-sub {{
  font-size: 10px;
  color: var(--text-secondary);
}}

/* ========== Bottom Tab ========== */
.tab-bar {{
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--cream-light);
  display: flex;
  justify-content: space-around;
  padding: 12px 0 24px;
  border-top: 1px solid var(--cream-deep);
}}

.tab {{
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-tertiary);
}}

.tab.active {{
  color: var(--him);
}}

.tab-icon {{
  font-size: 22px;
}}

.tab-dot {{
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: var(--him);
  border-radius: 50%;
}}

/* ========== Typography ========== */
.type-row {{
  display: grid;
  grid-template-columns: 100px 1fr 100px;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--cream-deep);
  align-items: center;
}}

.type-name {{
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
}}

.type-meta {{
  font-size: 12px;
  color: var(--text-tertiary);
  font-family: "SF Mono", monospace;
  text-align: right;
}}

.display-sample {{ font-family: "Didot", "Georgia", serif; font-size: 48px; font-weight: 300; color: var(--him); }}
.headline-sample {{ font-size: 28px; font-weight: 600; color: var(--text-primary); }}
.title-sample {{ font-size: 18px; font-weight: 600; }}
.body-sample {{ font-size: 16px; }}
.caption-sample {{ font-size: 12px; color: var(--text-secondary); }}

/* ========== Buttons ========== */
.btn-primary {{
  background: var(--him);
  color: var(--cream);
  padding: 16px 32px;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 600;
  display: inline-block;
  border: none;
  cursor: pointer;
}}

.btn-secondary {{
  background: var(--cream-light);
  color: var(--text-primary);
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  display: inline-block;
  border: 1px solid rgba(0,0,0,0.1);
}}

/* ========== Feature Grid ========== */
.feature-grid {{
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}}

.feature-card {{
  background: var(--cream-light);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}}

.feature-icon {{
  font-size: 36px;
  margin-bottom: 12px;
}}

.feature-title {{
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}}

.feature-desc {{
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}}

/* ========== Footer ========== */
.footer {{
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
  font-size: 14px;
}}

.footer-divider {{
  width: 40px;
  height: 1px;
  background: var(--her-soft);
  margin: 0 auto 24px;
}}

code {{
  background: var(--cream-deep);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: "SF Mono", Monaco, monospace;
  font-size: 13px;
}}

/* Responsive */
@media (max-width: 768px) {{
  .hero h1 {{ font-size: 48px; }}
  .section-title {{ font-size: 28px; }}
  .phone {{ transform: scale(0.9); }}
}}
</style>
</head>
<body>

<!-- HERO -->
<div class="hero">
  <div class="hero-icon">
    <img src="{icon_1024_data_uri}" alt="SLC-Lovers Icon">
  </div>
  <h1 class="serif">S & LC</h1>
  <div class="tagline">Just for the two of us</div>
  <div class="hero-divider"></div>
  <div class="subtitle">一个只属于两个人的私密空间</div>
  <div class="description">记录爱 · 守护时光 · 珍藏回忆</div>
</div>

<!-- FEATURES -->
<section class="section">
  <h2 class="section-title">七大功能模块</h2>
  <p class="section-subtitle">为两个人而设计的每一个细节</p>

  <div class="feature-grid">
    <div class="feature-card">
      <div class="feature-icon">🏠</div>
      <div class="feature-title">首页</div>
      <div class="feature-desc">在一起天数、纪念日、每日寄语</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">💬</div>
      <div class="feature-title">私密聊天</div>
      <div class="feature-desc">双向聊天，按角色区分气泡</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">🖼</div>
      <div class="feature-title">共享相册</div>
      <div class="feature-desc">共同上传的照片墙</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">📔</div>
      <div class="feature-title">心情日记</div>
      <div class="feature-desc">待办、愿望、心情记录</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">📍</div>
      <div class="feature-title">想见你</div>
      <div class="feature-desc">位置共享、距离计算</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">⏳</div>
      <div class="feature-title">时光胶囊</div>
      <div class="feature-desc">写给未来的信，到期才解封</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">💌</div>
      <div class="feature-title">留言板</div>
      <div class="feature-desc">小纸条 + 个人爱好</div>
    </div>
  </div>
</section>

<!-- COLOR PALETTE -->
<section class="section">
  <h2 class="section-title">设计令牌 · 色彩</h2>
  <p class="section-subtitle">黑金双色 + 米色基底，奠定轻奢基调</p>

  <h3 style="margin-top: 32px; font-size: 16px; color: var(--text-secondary); letter-spacing: 2px;">背景</h3>
  <div class="color-grid">
    <div class="color-card">
      <div class="color-swatch" style="background: var(--cream);"></div>
      <div class="color-info">
        <div class="color-name">cream</div>
        <div class="color-hex">#F5F1E8</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--cream-light);"></div>
      <div class="color-info">
        <div class="color-name">cream-light</div>
        <div class="color-hex">#FAF7F0</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--cream-deep);"></div>
      <div class="color-info">
        <div class="color-name">cream-deep</div>
        <div class="color-hex">#EDE6D5</div>
      </div>
    </div>
  </div>

  <h3 style="margin-top: 32px; font-size: 16px; color: var(--text-secondary); letter-spacing: 2px;">角色色</h3>
  <div class="color-grid">
    <div class="color-card">
      <div class="color-swatch" style="background: var(--him);"></div>
      <div class="color-info">
        <div class="color-name">him · 他</div>
        <div class="color-hex">#1A1A1A</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--him-soft);"></div>
      <div class="color-info">
        <div class="color-name">him-soft</div>
        <div class="color-hex">#3A3A3A</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--her);"></div>
      <div class="color-info">
        <div class="color-name">her · 她</div>
        <div class="color-hex">#C9A961</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--her-deep);"></div>
      <div class="color-info">
        <div class="color-name">her-deep</div>
        <div class="color-hex">#B8956A</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--her-soft);"></div>
      <div class="color-info">
        <div class="color-name">her-soft</div>
        <div class="color-hex">#E8D4A0</div>
      </div>
    </div>
  </div>

  <h3 style="margin-top: 32px; font-size: 16px; color: var(--text-secondary); letter-spacing: 2px;">语义</h3>
  <div class="color-grid">
    <div class="color-card">
      <div class="color-swatch" style="background: var(--success);"></div>
      <div class="color-info">
        <div class="color-name">success</div>
        <div class="color-hex">#7A9B6E</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--warning);"></div>
      <div class="color-info">
        <div class="color-name">warning</div>
        <div class="color-hex">#D4A574</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--danger);"></div>
      <div class="color-info">
        <div class="color-name">danger</div>
        <div class="color-hex">#C46B5A</div>
      </div>
    </div>
    <div class="color-card">
      <div class="color-swatch" style="background: var(--info);"></div>
      <div class="color-info">
        <div class="color-name">info</div>
        <div class="color-hex">#8FA4B5</div>
      </div>
    </div>
  </div>
</section>

<!-- TYPOGRAPHY -->
<section class="section">
  <h2 class="section-title">设计令牌 · 字体</h2>
  <p class="section-subtitle">衬线大标题 + 现代无衬线正文</p>

  <div style="background: var(--cream-light); border-radius: 16px; padding: 8px 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
    <div class="type-row">
      <div class="type-name">Display</div>
      <div class="display-sample serif">S &amp; LC</div>
      <div class="type-meta">48px / 300</div>
    </div>
    <div class="type-row">
      <div class="type-name">Headline</div>
      <div class="headline-sample">愿我们携手，从心动走到古稀</div>
      <div class="type-meta">28px / 600</div>
    </div>
    <div class="type-row">
      <div class="type-name">Title</div>
      <div class="title-sample">下一个纪念日</div>
      <div class="type-meta">18px / 600</div>
    </div>
    <div class="type-row">
      <div class="type-name">Body</div>
      <div class="body-sample">和你在一起的每一天，都是最好的时光。</div>
      <div class="type-meta">16px / 400</div>
    </div>
    <div class="type-row" style="border: none;">
      <div class="type-name">Caption</div>
      <div class="caption-sample">更新于 14:30</div>
      <div class="type-meta">12px / 400</div>
    </div>
  </div>
</section>

<!-- PHONE MOCKUPS -->
<section class="section">
  <h2 class="section-title">关键页面</h2>
  <p class="section-subtitle">双端原生 · iOS SwiftUI + Android Jetpack Compose</p>

  <div class="phones">

    <!-- Phone 1: Home -->
    <div class="phone">
      <div class="phone-screen">
        <div class="notch"></div>
        <div class="phone-content">
          <div class="home-header">
            <div>
              <div class="greeting-text">晚上好，</div>
              <div class="user-name">S</div>
            </div>
            <div class="profile-circle">S</div>
          </div>

          <div class="together-card">
            <div class="together-label">在一起</div>
            <div>
              <span class="together-number">128</span>
              <span class="together-unit">天</span>
            </div>
            <div class="together-date">2024 年 3 月 21 日</div>
          </div>

          <div class="card">
            <div class="card-row">
              <span style="color: var(--her-deep); opacity: 0.5; font-size: 24px;">"</span>
              <div class="card-title" style="font-weight: 400;">和你在一起的每一天，都是最好的时光。</div>
            </div>
          </div>

          <div class="card">
            <div class="card-row">
              <div class="card-icon">💕</div>
              <div style="flex: 1;">
                <div class="card-title">第一次见面</div>
                <div class="card-subtitle">还有 7 天</div>
              </div>
              <div style="color: var(--text-tertiary);">›</div>
            </div>
          </div>

          <div class="section-header">
            <div class="section-title-sm">待办</div>
            <div class="section-action">全部</div>
          </div>

          <div class="todo-row">
            <div class="todo-circle"></div>
            <div class="todo-title">周末去看展</div>
            <div class="todo-badge">她</div>
          </div>
          <div class="todo-row">
            <div class="todo-circle"></div>
            <div class="todo-title">买一束花</div>
            <div class="todo-badge">他</div>
          </div>
        </div>

        <div class="tab-bar">
          <div class="tab active">
            <div class="tab-icon">⌂</div>
            <div>首页</div>
            <div class="tab-dot"></div>
          </div>
          <div class="tab">
            <div class="tab-icon">💬</div>
            <div>聊天</div>
          </div>
          <div class="tab">
            <div class="tab-icon">📷</div>
            <div>相册</div>
          </div>
          <div class="tab">
            <div class="tab-icon">📖</div>
            <div>日记</div>
          </div>
          <div class="tab">
            <div class="tab-icon">⋯</div>
            <div>更多</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Phone 2: Chat -->
    <div class="phone">
      <div class="phone-screen">
        <div class="notch"></div>
        <div class="phone-content" style="padding: 0;">
          <div class="chat-header">
            <div class="chat-name">她</div>
            <div class="chat-status">在线</div>
          </div>

          <div class="chat-messages">
            <div class="message her">
              <div class="avatar her">她</div>
              <div>
                <div class="bubble her">下班了吗？</div>
                <div class="message-time" style="text-align: left;">14:25</div>
              </div>
            </div>

            <div class="message him">
              <div>
                <div class="bubble him">马上，路上买杯咖啡给你</div>
                <div class="message-time" style="text-align: right;">14:26</div>
              </div>
            </div>

            <div class="message her">
              <div class="avatar her">她</div>
              <div>
                <div class="bubble her">好嘞 ☕️ 顺便买点面包当早餐</div>
                <div class="message-time" style="text-align: left;">14:27</div>
              </div>
            </div>

            <div class="message him">
              <div>
                <div class="bubble him">OK，想吃什么口味？</div>
                <div class="message-time" style="text-align: right;">14:28</div>
              </div>
            </div>

            <div class="message her">
              <div class="avatar her">她</div>
              <div>
                <div class="bubble her">全麦的就好 🥐</div>
                <div class="message-time" style="text-align: left;">14:29</div>
              </div>
            </div>

            <div class="message him">
              <div>
                <div class="bubble him">收到～马上到家</div>
                <div class="message-time" style="text-align: right;">14:30</div>
              </div>
            </div>

            <div class="message her">
              <div class="avatar her">她</div>
              <div>
                <div class="bubble her">❤️</div>
                <div class="message-time" style="text-align: left;">14:31</div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <div class="chat-input-field">说点什么…</div>
          <div class="send-btn">↑</div>
        </div>
      </div>
    </div>

    <!-- Phone 3: Capsule -->
    <div class="phone">
      <div class="phone-screen">
        <div class="notch"></div>
        <div class="phone-content">
          <div class="home-header" style="padding: 16px 0;">
            <div style="font-size: 24px; font-weight: 600;">时光胶囊</div>
            <div style="width: 32px; height: 32px; border-radius: 50%; background: var(--him); color: var(--cream); display: flex; align-items: center; justify-content: center;">+</div>
          </div>

          <div class="capsule-card">
            <div class="capsule-header">
              <div class="capsule-icon">✉</div>
              <div style="flex: 1;">
                <div class="capsule-title">写给一年后的我们</div>
                <div class="capsule-meta">解封于 2025 年 7 月 27 日</div>
              </div>
            </div>
            <div class="capsule-status">
              <span style="font-size: 12px;">⏰</span>
              <span>237 天 4 小时</span>
            </div>
          </div>

          <div class="capsule-card">
            <div class="capsule-header">
              <div class="capsule-icon">📨</div>
              <div style="flex: 1;">
                <div class="capsule-title">2026 年的纪念</div>
                <div class="capsule-meta">解封于 2026 年 3 月 21 日</div>
              </div>
            </div>
            <div class="capsule-status">
              <span style="font-size: 12px;">⏰</span>
              <span>236 天</span>
            </div>
          </div>

          <div class="capsule-card">
            <div class="capsule-header">
              <div class="capsule-icon" style="color: var(--her-deep);">✉</div>
              <div style="flex: 1;">
                <div class="capsule-title">写给你的第一封信</div>
                <div class="capsule-meta">已解封</div>
              </div>
            </div>
            <div class="capsule-status" style="background: rgba(122,155,110,0.2); color: var(--success);">
              <span style="font-size: 12px;">✓</span>
              <span>已解封</span>
            </div>
          </div>

          <div style="text-align: center; color: var(--text-tertiary); font-size: 13px; margin-top: 40px; line-height: 1.8;">
            <div style="font-size: 64px;">🔒</div>
            <div style="margin-top: 12px; color: var(--text-secondary); font-size: 14px;">每封信都封存着此刻的心意</div>
            <div style="font-size: 12px;">等待那个约定的日子到来</div>
          </div>
        </div>

        <div class="tab-bar">
          <div class="tab">
            <div class="tab-icon">⌂</div>
            <div>首页</div>
          </div>
          <div class="tab">
            <div class="tab-icon">💬</div>
            <div>聊天</div>
          </div>
          <div class="tab">
            <div class="tab-icon">📷</div>
            <div>相册</div>
          </div>
          <div class="tab">
            <div class="tab-icon">📖</div>
            <div>日记</div>
          </div>
          <div class="tab active">
            <div class="tab-icon">⋯</div>
            <div>更多</div>
            <div class="tab-dot"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Phone 4: Board -->
    <div class="phone">
      <div class="phone-screen">
        <div class="notch"></div>
        <div class="phone-content">
          <div class="home-header" style="padding: 16px 0;">
            <div style="font-size: 24px; font-weight: 600;">我们</div>
            <div style="width: 32px; height: 32px; border-radius: 50%; background: var(--him); color: var(--cream); display: flex; align-items: center; justify-content: center;">+</div>
          </div>

          <div class="section-header" style="margin-top: 8px;">
            <div class="section-title-sm">他喜欢</div>
          </div>

          <div style="padding: 0 4px;">
            <div class="hobby-chip">
              <span class="hobby-emoji">☕️</span>
              <div>
                <div class="hobby-title">咖啡</div>
                <div class="hobby-sub">美式，少冰</div>
              </div>
            </div>
            <div class="hobby-chip">
              <span class="hobby-emoji">📷</span>
              <div>
                <div class="hobby-title">摄影</div>
                <div class="hobby-sub">胶片玩家</div>
              </div>
            </div>
            <div class="hobby-chip">
              <span class="hobby-emoji">🍰</span>
              <div>
                <div class="hobby-title">烘焙</div>
                <div class="hobby-sub">蛋糕面包</div>
              </div>
            </div>
            <div class="hobby-chip">
              <span class="hobby-emoji">✈️</span>
              <div>
                <div class="hobby-title">旅行</div>
                <div class="hobby-sub">已打卡 12 国</div>
              </div>
            </div>
          </div>

          <div class="section-header" style="margin-top: 16px;">
            <div class="section-title-sm">留言板</div>
          </div>

          <div class="notes-grid">
            <div class="note cream">记得早点睡 ❤️</div>
            <div class="note blush">爱你哟</div>
            <div class="note gold">今天辛苦了～</div>
            <div class="note sage">下次去那家咖啡店</div>
          </div>
        </div>

        <div class="tab-bar">
          <div class="tab active">
            <div class="tab-icon">⌂</div>
            <div>首页</div>
            <div class="tab-dot"></div>
          </div>
          <div class="tab">
            <div class="tab-icon">💬</div>
            <div>聊天</div>
          </div>
          <div class="tab">
            <div class="tab-icon">📷</div>
            <div>相册</div>
          </div>
          <div class="tab">
            <div class="tab-icon">📖</div>
            <div>日记</div>
          </div>
          <div class="tab">
            <div class="tab-icon">⋯</div>
            <div>更多</div>
          </div>
        </div>
      </div>
    </div>

  </div>
</section>

<!-- TECH STACK -->
<section class="section">
  <h2 class="section-title">技术架构</h2>
  <p class="section-subtitle">双原生 · 类型安全 · 离线优先</p>

  <div class="feature-grid">
    <div class="feature-card">
      <div class="feature-icon">🍎</div>
      <div class="feature-title">iOS · SwiftUI</div>
      <div class="feature-desc">Swift 5.9 + Combine + JSON 本地存储 + iOS 17+</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">🤖</div>
      <div class="feature-title">Android · Compose</div>
      <div class="feature-desc">Kotlin + Material 3 + Room + Coroutines + minSdk 26</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">💾</div>
      <div class="feature-title">本地优先</div>
      <div class="feature-desc">所有数据存本地，离线可用，无需注册</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">🔐</div>
      <div class="feature-title">配对码连接</div>
      <div class="feature-desc">6 位数字配对，模拟双端，可升级为 P2P</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">🎨</div>
      <div class="feature-title">统一设计系统</div>
      <div class="feature-desc">色板/字体/间距令牌化，双端同步</div>
    </div>
    <div class="feature-card">
      <div class="feature-icon">🚀</div>
      <div class="feature-title">演示数据</div>
      <div class="feature-desc">首次启动自动注入，立即看到所有功能</div>
    </div>
  </div>
</section>

<!-- PROJECT STRUCTURE -->
<section class="section">
  <h2 class="section-title">工程结构</h2>
  <p class="section-subtitle">一份完整可运行的源码</p>

  <div style="background: var(--cream-light); border-radius: 16px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); font-family: 'SF Mono', Monaco, monospace; font-size: 13px; line-height: 1.8; color: var(--text-secondary); white-space: pre-wrap;">SLC-Lovers/
├── README.md            ← 项目说明
├── docs/                ← 架构文档
│   ├── ARCHITECTURE.md
│   ├── DESIGN-TOKENS.md
│   └── DATA-MODEL.md
├── design/              ← 设计资源
│   ├── icon-source.jpg
│   ├── preview.html     ← 本预览页
│   └── ui-assets/
├── iOS/                 ← SwiftUI 完整源码
│   ├── README.md
│   ├── project.yml      ← XcodeGen 配置
│   └── SLC-Lovers/
│       ├── App.swift
│       ├── Info.plist
│       ├── Models/      (4 个文件)
│       ├── Views/       (12 个页面)
│       ├── Services/    (核心)
│       ├── Theme/       (设计令牌)
│       └── Utils/
└── Android/             ← Compose 完整源码
    ├── README.md
    ├── build.gradle.kts
    └── app/src/main/java/com/slclovers/app/
        ├── data/        (Room + Repository)
        └── ui/          (10 个页面)
</div>
</section>

<!-- CTA -->
<section class="section" style="text-align: center;">
  <h2 class="section-title">怎么运行？</h2>

  <div style="display: inline-block; text-align: left; background: var(--cream-light); border-radius: 16px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-top: 24px; max-width: 600px;">
    <h3 style="margin-bottom: 12px; font-size: 14px; letter-spacing: 2px; color: var(--text-secondary);">iOS</h3>
    <code style="display: block; padding: 12px; margin-bottom: 16px;">brew install xcodegen && cd iOS && xcodegen && open SLC-Lovers.xcodeproj</code>
    <h3 style="margin-bottom: 12px; font-size: 14px; letter-spacing: 2px; color: var(--text-secondary);">Android</h3>
    <code style="display: block; padding: 12px;">cd Android && ./gradlew assembleDebug</code>
  </div>
</section>

<div class="footer">
  <div class="footer-divider"></div>
  Built with 💛 for two people in love.<br>
  <span style="font-size: 12px; margin-top: 8px; display: inline-block;">© 2026 SLC-Lovers · 一个只属于两个人的 APP</span>
</div>

</body>
</html>'''

# 写入文件
out_path = PREVIEW_DIR / "index.html"
out_path.write_text(HTML, encoding="utf-8")
print(f"✅ 预览页生成完毕: {out_path}")
print(f"   大小: {len(HTML) / 1024:.1f} KB")