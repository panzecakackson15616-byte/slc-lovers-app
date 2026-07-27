package com.slclovers.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.slclovers.app.data.model.AnniversaryEntity
import com.slclovers.app.data.model.BucketItemEntity
import com.slclovers.app.data.model.DiaryEntryEntity
import com.slclovers.app.data.model.HobbyEntity
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
import com.slclovers.app.data.repository.DatabaseProvider
import com.slclovers.app.data.repository.SLCRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 全局 ViewModel
 * 单例（通过 Application 获取）
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DatabaseProvider.get(app)
    val repository = SLCRepository(db)

    // 同步管理器
    val syncManager = com.slclovers.app.sync.SyncManager(app)


    // 当前用户
    val currentUser: StateFlow<UserEntity?> = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // 配对信息
    val pairing: StateFlow<PairingEntity?> = repository.observePairing()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // 数据流
    val messages = repository.observeMessages().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val photos = repository.observePhotos().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val diaryEntries = repository.observeDiaryEntries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val todos = repository.observeTodos().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val bucketItems = repository.observeBuckets().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val anniversaries = repository.observeAnniversaries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val capsules = repository.observeCapsules().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val stickyNotes = repository.observeNotes().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val hobbies = repository.observeHobbies().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val locations = repository.observeLocations().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 临时向导状态
    private val _tempRole = MutableStateFlow<UserRole?>(null)
    val tempRole: StateFlow<UserRole?> = _tempRole.asStateFlow()

    private val _tempName = MutableStateFlow("")
    val tempName: StateFlow<String> = _tempName.asStateFlow()

    private val _tempStartDate = MutableStateFlow(System.currentTimeMillis())
    val tempStartDate: StateFlow<Long> = _tempStartDate.asStateFlow()

    init {
        // 首次启动时，若没有数据则注入演示数据
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                seedDemoData()
            }
        }
    }

    // 是否已配对
    val isPaired: StateFlow<Boolean> = MutableStateFlow(false).also { state ->
        viewModelScope.launch {
            pairing.collect { p ->
                state.value = p?.status == PairingStatus.Connected
            }
        }
    }.asStateFlow()

    // ============ 配对流程 ============

    suspend fun createPairing(name: String, role: UserRole, startDate: Long): String {
        val code = repository.createPairing(name, role, startDate)
        return code
    }

    suspend fun joinPairing(name: String, role: UserRole, code: String, startDate: Long): Boolean {
        return repository.joinPairing(name, role, code, startDate)
    }

    fun unpair() {
        viewModelScope.launch {
            repository.unpair()
        }
    }

    // ============ 临时数据 ============
    fun setTempRole(role: UserRole?) { _tempRole.value = role }
    fun setTempName(name: String) { _tempName.value = name }
    fun setTempStartDate(date: Long) { _tempStartDate.value = date }

    // ============ 业务操作 ============

    fun sendMessage(content: String) {
        val user = currentUser.value ?: return
        val partner = pairing.value?.partnerId ?: return
        viewModelScope.launch {
            repository.sendMessage(user.id, partner, content)
            syncManager.schedulePushAll(this)
        }
        syncManager.schedulePushAll(this)
    }

    fun addTodo(title: String, assignee: UserRole?, note: String? = null) {
        viewModelScope.launch {
            repository.addTodo(TodoEntity(title = title, note = note, assignee = assignee))
        }
        syncManager.schedulePushAll(this)
    }

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch { repository.toggleTodo(todo) }
        syncManager.schedulePushAll(this)
    }

    fun addBucket(title: String, description: String?) {
        viewModelScope.launch {
            repository.addBucket(BucketItemEntity(title = title, description = description))
        }
        syncManager.schedulePushAll(this)
    }

    fun toggleBucket(item: BucketItemEntity) {
        viewModelScope.launch { repository.toggleBucket(item) }
        syncManager.schedulePushAll(this)
    }

    fun addDiary(mood: Mood, title: String?, content: String) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.addDiary(DiaryEntryEntity(
                authorId = userId,
                mood = mood,
                title = title,
                content = content
            ))
        }
        syncManager.schedulePushAll(this)
    }

    fun addCapsule(title: String, content: String, unlockDate: Long) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.addCapsule(TimeCapsuleEntity(
                authorId = userId,
                title = title,
                content = content,
                unlockDate = unlockDate
            ))
        }
        syncManager.schedulePushAll(this)
    }

    fun unlockCapsule(capsule: TimeCapsuleEntity) {
        viewModelScope.launch { repository.unlockCapsule(capsule) }
        syncManager.schedulePushAll(this)
    }

    fun addNote(content: String, color: NoteColor) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.addNote(StickyNoteEntity(
                authorId = userId,
                content = content,
                color = color,
                rotation = (-3..3).random().toDouble()
            ))
        }
        syncManager.schedulePushAll(this)
    }

    fun addHobby(title: String, subtitle: String?, emoji: String) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.addHobby(HobbyEntity(
                userId = userId,
                title = title,
                subtitle = subtitle,
                emoji = emoji
            ))
        }
        syncManager.schedulePushAll(this)
    }

    fun addAnniversary(title: String, date: Long, icon: String, isRecurring: Boolean) {
        viewModelScope.launch {
            repository.addAnniversary(AnniversaryEntity(
                title = title,
                date = date,
                icon = icon,
                isRecurring = isRecurring
            ))
        }
        syncManager.schedulePushAll(this)
    }

    // ============ 演示数据 ============

    private suspend fun seedDemoData() {
        val user = UserEntity(id = "demo-user", name = "我", role = UserRole.Him)
        val partner = UserEntity(id = "demo-partner", name = "她", role = UserRole.Her)
        db.userDao().insert(user)
        db.userDao().insert(partner)

        val startDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -128)
        }.timeInMillis

        db.pairingDao().insert(PairingEntity(
            code = "888888",
            status = PairingStatus.Connected,
            initiatorId = user.id,
            partnerId = partner.id,
            startDate = startDate
        ))

        SLCRepository.currentUserId = user.id
        SLCRepository.partnerId = partner.id
        SLCRepository.startDate = startDate

        repository.seedDemoData(user.id, partner.id)

        syncManager.schedulePushAll(this)
    }
}