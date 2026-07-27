import Foundation
import Security

/// 安全存储：Keychain 封装
/// 用于存 GitHub Token、加密密钥等敏感信息
enum SecureStorage {

    private static let service = "com.slclovers.app"

    /// 保存
    @discardableResult
    static func save(_ value: String, forKey key: String) -> Bool {
        guard let data = value.data(using: .utf8) else { return false }
        return save(data, forKey: key)
    }

    @discardableResult
    static func save(_ data: Data, forKey key: String) -> Bool {
        // 先删除旧的
        delete(forKey: key)

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        ]
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }

    /// 读取字符串
    static func loadString(forKey key: String) -> String? {
        guard let data = loadData(forKey: key) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    /// 读取 Data
    static func loadData(forKey key: String) -> Data? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess else { return nil }
        return result as? Data
    }

    /// 删除
    @discardableResult
    static func delete(forKey key: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
        ]
        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }

    // 业务键
    enum Key {
        static let githubToken = "github.token"
        static let githubOwner = "github.owner"
        static let githubRepo = "github.repo"
        static let pairingCode = "pairing.code"
        static let pairingId = "pairing.id"
        static let lastSyncAt = "sync.lastAt"
    }
}