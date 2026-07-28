import SwiftUI

/// 配对引导页
struct PairingView: View {
    @EnvironmentObject var appState: AppState
    @State private var step: Step = .intro

    enum Step {
        case intro       // 介绍
        case role        // 选择角色
        case name        // 输入名字
        case startDate   // 设置在一起日期
        case createOrJoin // 创建/加入
        case create      // 创建配对（展示配对码）
        case join        // 输入配对码
        case success     // 成功
    }

    var body: some View {
        ZStack {
            SLCColor.cream.ignoresSafeArea()
            VStack {
                switch step {
                case .intro: IntroStep(onNext: { step = .role })
                case .role: RoleStep(onNext: { step = .name })
                case .name: NameStep(onNext: { step = .startDate })
                case .startDate: StartDateStep(onNext: { step = .createOrJoin })
                case .createOrJoin: CreateOrJoinStep(
                    onCreate: { step = .create },
                    onJoin: { step = .join }
                )
                case .create: CreateStep(onSuccess: { step = .success })
                case .join: JoinStep(onSuccess: { step = .success })
                case .success: SuccessStep()
                }
            }
        }
    }
}

// MARK: - 介绍
private struct IntroStep: View {
    let onNext: () -> Void

    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            // Logo
            VStack(spacing: SLCSpace.md) {
                Text("S & LC")
                    .font(.system(size: 64, weight: .regular, design: .serif))
                    .foregroundColor(SLCColor.him)
                Text("Just for the two of us")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
                    .tracking(2)
            }
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("一个只属于两个人的私密空间")
                    .font(SLCFont.title(18, weight: .medium))
                    .foregroundColor(SLCColor.textPrimary)
                    .multilineTextAlignment(.center)
                Text("记录爱 · 守护时光 · 珍藏回忆")
                    .font(SLCFont.body(14))
                    .foregroundColor(SLCColor.textSecondary)
            }
            .padding(.horizontal, SLCSpace.xl)
            Spacer()
            SLCPrimaryButton(title: "开始", action: onNext)
                .padding(.horizontal, SLCSpace.lg)
                .padding(.bottom, SLCSpace.xxl)
        }
    }
}

// MARK: - 角色选择
private struct RoleStep: View {
    @EnvironmentObject var appState: AppState
    let onNext: () -> Void
    @State private var selectedRole: UserRole? = nil

    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("你是？")
                    .font(SLCFont.title(28, weight: .semibold))
                    .foregroundColor(SLCColor.textPrimary)
                Text("选一个属于你的颜色")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
            }

            HStack(spacing: SLCSpace.lg) {
                RoleCard(role: .him, selected: selectedRole == .him, onTap: { selectedRole = .him })
                RoleCard(role: .her, selected: selectedRole == .her, onTap: { selectedRole = .her })
            }
            .padding(.horizontal, SLCSpace.xl)

            Spacer()
            SLCPrimaryButton(title: "下一步", action: onNext)
                .padding(.horizontal, SLCSpace.lg)
                .opacity(selectedRole == nil ? 0.4 : 1)
                .disabled(selectedRole == nil)
                .padding(.bottom, SLCSpace.xxl)
        }
        .onChange(of: selectedRole) { _, new in
            if let role = new {
                appState.tempRole = role
            }
        }
    }
}

private struct RoleCard: View {
    let role: UserRole
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: {
            Haptics.selection()
            onTap()
        }) {
            VStack(spacing: SLCSpace.md) {
                Circle()
                    .fill(SLCColor.person(role))
                    .frame(width: 80, height: 80)
                    .overlay(
                        Text(role.displayName)
                            .font(.system(size: 32, weight: .regular, design: .serif))
                            .foregroundColor(SLCColor.cream)
                    )
                Text(role == .him ? "沉稳 · 墨黑" : "温柔 · 玫瑰金")
                    .font(SLCFont.caption(12))
                    .foregroundColor(SLCColor.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, SLCSpace.lg)
            .background(SLCColor.creamLight)
            .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
            .overlay(
                RoundedRectangle(cornerRadius: SLCRadius.lg)
                    .stroke(selected ? SLCColor.person(role) : Color.clear, lineWidth: 2)
            )
            .scaleEffect(selected ? 1.05 : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.7), value: selected)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - 输入名字
private struct NameStep: View {
    @EnvironmentObject var appState: AppState
    let onNext: () -> Void
    @State private var name = ""

    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("给自己起个昵称")
                    .font(SLCFont.title(28, weight: .semibold))
                Text("对方会看到这个名字")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
            }

            TextField("例如：小柚", text: $name)
                .font(SLCFont.title(22))
                .multilineTextAlignment(.center)
                .padding()
                .background(SLCColor.creamLight)
                .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))
                .padding(.horizontal, SLCSpace.xl)

            Spacer()
            SLCPrimaryButton(title: "下一步", action: onNext)
                .padding(.horizontal, SLCSpace.lg)
                .opacity(name.isEmpty ? 0.4 : 1)
                .disabled(name.isEmpty)
                .padding(.bottom, SLCSpace.xxl)
        }
        .onChange(of: name) { _, new in
            appState.tempName = new
        }
    }
}

// MARK: - 设置在一起日期
private struct StartDateStep: View {
    @EnvironmentObject var appState: AppState
    let onNext: () -> Void
    @State private var date = Date()

    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("你们是什么时候在一起的？")
                    .font(SLCFont.title(24, weight: .semibold))
                    .multilineTextAlignment(.center)
                Text("我们会从这一天开始计算")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
            }
            .padding(.horizontal, SLCSpace.lg)

            DatePicker("", selection: $date, in: ...Date(), displayedComponents: .date)
                .datePickerStyle(.graphical)
                .tint(SLCColor.person(appState.tempRole ?? .him))
                .padding(.horizontal, SLCSpace.lg)

            Spacer()
            SLCPrimaryButton(title: "下一步", action: onNext)
                .padding(.horizontal, SLCSpace.lg)
                .padding(.bottom, SLCSpace.xxl)
        }
        .onChange(of: date) { _, new in
            appState.tempStartDate = new
        }
        .onAppear {
            if let saved = appState.tempStartDate { date = saved }
        }
    }
}

// MARK: - 创建 / 加入
private struct CreateOrJoinStep: View {
    let onCreate: () -> Void
    let onJoin: () -> Void

    var body: some View {
        VStack(spacing: SLCSpace.lg) {
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("怎么开始？")
                    .font(SLCFont.title(28, weight: .semibold))
                Text("选择一方先发起连接")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
            }

            VStack(spacing: SLCSpace.md) {
                SLCPrimaryButton(title: "我先创建连接", action: onCreate)
                SLCSecondaryButton(title: "对方已经创建，我加入", action: onJoin)
            }
            .padding(.horizontal, SLCSpace.lg)

            Spacer()
        }
    }
}

// MARK: - 创建连接
private struct CreateStep: View {
    @EnvironmentObject var appState: AppState
    let onSuccess: () -> Void
    @State private var code = Pairing.generateCode()
    @State private var showCopied = false

    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("把这个码发给 TA")
                    .font(SLCFont.title(24, weight: .semibold))
                Text("对方在另一台手机上输入即可连接")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
            }
            .padding(.horizontal, SLCSpace.lg)

            // 配对码展示
            VStack(spacing: SLCSpace.md) {
                Text(code)
                    .font(.system(size: 56, weight: .light, design: .serif))
                    .foregroundColor(SLCColor.him)
                    .tracking(8)
                    .padding(.horizontal, SLCSpace.lg)
                    .padding(.vertical, SLCSpace.lg)
                    .background(SLCColor.creamLight)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))

                Button {
                    UIPasteboard.general.string = code
                    Haptics.notify(.success)
                    showCopied = true
                } label: {
                    Label(showCopied ? "已复制" : "复制配对码", systemImage: showCopied ? "checkmark" : "doc.on.doc")
                        .font(SLCFont.body(14, weight: .medium))
                        .foregroundColor(SLCColor.herDeep)
                }
            }

            Spacer()

            VStack(spacing: SLCSpace.sm) {
                Text("等待对方输入...")
                    .font(SLCFont.body(13))
                    .foregroundColor(SLCColor.textSecondary)

                SLCPrimaryButton(title: "对方已输入，继续", action: {
                    let role = appState.tempRole ?? .him
                    appState.createPairing(
                        name: appState.tempName ?? "我",
                        role: role,
                        startDate: appState.tempStartDate ?? Date()
                    )
                    onSuccess()
                })
                .padding(.horizontal, SLCSpace.lg)
            }
            .padding(.bottom, SLCSpace.xxl)
        }
    }
}

// MARK: - 加入连接
private struct JoinStep: View {
    @EnvironmentObject var appState: AppState
    let onSuccess: () -> Void
    @State private var inputCode = ""

    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            VStack(spacing: SLCSpace.sm) {
                Text("输入对方的配对码")
                    .font(SLCFont.title(24, weight: .semibold))
                Text("6 位数字")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
            }

            TextField("", text: $inputCode)
                .font(.system(size: 48, weight: .light, design: .serif))
                .multilineTextAlignment(.center)
                .tracking(8)
                .keyboardType(.numberPad)
                .padding()
                .background(SLCColor.creamLight)
                .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))
                .padding(.horizontal, SLCSpace.xl)
                .onChange(of: inputCode) { _, new in
                    inputCode = String(new.filter { $0.isNumber }.prefix(6))
                }

            Spacer()
            SLCPrimaryButton(title: "连接", action: {
                let role = appState.tempRole ?? .her
                let success = appState.joinPairing(
                    name: appState.tempName ?? "TA",
                    role: role,
                    code: inputCode,
                    startDate: appState.tempStartDate ?? Date()
                )
                if success {
                    Haptics.notify(.success)
                    onSuccess()
                } else {
                    Haptics.notify(.error)
                }
            })
            .padding(.horizontal, SLCSpace.lg)
            .opacity(inputCode.count == 6 ? 1 : 0.4)
            .disabled(inputCode.count != 6)
            .padding(.bottom, SLCSpace.xxl)
        }
    }
}

// MARK: - 成功
private struct SuccessStep: View {
    var body: some View {
        VStack(spacing: SLCSpace.xl) {
            Spacer()
            VStack(spacing: SLCSpace.lg) {
                Image(systemName: "heart.circle.fill")
                    .font(.system(size: 96))
                    .foregroundColor(SLCColor.her)
                Text("连接成功")
                    .font(SLCFont.title(32, weight: .semibold))
                    .foregroundColor(SLCColor.him)
                Text("愿我们携手，从心动走到古稀。")
                    .font(SLCFont.body(15))
                    .foregroundColor(SLCColor.textSecondary)
                    .multilineTextAlignment(.center)
            }
            Spacer()
        }
    }
}

// MARK: - AppState 扩展（配对向导临时数据）
extension AppState {
    @MainActor
    var tempRole: UserRole? {
        get {
            if let raw: String = Prefs.get(Prefs.Key.currentUserId + ".role") {
                return UserRole(rawValue: raw)
            }
            return nil
        }
        set {
            if let role = newValue {
                Prefs.set(role.rawValue, forKey: Prefs.Key.currentUserId + ".role")
            } else {
                Prefs.remove(Prefs.Key.currentUserId + ".role")
            }
        }
    }

    @MainActor
    var tempName: String? {
        get { Prefs.get(Prefs.Key.currentUserId + ".name") }
        set {
            if let name = newValue {
                Prefs.set(name, forKey: Prefs.Key.currentUserId + ".name")
            } else {
                Prefs.remove(Prefs.Key.currentUserId + ".name")
            }
        }
    }

    @MainActor
    var tempStartDate: Date? {
        get { Prefs.get(Prefs.Key.startDate) }
        set {
            if let date = newValue {
                Prefs.set(date, forKey: Prefs.Key.startDate)
            }
        }
    }
}