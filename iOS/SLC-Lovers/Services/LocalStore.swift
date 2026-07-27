import Foundation
import Combine

/// 通用本地持久化（基于 JSON 文件 + UserDefaults）
/// 原型阶段使用此方案；正式版可替换为 SwiftData
final class LocalStore<T: Codable> {
    private let key: String
    private let fileURL: URL

    init(key: String) {
        self.key = key
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        self.fileURL = documents.appendingPathComponent("\(key).json")
    }

    func load() -> T? {
        guard let data = try? Data(contentsOf: fileURL) else { return nil }
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try? decoder.decode(T.self, from: data)
    }

    func save(_ value: T) {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = .prettyPrinted
        if let data = try? encoder.encode(value) {
            try? data.write(to: fileURL, options: .atomic)
        }
    }

    func clear() {
        try? FileManager.default.removeItem(at: fileURL)
    }
}

/// UserDefaults 包装
enum Prefs {
    private static let defaults = UserDefaults.standard

    static func set<T>(_ value: T, forKey key: String) {
        defaults.set(value, forKey: key)
    }

    static func get<T>(_ key: String) -> T? {
        defaults.value(forKey: key) as? T
    }

    static func remove(_ key: String) {
        defaults.removeObject(forKey: key)
    }

    // 业务键
    enum Key {
        static let currentUserId = "slc.currentUserId"
        static let partnerUserId = "slc.partnerUserId"
        static let pairingId = "slc.pairingId"
        static let startDate = "slc.startDate"
        static let dailyQuoteShown = "slc.dailyQuoteShown"
    }
}