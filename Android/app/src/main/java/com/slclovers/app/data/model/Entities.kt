package com.slclovers.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 用户
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: UserRole,
    val avatarData: ByteArray? = null,
    val partnerId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserEntity) return false
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

/**
 * 配对关系
 */
@Entity(tableName = "pairings")
data class PairingEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val code: String,
    val status: PairingStatus = PairingStatus.Pending,
    val initiatorId: String,
    val partnerId: String? = null,
    val startDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 聊天消息
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val content: String,
    val type: MessageType = MessageType.Text,
    val mediaPath: String? = null,
    val duration: Long? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 相册照片
 */
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val uploaderId: String,
    val imageData: ByteArray,
    val thumbnailData: ByteArray,
    val caption: String? = null,
    val location: String? = null,
    val takenAt: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoEntity) return false
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

/**
 * 日记条目
 */
@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val mood: Mood = Mood.Peaceful,
    val title: String? = null,
    val content: String,
    val weather: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 待办
 */
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String? = null,
    val assignee: UserRole? = null,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 愿望清单
 */
@Entity(tableName = "buckets")
data class BucketItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val coverImageData: ByteArray? = null,
    val targetDate: Long? = null,
    val isAchieved: Boolean = false,
    val achievedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketItemEntity) return false
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

/**
 * 纪念日
 */
@Entity(tableName = "anniversaries")
data class AnniversaryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: Long,
    val isRecurring: Boolean = true,
    val icon: String = "💕",
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 计算距离下一个纪念日的天数 */
    fun daysUntilNext(): Int {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val targetCal = java.util.Calendar.getInstance().apply {
            timeInMillis = date
        }
        if (isRecurring) {
            // 取今年或明年的同月同日
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            targetCal.set(java.util.Calendar.YEAR, currentYear)
            if (targetCal.timeInMillis < now) {
                targetCal.add(java.util.Calendar.YEAR, 1)
            }
        }
        val diffMs = targetCal.timeInMillis - now
        return (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
}

/**
 * 位置记录
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val batteryLevel: Float = 1.0f,
    val isSharing: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 时光胶囊
 */
@Entity(tableName = "capsules")
data class TimeCapsuleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val title: String,
    val content: String,
    val mediaData: ByteArray? = null,
    val unlockDate: Long,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 是否已到期可解封 */
    fun canUnlock(): Boolean = System.currentTimeMillis() >= unlockDate && !isUnlocked

    /** 解封倒计时描述 */
    fun countdownDescription(): String {
        val interval = unlockDate - System.currentTimeMillis()
        if (interval <= 0) return "可解封"
        val days = interval / (1000 * 60 * 60 * 24)
        val hours = (interval % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (interval % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            days > 0 -> "${days} 天 ${hours} 小时"
            hours > 0 -> "${hours} 小时 ${minutes} 分"
            else -> "${minutes} 分钟"
        }
    }
}

/**
 * 留言板留言
 */
@Entity(tableName = "sticky_notes")
data class StickyNoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val content: String,
    val color: NoteColor = NoteColor.Cream,
    val rotation: Double = 0.0,
    val positionX: Double = 0.5,
    val positionY: Double = 0.5,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 个人爱好
 */
@Entity(tableName = "hobbies")
data class HobbyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val subtitle: String? = null,
    val emoji: String = "✨",
    val createdAt: Long = System.currentTimeMillis()
)