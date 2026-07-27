import Foundation

/// 消息类型
enum MessageType: String, Codable {
    case text       // 文字
    case voice      // 语音
    case image      // 图片
    case paper      // 小纸条（富文本）
    case emoji      // 表情包
}

/// 聊天消息
struct Message: Identifiable, Codable, Hashable {
    let id: UUID
    var senderId: UUID
    var receiverId: UUID
    var content: String
    var type: MessageType
    var mediaPath: String?
    var duration: TimeInterval?
    var isRead: Bool
    var createdAt: Date

    init(
        id: UUID = UUID(),
        senderId: UUID,
        receiverId: UUID,
        content: String,
        type: MessageType = .text,
        mediaPath: String? = nil,
        duration: TimeInterval? = nil,
        isRead: Bool = false,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.senderId = senderId
        self.receiverId = receiverId
        self.content = content
        self.type = type
        self.mediaPath = mediaPath
        self.duration = duration
        self.isRead = isRead
        self.createdAt = createdAt
    }
}