import Foundation

/// 纪念日
struct Anniversary: Identifiable, Codable, Hashable {
    let id: UUID
    var title: String
    var date: Date
    var isRecurring: Bool
    var icon: String
    var note: String?
    var createdAt: Date

    init(
        id: UUID = UUID(),
        title: String,
        date: Date,
        isRecurring: Bool = true,
        icon: String = "💕",
        note: String? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.date = date
        self.isRecurring = isRecurring
        self.icon = icon
        self.note = note
        self.createdAt = createdAt
    }

    /// 计算距离下一个纪念日的天数
    func daysUntilNext() -> Int {
        let calendar = Calendar.current
        let now = calendar.startOfDay(for: Date())
        var target = calendar.startOfDay(for: date)
        if isRecurring {
            // 取今年或明年的同月同日
            let currentYear = calendar.component(.year, from: now)
            target = calendar.date(from: DateComponents(
                year: currentYear,
                month: calendar.component(.month, from: date),
                day: calendar.component(.day, from: date)
            )) ?? date
            if target < now {
                target = calendar.date(byAdding: .year, value: 1, to: target) ?? target
            }
        }
        return calendar.dateComponents([.day], from: now, to: target).day ?? 0
    }
}

/// 位置记录
struct LocationRecord: Identifiable, Codable, Hashable {
    let id: UUID
    var userId: UUID
    var latitude: Double
    var longitude: Double
    var address: String?
    var batteryLevel: Float
    var isSharing: Bool
    var updatedAt: Date

    init(
        id: UUID = UUID(),
        userId: UUID,
        latitude: Double,
        longitude: Double,
        address: String? = nil,
        batteryLevel: Float = 1.0,
        isSharing: Bool = true,
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.userId = userId
        self.latitude = latitude
        self.longitude = longitude
        self.address = address
        self.batteryLevel = batteryLevel
        self.isSharing = isSharing
        self.updatedAt = updatedAt
    }
}

/// 时光胶囊
struct TimeCapsule: Identifiable, Codable, Hashable {
    let id: UUID
    var authorId: UUID
    var title: String
    var content: String
    var mediaData: Data?
    var unlockDate: Date
    var isUnlocked: Bool
    var unlockedAt: Date?
    var createdAt: Date

    init(
        id: UUID = UUID(),
        authorId: UUID,
        title: String,
        content: String,
        mediaData: Data? = nil,
        unlockDate: Date,
        isUnlocked: Bool = false,
        unlockedAt: Date? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.authorId = authorId
        self.title = title
        self.content = content
        self.mediaData = mediaData
        self.unlockDate = unlockDate
        self.isUnlocked = isUnlocked
        self.unlockedAt = unlockedAt
        self.createdAt = createdAt
    }

    /// 距离解封还剩多久
    func timeRemaining() -> TimeInterval {
        max(0, unlockDate.timeIntervalSinceNow)
    }

    /// 是否已到期可解封
    func canUnlock() -> Bool {
        Date() >= unlockDate && !isUnlocked
    }
}

/// 留言纸颜色
enum NoteColor: String, Codable, CaseIterable {
    case cream, gold, black, blush, sage

    var hex: String {
        switch self {
        case .cream: return "#FAF7F0"
        case .gold: return "#E8D4A0"
        case .black: return "#2C2826"
        case .blush: return "#F0D5C8"
        case .sage: return "#C8D4C0"
        }
    }
}

/// 留言板留言
struct StickyNote: Identifiable, Codable, Hashable {
    let id: UUID
    var authorId: UUID
    var content: String
    var color: NoteColor
    var rotation: Double
    var positionX: Double
    var positionY: Double
    var createdAt: Date

    init(
        id: UUID = UUID(),
        authorId: UUID,
        content: String,
        color: NoteColor = .cream,
        rotation: Double = 0,
        positionX: Double = 0.5,
        positionY: Double = 0.5,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.authorId = authorId
        self.content = content
        self.color = color
        self.rotation = rotation
        self.positionX = positionX
        self.positionY = positionY
        self.createdAt = createdAt
    }
}

/// 个人爱好
struct Hobby: Identifiable, Codable, Hashable {
    let id: UUID
    var userId: UUID
    var title: String
    var subtitle: String?
    var emoji: String
    var createdAt: Date

    init(
        id: UUID = UUID(),
        userId: UUID,
        title: String,
        subtitle: String? = nil,
        emoji: String = "✨",
        createdAt: Date = Date()
    ) {
        self.id = id
        self.userId = userId
        self.title = title
        self.subtitle = subtitle
        self.emoji = emoji
        self.createdAt = createdAt
    }
}

/// 每日寄语
struct Quote: Identifiable, Codable, Hashable {
    let id: UUID
    var content: String
    var author: String?
    var date: Date

    init(
        id: UUID = UUID(),
        content: String,
        author: String? = nil,
        date: Date = Date()
    ) {
        self.id = id
        self.content = content
        self.author = author
        self.date = date
    }
}