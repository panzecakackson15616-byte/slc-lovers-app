# Android 端运行说明

## 方式 A：使用 Android Studio（推荐）

1. 打开 Android Studio Hedgehog 或更新版本
2. **File > Open**，选择 `Android/` 目录
3. 等待 Gradle 同步完成
4. 选择设备或模拟器（API 26+），点击 ▶️ 运行

## 方式 B：使用命令行

```bash
cd Android

# 给 gradlew 添加可执行权限（如果从 Windows 复制过来）
# gradlew 脚本需要自行下载：访问 https://services.gradle.org/distributions/gradle-8.5-bin.zip
# 解压后将 gradle-8.5/bin 添加到 PATH，或用 Android Studio 自带的 gradle

# 构建 Debug APK
./gradlew assembleDebug

# 生成的 APK
# app/build/outputs/apk/debug/app-debug.apk

# 安装到连接的设备
./gradlew installDebug
```

## 首次启动

应用首次启动会自动注入演示数据：
- 在一起 128 天
- 4 条愿望清单
- 3 条待办
- 1 个时光胶囊
- 4 张留言
- 4 个爱好
- 7 条聊天消息

要清空：进入 设置 > 解除配对

## 工程结构

```
Android/
├── build.gradle.kts                  ← 根 Gradle 配置
├── settings.gradle.kts               ← 项目包含
├── gradle.properties                 ← AndroidX 配置
├── gradle/wrapper/                   ← Gradle Wrapper
└── app/
    ├── build.gradle.kts              ← 模块配置
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/slclovers/app/
        │   ├── SLCApp.kt            ← Application
        │   ├── MainActivity.kt
        │   ├── AppViewModel.kt      ← 全局 ViewModel
        │   ├── data/                ← 数据层（Room）
        │   │   ├── SLCDatabase.kt
        │   │   ├── Daos.kt
        │   │   ├── Converters.kt
        │   │   ├── model/Entities.kt
        │   │   └── repository/
        │   ├── ui/
        │   │   ├── RootView.kt
        │   │   ├── MainTabView.kt
        │   │   ├── theme/
        │   │   ├── components/
        │   │   ├── pairing/
        │   │   ├── home/
        │   │   ├── chat/
        │   │   ├── gallery/
        │   │   ├── diary/
        │   │   ├── location/
        │   │   ├── capsule/
        │   │   ├── board/
        │   │   ├── more/
        │   │   └── settings/
        │   └── util/DateUtils.kt
        └── res/
            ├── values/
            ├── mipmap-*/
            └── xml/
```

## 关键依赖

- **Jetpack Compose**：现代化 UI 工具包
- **Material 3**：Google 设计语言
- **Room**：类型安全的 SQLite 封装
- **ViewModel + StateFlow**：状态管理
- **Coroutines + Flow**：异步数据流

## 设计亮点

- 始终浅色主题（米色背景），符合品牌定位
- 状态栏/导航栏与背景同色，全屏沉浸
- 他/她双角色系统贯穿全局
- 本地数据持久化（Room），无需网络即可使用
- 启动即注入演示数据，方便快速预览