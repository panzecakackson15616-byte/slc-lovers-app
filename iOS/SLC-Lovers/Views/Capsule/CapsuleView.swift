import SwiftUI

/// 时光胶囊列表
struct CapsuleView: View {
    @EnvironmentObject var appState: AppState
    @State private var showCreate = false

    var body: some View {
        ScrollView {
            VStack(spacing: SLCSpace.md) {
                if appState.capsules.isEmpty {
                    SLCEmptyView(
                        icon: "hourglass",
                        title: "还没有胶囊",
                        subtitle: "给未来的你们写封信"
                    )
                    .padding(.top, 60)
                } else {
                    ForEach(appState.capsules) { capsule in
                        NavigationLink(destination: CapsuleDetailView(capsule: capsule)) {
                            CapsuleCard(capsule: capsule)
                        }
                        .buttonStyle(.plain)
                        .padding(.horizontal, SLCSpace.md)
                    }
                }
            }
            .padding(.vertical, SLCSpace.md)
        }
        .background(SLCColor.cream)
        .navigationTitle("时光胶囊")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    Haptics.impact(.light)
                    showCreate = true
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(SLCColor.him)
                }
            }
        }
        .sheet(isPresented: $showCreate) {
            CreateCapsuleView()
        }
    }
}

private struct CapsuleCard: View {
    let capsule: TimeCapsule

    var body: some View {
        SLCCard {
            VStack(alignment: .leading, spacing: SLCSpace.sm) {
                HStack {
                    Image(systemName: capsule.canUnlock() ? "envelope.open.fill" : "envelope.fill")
                        .font(.system(size: 28))
                        .foregroundColor(capsule.canUnlock() ? SLCColor.herDeep : SLCColor.him)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(capsule.title)
                            .font(SLCFont.title(SLCFontSize.titleSmall, weight: .semibold))
                            .foregroundColor(SLCColor.textPrimary)
                        Text("解封于 \(DateUtils.fullChinese(capsule.unlockDate))")
                            .font(SLCFont.caption(SLCFontSize.bodySmall))
                            .foregroundColor(SLCColor.textSecondary)
                    }
                    Spacer()
                }
                if !capsule.canUnlock() && !capsule.isUnlocked {
                    HStack(spacing: 4) {
                        Image(systemName: "clock")
                            .font(.system(size: 12))
                        Text(DateUtils.countdownDescription(until: capsule.unlockDate))
                    }
                    .font(SLCFont.body(SLCFontSize.bodyMedium, weight: .medium))
                    .foregroundColor(SLCColor.herDeep)
                    .padding(.horizontal, SLCSpace.sm)
                    .padding(.vertical, SLCSpace.xs)
                    .background(SLCColor.herSoft.opacity(0.4))
                    .clipShape(Capsule())
                } else if capsule.isUnlocked {
                    Label("已解封", systemImage: "checkmark.seal.fill")
                        .font(SLCFont.body(SLCFontSize.bodySmall, weight: .medium))
                        .foregroundColor(SLCColor.success)
                } else {
                    Label("可以解封了", systemImage: "sparkles")
                        .font(SLCFont.body(SLCFontSize.bodySmall, weight: .medium))
                        .foregroundColor(SLCColor.herDeep)
                }
            }
        }
    }
}

// MARK: - 详情页
struct CapsuleDetailView: View {
    let capsule: TimeCapsule
    @EnvironmentObject var appState: AppState
    @State private var showUnlock = false

    var body: some View {
        ScrollView {
            VStack(spacing: SLCSpace.lg) {
                if capsule.isUnlocked {
                    // 已解封 - 显示内容
                    VStack(spacing: SLCSpace.md) {
                        Text(capsule.title)
                            .font(SLCFont.title(28, weight: .semibold))
                            .foregroundColor(SLCColor.textPrimary)
                        Text(DateUtils.fullChinese(capsule.unlockedAt ?? capsule.unlockDate))
                            .font(SLCFont.caption(SLCFontSize.bodySmall))
                            .foregroundColor(SLCColor.textSecondary)
                        SLCCard {
                            Text(capsule.content)
                                .font(SLCFont.body(SLCFontSize.bodyLarge))
                                .foregroundColor(SLCColor.textPrimary)
                                .lineSpacing(6)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .padding(.horizontal, SLCSpace.lg)
                    .padding(.top, SLCSpace.lg)
                } else if capsule.canUnlock() {
                    // 可解封
                    VStack(spacing: SLCSpace.lg) {
                        Image(systemName: "envelope.open.fill")
                            .font(.system(size: 96))
                            .foregroundColor(SLCColor.her)
                        Text("可以解封了")
                            .font(SLCFont.title(28, weight: .semibold))
                        Text(DateUtils.countdownDescription(until: capsule.unlockDate))
                            .font(SLCFont.body(15))
                            .foregroundColor(SLCColor.textSecondary)
                        SLCPrimaryButton(title: "拆开胶囊", action: {
                            showUnlock = true
                        })
                        .frame(maxWidth: 240)
                    }
                    .padding(.top, 80)
                } else {
                    // 未到期 - 锁住状态
                    VStack(spacing: SLCSpace.lg) {
                        Image(systemName: "envelope.fill")
                            .font(.system(size: 80))
                            .foregroundColor(SLCColor.him)
                        VStack(spacing: SLCSpace.xs) {
                            Text(capsule.title)
                                .font(SLCFont.title(22, weight: .semibold))
                            Text("解封于 \(DateUtils.fullChinese(capsule.unlockDate))")
                                .font(SLCFont.body(14))
                                .foregroundColor(SLCColor.textSecondary)
                        }
                        VStack(spacing: 4) {
                            Text("还剩")
                                .font(SLCFont.caption(13))
                                .foregroundColor(SLCColor.textSecondary)
                            Text(DateUtils.countdownDescription(until: capsule.unlockDate))
                                .font(.system(size: 36, weight: .light, design: .serif))
                                .foregroundColor(SLCColor.herDeep)
                        }
                        .padding(.vertical, SLCSpace.lg)
                        Text("🔒 内容已封存")
                            .font(SLCFont.body(SLCFontSize.bodyMedium))
                            .foregroundColor(SLCColor.textSecondary)
                    }
                    .padding(.top, 80)
                }
            }
        }
        .background(SLCColor.cream)
        .navigationTitle("时光胶囊")
        .navigationBarTitleDisplayMode(.inline)
        .alert("拆开胶囊？", isPresented: $showUnlock) {
            Button("拆开") { appState.unlockCapsule(capsule); Haptics.notify(.success) }
            Button("取消", role: .cancel) {}
        } message: {
            Text("这是一封写给过去的信，拆开后内容将无法再隐藏。")
        }
    }
}

// MARK: - 创建胶囊
private struct CreateCapsuleView: View {
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    @State private var title = ""
    @State private var content = ""
    @State private var unlockDate = Calendar.current.date(byAdding: .month, value: 6, to: Date()) ?? Date()

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: SLCSpace.md) {
                    TextField("标题", text: $title)
                        .font(SLCFont.title(SLCFontSize.titleSmall, weight: .medium))
                        .padding()
                        .background(SLCColor.creamLight)
                        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))

                    TextField("写给未来的你们...", text: $content, axis: .vertical)
                        .lineLimit(8...16)
                        .padding()
                        .background(SLCColor.creamLight)
                        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))

                    DatePicker("解封日期", selection: $unlockDate, in: Date()..., displayedComponents: .date)
                        .padding()
                        .background(SLCColor.creamLight)
                        .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
                }
                .padding(SLCSpace.md)
            }
            .background(SLCColor.cream)
            .navigationTitle("新建胶囊")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") { dismiss() }.foregroundColor(SLCColor.textSecondary)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("封存") {
                        guard let userId = appState.currentUser?.id,
                              !title.isEmpty, !content.isEmpty else { return }
                        let capsule = TimeCapsule(
                            authorId: userId,
                            title: title,
                            content: content,
                            unlockDate: unlockDate
                        )
                        appState.addCapsule(capsule)
                        dismiss()
                    }
                    .foregroundColor(SLCColor.him)
                    .fontWeight(.semibold)
                    .disabled(title.isEmpty || content.isEmpty)
                }
            }
        }
    }
}

#Preview {
    NavigationView { CapsuleView() }
        .environmentObject(AppState.shared)
}