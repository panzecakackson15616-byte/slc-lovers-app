package com.slclovers.app.data.model

/**
 * 用户角色
 */
enum class UserRole {
    Him,  // 他 — 墨黑
    Her;  // 她 — 玫瑰金

    val displayName: String
        get() = when (this) {
            Him -> "他"
            Her -> "她"
        }
}

/**
 * 心情
 */
enum class Mood {
    Happy, Peaceful, Missing, Excited, Sad, Angry;

    val displayName: String
        get() = when (this) {
            Happy -> "开心"
            Peaceful -> "平静"
            Missing -> "想你"
            Excited -> "激动"
            Sad -> "难过"
            Angry -> "生气"
        }

    val emoji: String
        get() = when (this) {
            Happy -> "☀️"
            Peaceful -> "🌿"
            Missing -> "🌙"
            Excited -> "✨"
            Sad -> "🌧"
            Angry -> "🔥"
        }
}

/**
 * 消息类型
 */
enum class MessageType {
    Text, Voice, Image, Paper, Emoji
}

/**
 * 配对状态
 */
enum class PairingStatus {
    Pending, Connected
}

/**
 * 留言纸颜色
 */
enum class NoteColor {
    Cream, Gold, Black, Blush, Sage;

    val hex: String
        get() = when (this) {
            Cream -> "#FAF7F0"
            Gold -> "#E8D4A0"
            Black -> "#2C2826"
            Blush -> "#F0D5C8"
            Sage -> "#C8D4C0"
        }
}