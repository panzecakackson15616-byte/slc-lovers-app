import SwiftUI

/// 同步设置页（简化版 - 功能暂时禁用）
/// 完整同步逻辑开发完成后将重新启用
struct SyncSettingsView: View {
    @StateObject private var syncManager = SyncManager.shared

    var body: some View {
        VStack(spacing: 16) {
            Text("数据同步")
                .font(.title)
                .fontWeight(.semibold)
                .padding(.top, 20)

            Text("⚠️ 同步功能开发中")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 20)

            Text("当前版本为本地优先架构：所有数据保存在你的 iPhone 本地，无需登录或同步。完整同步功能将在后续版本中提供。")
                .font(.footnote)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(SLCColor.cream)
        .navigationTitle("数据同步")
        .navigationBarTitleDisplayMode(.inline)
    }
}
