import SwiftUI

/// 设置页
struct SettingsView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var syncManager = SyncManager.shared
    @State private var showUnpairConfirm = false

    var body: some View {
        ScrollView {
            VStack(spacing: SLCSpace.lg) {
                // 头像 & 资料
                ProfileSection()

                // 设置项
                VStack(spacing: SLCSpace.sm) {
                    NavigationLink(destination: SyncSettingsView()) {
                        SettingsRowLink(
                            icon: "arrow.triangle.2.circlepath",
                            title: "数据同步",
                            color: SLCColor.info,
                            badge: syncManager.isConfigured ? "已开启" : "未配置"
                        )
                    }
                    .buttonStyle(.plain)

                    SettingsRow(icon: "bell.fill", title: "通知", color: SLCColor.warning) {}
                    SettingsRow(icon: "lock.fill", title: "隐私与安全", color: SLCColor.him) {}
                    SettingsRow(icon: "icloud.fill", title: "iCloud 同步", color: SLCColor.info) {}
                    SettingsRow(icon: "questionmark.circle.fill", title: "帮助与反馈", color: SLCColor.success) {}
                    SettingsRow(icon: "info.circle.fill", title: "关于 SLC-Lovers", color: SLCColor.textSecondary) {}
                }
                .padding(.horizontal, SLCSpace.md)

                // 危险操作
                Button {
                    showUnpairConfirm = true
                } label: {
                    Text("解除配对")
                        .font(SLCFont.body(SLCFontSize.bodyLarge, weight: .medium))
                        .foregroundColor(SLCColor.danger)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(SLCColor.creamLight)
                        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
                }
                .padding(.horizontal, SLCSpace.md)

                Spacer(minLength: SLCSpace.xxl)
            }
            .padding(.vertical, SLCSpace.md)
        }
        .background(SLCColor.cream)
        .navigationTitle("设置")
        .navigationBarTitleDisplayMode(.inline)
        .alert("解除配对？", isPresented: $showUnpairConfirm) {
            Button("解除", role: .destructive) {
                appState.unpair()
                Haptics.notify(.warning)
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("解除后所有数据将清除，且无法恢复。")
        }
    }
}

private struct ProfileSection: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        VStack(spacing: SLCSpace.md) {
            Circle()
                .fill(SLCColor.person(appState.currentUser?.role ?? .him))
                .frame(width: 88, height: 88)
                .overlay(
                    Text(appState.currentUser?.name.prefix(1) ?? "?")
                        .font(.system(size: 36, weight: .regular, design: .serif))
                        .foregroundColor(SLCColor.cream)
                )

            VStack(spacing: 4) {
                Text(appState.currentUser?.name ?? "我")
                    .font(SLCFont.title(24, weight: .semibold))
                Text(appState.partner.map { "与 \($0.name) 在一起" } ?? "")
                    .font(SLCFont.body(14))
                    .foregroundColor(SLCColor.textSecondary)
            }
        }
        .padding(.vertical, SLCSpace.lg)
        .frame(maxWidth: .infinity)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
        .padding(.horizontal, SLCSpace.md)
    }
}

private struct SettingsRow: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: SLCSpace.md) {
                ZStack {
                    Circle()
                        .fill(color.opacity(0.15))
                        .frame(width: 36, height: 36)
                    Image(systemName: icon)
                        .font(.system(size: 16))
                        .foregroundColor(color)
                }
                Text(title)
                    .font(SLCFont.body(SLCFontSize.bodyLarge))
                    .foregroundColor(SLCColor.textPrimary)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(SLCColor.textTertiary)
            }
            .padding(SLCSpace.md)
            .background(SLCColor.creamLight)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    NavigationView { SettingsView() }
        .environmentObject(AppState.shared)
}


/// 带导航箭头和 badge 的设置行
struct SettingsRowLink: View {
    let icon: String
    let title: String
    let color: Color
    let badge: String?

    init(icon: String, title: String, color: Color, badge: String? = nil) {
        self.icon = icon
        self.title = title
        self.color = color
        self.badge = badge
    }

    var body: some View {
        HStack(spacing: SLCSpace.md) {
            ZStack {
                Circle()
                    .fill(color.opacity(0.15))
                    .frame(width: 36, height: 36)
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(color)
            }
            Text(title)
                .font(SLCFont.body(SLCFontSize.bodyLarge))
                .foregroundColor(SLCColor.textPrimary)
            Spacer()
            if let badge = badge {
                Text(badge)
                    .font(SLCFont.caption(11))
                    .foregroundColor(SLCColor.textSecondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(SLCColor.creamDeep)
                    .clipShape(Capsule())
            }
            Image(systemName: "chevron.right")
                .font(.system(size: 12))
                .foregroundColor(SLCColor.textTertiary)
        }
        .padding(SLCSpace.md)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
    }
}
