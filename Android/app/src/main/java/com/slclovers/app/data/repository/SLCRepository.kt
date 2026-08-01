package com.slclovers.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.slclovers.app.data.SLCDatabase
import com.slclovers.app.data.model.AnniversaryEntity
import com.slclovers.app.data.model.BucketItemEntity
import com.slclovers.app.data.model.DiaryEntryEntity
import com.slclovers.app.data.model.HobbyEntity
import com.slclovers.app.data.model.LocationEntity
import com.slclovers.app.data.model.MessageEntity
import com.slclovers.app.data.model.MessageType
import com.slclovers.app.data.model.Mood
import com.slclovers.app.data.model.NoteColor
import com.slclovers.app.data.model.PairingEntity
import com.slclovers.app.data.model.PairingStatus
import com.slclovers.app.data.model.PhotoEntity
import com.slclovers.app.data.model.StickyNoteEntity
import com.slclovers.app.data.model.TimeCapsuleEntity
import com.slclovers.app.data.model.TodoEntity
import com.slclovers.app.data.model.UserEntity
import com.slclovers.app.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

/**
 * 仓库层 - 包装所有数据访问
 * 简化版：原型阶段直接调用 DAO
 */
class SLCRepository(private val db: SLCDatabase) {

    // ============ 用户 & 配对 ============

    suspend fun getCurrentUser(): UserEntity? {
        val id = getCurrentUserId() ?: return null
        return db.userDao().getById(id)
    }
    fun observeCurrentUser(): Flow<UserEntity?> = db.userDao().observeCurrent()
    fun observePairing(): Flow<PairingEntity?> = db.pairingDao().observeCurrent()
    suspend fun getPairing(): PairingEntity? = db.pairingDao().getCurrent()

    suspend fun createPairing(name: String, role: UserRole, startDate: Long): String {
        val userId = "user-${System.currentTimeMillis()}"
        val user = UserEntity(id = userId, name = name, role = role)
        db.userDao().insert(user)

        val code = generateCode()
        val pairing = PairingEntity(
            code = code,
            status = PairingStatus.Pending,
            initiatorId = userId,
            startDate = startDate
        )
        db.pairingDao().insert(pairing)

        saveCurrentUserId(userId)
        saveStartDate(startDate)
        seedAnniversary(startDate)

        return code
    }

    suspend fun joinPairing(name: String, role: UserRole, code: String, startDate: Long): Boolean {
        if (code.length != 6) return false

        val userId = "user-${System.currentTimeMillis()}"
        val partnerRole = if (role == UserRole.Him) UserRole.Her else UserRole.Him
        val partnerId = "user-partner"

        val user = UserEntity(id = userId, name = name, role = role)
        val partner = UserEntity(id = partnerId, name = if (partnerRole == UserRole.Him) "他" else "她", role = partnerRole)

        db.userDao().insert(user)
        db.userDao().insert(partner)

        val pairing = PairingEntity(
            code = code,
            status = PairingStatus.Connected,
            initiatorId = userId,
            partnerId = partnerId,
            startDate = startDate
        )
        db.pairingDao().insert(pairing)

        saveCurrentUserId(userId)
        savePartnerId(partnerId)
        saveStartDate(startDate)
        seedAnniversary(startDate)

        return true
    }

    suspend fun unpair() {
        db.userDao().clear()
        db.pairingDao().clear()
        db.messageDao().clear()
        db.photoDao().clear()
        db.diaryDao().clear()
        db.todoDao().clear()
        db.bucketDao().clear()
        db.anniversaryDao().clear()
        db.locationDao().clear()
        db.capsuleDao().clear()
        db.stickyNoteDao().clear()
        db.hobbyDao().clear()
        clearPrefs()
    }

    // ============ 消息 ============

    fun observeMessages(): Flow<List<MessageEntity>> = db.messageDao().observeAll()

    suspend fun sendMessage(senderId: String, receiverId: String, content: String) {
        val msg = MessageEntity(
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )
        db.messageDao().insert(msg)

        // 模拟对方回复
        if (content.isNotEmpty() && Random.nextBoolean()) {
            // 延迟 1.5s 回复
            kotlinx.coroutines.delay(1500)
            val replies = listOf("嗯嗯 ☀️", "想你了~", "❤️", "好的呀", "我也是", "抱抱")
            val reply = MessageEntity(
                senderId = receiverId,
                receiverId = senderId,
                content = replies.random()
            )
            db.messageDao().insert(reply)
        }
    }

    // ============ 纪念日 ============
    fun observeAnniversaries(): Flow<List<AnniversaryEntity>> = db.anniversaryDao().observeAll()

    suspend fun addAnniversary(anniversary: AnniversaryEntity) = db.anniversaryDao().insert(anniversary)
    suspend fun removeAnniversary(anniversary: AnniversaryEntity) = db.anniversaryDao().delete(anniversary)

    private suspend fun seedAnniversary(startDate: Long) {
        val anniversary = AnniversaryEntity(
            title = "在一起纪念日",
            date = startDate,
            isRecurring = true,
            icon = "💕"
        )
        db.anniversaryDao().insert(anniversary)
    }

    // ============ 待办 ============
    fun observeTodos(): Flow<List<TodoEntity>> = db.todoDao().observeAll()

    suspend fun addTodo(todo: TodoEntity) = db.todoDao().insert(todo)
    suspend fun toggleTodo(todo: TodoEntity) {
        db.todoDao().update(todo.copy(
            isCompleted = !todo.isCompleted,
            completedBy = if (!todo.isCompleted) getCurrentUserId() else null
        ))
    }
    suspend fun removeTodo(todo: TodoEntity) = db.todoDao().delete(todo)

    // ============ 愿望清单 ============
    fun observeBuckets(): Flow<List<BucketItemEntity>> = db.bucketDao().observeAll()

    suspend fun addBucket(item: BucketItemEntity) = db.bucketDao().insert(item)
    suspend fun toggleBucket(item: BucketItemEntity) {
        db.bucketDao().update(item.copy(
            isAchieved = !item.isAchieved,
            achievedAt = if (!item.isAchieved) System.currentTimeMillis() else null
        ))
    }
    suspend fun removeBucket(item: BucketItemEntity) = db.bucketDao().delete(item)

    // ============ 相册 ============
    fun observePhotos(): Flow<List<PhotoEntity>> = db.photoDao().observeAll()
    suspend fun addPhoto(photo: PhotoEntity) = db.photoDao().insert(photo)

    // ============ 日记 ============
    fun observeDiaryEntries(): Flow<List<DiaryEntryEntity>> = db.diaryDao().observeAll()
    suspend fun addDiary(entry: DiaryEntryEntity) = db.diaryDao().insert(entry)
    suspend fun removeDiary(entry: DiaryEntryEntity) = db.diaryDao().delete(entry)

    // ============ 时光胶囊 ============
    fun observeCapsules(): Flow<List<TimeCapsuleEntity>> = db.capsuleDao().observeAll()
    suspend fun addCapsule(capsule: TimeCapsuleEntity) = db.capsuleDao().insert(capsule)
    suspend fun unlockCapsule(capsule: TimeCapsuleEntity) {
        db.capsuleDao().update(capsule.copy(
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis()
        ))
    }
    suspend fun removeCapsule(capsule: TimeCapsuleEntity) = db.capsuleDao().delete(capsule)

    // ============ 留言 ============
    fun observeNotes(): Flow<List<StickyNoteEntity>> = db.stickyNoteDao().observeAll()
    suspend fun addNote(note: StickyNoteEntity) = db.stickyNoteDao().insert(note)
    suspend fun removeNote(note: StickyNoteEntity) = db.stickyNoteDao().delete(note)

    // ============ 爱好 ============
    fun observeHobbies(): Flow<List<HobbyEntity>> = db.hobbyDao().observeAll()
    suspend fun addHobby(hobby: HobbyEntity) = db.hobbyDao().insert(hobby)
    suspend fun removeHobby(hobby: HobbyEntity) = db.hobbyDao().delete(hobby)

    // ============ 位置 ============
    fun observeLocations(): Flow<List<LocationEntity>> = db.locationDao().observeAll()
    suspend fun updateLocation(location: LocationEntity) = db.locationDao().insert(location)

    // ============ 示例数据 ============
    suspend fun seedDemoData(userId: String, partnerId: String) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            // 待办
            addTodo(TodoEntity(title = "周末去看展", assignee = UserRole.Her))
            addTodo(TodoEntity(title = "买一束花", assignee = UserRole.Him))
            addTodo(TodoEntity(title = "回她爸妈家吃饭", assignee = null))

            // 愿望
            addBucket(BucketItemEntity(title = "一起去冰岛看极光", description = "2026 年冬天的约定"))
            addBucket(BucketItemEntity(title = "养一只猫", description = "英短 or 布偶？"))
            addBucket(BucketItemEntity(title = "潜水考证", description = "想去帕劳看水母湖"))
            addBucket(BucketItemEntity(title = "开一家小店", description = "咖啡 + 花艺"))

            // 时光胶囊
            val oneYearLater = Calendar.getInstance().apply {
                add(Calendar.YEAR, 1)
            }.timeInMillis
            addCapsule(TimeCapsuleEntity(
                authorId = userId,
                title = "写给一年后的我们",
                content = "希望那时候的我们，依然手牵手走在夕阳下。",
                unlockDate = oneYearLater
            ))

            // 留言
            addNote(StickyNoteEntity(authorId = userId, content = "记得早点睡 ❤️", color = NoteColor.Cream, rotation = -3.0))
            addNote(StickyNoteEntity(authorId = partnerId, content = "爱你哟", color = NoteColor.Blush, rotation = 2.0))
            addNote(StickyNoteEntity(authorId = userId, content = "今天辛苦了～", color = NoteColor.Gold, rotation = -1.0))
            addNote(StickyNoteEntity(authorId = partnerId, content = "下次去那家咖啡店", color = NoteColor.Sage, rotation = 4.0))

            // 爱好
            addHobby(HobbyEntity(userId = userId, title = "咖啡", subtitle = "美式，少冰", emoji = "☕️"))
            addHobby(HobbyEntity(userId = userId, title = "摄影", subtitle = "胶片玩家", emoji = "📷"))
            addHobby(HobbyEntity(userId = partnerId, title = "烘焙", subtitle = "蛋糕面包", emoji = "🍰"))
            addHobby(HobbyEntity(userId = partnerId, title = "旅行", subtitle = "已打卡 12 国", emoji = "✈️"))

            // 示例聊天
            val now = System.currentTimeMillis()
            listOf(
                MessageEntity(senderId = partnerId, receiverId = userId, content = "下班了吗？", createdAt = now - 7200000),
                MessageEntity(senderId = userId, receiverId = partnerId, content = "马上，路上买杯咖啡给你", createdAt = now - 7100000),
                MessageEntity(senderId = partnerId, receiverId = userId, content = "好嘞 ☕️ 顺便买点面包当早餐", createdAt = now - 7000000),
                MessageEntity(senderId = userId, receiverId = partnerId, content = "OK，想吃什么口味？", createdAt = now - 6900000),
                MessageEntity(senderId = partnerId, receiverId = userId, content = "全麦的就好 🥐", createdAt = now - 6800000),
                MessageEntity(senderId = userId, receiverId = partnerId, content = "收到～马上到家", createdAt = now - 6700000),
                MessageEntity(senderId = partnerId, receiverId = userId, content = "❤️", createdAt = now - 6600000),
            ).forEach { db.messageDao().insert(it) }
        }
    }

    // ============ 工具方法 ============
    private fun generateCode(): String = (100000..999999).random().toString()

    // ============ Prefs ============
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("slc_prefs", Context.MODE_PRIVATE)

    private fun saveCurrentUserId(id: String) {
        currentUserId = id
    }

    private fun savePartnerId(id: String) {
        partnerId = id
    }

    private fun saveStartDate(date: Long) {
        startDate = date
    }

    private fun clearPrefs() {
        currentUserId = null
        partnerId = null
        startDate = null
    }

    private fun getCurrentUserId(): String? = currentUserId

    companion object {
        // 简化：使用静态字段保存当前用户
        // 实际生产可改为 SessionManager + EncryptedSharedPreferences
        @Volatile var currentUserId: String? = null
        @Volatile var partnerId: String? = null
        @Volatile var startDate: Long? = null
    }
}