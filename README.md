# SLC-Lovers · 情侣专属 APP

> 一个只为两个人的私密空间，记录爱与时光。

![App Icon](design/icon-1024.png)

## 项目定位

SLC-Lovers 是一款**专属两人**的私密 APP。它不是社交软件、没有陌生人、没有信息流——只有你和你最爱的人。所有功能都为「两个人的关系」而设计：从纪念日倒计时到时光胶囊，从心情日记到位置共享，每一个细节都在说：**这段关系，值得被认真对待**。

设计灵感来自图标：黑色剪影与金色剪影相拥，"S&LC" 字母优雅交融——轻奢、克制、温度感。

---

## 功能模块

| 模块 | 核心能力 |
|------|---------|
| 🏠 **首页** | 在一起天数、纪念日倒计时、每日寄语、最近动态 |
| 💬 **私密聊天** | 双向聊天，支持文字/语音/小纸条/表情，气泡按发送方区分黑金 |
| 🖼️ **共享相册** | 共同上传的照片墙，时间线布局，本地缓存 + 缩略图 |
| 📔 **心情日记** | 每日心情记录，待办清单，想做的事 Bucket List |
| 📍 **想见你** | 位置共享，距离计算，预计见面时间 |
| ⏳ **时光胶囊** | 写给未来的信，封存到指定日期才能打开 |
| 💌 **留言板** | 个人爱好展示，互相留言贴纸，互问互答 |

---

## 技术架构

### 双端原生实现
- **iOS**：SwiftUI + Combine + SwiftData（iOS 17+）
- **Android**：Kotlin + Jetpack Compose + Material 3 + Room

### 数据策略（原型阶段）
- 所有数据**本地存储**，无需注册即可使用
- **配对码**机制模拟双端连接：两端输入相同 6 位配对码即视为已绑定
- 真实落地时，配对码可升级为端到端加密的 P2P/WebSocket 通道

### 设计系统
```
背景主色  #F5F1E8  米色 / 奶油色
文字主色  #1A1A1A  墨黑（他）
文字次色  #C9A961  玫瑰金（她）
辅助灰    #8B8680  高级灰
强调色    #B8956A  暗金
```
字体：**Didot + SF Pro Display**（iOS）/ **Noto Serif + Roboto**（Android）

---

## 目录结构

```
SLC-Lovers/
├── README.md                ← 你正在读
├── docs/                    ← 架构与设计文档
│   ├── ARCHITECTURE.md
│   ├── DESIGN-TOKENS.md
│   └── DATA-MODEL.md
├── design/                  ← 图标与设计资源
├── iOS/                     ← SwiftUI 完整源码
│   └── SLC-Lovers/
│       ├── Models/          ← 数据模型
│       ├── Views/           ← 视图（按模块）
│       ├── ViewModels/      ← 状态管理
│       ├── Services/        ← 业务逻辑
│       ├── Theme/           ← 设计令牌
│       └── Utils/           ← 工具方法
└── Android/                 ← Compose 完整源码
    └── app/src/main/java/com/slclovers/app/
        ├── data/            ← 数据层（Room + Repository）
        └── ui/              ← 界面层
```

---

## 如何运行

### iOS
```bash
cd iOS
open SLC-Lovers.xcodeproj
# 选择真机或模拟器，⌘R 运行
# 要求：Xcode 15+ / iOS 17+
```

### Android
```bash
cd Android
./gradlew assembleDebug
# 或在 Android Studio 中打开 /Android 目录
# 要求：Android Studio Hedgehog+ / minSdk 26
```

---

## 设计理念

1. **克制**：少即是多。每一个像素都该承载情感，而非填充。
2. **温度**：黑金配色 + 米色基底，让界面有纸张般的高级感。
3. **对称**：他为墨黑，她为玫瑰金。两个色调相互呼应，对话般平衡。
4. **持久**：本地优先，不依赖网络。所有记录永远属于你们。

---

**Built with 💛 for two people in love.**