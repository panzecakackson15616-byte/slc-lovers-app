# iOS 端运行说明

## 方式 A：使用 XcodeGen（推荐）

```bash
# 1. 安装 XcodeGen（如果还没装）
brew install xcodegen

# 2. 生成 Xcode 工程
cd iOS
xcodegen

# 3. 打开工程
open SLC-Lovers.xcodeproj

# 4. 选择模拟器或真机，⌘R 运行
```

## 方式 B：手动创建工程

1. 打开 Xcode 15+，选择 **File > New > Project > iOS > App**
2. 填写：
   - Product Name: `SLC-Lovers`
   - Interface: **SwiftUI**
   - Language: **Swift**
   - Bundle Identifier: `com.slclovers.app`
3. 把本目录的 `SLC-Lovers/` 文件夹**拖入**工程（勾选 "Create groups"）
4. 在项目设置中替换默认的 `Info.plist` 为本目录的 `Info.plist`
5. 把 `Resources/Assets.xcassets` 设为 App 的资源目录
6. ⌘R 运行

## 工程结构

```
SLC-Lovers/
├── App.swift                  ← 应用入口（@main）
├── Info.plist                 ← 权限声明
├── Models/                    ← 数据模型
│   ├── User.swift
│   ├── Message.swift
│   ├── Diary.swift
│   └── Lifecycle.swift
├── Services/
│   ├── AppState.swift         ← 全局状态（核心）
│   └── LocalStore.swift       ← 本地持久化
├── Views/
│   ├── RootView.swift         ← 根视图
│   ├── MainTabView.swift      ← Tab 栏
│   ├── Pairing/PairingView.swift    ← 配对流程
│   ├── Home/HomeView.swift          ← 首页
│   ├── Chat/ChatView.swift          ← 聊天
│   ├── Gallery/GalleryView.swift    ← 相册
│   ├── Diary/DiaryView.swift        ← 日记/待办/愿望
│   ├── Location/LocationView.swift  ← 想见你
│   ├── Capsule/CapsuleView.swift    ← 时光胶囊
│   ├── Board/BoardView.swift        ← 留言板
│   ├── More/MoreView.swift          ← 更多
│   ├── Settings/SettingsView.swift  ← 设置
│   └── Components/SLCComponents.swift
├── Theme/
│   ├── Colors.swift
│   ├── Typography.swift
│   └── Spacing.swift
├── Utils/
│   ├── DateUtils.swift
│   └── Haptics.swift
└── Resources/
    └── Assets.xcassets/
        ├── AppIcon.appiconset/
        ├── AccentColor.colorset/
        └── Cream.colorset/
```

## 设计亮点

- **MVVM 架构**：所有 UI 状态都在 `AppState`（@MainActor ObservableObject）
- **配色系统**：从 `SLCColor` 引用，禁止硬编码
- **本地存储**：JSON 文件 + UserDefaults（原型阶段），生产可换 SwiftData
- **角色系统**：`.him` / `.her` 两种角色，颜色不同
- **配对码**：6 位数字连接，模拟双端

## 模拟数据

首次启动会自动注入演示数据（在一起 128 天、4 条愿望、3 个待办、7 条消息、4 张留言）。
要清空：进入 设置 > 解除配对。