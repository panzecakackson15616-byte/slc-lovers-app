# 数据模型 (Data Model)

> 所有持久化实体的定义。SwiftData 与 Room 共享同一逻辑结构。

## 实体清单

### User（用户/情侣）
```swift
struct User {
    let id: UUID
    var name: String              // 昵称
    var role: UserRole            // him / her
    var avatarData: Data?         // 头像（可选）
    var pairingCode: String?      // 配对码（仅发起方）
    var partnerId: UUID?          // 对方 ID
    var createdAt: Date
}

enum UserRole {
    case him   // 墨黑
    case her   // 玫瑰金
}
```

### Pairing（配对关系）
```swift
struct Pairing {
    let id: UUID
    var code: String              // 6 位配对码
    var status: PairingStatus     // pending / connected
    var hisUserId: UUID
    var herUserId: UUID?
    var startDate: Date           // 在一起日期
    var createdAt: Date
}

enum PairingStatus {
    case pending     // 等待对方输入
    case connected   // 已配对
}
```

### Message（聊天消息）
```swift
struct Message {
    let id: UUID
    var senderId: UUID
    var receiverId: UUID
    var content: String           // 文字内容
    var type: MessageType         // text / voice / image / paper
    var mediaURL: String?         // 媒体路径
    var duration: TimeInterval?   // 语音时长
    var isRead: Bool
    var createdAt: Date
}

enum MessageType {
    case text
    case voice
    case image
    case paper  // 小纸条（富文本）
}
```

### Photo（相册照片）
```swift
struct Photo {
    let id: UUID
    var uploaderId: UUID
    var imageData: Data           // 原图
    var thumbnailData: Data       // 缩略图
    var caption: String?
    var location: String?
    var takenAt: Date             // 拍摄时间
    var createdAt: Date
}
```

### Diary（日记条目）
```swift
struct DiaryEntry {
    let id: UUID
    var authorId: UUID
    var mood: Mood                // 心情
    var title: String?
    var content: String           // Markdown
    var images: [Data]            // 附图
    var weather: String?
    var createdAt: Date
}

enum Mood: String {
    case happy, peaceful, missing, excited, sad, angry
}
```

### TodoItem（待办）
```swift
struct TodoItem {
    let id: UUID
    var title: String
    var note: String?
    var assignee: UserRole?       // 谁负责
    var dueDate: Date?
    var isCompleted: Bool
    var completedBy: UUID?
    var createdAt: Date
}
```

### BucketItem（愿望清单）
```swift
struct BucketItem {
    let id: UUID
    var title: String
    var description: String?
    var coverImage: Data?
    var targetDate: Date?
    var isAchieved: Bool
    var achievedAt: Date?
    var createdAt: Date
}
```

### Anniversary（纪念日）
```swift
struct Anniversary {
    let id: UUID
    var title: String             // "第一次约会"
    var date: Date
    var isRecurring: Bool         // 是否每年提醒
    var icon: String              // emoji 或图标名
    var note: String?
    var createdAt: Date
}
```

### Location（位置记录）
```swift
struct LocationRecord {
    let id: UUID
    var userId: UUID
    var latitude: Double
    var longitude: Double
    var address: String?          // 反地理编码
    var batteryLevel: Float
    var isSharing: Bool           // 是否在共享
    var updatedAt: Date
}
```

### Capsule（时光胶囊）
```swift
struct TimeCapsule {
    let id: UUID
    var authorId: UUID
    var title: String
    var content: String
    var mediaData: Data?
    var unlockDate: Date          // 解封时间
    var isUnlocked: Bool
    var unlockedAt: Date?
    var createdAt: Date
}
```

### Note（留言板留言）
```swift
struct StickyNote {
    let id: UUID
    var authorId: UUID
    var content: String
    var color: NoteColor          // 5 种留言纸颜色
    var rotation: Double          // 旋转角度 ±3°
    var positionX: Double
    var positionY: Double
    var createdAt: Date
}

enum NoteColor: String {
    case cream, gold, black, blush, sage
}
```

### Hobby（个人爱好展示）
```swift
struct Hobby {
    let id: UUID
    var userId: UUID
    var title: String             // "咖啡"
    var subtitle: String?         // "喜欢奶咖，少糖"
    var emoji: String
    var createdAt: Date
}
```

### Quote（每日寄语）
```swift
struct Quote {
    let id: UUID
    var content: String
    var author: String?
    var date: Date                // 当天日期
}
```

## 关系图

```
Pairing (1) ─── (2) User
   │
   ├── Message (sender → receiver)
   ├── Photo (uploader)
   ├── DiaryEntry (author)
   ├── LocationRecord (user)
   ├── Capsule (author)
   ├── StickyNote (author)
   ├── Hobby (user)
   │
   └── 共享集合
       ├── Anniversary
       ├── TodoItem
       └── BucketItem
```

## 本地存储策略

### iOS (SwiftData)
```swift
@Model class User { ... }
@Model class Pairing { ... }
// 每个实体对应一张表
```

### Android (Room)
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    ...
)
```

类型映射：
- `UUID` ↔ `String`
- `Date` ↔ `Long`（毫秒时间戳）
- `Data` ↔ `ByteArray` ↔ `BLOB`
- `enum` ↔ `String`（用 `TypeConverter`）

## 数据迁移

后续版本迭代采用：
- **iOS**：SwiftData 自动迁移 + 版本化 `Schema`
- **Android**：Room `Migration` 显式声明