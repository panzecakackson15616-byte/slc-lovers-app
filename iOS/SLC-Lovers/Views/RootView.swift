import SwiftUI

/// 根视图：根据配对状态切换
struct RootView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        ZStack {
            SLCColor.cream.ignoresSafeArea()
            if appState.isPaired {
                MainTabView()
                    .transition(.opacity.combined(with: .move(edge: .trailing)))
            } else {
                PairingView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.4), value: appState.isPaired)
    }
}

#Preview {
    RootView()
        .environmentObject(AppState.shared)
}