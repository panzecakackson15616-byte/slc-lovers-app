import SwiftUI

/// 聊天列表/对话页
struct ChatView: View {
    @EnvironmentObject var appState: AppState
    @State private var draft = ""
    @FocusState private var isInputFocused: Bool

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // 消息列表
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: SLCSpace.sm) {
                            ForEach(groupedMessages(), id: \.0) { (date, msgs) in
                                DateDivider(date: date)
                                ForEach(msgs) { msg in
                                    MessageBubble(
                                        message: msg,
                                        isCurrentUser: msg.senderId == appState.currentUser?.id
                                    )
                                    .id(msg.id)
                                }
                            }
                        }
                        .padding(.horizontal, SLCSpace.md)
                        .padding(.vertical, SLCSpace.md)
                    }
                    .onChange(of: appState.messages.count) { _, _ in
                        if let lastId = appState.messages.last?.id {
                            withAnimation(.easeOut(duration: 0.3)) {
                                proxy.scrollTo(lastId, anchor: .bottom)
                            }
                        }
                    }
                    .onAppear {
                        if let lastId = appState.messages.last?.id {
                            proxy.scrollTo(lastId, anchor: .bottom)
                        }
                    }
                }

                // 输入栏
                ChatInputBar(draft: $draft, onSend: sendMessage)
                    .focused($isInputFocused)
            }
            .background(SLCColor.cream)
            .navigationTitle(appState.partner?.name ?? "TA")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    VStack(spacing: 0) {
                        Text(appState.partner?.name ?? "TA")
                            .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                        Text("在线")
                            .font(SLCFont.caption(10))
                            .foregroundColor(SLCColor.success)
                    }
                }
            }
        }
    }

    private func sendMessage() {
        let trimmed = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        appState.sendMessage(trimmed)
        draft = ""
        Haptics.impact(.light)
    }

    /// 按日期分组
    private func groupedMessages() -> [(Date, [Message])] {
        let calendar = Calendar.current
        let dict = Dictionary(grouping: appState.messages) { msg in
            calendar.startOfDay(for: msg.createdAt)
        }
        return dict.sorted { $0.key < $1.key }.map { ($0.key, $0.value.sorted { $0.createdAt < $1.createdAt }) }
    }
}

// MARK: - 日期分割线
private struct DateDivider: View {
    let date: Date

    var body: some View {
        HStack {
            line
            Text(DateUtils.fullChinese(date))
                .font(SLCFont.caption(SLCFontSize.bodySmall))
                .foregroundColor(SLCColor.textSecondary)
            line
        }
        .padding(.vertical, SLCSpace.sm)
    }

    private var line: some View {
        Rectangle()
            .fill(SLCColor.textTertiary.opacity(0.2))
            .frame(height: 1)
    }
}

// MARK: - 消息气泡
private struct MessageBubble: View {
    let message: Message
    let isCurrentUser: Bool

    var body: some View {
        HStack(alignment: .bottom, spacing: SLCSpace.sm) {
            if isCurrentUser { Spacer(minLength: 60) }
            if !isCurrentUser {
                Circle()
                    .fill(SLCColor.person(.her))
                    .frame(width: 32, height: 32)
                    .overlay(
                        Text("她")
                            .font(SLCFont.caption(12, weight: .semibold))
                            .foregroundColor(SLCColor.cream)
                    )
            }
            VStack(alignment: isCurrentUser ? .trailing : .leading, spacing: 2) {
                Text(message.content)
                    .font(SLCFont.body(SLCFontSize.bodyLarge))
                    .foregroundColor(textColor)
                    .padding(.horizontal, SLCSpace.md)
                    .padding(.vertical, SLCSpace.sm + 2)
                    .background(bubbleColor)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg, style: .continuous))
                Text(DateUtils.timeOnly(message.createdAt))
                    .font(SLCFont.caption(10))
                    .foregroundColor(SLCColor.textTertiary)
                    .padding(.horizontal, 4)
            }
            if !isCurrentUser { Spacer(minLength: 60) }
        }
    }

    private var bubbleColor: Color {
        isCurrentUser ? SLCColor.him : SLCColor.creamDeep
    }

    private var textColor: Color {
        isCurrentUser ? SLCColor.cream : SLCColor.textPrimary
    }
}

// MARK: - 输入栏
private struct ChatInputBar: View {
    @Binding var draft: String
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: SLCSpace.sm) {
            // 附件按钮（小纸条）
            Button {
                Haptics.impact(.light)
            } label: {
                Image(systemName: "envelope.fill")
                    .font(.system(size: 22))
                    .foregroundColor(SLCColor.herDeep)
                    .frame(width: 40, height: 40)
            }

            // 输入框
            TextField("说点什么…", text: $draft, axis: .vertical)
                .lineLimit(1...4)
                .padding(.horizontal, SLCSpace.md)
                .padding(.vertical, SLCSpace.sm)
                .background(SLCColor.creamLight)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .font(SLCFont.body(SLCFontSize.bodyLarge))

            // 发送
            Button(action: onSend) {
                Image(systemName: "arrow.up.circle.fill")
                    .font(.system(size: 36))
                    .foregroundColor(draft.isEmpty ? SLCColor.textTertiary : SLCColor.him)
            }
            .disabled(draft.isEmpty)
        }
        .padding(.horizontal, SLCSpace.md)
        .padding(.vertical, SLCSpace.sm)
        .background(
            SLCColor.creamLight
                .shadow(color: Color.black.opacity(0.04), radius: 8, y: -2)
                .ignoresSafeArea()
        )
    }
}

#Preview {
    ChatView()
        .environmentObject(AppState.shared)
}