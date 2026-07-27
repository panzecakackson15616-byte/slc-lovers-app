import SwiftUI

@main
struct SLCApp: App {
    @StateObject private var appState = AppState.shared

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(appState)
                .preferredColorScheme(.light)
                .tint(SLCColor.him)
        }
    }
}