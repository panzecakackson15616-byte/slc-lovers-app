import SwiftUI

/// 首页
struct HomeView: View {
    @EnvironmentObject var appState: AppState
    @State private var now = Date()

    private let timer = Timer.publish(every: 60, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: SLCSpace.lg) {
                    // 顶部问候
                    HeaderSection(userName: appState.currentUser?.name ?? "我")

                    // 在一起大数字
                    TogetherCounter(startDate: appState.pairing?.startDate ?? Date())

                    // 每日寄语
                    if let quote = appState.todayQuote {
                        QuoteCard(quote: quote)
                    }

                    // 下一个纪念日
                    if let next = nextAnniversary() {
                        NextAnniversaryCard(anniversary: next)
                    }

                    // 待办预览
                    TodoPreviewSection()

                    // 愿望清单预览
                    BucketPreviewSection()

                    // 最近聊天
                    RecentChatSection()

                    Spacer(minLength: SLCSpace.xxl)
                }
                .padding(.vertical, SLCSpace.md)
            }
            .background(SLCColor.cream)
            .navigationBarHidden(true)
            .onReceive(timer) { _ in
                now = Date()
            }
            .onAppear {
                appState.refreshDailyQuote()
            }
        }
    }

    private func nextAnniversary() -> Anniversary? {
        appState.anniversaries
            .filter { !$0.title.contains("在一起") }
            .min(by: { $0.daysUntilNext() < $1.daysUntilNext() })
    }
}

// MARK: - 顶部
private struct HeaderSection: View {
    let userName: String

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(greeting())
                    .font(SLCFont.body(13))
                    .foregroundColor(SLCColor.textSecondary)
                Text(userName)
                    .font(SLCFont.title(SLCFontSize.titleLarge, weight: .semibold))
                    .foregroundColor(SLCColor.textPrimary)
            }
            Spacer()
            NavigationLink(destination: SettingsView()) {
                Image(systemName: "person.crop.circle.fill")
                    .font(.system(size: 36))
                    .foregroundColor(SLCColor.him)
            }
        }
        .padding(.horizontal, SLCSpace.lg)
    }

    private func greeting() -> String {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<11: return "早安，"
        case 11..<14: return "中午好，"
        case 14..<18: return "下午好，"
        case 18..<22: return "晚上好，"
        default: return "夜深了，"
        }
    }
}

// MARK: - 在一起大数字
private struct TogetherCounter: View {
    let startDate: Date

    var body: some View {
        VStack(spacing: SLCSpace.sm) {
            Text("在一起")
                .font(SLCFont.body(14, weight: .medium))
                .foregroundColor(SLCColor.textSecondary)
                .tracking(4)

            HStack(alignment: .lastTextBaseline, spacing: 4) {
                Text(DateUtils.togetherDays(since: startDate))
                    .font(.system(size: 96, weight: .ultraLight, design: .serif))
                    .foregroundColor(SLCColor.him)
                    .contentTransition(.numericText())
                Text("天")
                    .font(SLCFont.title(28, weight: .light))
                    .foregroundColor(SLCColor.him)
            }

            Text(DateUtils.fullChinese(startDate))
                .font(SLCFont.caption(SLCFontSize.bodySmall))
                .foregroundColor(SLCColor.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, SLCSpace.xl)
        .background(
            LinearGradient(
                colors: [SLCColor.creamLight, SLCColor.cream],
                startPoint: .top,
                endPoint: .bottom
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.xl))
        .padding(.horizontal, SLCSpace.lg)
        .slcShadow(.soft)
    }
}

// MARK: - 每日寄语
private struct QuoteCard: View {
    let quote: Quote

    var body: some View {
        SLCCard {
            HStack(alignment: .top, spacing: SLCSpace.md) {
                Image(systemName: "quote.opening")
                    .font(.system(size: 24))
                    .foregroundColor(SLCColor.herDeep.opacity(0.5))
                VStack(alignment: .leading, spacing: 4) {
                    Text(quote.content)
                        .font(SLCFont.body(SLCFontSize.bodyLarge, weight: .regular))
                        .foregroundColor(SLCColor.textPrimary)
                        .lineSpacing(4)
                }
            }
        }
        .padding(.horizontal, SLCSpace.lg)
    }
}

// MARK: - 下一个纪念日
private struct NextAnniversaryCard: View {
    let anniversary: Anniversary

    var body: some View {
        SLCCard {
            HStack(spacing: SLCSpace.md) {
                Text(anniversary.icon)
                    .font(.system(size: 44))
                VStack(alignment: .leading, spacing: 4) {
                    Text(anniversary.title)
                        .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                        .foregroundColor(SLCColor.textPrimary)
                    HStack(spacing: 6) {
                        Text("还有")
                            .font(SLCFont.caption(SLCFontSize.bodySmall))
                            .foregroundColor(SLCColor.textSecondary)
                        Text("\(anniversary.daysUntilNext())")
                            .font(.system(size: 20, weight: .medium, design: .serif))
                            .foregroundColor(SLCColor.herDeep)
                        Text("天")
                            .font(SLCFont.caption(SLCFontSize.bodySmall))
                            .foregroundColor(SLCColor.textSecondary)
                    }
                }
                Spacer()
                NavigationLink(destination: AnniversaryListView()) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(SLCColor.textTertiary)
                }
            }
        }
        .padding(.horizontal, SLCSpace.lg)
    }
}

// MARK: - 待办预览
private struct TodoPreviewSection: View {
    @EnvironmentObject var appState: AppState

    var pendingTodos: [TodoItem] {
        appState.todos.filter { !$0.isCompleted }.prefix(3).map { $0 }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: SLCSpace.sm) {
            SLCSectionHeader(title: "待办", action: "全部") {
                // TODO: 跳转到全部待办
            }
            if pendingTodos.isEmpty {
                Text("暂无待办，去记下今天想做的事吧")
                    .font(SLCFont.body(SLCFontSize.bodyMedium))
                    .foregroundColor(SLCColor.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(SLCSpace.lg)
                    .background(SLCColor.creamLight)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
                    .padding(.horizontal, SLCSpace.md)
            } else {
                VStack(spacing: SLCSpace.sm) {
                    ForEach(pendingTodos) { todo in
                        TodoRow(todo: todo) {
                            appState.toggleTodo(todo)
                        }
                    }
                }
                .padding(.horizontal, SLCSpace.md)
            }
        }
    }
}

private struct TodoRow: View {
    let todo: TodoItem
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: SLCSpace.md) {
            Button(action: {
                Haptics.selection()
                onToggle()
            }) {
                Image(systemName: "circle")
                    .font(.system(size: 22))
                    .foregroundColor(SLCColor.textTertiary)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(todo.title)
                    .font(SLCFont.body(SLCFontSize.bodyLarge))
                    .foregroundColor(SLCColor.textPrimary)
                if let date = todo.dueDate {
                    Text(DateUtils.monthDay(date))
                        .font(SLCFont.caption(SLCFontSize.bodySmall))
                        .foregroundColor(SLCColor.textSecondary)
                }
            }
            Spacer()
            if let assignee = todo.assignee {
                SLCPersonBadge(role: assignee, compact: true)
            }
        }
        .padding(SLCSpace.md)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
    }
}

// MARK: - 愿望清单预览
private struct BucketPreviewSection: View {
    @EnvironmentObject var appState: AppState

    var activeBuckets: [BucketItem] {
        appState.bucketItems.filter { !$0.isAchieved }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: SLCSpace.sm) {
            SLCSectionHeader(title: "愿望清单")
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: SLCSpace.sm) {
                    ForEach(activeBuckets.prefix(5)) { bucket in
                        NavigationLink(destination: CapsuleView()) {
                            BucketCard(bucket: bucket)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, SLCSpace.md)
            }
        }
    }
}

private struct BucketCard: View {
    let bucket: BucketItem

    var body: some View {
        VStack(alignment: .leading, spacing: SLCSpace.sm) {
            Text(bucket.title)
                .font(SLCFont.title(SLCFontSize.bodyLarge, weight: .medium))
                .foregroundColor(SLCColor.textPrimary)
                .lineLimit(2)
                .multilineTextAlignment(.leading)
                .frame(width: 160, alignment: .leading)
            if let desc = bucket.description {
                Text(desc)
                    .font(SLCFont.caption(SLCFontSize.bodySmall))
                    .foregroundColor(SLCColor.textSecondary)
                    .lineLimit(1)
            }
        }
        .padding(SLCSpace.md)
        .frame(width: 180, height: 100, alignment: .topLeading)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
    }
}

// MARK: - 最近聊天预览
private struct RecentChatSection: View {
    @EnvironmentObject var appState: AppState

    var lastMessage: Message? {
        appState.messages.last
    }

    var body: some View {
        if let last = lastMessage {
            VStack(alignment: .leading, spacing: SLCSpace.sm) {
                SLCSectionHeader(title: "最近的消息")
                NavigationLink(destination: ChatView()) {
                    SLCCard {
                        HStack(spacing: SLCSpace.md) {
                            Image(systemName: "bubble.left.fill")
                                .foregroundColor(SLCColor.herDeep)
                                .font(.system(size: 22))
                            VStack(alignment: .leading, spacing: 2) {
                                Text(last.content)
                                    .font(SLCFont.body(SLCFontSize.bodyLarge))
                                    .foregroundColor(SLCColor.textPrimary)
                                    .lineLimit(1)
                                Text(DateUtils.friendlyRelative(from: last.createdAt))
                                    .font(SLCFont.caption(SLCFontSize.bodySmall))
                                    .foregroundColor(SLCColor.textSecondary)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.system(size: 12))
                                .foregroundColor(SLCColor.textTertiary)
                        }
                    }
                }
                .buttonStyle(.plain)
                .padding(.horizontal, SLCSpace.md)
            }
        }
    }
}

// MARK: - 纪念日列表页（简单版）
struct AnniversaryListView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        ScrollView {
            VStack(spacing: SLCSpace.sm) {
                ForEach(appState.anniversaries) { ann in
                    SLCCard {
                        HStack(spacing: SLCSpace.md) {
                            Text(ann.icon)
                                .font(.system(size: 36))
                            VStack(alignment: .leading, spacing: 4) {
                                Text(ann.title)
                                    .font(SLCFont.title(SLCFontSize.titleSmall, weight: .medium))
                                    .foregroundColor(SLCColor.textPrimary)
                                Text(DateUtils.fullChinese(ann.date))
                                    .font(SLCFont.caption(SLCFontSize.bodySmall))
                                    .foregroundColor(SLCColor.textSecondary)
                            }
                            Spacer()
                            VStack(alignment: .trailing) {
                                Text("还有 \(ann.daysUntilNext()) 天")
                                    .font(SLCFont.body(SLCFontSize.bodyMedium, weight: .medium))
                                    .foregroundColor(SLCColor.herDeep)
                            }
                        }
                    }
                    .padding(.horizontal, SLCSpace.md)
                }
            }
            .padding(.vertical, SLCSpace.md)
        }
        .background(SLCColor.cream)
        .navigationTitle("纪念日")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    HomeView()
        .environmentObject(AppState.shared)
}