import SwiftUI

/// SLC-Lovers 设计系统的核心色板
/// 所有颜色都应从此处引用，禁止硬编码
enum SLCColor {
    // MARK: - 背景
    static let cream = Color(hex: "#F5F1E8")
    static let creamLight = Color(hex: "#FAF7F0")
    static let creamDeep = Color(hex: "#EDE6D5")

    // MARK: - 角色色（他 / 她）
    static let him = Color(hex: "#1A1A1A")
    static let himSoft = Color(hex: "#3A3A3A")
    static let her = Color(hex: "#C9A961")
    static let herDeep = Color(hex: "#B8956A")
    static let herSoft = Color(hex: "#E8D4A0")

    // MARK: - 文字
    static let textPrimary = Color(hex: "#2C2826")
    static let textSecondary = Color(hex: "#6B6560")
    static let textTertiary = Color(hex: "#9C958E")
    static let textOnDark = Color(hex: "#F5F1E8")

    // MARK: - 语义
    static let success = Color(hex: "#7A9B6E")
    static let warning = Color(hex: "#D4A574")
    static let danger = Color(hex: "#C46B5A")
    static let info = Color(hex: "#8FA4B5")

    // MARK: - 便利构造
    static func person(_ role: UserRole) -> Color {
        role == .him ? him : her
    }

    static func personDeep(_ role: UserRole) -> Color {
        role == .him ? himSoft : herDeep
    }

    static func personSoft(_ role: UserRole) -> Color {
        role == .him ? himSoft : herSoft
    }
}

// MARK: - Color 扩展
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b, a: UInt64
        switch hex.count {
        case 3: // RGB
            (r, g, b, a) = ((int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17, 255)
        case 6: // RRGGBB
            (r, g, b, a) = (int >> 16, int >> 8 & 0xFF, int & 0xFF, 255)
        case 8: // RRGGBBAA
            (r, g, b, a) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (r, g, b, a) = (0, 0, 0, 255)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}