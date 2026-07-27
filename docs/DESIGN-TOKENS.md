# 设计令牌 (Design Tokens)

> SLC-Lovers 的视觉语言凝结在这一组令牌里。改这里，全局联动。

## 1. 色彩系统

### 1.1 主色板

| 名称 | Hex | 用途 | 情感标签 |
|------|-----|------|---------|
| `bg.cream` | `#F5F1E8` | 页面背景 | 温润纸张 |
| `bg.creamLight` | `#FAF7F0` | 卡片背景 | 轻柔奶油 |
| `bg.creamDeep` | `#EDE6D5` | 分割区域 | 牛皮纸 |

### 1.2 角色色（两人主题色）

| 名称 | Hex | 含义 | 视觉 |
|------|-----|------|------|
| `person.him` | `#1A1A1A` | 他 | 墨黑，沉稳 |
| `person.himSoft` | `#3A3A3A` | 他的辅助 | 深灰 |
| `person.her` | `#C9A961` | 她 | 玫瑰金，温婉 |
| `person.herDeep` | `#B8956A` | 她的强调 | 暗金 |
| `person.herSoft` | `#E8D4A0` | 她的辅助 | 浅金 |

### 1.3 文字色

| 名称 | Hex | 用途 |
|------|-----|------|
| `text.primary` | `#2C2826` | 标题、正文 |
| `text.secondary` | `#6B6560` | 次要文字、说明 |
| `text.tertiary` | `#9C958E` | 占位、禁用 |
| `text.onDark` | `#F5F1E8` | 深色背景上的文字 |

### 1.4 语义色

| 名称 | Hex | 用途 |
|------|-----|------|
| `semantic.success` | `#7A9B6E` | 已完成、心情好 |
| `semantic.warning` | `#D4A574` | 即将到期的纪念日 |
| `semantic.danger` | `#C46B5A` | 删除、清空 |
| `semantic.info` | `#8FA4B5` | 提示 |

---

## 2. 字体系统

### iOS
- **Display（标题、纪念日数字）**：`Didot` → 系统衬线
- **Body（正文）**：`SF Pro Display`
- **Caption（说明）**：`SF Pro Text`

### Android
- **Display**：`Noto Serif` → 系统衬线
- **Body**：`Roboto`
- **Caption**：`Roboto Light`

### 字号阶梯（pt/sp）

| Token | iOS | Android | 用途 |
|-------|-----|---------|------|
| `display.large` | 48 | 48sp | 纪念日大数字 |
| `display.medium` | 36 | 36sp | 卡片标题 |
| `title.large` | 28 | 28sp | 页面标题 |
| `title.medium` | 22 | 22sp | 模块标题 |
| `title.small` | 18 | 18sp | 卡片标题 |
| `body.large` | 17 | 16sp | 正文 |
| `body.medium` | 15 | 14sp | 副文 |
| `body.small` | 13 | 12sp | 说明 |
| `caption` | 11 | 11sp | 标签、时间戳 |

### 字重
- `regular` (400)：正文
- `medium` (500)：次级标题
- `semibold` (600)：标题
- `bold` (700)：强调数字

---

## 3. 间距系统（8dp Grid）

| Token | 值 | 用途 |
|-------|-----|------|
| `space.xs` | 4 | 图标内边距 |
| `space.sm` | 8 | 紧凑元素间距 |
| `space.md` | 16 | 默认内边距 |
| `space.lg` | 24 | 章节间距 |
| `space.xl` | 32 | 大段间距 |
| `space.xxl` | 48 | 页面边距 |

---

## 4. 圆角系统

| Token | 值 | 用途 |
|-------|-----|------|
| `radius.sm` | 8 | 标签、小按钮 |
| `radius.md` | 12 | 输入框 |
| `radius.lg` | 16 | 卡片 |
| `radius.xl` | 20 | 大卡片 |
| `radius.xxl` | 24 | 模态框 |
| `radius.full` | 999 | 头像、气泡 |

---

## 5. 阴影

```css
/* iOS */
shadow.soft: 0 2 8 rgba(26, 26, 26, 0.06)
shadow.medium: 0 4 16 rgba(26, 26, 26, 0.08)
shadow.deep: 0 8 24 rgba(26, 26, 26, 0.12)

/* Android */
elevation.1: 2dp  /* 卡片 */
elevation.2: 4dp  /* 浮层 */
elevation.3: 8dp  /* 模态 */
```

---

## 6. 动效曲线

| Token | 曲线 | 用途 |
|-------|------|------|
| `motion.entrance` | `easeOut` 300ms | 页面进入 |
| `motion.exit` | `easeIn` 200ms | 页面退出 |
| `motion.spring` | `spring(response: 0.4, damping: 0.7)` | 弹跳反馈 |
| `motion.fade` | `easeInOut` 250ms | 状态切换 |

---

## 7. 图标系统

- **线性图标**：1.5pt 描边，圆角端点
- **填充图标**：用于选中态
- **尺寸**：16 / 20 / 24 / 32
- **来源**：自定义 SVG，统一向 `person.him`/`person.her` 注入颜色