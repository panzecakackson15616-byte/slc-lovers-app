import SwiftUI

/// "更多"页 - 聚合次级功能
struct MoreView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: SLCSpace.lg) {
                    // 用户信息卡
                    ProfileHeader()
                        .padding(.horizontal, SLCSpace.md)

                    // 功能菜单
                    VStack(spacing: SLCSpace.sm) {
                        MoreMenuItem(
                            icon: "mappin.and.ellipse",
                            title: "想见你",
                            subtitle: "看到 TA 在哪",
                            color: SLCColor.her
                        ) {
                            LocationView()
                        }
                        MoreMenuItem(
                            icon: "hourglass",
                            title: "时光胶囊",
                            subtitle: "写给未来的信",
                            color: SLCColor.herDeep
                        ) {
                            CapsuleView()
                        }
                        MoreMenuItem(
                            icon: "note.text",
                            title: "留言板",
                            subtitle: "我们的小纸条",
                            color: SLCColor.him
                        ) {
                            BoardView()
                        }
                        MoreMenuItem(
                            icon: "calendar.badge.plus",
                            title: "纪念日管理",
                            subtitle: "重要的日子",
                            color: SLCColor.warning
                        ) {
                            AnniversaryListView()
                        }
                    }
                    .padding(.horizontal, SLCSpace.md)

                    Spacer(minLength: SLCSpace.xxl)
                }
                .padding(.vertical, SLCSpace.md)
            }
            .background(SLCColor.cream)
            .navigationTitle("更多")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

private struct ProfileHeader: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        SLCCard {
            HStack(spacing: SLCSpace.md) {
                Circle()
                    .fill(SLCColor.person(appState.currentUser?.role ?? .him))
                    .frame(width: 56, height: 56)
                    .overlay(
                        Text(appState.currentUser?.name.prefix(1) ?? "?")
                            .font(.system(size: 24, weight: .regular, design: .serif))
                            .foregroundColor(SLCColor.cream)
                    )
                VStack(alignment: .leading, spacing: 2) {
                    Text(appState.currentUser?.name ?? "我")
                        .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                        .foregroundColor(SLCColor.textPrimary)
                    if let date = appState.pairing?.startDate {
                        Text(DateUtils.fullChinese(date))
                            .font(SLCFont.caption(SLCFontSize.bodySmall))
                            .foregroundColor(SLCColor.textSecondary)
                    }
                }
                Spacer()
                NavigationLink(destination: SettingsView()) {
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 22))
                        .foregroundColor(SLCColor.textSecondary)
                }
            }
        }
    }
}

private struct MoreMenuItem<Destination: View>: View {
    let icon: String
    let title: String
    let subtitle: String
    let color: Color
    let destination: () -> Destination

    var body: some View {
        NavigationLink(destination: destination()) {
            HStack(spacing: SLCSpace.md) {
                ZStack {
                    Circle()
                        .fill(color.opacity(0.15))
                        .frame(width: 44, height: 44)
                    Image(systemName: icon)
                        .font(.system(size: 20))
                        .foregroundColor(color)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(SLCFont.body(SLCFontSize.bodyLarge, weight: .medium))
                        .foregroundColor(SLCColor.textPrimary)
                    Text(subtitle)
                        .font(SLCFont.caption(SLCFontSize.bodySmall))
                        .foregroundColor(SLCColor.textSecondary)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(SLCColor.textTertiary)
            }
            .padding(SLCSpace.md)
            .background(SLCColor.creamLight)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    MoreView()
        .environmentObject(AppState.shared)
}