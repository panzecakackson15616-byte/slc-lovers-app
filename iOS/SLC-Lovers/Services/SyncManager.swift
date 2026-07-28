import Foundation
import SwiftUI

/// 同步管理器（简化版）
/// 原版本涉及 GitHub API 加密同步，因编译错误暂时禁用
/// 本地数据功能完整可用
@MainActor
final class SyncManager: ObservableObject {
    static let shared = SyncManager()

    @Published var isConfigured: Bool = false
    @Published var isSyncing: Bool = false
    @Published var lastSyncDate: Date?
    @Published var lastError: String?
    @Published var syncEnabled: Bool = true

    private init() {}

    func saveConfig(token: String, owner: String, repo: String) {
        // 待实现：GitHub 同步
    }

    func savePairingForCrypto(code: String, pairingId: String) {
        // 待实现
    }

    func clearConfig() {
        isConfigured = false
        lastSyncDate = nil
        lastError = nil
    }

    func setSyncEnabled(_ enabled: Bool) {
        syncEnabled = enabled
    }

    func schedulePushAll() {
        // 待实现
    }

    func startAutoSync() {}

    func stopAutoSync() {}

    func verifyConfig() async -> Bool {
        return false
    }

    func pullAll() async {}

    func pushAllNow() async {}
}
