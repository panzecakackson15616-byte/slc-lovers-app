import SwiftUI

/// 8dp 间距系统
enum SLCSpace {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 48
}

/// 圆角
enum SLCRadius {
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 20
    static let xxl: CGFloat = 24
    static let full: CGFloat = 999
}

/// 阴影
extension View {
    func slcShadow(_ level: SLCShadowLevel = .soft) -> some View {
        shadow(
            color: Color.black.opacity(level.opacity),
            radius: level.radius,
            x: 0,
            y: level.y
        )
    }
}

enum SLCShadowLevel {
    case soft, medium, deep

    var opacity: Double {
        switch self {
        case .soft: return 0.06
        case .medium: return 0.08
        case .deep: return 0.12
        }
    }

    var radius: CGFloat {
        switch self {
        case .soft: return 8
        case .medium: return 16
        case .deep: return 24
        }
    }

    var y: CGFloat {
        switch self {
        case .soft: return 2
        case .medium: return 4
        case .deep: return 8
        }
    }
}