import SwiftUI

/// 字体系统
enum SLCFont {
    /// 衬线大标题（纪念日数字、卡片标题）
    static func display(_ size: CGFloat = 48, weight: Font.Weight = .regular) -> Font {
        .system(size: size, weight: weight, design: .serif)
    }

    /// 标题
    static func title(_ size: CGFloat = 22, weight: Font.Weight = .semibold) -> Font {
        .system(size: size, weight: weight, design: .default)
    }

    /// 正文
    static func body(_ size: CGFloat = 16, weight: Font.Weight = .regular) -> Font {
        .system(size: size, weight: weight, design: .default)
    }

    /// 标签
    static func caption(_ size: CGFloat = 12, weight: Font.Weight = .regular) -> Font {
        .system(size: size, weight: weight, design: .default)
    }
}

/// 字号阶梯（按平台规范）
enum SLCFontSize {
    static let displayLarge: CGFloat = 48    // 纪念日大数字
    static let displayMedium: CGFloat = 36   // 卡片标题
    static let titleLarge: CGFloat = 28      // 页面标题
    static let titleMedium: CGFloat = 22     // 模块标题
    static let titleSmall: CGFloat = 18      // 卡片标题
    static let bodyLarge: CGFloat = 17       // 正文
    static let bodyMedium: CGFloat = 15      // 副文
    static let bodySmall: CGFloat = 13       // 说明
    static let caption: CGFloat = 11         // 标签、时间戳
}