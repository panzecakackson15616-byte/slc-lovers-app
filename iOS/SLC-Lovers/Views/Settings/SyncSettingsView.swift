import SwiftUI

/// 同步设置页
struct SyncSettingsView: View {
    @StateObject private var syncManager = SyncManager.shared
    @EnvironmentObject var appState: AppState

    @State private var token = ""
    @State private var owner = ""
    @State private var repo = ""
    @State private var isVerifying = false
    @State private var verifyResult: String?
    @State private var showClearConfirm = false

    var body: some View {
        Form {
            // 状态
            Section {
                HStack {
                    Text("同步状态")
                    Spacer()
                    if syncManager.isSyncing {
                        ProgressView().scaleEffect(0.8)
                        Text("同步中…").foregroundColor(.secondary).font(.caption)
                    } else if syncManager.isConfigured {
                        Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
                        Text("已配置").foregroundColor(.secondary).font(.caption)
                    } else {
                        Image(systemName: "exclamationmark.triangle.fill").foregroundColor(.orange)
                        Text("未配置").foregroundColor(.secondary).font(.caption)
                    }
                }

                if let last = syncManager.lastSyncDate {
                    HStack {
                        Text("上次同步")
                        Spacer()
                        Text(DateUtils.friendlyRelative(from: last))
                            .foregroundColor(.secondary).font(.caption)
                    }
                }

                if let err = syncManager.lastError {
                    Text(err)
                        .foregroundColor(.red)
                        .font(.caption)
                }
            } header: {
                Text("状态")
            }

            // 配置
            Section {
                TextField("GitHub Username", text: $owner)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                TextField("Repository Name", text: $repo)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                SecureField("Personal Access Token", text: $token)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
            } header: {
                Text("GitHub 配置")
            } footer: {
                VStack(alignment: .leading, spacing: 8) {
                    Text("1. 在 GitHub 创建一个私有仓库")
                    Text("2. 去 github.com/settings/tokens 生成 Token (classic)")
                    Text("3. 勾选 repo 权限")
                    Text("4. 把 Token 粘贴到上方")
                    Text("⚠️ Token 只存在本机 Keychain，不会上传任何地方")
                        .foregroundColor(.orange)
                }
                .font(.caption)
                .padding(.top, 4)
            }

            // 操作
            Section {
                Button {
                    Task { await verify() }
                } label: {
                    HStack {
                        if isVerifying { ProgressView().scaleEffect(0.8) }
                        Text(isVerifying ? "验证中…" : "验证配置")
                    }
                }
                .disabled(!canSave || isVerifying)

                if let result = verifyResult {
                    Text(result)
                        .font(.caption)
                        .foregroundColor(result.contains("✓") ? .green : .red)
                }

                Button {
                    saveConfig()
                } label: {
                    Text("保存配置")
                        .fontWeight(.semibold)
                }
                .disabled(!canSave)

                if syncManager.isConfigured {
                    Button {
                        Task { await syncManager.pullAll() }
                    } label: {
                        Label("立即拉取", systemImage: "arrow.down.circle")
                    }

                    Button {
                        Task { await syncManager.pushAllNow() }
                    } label: {
                        Label("立即推送", systemImage: "arrow.up.circle")
                    }
                }
            }

            // 同步开关
            Section {
                Toggle("自动同步", isOn: Binding(
                    get: { syncManager.syncEnabled },
                    set: { syncManager.syncEnabled = $0 }
                ))
            } header: {
                Text("选项")
            } footer: {
                Text("关闭后 APP 仍可正常使用本地数据，但不会上传到 GitHub")
            }

            // 危险操作
            Section {
                Button(role: .destructive) {
                    showClearConfirm = true
                } label: {
                    Label("清空同步配置", systemImage: "trash")
                }
                .disabled(!syncManager.isConfigured)
            }
        }
        .navigationTitle("数据同步")
        .navigationBarTitleDisplayMode(.inline)
        .alert("清空同步配置？", isPresented: $showClearConfirm) {
            Button("清空", role: .destructive) {
                syncManager.clearConfig()
                token = ""
                owner = ""
                repo = ""
                verifyResult = nil
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("清空后本地数据保留，但不再自动同步。")
        }
        .onAppear {
            // 加载已保存的配置（仅显示 owner 和 repo，token 不显示）
            if syncManager.isConfigured {
                owner = SecureStorage.loadString(forKey: SecureStorage.Key.githubOwner) ?? ""
                repo = SecureStorage.loadString(forKey: SecureStorage.Key.githubRepo) ?? ""
                token = "" // 出于安全，不回显 token
            }
        }
    }

    private var canSave: Bool {
        !token.isEmpty && !owner.isEmpty && !repo.isEmpty
    }

    private func saveConfig() {
        syncManager.saveConfig(token: token, owner: owner, repo: repo)
        // 同步配对码用于加密
        if let code = appState.pairing?.code,
           let id = appState.pairing?.id {
            syncManager.savePairingForCrypto(code: code, pairingId: id.uuidString)
        }
        // 启动自动同步
        syncManager.startAutoSync()
        // 立即拉取一次
        Task { await syncManager.pullAll() }
        verifyResult = "✓ 配置已保存"
    }

    private func verify() async {
        isVerifying = true
        defer { isVerifying = false }
        // 临时保存配置以便验证
        syncManager.saveConfig(token: token, owner: owner, repo: repo)
        let ok = await syncManager.verifyConfig()
        verifyResult = ok ? "✓ 仓库可访问" : "✗ 仓库不存在或 Token 无效"
    }
}