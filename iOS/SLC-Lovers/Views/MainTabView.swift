import SwiftUI

/// 主 Tab 栏
struct MainTabView: View {
    @EnvironmentObject var appState: AppState
    @State private var selectedTab: Tab = .home

    enum Tab: Hashable {
        case home, chat, gallery, diary, more
    }

    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView()
                .tabItem {
                    Label("首页", systemImage: "house.fill")
                }
                .tag(Tab.home)

            ChatView()
                .tabItem {
                    Label("聊天", systemImage: "bubble.left.and.bubble.right.fill")
                }
                .tag(Tab.chat)

            GalleryView()
                .tabItem {
                    Label("相册", systemImage: "photo.fill")
                }
                .tag(Tab.gallery)

            DiaryView()
                .tabItem {
                    Label("日记", systemImage: "book.closed.fill")
                }
                .tag(Tab.diary)

            MoreView()
                .tabItem {
                    Label("更多", systemImage: "ellipsis.circle.fill")
                }
                .tag(Tab.more)
        }
        .tint(SLCColor.him)
        .onAppear {
            // 自定义 TabBar 外观
            let appearance = UITabBarAppearance()
            appearance.configureWithDefaultBackground()
            appearance.backgroundColor = UIColor(SLCColor.creamLight)
            UITabBar.appearance().standardAppearance = appearance
            UITabBar.appearance().scrollEdgeAppearance = appearance
        }
    }
}

#Preview {
    MainTabView()
        .environmentObject(AppState.shared)
}