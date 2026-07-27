import Foundation

/// 相册照片
struct Photo: Identifiable, Codable, Hashable {
    let id: UUID
    var uploaderId: UUID
    var imageData: Data          // 原图
    var thumbnailData: Data      // 缩略图
    var caption: String?
    var location: String?
    var takenAt: Date
    var createdAt: Date

    init(
        id: UUID = UUID(),
        uploaderId: UUID,
        imageData: Data,
        thumbnailData: Data? = nil,
        caption: String? = nil,
        location: String? = nil,
        takenAt: Date = Date(),
        createdAt: Date = Date()
    ) {
        self.id = id
        self.uploaderId = uploaderId
        self.imageData = imageData
        self.thumbnailData = thumbnailData ?? imageData
        self.caption = caption
        self.location = location
        self.takenAt = takenAt
        self.createdAt = createdAt
    }
}

/// 心情
enum Mood: String, Codable, CaseIterable {
    case happy, peaceful, missing, excited, sad, angry

    var displayName: String {
        switch self {
        case .happy: return "开心"
        case .peaceful: return "平静"
        case .missing: return "想你"
        case .excited: return "激动"
        case .sad: return "难过"
        case .angry: return "生气"
        }
    }

    var emoji: String {
        switch self {
        case .happy: return "☀️"
        case .peaceful: return "🌿"
        case .missing: return "🌙"
        case .excited: return "✨"
        case .sad: return "🌧"
        case .angry: return "🔥"
        }
    }
}

/// 日记条目
struct DiaryEntry: Identifiable, Codable, Hashable {
    let id: UUID
    var authorId: UUID
    var mood: Mood
    var title: String?
    var content: String
    var weather: String?
    var createdAt: Date

    init(
        id: UUID = UUID(),
        authorId: UUID,
        mood: Mood = .peaceful,
        title: String? = nil,
        content: String,
        weather: String? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.authorId = authorId
        self.mood = mood
        self.title = title
        self.content = content
        self.weather = weather
        self.createdAt = createdAt
    }
}

/// 待办
struct TodoItem: Identifiable, Codable, Hashable {
    let id: UUID
    var title: String
    var note: String?
    var assignee: UserRole?
    var dueDate: Date?
    var isCompleted: Bool
    var completedBy: UUID?
    var createdAt: Date

    init(
        id: UUID = UUID(),
        title: String,
        note: String? = nil,
        assignee: UserRole? = nil,
        dueDate: Date? = nil,
        isCompleted: Bool = false,
        completedBy: UUID? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.note = note
        self.assignee = assignee
        self.dueDate = dueDate
        self.isCompleted = isCompleted
        self.completedBy = completedBy
        self.createdAt = createdAt
    }
}

/// 愿望清单
struct BucketItem: Identifiable, Codable, Hashable {
    let id: UUID
    var title: String
    var description: String?
    var coverImageData: Data?
    var targetDate: Date?
    var isAchieved: Bool
    var achievedAt: Date?
    var createdAt: Date

    init(
        id: UUID = UUID(),
        title: String,
        description: String? = nil,
        coverImageData: Data? = nil,
        targetDate: Date? = nil,
        isAchieved: Bool = false,
        achievedAt: Date? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.coverImageData = coverImageData
        self.targetDate = targetDate
        self.isAchieved = isAchieved
        self.achievedAt = achievedAt
        self.createdAt = createdAt
    }
}