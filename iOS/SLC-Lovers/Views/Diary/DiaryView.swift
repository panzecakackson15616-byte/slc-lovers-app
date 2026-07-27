import SwiftUI

/// 日记主页（含心情/待办/愿望三个 Tab）
struct DiaryView: View {
    @State private var selectedTab: DiaryTab = .mood
    @State private var showAddSheet = false

    enum DiaryTab: String, CaseIterable {
        case mood = "心情"
        case todo = "待办"
        case bucket = "愿望"
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Tab 选择
                Picker("", selection: $selectedTab) {
                    ForEach(DiaryTab.allCases, id: \.self) { tab in
                        Text(tab.rawValue).tag(tab)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, SLCSpace.md)
                .padding(.vertical, SLCSpace.sm)

                // 内容
                switch selectedTab {
                case .mood:
                    MoodDiaryList()
                case .todo:
                    TodoList()
                case .bucket:
                    BucketList()
                }
            }
            .background(SLCColor.cream)
            .navigationTitle("日记")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        Haptics.impact(.light)
                        showAddSheet = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(SLCColor.him)
                    }
                }
            }
            .sheet(isPresented: $showAddSheet) {
                AddDiarySheet(tab: selectedTab)
            }
        }
    }
}

// MARK: - 心情日记列表
private struct MoodDiaryList: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        if appState.diaryEntries.isEmpty {
            SLCEmptyView(
                icon: "book.closed",
                title: "还没有日记",
                subtitle: "记录心情，留下此刻"
            )
            .padding(.top, 60)
        } else {
            ScrollView {
                LazyVStack(spacing: SLCSpace.md) {
                    ForEach(appState.diaryEntries) { entry in
                        DiaryCard(entry: entry)
                            .padding(.horizontal, SLCSpace.md)
                    }
                }
                .padding(.vertical, SLCSpace.sm)
            }
        }
    }
}

private struct DiaryCard: View {
    let entry: DiaryEntry

    var body: some View {
        SLCCard {
            VStack(alignment: .leading, spacing: SLCSpace.sm) {
                HStack {
                    Text(entry.mood.emoji)
                        .font(.system(size: 28))
                    VStack(alignment: .leading, spacing: 2) {
                        Text(entry.mood.displayName)
                            .font(SLCFont.title(SLCFontSize.titleSmall, weight: .medium))
                            .foregroundColor(SLCColor.textPrimary)
                        Text(DateUtils.fullChinese(entry.createdAt))
                            .font(SLCFont.caption(SLCFontSize.bodySmall))
                            .foregroundColor(SLCColor.textSecondary)
                    }
                    Spacer()
                }
                if let title = entry.title, !title.isEmpty {
                    Text(title)
                        .font(SLCFont.body(SLCFontSize.bodyLarge, weight: .medium))
                        .foregroundColor(SLCColor.textPrimary)
                }
                Text(entry.content)
                    .font(SLCFont.body(SLCFontSize.bodyMedium))
                    .foregroundColor(SLCColor.textSecondary)
                    .lineSpacing(3)
            }
        }
    }
}

// MARK: - 待办列表
private struct TodoList: View {
    @EnvironmentObject var appState: AppState

    var pending: [TodoItem] { appState.todos.filter { !$0.isCompleted } }
    var completed: [TodoItem] { appState.todos.filter { $0.isCompleted } }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: SLCSpace.lg) {
                if !pending.isEmpty {
                    VStack(spacing: SLCSpace.sm) {
                        ForEach(pending) { todo in
                            TodoRow(todo: todo) { appState.toggleTodo(todo) }
                        }
                    }
                    .padding(.horizontal, SLCSpace.md)
                }
                if !completed.isEmpty {
                    SLCSectionHeader(title: "已完成")
                    VStack(spacing: SLCSpace.sm) {
                        ForEach(completed) { todo in
                            TodoRow(todo: todo) { appState.toggleTodo(todo) }
                                .opacity(0.5)
                        }
                    }
                    .padding(.horizontal, SLCSpace.md)
                }
                if pending.isEmpty && completed.isEmpty {
                    SLCEmptyView(
                        icon: "checklist",
                        title: "还没有待办",
                        subtitle: "把想做的事记下来"
                    )
                    .padding(.top, 40)
                }
            }
            .padding(.vertical, SLCSpace.sm)
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
                Image(systemName: todo.isCompleted ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 24))
                    .foregroundColor(todo.isCompleted ? SLCColor.success : SLCColor.textTertiary)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(todo.title)
                    .font(SLCFont.body(SLCFontSize.bodyLarge))
                    .foregroundColor(todo.isCompleted ? SLCColor.textSecondary : SLCColor.textPrimary)
                    .strikethrough(todo.isCompleted)
                if let date = todo.dueDate {
                    Text(DateUtils.monthDay(date))
                        .font(SLCFont.caption(SLCFontSize.bodySmall))
                        .foregroundColor(SLCColor.textTertiary)
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

// MARK: - 愿望清单
private struct BucketList: View {
    @EnvironmentObject var appState: AppState

    var active: [BucketItem] { appState.bucketItems.filter { !$0.isAchieved } }
    var done: [BucketItem] { appState.bucketItems.filter { $0.isAchieved } }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: SLCSpace.lg) {
                if !active.isEmpty {
                    VStack(spacing: SLCSpace.sm) {
                        ForEach(active) { item in
                            BucketCard(item: item) { appState.toggleBucket(item) }
                        }
                    }
                    .padding(.horizontal, SLCSpace.md)
                }
                if !done.isEmpty {
                    SLCSectionHeader(title: "已实现")
                    VStack(spacing: SLCSpace.sm) {
                        ForEach(done) { item in
                            BucketCard(item: item) { appState.toggleBucket(item) }
                                .opacity(0.5)
                        }
                    }
                    .padding(.horizontal, SLCSpace.md)
                }
                if active.isEmpty && done.isEmpty {
                    SLCEmptyView(
                        icon: "list.star",
                        title: "还没有愿望",
                        subtitle: "写下想一起做的事"
                    )
                    .padding(.top, 40)
                }
            }
            .padding(.vertical, SLCSpace.sm)
        }
    }
}

private struct BucketCard: View {
    let item: BucketItem
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: SLCSpace.md) {
            Button(action: {
                Haptics.selection()
                onToggle()
            }) {
                Image(systemName: item.isAchieved ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 24))
                    .foregroundColor(item.isAchieved ? SLCColor.success : SLCColor.textTertiary)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(item.title)
                    .font(SLCFont.title(SLCFontSize.bodyLarge, weight: .medium))
                    .foregroundColor(SLCColor.textPrimary)
                    .strikethrough(item.isAchieved)
                if let desc = item.description {
                    Text(desc)
                        .font(SLCFont.caption(SLCFontSize.bodySmall))
                        .foregroundColor(SLCColor.textSecondary)
                }
            }
            Spacer()
        }
        .padding(SLCSpace.md)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
    }
}

// MARK: - 添加表单
private struct AddDiarySheet: View {
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    let tab: DiaryView.DiaryTab

    @State private var title = ""
    @State private var content = ""
    @State private var mood: Mood = .peaceful
    @State private var assignee: UserRole? = nil
    @State private var description = ""
    @State private var targetDate = Date()

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: SLCSpace.lg) {
                    switch tab {
                    case .mood:
                        moodSection
                    case .todo:
                        todoSection
                    case .bucket:
                        bucketSection
                    }
                }
                .padding(SLCSpace.md)
            }
            .background(SLCColor.cream)
            .navigationTitle("添加\(tab.rawValue)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") { dismiss() }.foregroundColor(SLCColor.textSecondary)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("保存") { save() }
                        .foregroundColor(SLCColor.him)
                        .fontWeight(.semibold)
                        .disabled(!canSave)
                }
            }
        }
    }

    @ViewBuilder private var moodSection: some View {
        TextField("标题（可选）", text: $title)
            .textFieldStyle(.roundedBorder)
        HStack {
            ForEach(Mood.allCases, id: \.self) { m in
                Button {
                    mood = m
                    Haptics.selection()
                } label: {
                    VStack {
                        Text(m.emoji).font(.system(size: 32))
                        Text(m.displayName).font(SLCFont.caption(10))
                    }
                    .frame(width: 60)
                    .padding(.vertical, 8)
                    .background(mood == m ? SLCColor.creamDeep : Color.clear)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
                }
                .buttonStyle(.plain)
            }
        }
        TextField("今天想说什么...", text: $content, axis: .vertical)
            .lineLimit(5...12)
            .padding()
            .background(SLCColor.creamLight)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
    }

    @ViewBuilder private var todoSection: some View {
        TextField("待办事项", text: $title)
            .textFieldStyle(.roundedBorder)
        HStack {
            Text("谁负责")
                .font(SLCFont.body(SLCFontSize.bodyMedium))
            Picker("", selection: $assignee) {
                Text("共同").tag(UserRole?.none)
                Text("他").tag(UserRole?.some(.him))
                Text("她").tag(UserRole?.some(.her))
            }
            .pickerStyle(.segmented)
        }
        DatePicker("截止日期", selection: $targetDate, displayedComponents: .date)
    }

    @ViewBuilder private var bucketSection: some View {
        TextField("愿望", text: $title)
            .textFieldStyle(.roundedBorder)
        TextField("描述（可选）", text: $description, axis: .vertical)
            .lineLimit(3...6)
            .padding()
            .background(SLCColor.creamLight)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
    }

    private var canSave: Bool {
        switch tab {
        case .mood: return !content.isEmpty
        case .todo: return !title.isEmpty
        case .bucket: return !title.isEmpty
        }
    }

    private func save() {
        guard let userId = appState.currentUser?.id else { return }
        switch tab {
        case .mood:
            let entry = DiaryEntry(
                authorId: userId,
                mood: mood,
                title: title.isEmpty ? nil : title,
                content: content
            )
            appState.addDiary(entry)
        case .todo:
            let todo = TodoItem(title: title, assignee: assignee)
            appState.addTodo(todo)
        case .bucket:
            let item = BucketItem(title: title, description: description.isEmpty ? nil : description)
            appState.addBucket(item)
        }
        dismiss()
    }
}

#Preview {
    DiaryView().environmentObject(AppState.shared)
}