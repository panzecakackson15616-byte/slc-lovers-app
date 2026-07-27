# 架构文档

## 整体架构

SLC-Lovers 采用**分层 + MVVM** 架构：

```
┌─────────────────────────────────────┐
│           View (SwiftUI / Compose)   │  ← 声明式 UI
├─────────────────────────────────────┤
│         ViewModel (StateFlow)        │  ← UI 状态机
├─────────────────────────────────────┤
│     Repository (协议 + 实现)         │  ← 业务编排
├─────────────────────────────────────┤
│   Local DB (SwiftData / Room)        │  ← 数据持久化
└─────────────────────────────────────┘
```

## 模块拆分

```
SLC-Lovers
├── Pairing（配对模块）
│   └─ 配对码生成、输入、状态管理
├── Home（首页）
│   └─ 在一起天数 / 纪念日 / 每日寄语
├── Chat（聊天）
│   └─ 消息列表 / 输入 / 气泡组件
├── Gallery（相册）
│   └─ 网格 / 时间线 / 上传
├── Diary（日记）
│   └─ 心情 / 待办 / 愿望清单
├── Location（位置）
│   └─ 地图 / 距离 / 状态
├── Capsule（时光胶囊）
│   └─ 创建 / 列表 / 解封动画
└── Board（留言板）
    └─ 个人爱好 / 留言贴纸
```

## 数据流

以"发送一条聊天消息"为例：

```
用户输入 → ChatView.onSend()
   ↓
ChatViewModel.sendMessage(text)
   ↓ (Dispatchers.IO / Task)
ChatRepository.addMessage(msg)
   ↓
SwiftData/Room 持久化
   ↓
@Published / StateFlow 触发 UI 更新
```

## 关键设计决策

### 1. 本地优先（Local-First）
- **默认所有数据存本地**，不依赖网络
- 离线也能完整使用
- 同步作为可选增强

### 2. 配对码连接
- 一端生成 6 位数字配对码
- 另一端输入相同码确认连接
- 配对信息存本地 `UserDefaults` / `SharedPreferences`
- 真实落地可升级为端到端加密协议（Signal Protocol）

### 3. 角色识别（他 / 她）
- 配对时选定角色：`him` / `her`
- 整个 APP 的颜色系统围绕这两个角色构建
- 聊天气泡按角色着色，他墨黑，她玫瑰金

### 4. 时间不变量
- 所有时间存为 `Date`（UTC）
- 显示时按本地时区
- 「在一起天数」每天凌晨自动刷新（`TimelineView` / `WorkManager`）

## 导航结构

```
RootView
├── 未配对 → PairingView
└── 已配对 → MainTabView
    ├── HomeView（首页）
    ├── ChatView（聊天）
    ├── GalleryView（相册）
    ├── DiaryView（日记）
    └── MoreView（更多）
        ├── LocationView
        ├── CapsuleView
        ├── BoardView
        └── SettingsView
```

## iOS 端技术选型

| 组件 | 选择 | 理由 |
|------|------|------|
| UI | SwiftUI | 声明式，动画流畅 |
| 状态 | @StateObject + @Published | 简单清晰 |
| 数据 | SwiftData | 现代化，类型安全 |
| 图片 | PhotosUI + 异步加载 | 系统原生 |
| 位置 | CoreLocation | 系统原生 |
| 通知 | UserNotifications | 系统原生 |

## Android 端技术选型

| 组件 | 选择 | 理由 |
|------|------|------|
| UI | Jetpack Compose | 现代化声明式 |
| 状态 | StateFlow + collectAsStateWithLifecycle | 生命周期安全 |
| 数据 | Room | 现代化 SQLite 封装 |
| 图片 | Coil | Compose 友好 |
| 位置 | FusedLocationProvider | 最佳实践 |
| 通知 | NotificationCompat | 兼容性好 |

## 后续可扩展

1. **同步层**：接入 CloudKit / Firebase，让两端数据真的同步
2. **端到端加密**：Signal Protocol 保护隐私
3. **Widget 桌面小部件**：展示在一起天数
4. **Watch 端**：快捷回复消息
5. **AR 回忆**：把照片摆到真实空间里