import SwiftUI

/// 留言板 / 个人爱好
struct BoardView: View {
    @EnvironmentObject var appState: AppState
    @State private var showAddNote = false
    @State private var showAddHobby = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: SLCSpace.lg) {
                // 个人爱好
                HobbiesSection(showAddHobby: $showAddHobby)

                // 留言板
                StickyNotesSection(showAddNote: $showAddNote)

                Spacer(minLength: SLCSpace.xxl)
            }
            .padding(.vertical, SLCSpace.md)
        }
        .background(SLCColor.cream)
        .navigationTitle("我们")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showAddNote) {
            AddStickyNoteSheet()
        }
        .sheet(isPresented: $showAddHobby) {
            AddHobbySheet()
        }
    }
}

// MARK: - 爱好
private struct HobbiesSection: View {
    @EnvironmentObject var appState: AppState
    @Binding var showAddHobby: Bool

    var himHobbies: [Hobby] {
        appState.hobbies.filter { $0.userId == appState.currentUser?.id || (appState.currentUser?.role == .him) }
    }

    var herHobbies: [Hobby] {
        appState.hobbies.filter { $0.userId == appState.partner?.id }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: SLCSpace.sm) {
            HStack {
                Text("他喜欢")
                    .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                Spacer()
                Button {
                    Haptics.impact(.light)
                    showAddHobby = true
                } label: {
                    Image(systemName: "plus.circle")
                        .foregroundColor(SLCColor.herDeep)
                }
            }
            .padding(.horizontal, SLCSpace.md)

            FlowLayout(spacing: SLCSpace.sm) {
                ForEach(appState.hobbies) { hobby in
                    HobbyChip(hobby: hobby)
                }
            }
            .padding(.horizontal, SLCSpace.md)
        }
    }
}

private struct HobbyChip: View {
    let hobby: Hobby

    var body: some View {
        HStack(spacing: 4) {
            Text(hobby.emoji)
                .font(.system(size: 16))
            VStack(alignment: .leading, spacing: 0) {
                Text(hobby.title)
                    .font(SLCFont.body(SLCFontSize.bodyMedium, weight: .medium))
                    .foregroundColor(SLCColor.textPrimary)
                if let sub = hobby.subtitle {
                    Text(sub)
                        .font(SLCFont.caption(10))
                        .foregroundColor(SLCColor.textSecondary)
                }
            }
        }
        .padding(.horizontal, SLCSpace.sm)
        .padding(.vertical, 6)
        .background(SLCColor.creamLight)
        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
        .overlay(
            RoundedRectangle(cornerRadius: SLCRadius.md)
                .stroke(SLCColor.textTertiary.opacity(0.15), lineWidth: 1)
        )
    }
}

// MARK: - 留言
private struct StickyNotesSection: View {
    @EnvironmentObject var appState: AppState
    @Binding var showAddNote: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: SLCSpace.sm) {
            HStack {
                Text("留言板")
                    .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                Spacer()
                Button {
                    Haptics.impact(.light)
                    showAddNote = true
                } label: {
                    Image(systemName: "plus.circle")
                        .foregroundColor(SLCColor.herDeep)
                }
            }
            .padding(.horizontal, SLCSpace.md)

            if appState.stickyNotes.isEmpty {
                SLCEmptyView(icon: "note.text", title: "还没有留言", subtitle: "贴一张小纸条吧")
                    .padding(.top, 20)
            } else {
                LazyVGrid(
                    columns: [GridItem(.flexible(), spacing: SLCSpace.sm), GridItem(.flexible(), spacing: SLCSpace.sm)],
                    spacing: SLCSpace.sm
                ) {
                    ForEach(appState.stickyNotes) { note in
                        StickyNoteCard(note: note)
                    }
                }
                .padding(.horizontal, SLCSpace.md)
            }
        }
    }
}

private struct StickyNoteCard: View {
    let note: StickyNote

    var color: Color {
        Color(hex: note.color.hex)
    }

    var textColor: Color {
        switch note.color {
        case .black: return SLCColor.cream
        default: return SLCColor.textPrimary
        }
    }

    var body: some View {
        Text(note.content)
            .font(SLCFont.body(SLCFontSize.bodyMedium, weight: .medium))
            .foregroundColor(textColor)
            .padding(SLCSpace.md)
            .frame(maxWidth: .infinity, minHeight: 100, alignment: .topLeading)
            .background(color)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.sm))
            .rotationEffect(.degrees(note.rotation))
            .slcShadow(.soft)
    }
}

// MARK: - FlowLayout（简易换行布局）
private struct FlowLayout: Layout {
    let spacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }

        return CGSize(width: maxWidth, height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x: CGFloat = bounds.minX
        var y: CGFloat = bounds.minY
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

// MARK: - 添加留言
private struct AddStickyNoteSheet: View {
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    @State private var content = ""
    @State private var color: NoteColor = .cream

    var body: some View {
        NavigationView {
            VStack(spacing: SLCSpace.md) {
                TextField("写点什么...", text: $content, axis: .vertical)
                    .lineLimit(3...8)
                    .padding()
                    .background(SLCColor.creamLight)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))

                HStack {
                    ForEach(NoteColor.allCases, id: \.self) { c in
                        Circle()
                            .fill(Color(hex: c.hex))
                            .frame(width: 32, height: 32)
                            .overlay(
                                Circle().stroke(color == c ? SLCColor.him : Color.clear, lineWidth: 2)
                            )
                            .onTapGesture { color = c }
                    }
                }

                Spacer()
            }
            .padding(SLCSpace.md)
            .background(SLCColor.cream)
            .navigationTitle("贴留言")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") { dismiss() }.foregroundColor(SLCColor.textSecondary)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("贴上") {
                        guard let userId = appState.currentUser?.id else { return }
                        let note = StickyNote(
                            authorId: userId,
                            content: content,
                            color: color,
                            rotation: Double.random(in: -3...3)
                        )
                        appState.addNote(note)
                        dismiss()
                    }
                    .foregroundColor(SLCColor.him)
                    .fontWeight(.semibold)
                    .disabled(content.isEmpty)
                }
            }
        }
    }
}

// MARK: - 添加爱好
private struct AddHobbySheet: View {
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    @State private var title = ""
    @State private var subtitle = ""
    @State private var emoji = "✨"

    let emojis = ["☕️", "🍰", "📷", "✈️", "🎵", "📚", "🎨", "🎮", "⚽️", "🎬", "🍵", "🌸", "🐱", "🌿", "🎻"]

    var body: some View {
        NavigationView {
            VStack(spacing: SLCSpace.md) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: SLCSpace.sm) {
                        ForEach(emojis, id: \.self) { e in
                            Text(e)
                                .font(.system(size: 32))
                                .padding(8)
                                .background(emoji == e ? SLCColor.creamDeep : Color.clear)
                                .clipShape(RoundedRectangle(cornerRadius: SLCRadius.sm))
                                .onTapGesture { emoji = e }
                        }
                    }
                }

                TextField("爱好", text: $title)
                    .padding()
                    .background(SLCColor.creamLight)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))

                TextField("一句话描述（可选）", text: $subtitle)
                    .padding()
                    .background(SLCColor.creamLight)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))

                Spacer()
            }
            .padding(SLCSpace.md)
            .background(SLCColor.cream)
            .navigationTitle("添加爱好")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") { dismiss() }.foregroundColor(SLCColor.textSecondary)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("保存") {
                        guard let userId = appState.currentUser?.id, !title.isEmpty else { return }
                        let hobby = Hobby(
                            userId: userId,
                            title: title,
                            subtitle: subtitle.isEmpty ? nil : subtitle,
                            emoji: emoji
                        )
                        appState.addHobby(hobby)
                        dismiss()
                    }
                    .foregroundColor(SLCColor.him)
                    .fontWeight(.semibold)
                    .disabled(title.isEmpty)
                }
            }
        }
    }
}

#Preview {
    NavigationView { BoardView() }
        .environmentObject(AppState.shared)
}