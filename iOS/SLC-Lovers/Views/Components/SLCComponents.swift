import SwiftUI

/// 主卡片容器
struct SLCCard<Content: View>: View {
    let content: Content
    var padding: CGFloat = SLCSpace.md

    init(padding: CGFloat = SLCSpace.md, @ViewBuilder content: () -> Content) {
        self.padding = padding
        self.content = content()
    }

    var body: some View {
        content
            .padding(padding)
            .background(SLCColor.creamLight)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
            .slcShadow(.soft)
    }
}

/// 头像
struct SLCAvatar: View {
    let role: UserRole
    let initial: String
    var size: CGFloat = 48

    var body: some View {
        ZStack {
            Circle()
                .fill(SLCColor.person(role))
            Text(initial)
                .font(.system(size: size * 0.45, weight: .semibold, design: .serif))
                .foregroundColor(role == .him ? SLCColor.cream : SLCColor.cream)
        }
        .frame(width: size, height: size)
    }
}

/// 主按钮
struct SLCPrimaryButton: View {
    let title: String
    let action: () -> Void
    var color: Color = SLCColor.him

    var body: some View {
        Button(action: {
            Haptics.impact(.light)
            action()
        }) {
            Text(title)
                .font(SLCFont.body(17, weight: .semibold))
                .foregroundColor(SLCColor.cream)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(color)
                .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
        }
    }
}

/// 次按钮
struct SLCSecondaryButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: {
            Haptics.impact(.light)
            action()
        }) {
            Text(title)
                .font(SLCFont.body(15, weight: .medium))
                .foregroundColor(SLCColor.textPrimary)
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .background(SLCColor.creamLight)
                .overlay(
                    RoundedRectangle(cornerRadius: SLCRadius.md)
                        .stroke(SLCColor.textTertiary.opacity(0.3), lineWidth: 1)
                )
        }
    }
}

/// 空状态
struct SLCEmptyView: View {
    let icon: String
    let title: String
    let subtitle: String?

    init(icon: String, title: String, subtitle: String? = nil) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
    }

    var body: some View {
        VStack(spacing: SLCSpace.md) {
            Image(systemName: icon)
                .font(.system(size: 56, weight: .light))
                .foregroundColor(SLCColor.textTertiary)
            Text(title)
                .font(SLCFont.title(SLCFontSize.titleSmall, weight: .medium))
                .foregroundColor(SLCColor.textPrimary)
            if let subtitle = subtitle {
                Text(subtitle)
                    .font(SLCFont.body(SLCFontSize.bodyMedium))
                    .foregroundColor(SLCColor.textSecondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(SLCSpace.xl)
    }
}

/// Section 标题
struct SLCSectionHeader: View {
    let title: String
    var action: String?
    var onAction: (() -> Void)?

    var body: some View {
        HStack {
            Text(title)
                .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                .foregroundColor(SLCColor.textPrimary)
            Spacer()
            if let action = action, let onAction = onAction {
                Button(action: onAction) {
                    Text(action)
                        .font(SLCFont.body(SLCFontSize.bodyMedium))
                        .foregroundColor(SLCColor.herDeep)
                }
            }
        }
        .padding(.horizontal, SLCSpace.md)
    }
}

/// 角色徽章
struct SLCPersonBadge: View {
    let role: UserRole
    var compact: Bool = false

    var body: some View {
        Text(role.displayName)
            .font(SLCFont.caption(compact ? 10 : 11, weight: .medium))
            .foregroundColor(SLCColor.cream)
            .padding(.horizontal, compact ? 6 : 8)
            .padding(.vertical, compact ? 2 : 3)
            .background(SLCColor.person(role))
            .clipShape(Capsule())
    }
}