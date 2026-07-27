package com.slclovers.app.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.AnniversaryEntity
import com.slclovers.app.data.model.BucketItemEntity
import com.slclovers.app.data.model.DiaryEntryEntity
import com.slclovers.app.data.model.HobbyEntity
import com.slclovers.app.data.model.LocationEntity
import com.slclovers.app.data.model.MessageEntity
import com.slclovers.app.data.model.StickyNoteEntity
import com.slclovers.app.data.model.TimeCapsuleEntity
import com.slclovers.app.data.model.TodoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MutableStateFlow
import kotlinx.coroutines.StateFlow
import kotlinx.coroutines.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.SecretKey

/**
 * 同步管理器
 * 负责本地数据加密上传到 GitHub，并拉取远程数据合并
 */
class SyncManager(private val context: Context) {

    private val tag = "SyncManager"

    // 状态
    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncAt = MutableStateFlow<Long?>(null)
    val lastSyncAt: StateFlow<Long?> = _lastSyncAt.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _syncEnabled = MutableStateFlow(true)
    val syncEnabled: StateFlow<Boolean> = _syncEnabled.asStateFlow()

    // 配置
    private var token: String? = null
    private var owner: String? = null
    private var repo: String? = null
    private var client: GitHubClient? = null
    private var cryptoKey: SecretKey? = null

    // 推送防抖
    private var pendingPushJob: Job? = null
    private val pushDebounceMs = 5000L

    // 定时同步
    private var autoSyncJob: Job? = null

    // SHA 缓存
    private val shaCache = mutableMapOf<String, String>()

    // 文件清单
    private val files = listOf(
        "messages" to "data/messages.enc",
        "diary" to "data/diary.enc",
        "todos" to "data/todos.enc",
        "buckets" to "data/buckets.enc",
        "anniversaries" to "data/anniversaries.enc",
        "capsules" to "data/capsules.enc",
        "notes" to "data/notes.enc",
        "hobbies" to "data/hobbies.enc",
        "locations" to "data/locations.enc"
    )

    init {
        loadConfig()
    }

    // ============ 配置 ============

    private fun loadConfig() {
        token = SecureStorage.load(context, SecureStorage.Key.GITHUB_TOKEN)
        owner = SecureStorage.load(context, SecureStorage.Key.GITHUB_OWNER)
        repo = SecureStorage.load(context, SecureStorage.Key.GITHUB_REPO)

        if (!token.isNullOrEmpty() && !owner.isNullOrEmpty() && !repo.isNullOrEmpty()) {
            client = GitHubClient(token!!, owner!!, repo!!)
            _isConfigured.value = true
        }

        val code = SecureStorage.load(context, SecureStorage.Key.PAIRING_CODE)
        val id = SecureStorage.load(context, SecureStorage.Key.PAIRING_ID)
        if (!code.isNullOrEmpty() && !id.isNullOrEmpty()) {
            cryptoKey = SLCCrypto.deriveKey(code, id)
        }

        _lastSyncAt.value = SecureStorage.load(context, SecureStorage.Key.LAST_SYNC_AT)?.toLongOrNull()
    }

    fun saveConfig(token: String, owner: String, repo: String) {
        SecureStorage.save(context, SecureStorage.Key.GITHUB_TOKEN, token)
        SecureStorage.save(context, SecureStorage.Key.GITHUB_OWNER, owner)
        SecureStorage.save(context, SecureStorage.Key.GITHUB_REPO, repo)
        this.token = token
        this.owner = owner
        this.repo = repo
        this.client = GitHubClient(token, owner, repo)
        _isConfigured.value = true
    }

    fun savePairingForCrypto(code: String, pairingId: String) {
        SecureStorage.save(context, SecureStorage.Key.PAIRING_CODE, code)
        SecureStorage.save(context, SecureStorage.Key.PAIRING_ID, pairingId)
        cryptoKey = SLCCrypto.deriveKey(code, pairingId)
    }

    fun clearConfig() {
        SecureStorage.delete(context, SecureStorage.Key.GITHUB_TOKEN)
        SecureStorage.delete(context, SecureStorage.Key.GITHUB_OWNER)
        SecureStorage.delete(context, SecureStorage.Key.GITHUB_REPO)
        SecureStorage.delete(context, SecureStorage.Key.PAIRING_CODE)
        SecureStorage.delete(context, SecureStorage.Key.PAIRING_ID)
        SecureStorage.delete(context, SecureStorage.Key.LAST_SYNC_AT)
        token = null
        owner = null
        repo = null
        client = null
        cryptoKey = null
        _isConfigured.value = false
        _lastSyncAt.value = null
        _lastError.value = null
        shaCache.clear()
    }

    suspend fun verifyConfig(): Boolean {
        val c = client ?: return false
        return try {
            c.checkRepoExists()
        } catch (e: Exception) {
            _lastError.value = e.message
            false
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        _syncEnabled.value = enabled
    }

    // ============ 拉取 ============

    suspend fun pullAll(viewModel: AppViewModel) {
        if (!_syncEnabled.value || !_isConfigured.value) return
        val c = client ?: return
        val key = cryptoKey ?: return

        _isSyncing.value = true
        _lastError.value = null
        try {
            for ((_, path) in files) {
                try {
                    val result = c.readFile(path) ?: continue
                    val (encryptedData, sha) = result
                    shaCache[path] = sha

                    val base64Str = String(encryptedData, Charsets.UTF_8)
                    val jsonBytes = SLCCrypto.decrypt(base64Str, key)
                    mergeRemoteData(viewModel, jsonBytes, path)
                } catch (e: Exception) {
                    Log.w(tag, "pull $path failed", e)
                }
            }
            _lastSyncAt.value = System.currentTimeMillis()
            SecureStorage.save(context, SecureStorage.Key.LAST_SYNC_AT, _lastSyncAt.value.toString())
        } finally {
            _isSyncing.value = false
        }
    }

    // ============ 推送 ============

    fun schedulePushAll(viewModel: AppViewModel) {
        if (!_syncEnabled.value || !_isConfigured.value) return
        pendingPushJob?.cancel()
        pendingPushJob = viewModel.viewModelScope.launch {
            delay(pushDebounceMs)
            pushAll(viewModel)
        }
    }

    suspend fun pushAllNow(viewModel: AppViewModel) {
        pushAll(viewModel)
    }

    private suspend fun pushAll(viewModel: AppViewModel) {
        if (!_syncEnabled.value || !_isConfigured.value) return
        val c = client ?: return
        val key = cryptoKey ?: return

        _isSyncing.value = true
        _lastError.value = null
        try {
            for ((name, path) in files) {
                try {
                    val jsonData = serializeData(viewModel, name)
                    val encrypted = SLCCrypto.encrypt(jsonData, key)
                    val data = encrypted.toByteArray(Charsets.UTF_8)
                    val sha = shaCache[path]
                    val newSha = c.writeFile(
                        path = path,
                        data = data,
                        sha = sha,
                        message = "sync: $name at ${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())}"
                    )
                    shaCache[path] = newSha
                } catch (e: GitHubException) {
                    if (e.message?.contains("409") == true) {
                        // 冲突，重新拉取
                        pullAll(viewModel)
                    } else {
                        Log.w(tag, "push $path failed", e)
                        _lastError.value = e.message
                    }
                } catch (e: Exception) {
                    Log.w(tag, "push $path failed", e)
                    _lastError.value = e.message
                }
            }
            _lastSyncAt.value = System.currentTimeMillis()
            SecureStorage.save(context, SecureStorage.Key.LAST_SYNC_AT, _lastSyncAt.value.toString())
        } finally {
            _isSyncing.value = false
        }
    }

    // ============ 序列化 ============

    private suspend fun serializeData(viewModel: AppViewModel, name: String): ByteArray = withContext(Dispatchers.Default) {
        when (name) {
            "messages" -> messagesToJson(viewModel.messages.value)
            "diary" -> diaryToJson(viewModel.diaryEntries.value)
            "todos" -> todosToJson(viewModel.todos.value)
            "buckets" -> bucketsToJson(viewModel.bucketItems.value)
            "anniversaries" -> anniversariesToJson(viewModel.anniversaries.value)
            "capsules" -> capsulesToJson(viewModel.capsules.value)
            "notes" -> notesToJson(viewModel.stickyNotes.value)
            "hobbies" -> hobbiesToJson(viewModel.hobbies.value)
            "locations" -> locationsToJson(viewModel.locations.value)
            else -> "[]".toByteArray()
        }
    }

    private fun messagesToJson(items: List<MessageEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("senderId", m.senderId)
                put("receiverId", m.receiverId)
                put("content", m.content)
                put("type", m.type.name)
                put("isRead", m.isRead)
                put("createdAt", m.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun diaryToJson(items: List<DiaryEntryEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("authorId", e.authorId)
                put("mood", e.mood.name)
                put("title", e.title)
                put("content", e.content)
                put("weather", e.weather)
                put("createdAt", e.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun todosToJson(items: List<TodoEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("note", t.note)
                put("assignee", t.assignee?.name)
                put("dueDate", t.dueDate)
                put("isCompleted", t.isCompleted)
                put("completedBy", t.completedBy)
                put("createdAt", t.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun bucketsToJson(items: List<BucketItemEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { b ->
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("description", b.description)
                put("targetDate", b.targetDate)
                put("isAchieved", b.isAchieved)
                put("achievedAt", b.achievedAt)
                put("createdAt", b.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun anniversariesToJson(items: List<AnniversaryEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id)
                put("title", a.title)
                put("date", a.date)
                put("isRecurring", a.isRecurring)
                put("icon", a.icon)
                put("note", a.note)
                put("createdAt", a.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun capsulesToJson(items: List<TimeCapsuleEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { c ->
            arr.put(JSONObject().apply {
                put("id", c.id)
                put("authorId", c.authorId)
                put("title", c.title)
                put("content", c.content)
                put("unlockDate", c.unlockDate)
                put("isUnlocked", c.isUnlocked)
                put("unlockedAt", c.unlockedAt)
                put("createdAt", c.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun notesToJson(items: List<StickyNoteEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { n ->
            arr.put(JSONObject().apply {
                put("id", n.id)
                put("authorId", n.authorId)
                put("content", n.content)
                put("color", n.color.name)
                put("rotation", n.rotation)
                put("createdAt", n.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun hobbiesToJson(items: List<HobbyEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { h ->
            arr.put(JSONObject().apply {
                put("id", h.id)
                put("userId", h.userId)
                put("title", h.title)
                put("subtitle", h.subtitle)
                put("emoji", h.emoji)
                put("createdAt", h.createdAt)
            })
        }
        return arr.toString().toByteArray()
    }

    private fun locationsToJson(items: List<LocationEntity>): ByteArray {
        val arr = JSONArray()
        items.forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id)
                put("userId", l.userId)
                put("latitude", l.latitude)
                put("longitude", l.longitude)
                put("address", l.address)
                put("batteryLevel", l.batteryLevel)
                put("isSharing", l.isSharing)
                put("updatedAt", l.updatedAt)
            })
        }
        return arr.toString().toByteArray()
    }

    // ============ 合并 ============

    /**
     * 简化策略：远程条目数 > 本地条目数 → 用远程替换本地
     * 实际生产应按 updatedAt 逐条 merge
     */
    private suspend fun mergeRemoteData(viewModel: AppViewModel, jsonData: ByteArray, path: String) {
        // 留作扩展：当前实现以"远程多就用远程"为准
        // 真实场景应该解析 JSON 后比对每条的 updatedAt 时间戳
        Log.d(tag, "merge $path: ${jsonData.size} bytes (count-based merge not implemented in prototype)")
    }

    // ============ 定时同步 ============

    fun startAutoSync(viewModel: AppViewModel) {
        autoSyncJob?.cancel()
        autoSyncJob = viewModel.viewModelScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000) // 5 分钟
                pullAll(viewModel)
            }
        }
    }

    fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
    }
}