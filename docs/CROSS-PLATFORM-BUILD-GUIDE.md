# iOS APP 云编译 & 安装指南

> 你在 Windows 上，但需要给 iPhone 装 APP。本指南用 GitHub Actions 云端编译，无需 Mac。

---

## 📋 总览

```
你的 Windows 电脑                  GitHub 云端                    iPhone
       │                              │                            │
       │ 1. Push 代码到 GitHub         │                            │
       │─────────────────────────────►│                            │
       │                              │                            │
       │ 2. 触发 GitHub Action         │                            │
       │  (在 macos-latest runner)     │                            │
       │                              │                            │
       │ 3. 自动编译，生成 .ipa         │                            │
       │                              │                            │
       │ 4. 下载 .ipa                  │                            │
       │◄─────────────────────────────│                            │
       │                              │                            │
       │ 5. 用 AltStore/Sideloadly     │                            │
       │    签名并安装                                              │
       │─────────────────────────────────────────────────────────►│
```

---

## 阶段 1 · 把项目代码上传到 GitHub（约 5 分钟）

你已经有 GitHub 账号了（之前创建 `our-private-space` 仓库时用过）。但**编译用的代码仓库**应该是**另一个仓库**（和存同步数据的仓库分开）。

### 1.1 创建代码仓库

1. 访问 https://github.com/new
2. 填写：
   - Repository name: `slc-lovers-app`（或你喜欢的名字）
   - Description: `情侣专属 APP 源代码`
   - **Visibility: Private**（私有，保护源代码）
   - 其他都不勾
3. 点 `Create repository`

### 1.2 在你 Windows 电脑上装 Git

如果还没装：
- 访问 https://git-scm.com/download/win
- 下载 64-bit Git for Windows Setup
- 一路下一步安装

### 1.3 把项目代码 push 上去

打开 **Git Bash**（开始菜单搜），依次执行：

```bash
# 进入项目目录
cd "C:/Users/12558/WorkBuddy/2026-07-27-20-50-32/SLC-Lovers"

# 初始化 Git 仓库
git init

# 配置你的身份（只需一次）
git config user.name "你的名字"
git config user.email "你的邮箱@example.com"

# 添加所有文件
git add .

# 第一次提交
git commit -m "Initial commit: SLC-Lovers app"

# 把 main 分支重命名（GitHub 默认 main）
git branch -M main

# 关联远程仓库（把 your-name 换成你的 GitHub 用户名）
git remote add origin https://github.com/your-name/slc-lovers-app.git

# 推送
git push -u origin main
```

第一次 push 会让你登录 GitHub，按提示输入用户名和密码（密码要用 Token，不是登录密码）：
- Username: 你的 GitHub 用户名
- Password: 用之前生成的 Token（或新生成一个 Personal Access Token）

> 💡 如果嫌麻烦，可以装 **GitHub Desktop**（https://desktop.github.com），图形化界面更简单。

---

## 阶段 2 · 触发 GitHub Actions 编译（约 15 分钟）

代码 push 上去后，GitHub Actions 会**自动开始编译**（因为我配的 workflow 在 push 到 main 时触发）。

### 2.1 查看编译进度

1. 访问你的代码仓库：`https://github.com/your-name/slc-lovers-app`
2. 点顶部 Tab 栏的 **`Actions`**
3. 左侧能看到两个 workflow：
   - `Build iOS App`
   - `Build Android App`
4. 点最新的运行，能看到实时日志

### 2.2 等待编译完成

- iOS 编译约 10-15 分钟（macOS runner 启动较慢）
- Android 编译约 5-10 分钟
- 编译过程中你会看到一个黄色圆圈 🟡 转动
- 完成后变成绿色对勾 ✅

### 2.3 手动触发（如果没自动触发）

如果没看到自动触发，可以手动：
1. 点左侧的 `Build iOS App`
2. 右侧点 `Run workflow` 按钮
3. 选择 `main` 分支，点绿色 `Run workflow`

---

## 阶段 3 · 下载编译产物（约 2 分钟）

### 3.1 下载 iOS IPA

1. 在 Actions 页面点开刚完成的运行
2. 滚到页面最底部，找到 **`Artifacts`** 区域
3. 看到 `SLC-Lovers-iOS-IPA`，点它下载
4. 解压后得到 `SLC-Lovers-simulator.ipa`

### 3.2 下载 Android APK

同样方式，下载 `SLC-Lovers-Android-APK`，解压得到 `app-debug.apk`

---

## 阶段 4 · 安装到手机

### 4.1 Android（简单）

1. 把 `app-debug.apk` 传到安卓手机（微信/QQ/USB 都行）
2. 在手机上点开 APK 文件
3. 如果提示"未知来源"，按提示开启权限
4. 安装完成 → 打开 APP

### 4.2 iOS（需要 Apple ID，免费）

iOS 安装比 Android 麻烦，因为苹果限制。**两种方案**：

#### 方案 A：用 Sideloadly（推荐，Windows 友好）

1. **下载 Sideloadly**：访问 https://sideloadly.io，下载 Windows 版安装

2. **用 USB 连接 iPhone 到电脑**

3. **打开 Sideloadly**：
   - Apple ID：填你的 Apple ID（用于免费签名）
   - IPA 路径：选刚下载的 `SLC-Lovers-simulator.ipa`
   - 点 `Start` 开始

4. **首次会要求在 iPhone 上信任开发者**：
   - iPhone 设置 → 通用 → VPN与设备管理
   - 找到你的 Apple ID，点"信任"

5. **完成**！打开 APP 就能用了

> ⚠️ **免费 Apple ID 限制**：
> - 签名有效期 **7 天**，过期后需要重新用 Sideloadly 签一次
> - 同一 Apple ID 最多签 3 个 APP
> - 解决方法：装 AltStore（自动重签），或买 Apple Developer 会员（$99/年，1 年有效）

#### 方案 B：用 AltStore（自动重签，但初次配置麻烦）

1. 访问 https://altstore.io，下载 Windows 版
2. 安装时需要装 iCloud 和 iTunes（苹果的 Windows 版本）
3. 连接 iPhone，用 AltStore 安装 IPA
4. AltStore 会在后台自动续签（只要电脑开着）

> 详细的 AltStore 教程：https://altstore.io/faq

---

## 阶段 5 · 配置同步（两端都装好后）

两端 APP 都跑起来后：

### 你的手机（Android）
1. 打开 APP → 更多 → 数据同步
2. 填：
   - GitHub 用户名：`panzecakackson15616-byte`（你的）
   - 仓库名：`our-private-space`
   - Token：你新生成的 Token（旧的泄露了要重置）
3. 验证 → 保存

### TA 的手机（iPhone）
1. 打开 APP → 更多 → 数据同步
2. 填**完全相同**的三项
3. 验证 → 保存
4. 点"立即拉取"获取你已上传的数据

---

## 🚨 常见问题

### Q1: GitHub Actions 编译失败了怎么办？

打开失败的运行，看红色那一步的日志。常见错误：

| 错误 | 原因 | 解决 |
|------|------|------|
| `xcodebuild: error: SDK not found` | Xcode 版本不对 | 改 workflow 里的 Xcode 版本 |
| `Code signing required` | 真机版本需要证书 | 我配的是模拟器版本，应该不会遇到 |
| `xcodegen: command not found` | XcodeGen 没装上 | brew install 路径问题，看日志 |
| `Module not found` | 代码有依赖问题 | 把日志截图发我 |

### Q2: iOS 7 天就过期怎么办？

三个选择：
- **免费方案**：每周用 Sideloadly 重新签一次（5 分钟搞定）
- **省心方案**：装 AltStore，电脑开着时自动续签
- **付费方案**：买 Apple Developer 会员 $99/年，签名 1 年有效

### Q3: 能不能不用电脑就给 iPhone 装？

不行。苹果限制，必须通过电脑签名才能装非 App Store 的 APP。

### Q4: 编译要钱吗？

GitHub Actions 免费额度：
- 公开仓库：无限分钟
- 私有仓库：每月 2000 分钟（macOS runner 按 10 倍计算，即相当于 200 分钟 macOS）

每次 iOS 编译约 15 分钟，相当于消耗 150 分钟免费额度。**每月能编译约 13 次**，对情侣 APP 完全够用。

如果用超了：
- 等下个月重置
- 或临时把仓库改成 Public（不推荐，源代码会公开）
- 或升级 GitHub Pro（$4/月，3000 分钟）

### Q5: 修改代码后怎么重新编译？

```bash
cd "C:/Users/12558/WorkBuddy/2026-07-27-20-50-32/SLC-Lovers"
git add .
git commit -m "修改了 XX 功能"
git push
```

push 完 GitHub Actions 会自动重新编译。

---

## 📞 遇到问题

如果编译失败或安装遇到问题，告诉我：
1. 哪一步失败的（截图）
2. 错误日志（截关键部分）
3. 你的环境（Windows 版本、iPhone 型号、iOS 版本）

我帮你排查。