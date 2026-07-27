# 数据同步设计文档

> 让两端数据通过 GitHub 私有仓库实时同步，全部内容端到端加密。

## 设计目标

- **两端互通**：你和她分别装在不同手机，数据自动同步
- **离线优先**：无网络时本地完整可用，恢复网络后自动同步
- **隐私安全**：内容用 AES-GCM-256 加密，密钥从配对码派生，GitHub 即使泄露也无法解密
- **零服务器**：用 GitHub 私有仓库做存储，免维护、免费、有版本历史
- **浪漫副产品**：天然版本控制，可以回看每一次修改（"我们说过的每一句话"）

## 架构

```
┌─────────────┐                ┌─────────────┐
│   你的手机   │                │  TA 的手机   │
│             │                │             │
│  ┌────────┐ │                │  ┌────────┐ │
│  │ Local  │ │                │  │ Local  │ │
│  │  DB    │ │                │  │  DB    │ │
│  └────┬───┘ │                │  └───┬───┘ │
│       │     │                │      │     │
│  ┌────▼───┐ │                │  ┌───▼────┐ │
│  │ Sync   │ │                │  │ Sync   │ │
│  │Manager │ │                │  │Manager │ │
│  └────┬───┘ │                │  └───┬────┘ │
│       │     │                │      │     │
│  ┌────▼───┐ │                │  ┌───▼────┐ │
│  │ Crypto │ │  AES-GCM-256   │  │ Crypto │ │
│  │ AES-GCM│ │                │  │ AES-GCM│ │
│  └────┬───┘ │                │  └───┬────┘ │
│       │     │                │      │     │
│  ┌────▼───┐ │                │  ┌───▼────┐ │
│  │GitHub  │ │  HTTPS+Token   │  │GitHub  │ │
│  │Client  │◄┼───────────────►│  │Client  │ │
│  └────────┘ │                │  └────────┘ │
└─────────────┘                └─────────────┘
                    │
                    ▼
        ┌──────────────────────┐
        │ GitHub 私有仓库        │
        │  (你的账号)            │
        │                       │
        │  data/                │
        │  ├── messages.enc     │ ← AES-GCM 加密
        │  ├── diary.enc        │
        │  ├── todos.enc        │
        │  ├── buckets.enc      │
        │  ├── anniversaries.enc│
        │  ├── capsules.enc     │
        │  ├── notes.enc        │
        │  ├── hobbies.enc      │
        │  ├── locations.enc    │
        │  └── photos/          │ ← Base64 后加密
        │      ├── xxx.enc      │
        │      └── ...          │
        └──────────────────────┘
```

## 加密方案

### 密钥派生
- **算法**：PBKDF2-HMAC-SHA256
- **迭代次数**：100,000（移动端平衡安全与性能）
- **盐**：固定常量（`SLC-Lovers-v1-salt`）——盐的目的是防彩虹表，常量即可
- **输入**：配对码（6 位数字）+ 配对 ID（UUID，创建时生成）
- **输出**：32 字节 AES 密钥

> 注：6 位配对码本身较弱（仅 100 万种），所以加上 UUID 作为盐的一部分。攻击者要破解必须同时知道配对码和配对 ID。

### 加密算法
- **算法**：AES-256-GCM
- **IV**：每次加密随机生成 12 字节
- **认证标签**：16 字节
- **输出格式**：`IV(12) || Ciphertext || Tag(16)` → Base64

### 密钥不离开设备
密钥只存在内存和 Keychain/Keystore 中，**绝不上传**。即使仓库和 Token 都泄露，没有配对码也无法解密。

## GitHub API 使用

### 端点
- `GET /repos/{owner}/{repo}/contents/{path}` —— 拉取文件（含 sha）
- `PUT /repos/{owner}/{repo}/contents/{path}` —— 上传/更新文件（需带 sha）
- `DELETE /repos/{owner}/{repo}/contents/{path}` —— 删除文件
- `GET /repos/{owner}/{repo}/git/blobs/{sha}` —— 拉取大文件（备用）

### 认证
- Header：`Authorization: Bearer {token}`
- Token 类型：GitHub Personal Access Token（classic），权限只需 `repo`（私有仓库读写）
- Token 存储位置：
  - iOS：Keychain（kSecClassGenericPassword）
  - Android：EncryptedSharedPreferences

### 限流
- 认证用户 5000 req/hour，对情侣双人应用绰绰有余
- 本地用 ETag 缓存 + If-None-Match，减少请求

## 同步策略

### 触发时机
1. **APP 启动**：拉取一次
2. **写操作后**：debounce 5 秒后推送（防抖避免频繁请求）
3. **APP 进入前台**：拉取一次
4. **定时**：每 5 分钟拉一次（用 WorkManager / BGTaskScheduler）
5. **手动**：Settings 中"立即同步"按钮

### 拉取流程（Pull）
```
1. GET /repos/.../contents/data/{file}.enc
2. Base64 decode → 拿到 ciphertext
3. AES-GCM 解密 → 拿到 JSON
4. 与本地数据合并：
   - 按条目的 updatedAt 时间戳
   - 远程 > 本地 → 覆盖本地
   - 本地 > 远程 → 保留本地（等下次 push）
   - 时间戳相同 → 保留任意一方
5. 保存合并结果到本地 DB
```

### 推送流程（Push）
```
1. 读取本地 DB 全量数据
2. JSON 序列化
3. AES-GCM 加密 → ciphertext
4. Base64 编码
5. PUT /repos/.../contents/data/{file}.enc
   body: {
     message: "sync: {file} at {timestamp}",
     content: "{base64}",
     sha: "{previous_sha}"  // 必须带，否则冲突
   }
6. 如果返回 409（冲突）→ 重新拉取 → 合并 → 重试
```

### 冲突解决
- **同一文件并发修改**：last-write-wins（用时间戳判断）
- **删除 vs 修改**：修改优先（避免误删）
- **重试上限**：3 次，超过则放弃本次推送，等下次

## 文件结构

仓库内文件组织：
```
data/
├── meta.enc              ← 元信息（配对 ID、版本号、最后同步时间）
├── messages.enc          ← 聊天记录
├── diary.enc             ← 日记
├── todos.enc             ← 待办
├── buckets.enc           ← 愿望清单
├── anniversaries.enc     ← 纪念日
├── capsules.enc          ← 时光胶囊
├── notes.enc             ← 留言
├── hobbies.enc           ← 爱好
├── locations.enc         ← 位置记录
└── photos/
    ├── {uuid}.enc        ← 每张照片一个文件
    └── ...
```

## 安全注意事项

1. **Token 永不外泄**：只存在本地加密存储中，不上传、不打印、不日志
2. **配对码不存明文**：只存派生后的密钥（Keychain/Keystore）
3. **HTTPS 强制**：GitHub API 默认 HTTPS，但代码中显式校验
4. **错误处理不暴露信息**：日志只记 "sync failed"，不记具体内容
5. **离线降级**：网络失败时静默降级到本地模式，不打扰用户
6. **可随时关闭**：Settings 中可关闭同步，APP 仍可用本地数据
7. **解除配对**：清空本地数据 + 询问是否删除远程仓库数据

## Token 获取指引（用户操作）

1. 打开 https://github.com/settings/tokens
2. 点 "Generate new token (classic)"
3. Note 填 `SLC-Lovers`
4. Expiration 选 `No expiration`（或 1 年后手动续）
5. 勾选权限：`repo`（完整仓库访问）
6. 生成后**立即复制**（只显示一次）
7. 在 APP 设置中粘贴 Token、Owner（用户名）、Repo（仓库名）
8. APP 会自动创建 `data/` 目录和初始文件

## 仓库初始化

APP 检测到 Token 配置完成后：
1. 调用 `GET /repos/{owner}/{repo}` 检查仓库是否存在
2. 不存在则提示用户去 GitHub 创建私有仓库
3. 仓库存在则尝试 `GET /repos/.../contents/data/meta.enc`
4. 不存在则上传初始 meta.enc（含配对 ID、版本号 v1）
5. 存在则拉取所有 .enc 文件并解密合并

## 性能指标

- 单次同步耗时：< 3 秒（5 个文件并发拉取）
- 加密/解密耗时：< 100ms（10KB JSON）
- 流量消耗：单次同步 < 50KB（不含照片）
- 电池影响：每小时同步一次约 0.1% 电量

## 后续可扩展

1. **WebSocket 实时推送**：用 GitHub Webhook + 自建中转，实现真·实时
2. **图床集成**：接入 sm.ms / 路过图床，照片走 URL
3. **多端**：网页版、桌面版（Electron）
4. **导出/迁移**：一键导出为 zip，迁移到其他存储后端