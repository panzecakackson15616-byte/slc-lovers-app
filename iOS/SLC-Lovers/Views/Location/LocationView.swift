import SwiftUI
import MapKit

/// 想见你 / 位置共享
struct LocationView: View {
    @EnvironmentObject var appState: AppState
    @State private var myCoordinate = CLLocationCoordinate2D(latitude: 39.9042, longitude: 116.4074)
    @State private var partnerCoordinate = CLLocationCoordinate2D(latitude: 39.9142, longitude: 116.4174)

    var body: some View {
        ScrollView {
            VStack(spacing: SLCSpace.lg) {
                // 距离卡片
                DistanceCard(
                    distance: LocationUtils.distance(
                        lat1: myCoordinate.latitude, lon1: myCoordinate.longitude,
                        lat2: partnerCoordinate.latitude, lon2: partnerCoordinate.longitude
                    ),
                    lastUpdate: Date()
                )
                .padding(.horizontal, SLCSpace.lg)

                // 简易地图（用 SF Symbols 模拟）
                SimpleMapView(
                    myLocation: myCoordinate,
                    partnerLocation: partnerCoordinate
                )
                .frame(height: 320)
                .clipShape(RoundedRectangle(cornerRadius: SLCRadius.xl))
                .padding(.horizontal, SLCSpace.lg)

                // 状态行
                VStack(spacing: SLCSpace.sm) {
                    LocationStatusRow(role: appState.currentUser?.role ?? .him, address: "北京市朝阳区三里屯", isSharing: true)
                    LocationStatusRow(role: appState.partner?.role ?? .her, address: "北京市海淀区中关村", isSharing: true)
                }
                .padding(.horizontal, SLCSpace.lg)

                Spacer(minLength: SLCSpace.xxl)
            }
            .padding(.vertical, SLCSpace.md)
        }
        .background(SLCColor.cream)
        .navigationTitle("想见你")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - 距离卡片
private struct DistanceCard: View {
    let distance: Double  // km
    let lastUpdate: Date

    var body: some View {
        VStack(spacing: SLCSpace.sm) {
            Text("相距")
                .font(SLCFont.body(14))
                .foregroundColor(SLCColor.textSecondary)

            HStack(alignment: .lastTextBaseline, spacing: 4) {
                Text(LocationUtils.formattedDistance(distance))
                    .font(.system(size: 56, weight: .ultraLight, design: .serif))
                    .foregroundColor(SLCColor.him)
            }

            Text("更新于 \(DateUtils.timeOnly(lastUpdate))")
                .font(SLCFont.caption(SLCFontSize.bodySmall))
                .foregroundColor(SLCColor.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, SLCSpace.xl)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.xl))
        .slcShadow(.soft)
    }
}

// MARK: - 简易地图
private struct SimpleMapView: View {
    let myLocation: CLLocationCoordinate2D
    let partnerLocation: CLLocationCoordinate2D

    var body: some View {
        ZStack {
            // 背景模拟地图
            LinearGradient(
                colors: [SLCColor.creamLight, SLCColor.creamDeep],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            // 网格线
            Path { path in
                for i in stride(from: 0, to: 400, by: 40) {
                    path.move(to: CGPoint(x: i, y: 0))
                    path.addLine(to: CGPoint(x: i, y: 320))
                    path.move(to: CGPoint(x: 0, y: i))
                    path.addLine(to: CGPoint(x: 400, y: i))
                }
            }
            .stroke(SLCColor.textTertiary.opacity(0.1), lineWidth: 0.5)

            // 路线
            Path { path in
                path.move(to: CGPoint(x: 120, y: 200))
                path.addLine(to: CGPoint(x: 280, y: 120))
            }
            .stroke(SLCColor.herDeep.opacity(0.6), style: StrokeStyle(lineWidth: 2, dash: [6, 4]))

            // 我的位置
            VStack {
                Circle()
                    .fill(SLCColor.him)
                    .frame(width: 16, height: 16)
                    .overlay(Circle().stroke(SLCColor.cream, lineWidth: 3))
                Text("我")
                    .font(SLCFont.caption(11, weight: .semibold))
                    .foregroundColor(SLCColor.him)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(SLCColor.cream)
                    .clipShape(Capsule())
                    .offset(y: 4)
            }
            .position(x: 120, y: 200)

            // TA 的位置
            VStack {
                Circle()
                    .fill(SLCColor.her)
                    .frame(width: 16, height: 16)
                    .overlay(Circle().stroke(SLCColor.cream, lineWidth: 3))
                Text("TA")
                    .font(SLCFont.caption(11, weight: .semibold))
                    .foregroundColor(SLCColor.herDeep)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(SLCColor.cream)
                    .clipShape(Capsule())
                    .offset(y: 4)
            }
            .position(x: 280, y: 120)
        }
    }
}

// MARK: - 状态行
private struct LocationStatusRow: View {
    let role: UserRole
    let address: String
    let isSharing: Bool

    var body: some View {
        HStack(spacing: SLCSpace.md) {
            Circle()
                .fill(SLCColor.person(role))
                .frame(width: 36, height: 36)
                .overlay(
                    Text(role.displayName)
                        .font(.system(size: 14, weight: .regular, design: .serif))
                        .foregroundColor(SLCColor.cream)
                )
            VStack(alignment: .leading, spacing: 2) {
                Text(address)
                    .font(SLCFont.body(SLCFontSize.bodyMedium, weight: .medium))
                    .foregroundColor(SLCColor.textPrimary)
                HStack(spacing: 4) {
                    Circle()
                        .fill(isSharing ? SLCColor.success : SLCColor.textTertiary)
                        .frame(width: 6, height: 6)
                    Text(isSharing ? "正在共享位置" : "未共享")
                        .font(SLCFont.caption(SLCFontSize.bodySmall))
                        .foregroundColor(SLCColor.textSecondary)
                }
            }
            Spacer()
            Text("电量 78%")
                .font(SLCFont.caption(SLCFontSize.bodySmall))
                .foregroundColor(SLCColor.textSecondary)
        }
        .padding(SLCSpace.md)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
    }
}

#Preview {
    NavigationView { LocationView() }
        .environmentObject(AppState.shared)
}