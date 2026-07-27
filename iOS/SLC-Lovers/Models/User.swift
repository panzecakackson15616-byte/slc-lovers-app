import Foundation
import SwiftUI

/// 用户角色
enum UserRole: String, Codable, CaseIterable {
    case him   // 他 — 墨黑
    case her   // 她 — 玫瑰金

    var displayName: String {
        switch self {
        case .him: return "他"
        case .her: return "她"
        }
    }

    var color: Color {
        switch self {
        case .him: return SLCColor.him
        case .her: return SLCColor.her
        }
    }
}

/// 用户/情侣
struct User: Identifiable, Codable, Hashable {
    let id: UUID
    var name: String
    var role: UserRole
    var avatarData: Data?
    var partnerId: UUID?
    var createdAt: Date

    init(
        id: UUID = UUID(),
        name: String,
        role: UserRole,
        avatarData: Data? = nil,
        partnerId: UUID? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.role = role
        self.avatarData = avatarData
        self.partnerId = partnerId
        self.createdAt = createdAt
    }
}

/// 配对状态
enum PairingStatus: String, Codable {
    case pending     // 等待对方
    case connected   // 已配对
}

/// 配对关系
struct Pairing: Identifiable, Codable {
    let id: UUID
    var code: String              // 6 位配对码
    var status: PairingStatus
    var initiatorId: UUID         // 发起方 ID
    var partnerId: UUID?          // 对方 ID
    var startDate: Date           // 在一起日期
    var createdAt: Date

    init(
        id: UUID = UUID(),
        code: String,
        status: PairingStatus = .pending,
        initiatorId: UUID,
        partnerId: UUID? = nil,
        startDate: Date = Date(),
        createdAt: Date = Date()
    ) {
        self.id = id
        self.code = code
        self.status = status
        self.initiatorId = initiatorId
        self.partnerId = partnerId
        self.startDate = startDate
        self.createdAt = createdAt
    }

    /// 生成 6 位数字配对码
    static func generateCode() -> String {
        String(format: "%06d", Int.random(in: 100000...999999))
    }
}