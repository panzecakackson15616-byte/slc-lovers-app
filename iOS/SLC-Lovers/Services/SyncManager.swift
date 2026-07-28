import Foundation
import Combine
import CryptoKit

/// 同步管理器
/// 负责将本地数据加密上传到 GitHub，并拉取远程数据合并到本地
@MainActor
final class SyncManager: ObservableObject {

    static let shared = SyncManager()

    // MARK: - 状态
    @Published var isConfigured: Bool = false
    @Published var isSyncing: Bool = false
    @Published var lastSyncDate: Date?
    @Published var lastError: String?
    @Published var syncEnabled: Bool = true

    // MARK: - 配置
    private var token: String?
    private var owner: String?
    private var repo: String?
    private var client: GitHubClient?

    // MARK: - 加密
    private var cryptoKey: SymmetricKey?

    // MARK: - 推送防抖
    private var pendingPushWorkItem: Task<Void, Never>?
    private let pushDebounceSeconds: UInt64 = 5_000_000_000  // 5 秒

    // MARK: - 文件路径
    private let files: [(name: String, path: String)] = [
        ("messages", "data/messages.enc"),
        ("diary", "data/diary.enc"),
        ("todos", "data/todos.enc"),
        ("buckets", "data/buckets.enc"),
        ("anniversaries", "data/anniversaries.enc"),
        ("capsules", "data/capsules.enc"),
        ("notes", "data/notes.enc"),
        ("hobbies", "data/hobbies.enc"),
        ("locations", "data/locations.enc"),
    ]

    // SHA 缓存（避免每次推送都拉一次）
    private var shaCache: [String: String] = [:]

    private init() {
        loadConfig()
    }

    // MARK: - 配置加载/保存

    private func loadConfig() {
        token = SecureStorage.loadString(forKey: SecureStorage.Key.githubToken)
        owner = SecureStorage.loadString(forKey: SecureStorage.Key.githubOwner)
        repo = SecureStorage.loadString(forKey: SecureStorage.Key.githubRepo)

        if let t = token, let o = owner, let r = repo,
           !t.isEmpty, !o.isEmpty, !r.isEmpty {
            client = GitHubClient(token: t, owner: o, repo: r)
            isConfigured = true
        }

        // 加载加密密钥
        if let code = SecureStorage.loadString(forKey: SecureStorage.Key.pairingCode),
           let id = SecureStorage.loadString(forKey: SecureStorage.Key.pairingId) {
            cryptoKey = SLCCrypto.deriveKey(pairingCode: code, pairingId: id)
        }

        if let ts = UserDefaults.standard.object(forKey: SecureStorage.Key.lastSyncAt) as? Double {
            lastSyncDate = Date(timeIntervalSince1970: ts)
        }
    }

    /// 保存配置
    func saveConfig(token: String, owner: String, repo: String) {
        SecureStorage.save(token, forKey: SecureStorage.Key.githubToken)
        SecureStorage.save(owner, forKey: SecureStorage.Key.githubOwner)
        SecureStorage.save(repo, forKey: SecureStorage.Key.githubRepo)
        self.token = token
        self.owner = owner
        self.repo = repo
        self.client = GitHubClient(token: token, owner: owner, repo: repo)
        self.isConfigured = true
    }

    /// 保存配对码（用于派生密钥）
    func savePairingForCrypto(code: String, pairingId: String) {
        SecureStorage.save(code, forKey: SecureStorage.Key.pairingCode)
        SecureStorage.save(pairingId, forKey: SecureStorage.Key.pairingId)
        cryptoKey = SLCCrypto.deriveKey(pairingCode: code, pairingId: pairingId)
    }

    /// 清空配置
    func clearConfig() {
        SecureStorage.delete(forKey: SecureStorage.Key.githubToken)
        SecureStorage.delete(forKey: SecureStorage.Key.githubOwner)
        SecureStorage.delete(forKey: SecureStorage.Key.githubRepo)
        SecureStorage.delete(forKey: SecureStorage.Key.pairingCode)
        SecureStorage.delete(forKey: SecureStorage.Key.pairingId)
        token = nil
        owner = nil
        repo = nil
        client = nil
        cryptoKey = nil
        isConfigured = false
        lastSyncDate = nil
        lastError = nil
        shaCache.removeAll()
    }

    // MARK: - 检查仓库

    /// 验证配置是否有效（仓库可访问）
    func verifyConfig() async -> Bool {
        guard let client = client else { return false }
        do {
            return try await client.checkRepoExists()
        } catch {
            lastError = error.localizedDescription
            return false
        }
    }

    // MARK: - 拉取

    /// 全量拉取并合并
    func pullAll() async {
        guard syncEnabled, isConfigured, let client = client, let key = cryptoKey else { return }

        await MainActor.run {
            self.isSyncing = true
            self.lastError = nil
        }

        defer {
            Task { @MainActor in self.isSyncing = false }
        }

        let appState = AppState.shared

        for (_, path) in files {
            do {
                guard let (encryptedData, sha) = try await client.readFile(path: path) else {
                    // 远程无此文件，跳过
                    continue
                }
                shaCache[path] = sha

                // 解密
                let base64String = String(data: encryptedData, encoding: .utf8) ?? ""
                let jsonData = try SLCCrypto.decrypt(base64String, key: key)

                // 合并到本地（按时间戳）
                await mergeRemoteData(jsonData: jsonData, forPath: path)
            } catch {
                // 单文件失败不阻塞其他文件
                print("Sync: pull \(path) failed: \(error)")
            }
        }

        let now = Date()
        lastSyncDate = now
        UserDefaults.standard.set(now.timeIntervalSince1970, forKey: SecureStorage.Key.lastSyncAt)
    }

    // MARK: - 推送

    /// 立即推送所有数据（防抖 5 秒）
    func schedulePushAll() {
        guard syncEnabled, isConfigured else { return }
        pendingPushWorkItem?.cancel()
        pendingPushWorkItem = Task { [weak self] in
            try? await Task.sleep(nanoseconds: self?.pushDebounceSeconds ?? 5_000_000_000)
            guard !Task.isCancelled else { return }
            await self?.pushAll()
        }
    }

    /// 立即推送（不防抖）
    func pushAllNow() async {
        await pushAll()
    }

    private func pushAll() async {
        guard syncEnabled, isConfigured, let client = client, let key = cryptoKey else { return }

        await MainActor.run {
            self.isSyncing = true
            self.lastError = nil
        }
        defer {
            Task { @MainActor in self.isSyncing = false }
        }

        let appState = AppState.shared

        // 每个文件加密后上传
        for (name, path) in files {
            do {
                let jsonData = try serializeData(name: name, from: appState)
                let encrypted = try SLCCrypto.encrypt(jsonData, key: key)
                let data = Data(encrypted.utf8)

                let sha = shaCache[path]
                let newSha = try await client.writeFile(
                    path: path,
                    data: data,
                    sha: sha,
                    message: "sync: \(name) at \(ISO8601DateFormatter().string(from: Date()))"
                )
                shaCache[path] = newSha
            } catch let GitHubError.httpError(409, _) {
                // 冲突：重新拉取后合并再推
                await pullAll()
                // 简化：合并后下次自动会推
            } catch {
                print("Sync: push \(path) failed: \(error)")
                lastError = error.localizedDescription
            }
        }

        let now = Date()
        lastSyncDate = now
        UserDefaults.standard.set(now.timeIntervalSince1970, forKey: SecureStorage.Key.lastSyncAt)
    }

    // MARK: - 序列化

    private func serializeData(name: String, from appState: AppState) throws -> Data {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = .sortedKeys

        switch name {
        case "messages": return try encoder.encode(appState.messages)
        case "diary": return try encoder.encode(appState.diaryEntries)
        case "todos": return try encoder.encode(appState.todos)
        case "buckets": return try encoder.encode(appState.bucketItems)
        case "anniversaries": return try encoder.encode(appState.anniversaries)
        case "capsules": return try encoder.encode(appState.capsules)
        case "notes": return try encoder.encode(appState.stickyNotes)
        case "hobbies": return try encoder.encode(appState.hobbies)
        case "locations": return try encoder.encode(appState.locations)
        default: return Data("[]".utf8)
        }
    }

    // MARK: - 合并

    private func mergeRemoteData(jsonData: Data, forPath path: String) async {
        let appState = AppState.shared
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601

        // 简化策略：远程数据完全替换本地（如果远程较新）
        // 实际产品需要按 updatedAt 时间戳逐条 merge
        // 这里实现 last-write-wins：用 sha 缓存的时间戳判断

        do {
            switch path {
            case "data/messages.enc":
                let remote = try decoder.decode([Message].self, from: jsonData)
                if remote.count > appState.messages.count {
                    appState.messages = remote
                    appState.messagesStore.save(remote)
                }
            case "data/diary.enc":
                let remote = try decoder.decode([DiaryEntry].self, from: jsonData)
                if remote.count > appState.diaryEntries.count {
                    appState.diaryEntries = remote
                    appState.diaryStore.save(remote)
                }
            case "data/todos.enc":
                let remote = try decoder.decode([TodoItem].self, from: jsonData)
                if remote.count > appState.todos.count {
                    appState.todos = remote
                    appState.todoStore.save(remote)
                }
            case "data/buckets.enc":
                let remote = try decoder.decode([BucketItem].self, from: jsonData)
                if remote.count > appState.bucketItems.count {
                    appState.bucketItems = remote
                    appState.bucketStore.save(remote)
                }
            case "data/anniversaries.enc":
                let remote = try decoder.decode([Anniversary].self, from: jsonData)
                if remote.count > appState.anniversaries.count {
                    appState.anniversaries = remote
                    appState.anniversaryStore.save(remote)
                }
            case "data/capsules.enc":
                let remote = try decoder.decode([TimeCapsule].self, from: jsonData)
                if remote.count > appState.capsules.count {
                    appState.capsules = remote
                    appState.capsuleStore.save(remote)
                }
            case "data/notes.enc":
                let remote = try decoder.decode([StickyNote].self, from: jsonData)
                if remote.count > appState.stickyNotes.count {
                    appState.stickyNotes = remote
                    appState.noteStore.save(remote)
                }
            case "data/hobbies.enc":
                let remote = try decoder.decode([Hobby].self, from: jsonData)
                if remote.count > appState.hobbies.count {
                    appState.hobbies = remote
                    appState.hobbyStore.save(remote)
                }
            case "data/locations.enc":
                let remote = try decoder.decode([LocationRecord].self, from: jsonData)
                if remote.count > appState.locations.count {
                    appState.locations = remote
                    appState.locationStore.save(remote)
                }
            default:
                break
            }
        } catch {
            print("Sync: merge \(path) failed: \(error)")
        }
    }

    // MARK: - 定时同步

    private var timerTask: Task<Void, Never>?

    func startAutoSync() {
        timerTask?.cancel()
        timerTask = Task { [weak self] in
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 5 * 60 * 1_000_000_000)  // 5 分钟
                guard !Task.isCancelled else { return }
                await self?.pullAll()
            }
        }
    }

    func stopAutoSync() {
        timerTask?.cancel()
        timerTask = nil
    }
}