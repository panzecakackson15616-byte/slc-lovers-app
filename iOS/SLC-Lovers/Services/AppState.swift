import Foundation
import Combine
import SwiftUI

/// 全局应用状态
/// 单例，管理所有数据 + 业务逻辑
@MainActor
final class AppState: ObservableObject {

    // MARK: - 单例
    static let shared = AppState()

    // MARK: - 用户与配对
    @Published var currentUser: User?
    @Published var partner: User?
    @Published var pairing: Pairing?

    // MARK: - 数据集合
    @Published var messages: [Message] = []
    @Published var photos: [Photo] = []
    @Published var diaryEntries: [DiaryEntry] = []
    @Published var todos: [TodoItem] = []
    @Published var bucketItems: [BucketItem] = []
    @Published var anniversaries: [Anniversary] = []
    @Published var locations: [LocationRecord] = []
    @Published var capsules: [TimeCapsule] = []
    @Published var stickyNotes: [StickyNote] = []
    @Published var hobbies: [Hobby] = []
    @Published var todayQuote: Quote?

    // MARK: - Stores
    let messagesStore = LocalStore<[Message]>(key: "messages")
    let photosStore = LocalStore<[Photo]>(key: "photos")
    let diaryStore = LocalStore<[DiaryEntry]>(key: "diary")
    let todoStore = LocalStore<[TodoItem]>(key: "todos")
    let bucketStore = LocalStore<[BucketItem]>(key: "buckets")
    let anniversaryStore = LocalStore<[Anniversary]>(key: "anniversaries")
    let locationStore = LocalStore<[LocationRecord]>(key: "locations")
    let capsuleStore = LocalStore<[TimeCapsule]>(key: "capsules")
    let noteStore = LocalStore<[StickyNote]>(key: "notes")
    let hobbyStore = LocalStore<[Hobby]>(key: "hobbies")

    // MARK: - 初始化
    private init() {
        loadAll()
        if messages.isEmpty { seedDemoData() }
    }

    // MARK: - 配对流程

    /// 创建新的配对（当前用户成为发起方）
    func createPairing(name: String, role: UserRole, startDate: Date) {
        let user = User(name: name, role: role)
        let code = Pairing.generateCode()
        let pairing = Pairing(code: code, initiatorId: user.id, startDate: startDate)

        self.currentUser = user
        self.pairing = pairing

        // 持久化
        Prefs.set(user.id.uuidString, forKey: Prefs.Key.currentUserId)
        Prefs.set(pairing.id.uuidString, forKey: Prefs.Key.pairingId)
        Prefs.set(startDate, forKey: Prefs.Key.startDate)

        saveAnniversaries()
    }

    /// 加入已存在的配对
    func joinPairing(name: String, role: UserRole, code: String, startDate: Date) -> Bool {
        // 原型阶段：只要码是 6 位数字就算成功
        guard code.count == 6, Int(code) != nil else { return false }

        let user = User(name: name, role: role)
        // 模拟对方用户
        let partnerRole: UserRole = (role == .him) ? .her : .him
        let partner = User(name: partnerRole == .him ? "他" : "她", role: partnerRole)

        // 创建配对记录
        let pairing = Pairing(
            code: code,
            status: .connected,
            initiatorId: user.id,
            partnerId: partner.id,
            startDate: startDate
        )

        self.currentUser = user
        self.partner = partner
        self.pairing = pairing

        // 持久化
        Prefs.set(user.id.uuidString, forKey: Prefs.Key.currentUserId)
        Prefs.set(partner.id.uuidString, forKey: Prefs.Key.partnerUserId)
        Prefs.set(pairing.id.uuidString, forKey: Prefs.Key.pairingId)
        Prefs.set(startDate, forKey: Prefs.Key.startDate)

        saveAnniversaries()
        return true
    }

    /// 是否已配对
    var isPaired: Bool {
        pairing?.status == .connected && currentUser != nil
    }

    /// 解除配对（清空所有数据）
    func unpair() {
        currentUser = nil
        partner = nil
        pairing = nil
        messages = []
        photos = []
        diaryEntries = []
        todos = []
        bucketItems = []
        anniversaries = []
        locations = []
        capsules = []
        stickyNotes = []
        hobbies = []

        messagesStore.clear()
        photosStore.clear()
        diaryStore.clear()
        todoStore.clear()
        bucketStore.clear()
        anniversaryStore.clear()
        locationStore.clear()
        capsuleStore.clear()
        noteStore.clear()
        hobbyStore.clear()

        Prefs.remove(Prefs.Key.currentUserId)
        Prefs.remove(Prefs.Key.partnerUserId)
        Prefs.remove(Prefs.Key.pairingId)
        Prefs.remove(Prefs.Key.startDate)
    }

    // MARK: - 数据加载/保存

    private func loadAll() {
        messages = messagesStore.load() ?? []
        photos = photosStore.load() ?? []
        diaryEntries = diaryStore.load() ?? []
        todos = todoStore.load() ?? []
        bucketItems = bucketStore.load() ?? []
        anniversaries = anniversaryStore.load() ?? []
        locations = locationStore.load() ?? []
        capsules = capsuleStore.load() ?? []
        stickyNotes = noteStore.load() ?? []
        hobbies = hobbyStore.load() ?? []

        // 恢复当前用户
        if let userIdStr: String = Prefs.get(Prefs.Key.currentUserId),
           let userId = UUID(uuidString: userIdStr) {
            // 原型中简单处理：用户名从 UserDefaults 拿不到，从 messages 等推断
            // 这里仅做占位
        }
        // 恢复纪念日（首次启动创建"在一起"纪念日）
        if anniversaries.isEmpty, let _: Date = Prefs.get(Prefs.Key.startDate) {
            saveAnniversaries()
        }
    }

    private func saveAnniversaries() {
        if let startDate = pairing?.startDate {
            let exist = anniversaries.first { $0.title == "在一起纪念日" }
            if exist == nil {
                let anniversary = Anniversary(
                    title: "在一起纪念日",
                    date: startDate,
                    isRecurring: true,
                    icon: "💕"
                )
                anniversaries.append(anniversary)
                anniversaryStore.save(anniversaries)
                triggerSync()
            }
        }
    }

    // MARK: - 操作：消息
    func sendMessage(_ content: String) {
        guard let sender = currentUser, let receiver = partner else { return }
        let msg = Message(
            senderId: sender.id,
            receiverId: receiver.id,
            content: content
        )
        messages.append(msg)
        messagesStore.save(messages)
        triggerSync()

        // 模拟对方回复（仅原型）
        if Bool.random() && !content.isEmpty {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { [weak self] in
                self?.simulateReply(to: content)
            }
        }
    }

    private func simulateReply(to original: String) {
        guard let sender = currentUser, let receiver = partner else { return }
        let replies = ["嗯嗯 ☀️", "想你了~", "❤️", "好的呀", "我也是", "抱抱", "嗯", "收到"]
        let reply = replies.randomElement() ?? "嗯"
        let msg = Message(
            senderId: receiver.id,
            receiverId: sender.id,
            content: reply
        )
        messages.append(msg)
        messagesStore.save(messages)
        triggerSync()
    }

    // MARK: - 操作：纪念日
    func addAnniversary(_ anniversary: Anniversary) {
        anniversaries.append(anniversary)
        anniversaryStore.save(anniversaries)
        triggerSync()
    }

    func removeAnniversary(_ anniversary: Anniversary) {
        anniversaries.removeAll { $0.id == anniversary.id }
        anniversaryStore.save(anniversaries)
        triggerSync()
    }

    // MARK: - 操作：相册
    func addPhoto(_ photo: Photo) {
        photos.append(photo)
        photos.sort { $0.takenAt > $1.takenAt }
        photosStore.save(photos)
        triggerSync()
    }

    // MARK: - 操作：日记
    func addDiary(_ entry: DiaryEntry) {
        diaryEntries.append(entry)
        diaryEntries.sort { $0.createdAt > $1.createdAt }
        diaryStore.save(diaryEntries)
        triggerSync()
    }

    func removeDiary(_ entry: DiaryEntry) {
        diaryEntries.removeAll { $0.id == entry.id }
        diaryStore.save(diaryEntries)
        triggerSync()
    }

    // MARK: - 操作：待办
    func toggleTodo(_ todo: TodoItem) {
        guard let idx = todos.firstIndex(where: { $0.id == todo.id }) else { return }
        todos[idx].isCompleted.toggle()
        todos[idx].completedBy = todos[idx].isCompleted ? currentUser?.id : nil
        todoStore.save(todos)
        triggerSync()
    }

    func addTodo(_ todo: TodoItem) {
        todos.append(todo)
        todoStore.save(todos)
        triggerSync()
    }

    func removeTodo(_ todo: TodoItem) {
        todos.removeAll { $0.id == todo.id }
        todoStore.save(todos)
        triggerSync()
    }

    // MARK: - 操作：愿望清单
    func addBucket(_ item: BucketItem) {
        bucketItems.append(item)
        bucketStore.save(bucketItems)
        triggerSync()
    }

    func toggleBucket(_ item: BucketItem) {
        guard let idx = bucketItems.firstIndex(where: { $0.id == item.id }) else { return }
        bucketItems[idx].isAchieved.toggle()
        bucketItems[idx].achievedAt = bucketItems[idx].isAchieved ? Date() : nil
        bucketStore.save(bucketItems)
        triggerSync()
    }

    func removeBucket(_ item: BucketItem) {
        bucketItems.removeAll { $0.id == item.id }
        bucketStore.save(bucketItems)
        triggerSync()
    }

    // MARK: - 操作：时光胶囊
    func addCapsule(_ capsule: TimeCapsule) {
        capsules.append(capsule)
        capsuleStore.save(capsules)
        triggerSync()
    }

    func unlockCapsule(_ capsule: TimeCapsule) {
        guard let idx = capsules.firstIndex(where: { $0.id == capsule.id }) else { return }
        capsules[idx].isUnlocked = true
        capsules[idx].unlockedAt = Date()
        capsuleStore.save(capsules)
        triggerSync()
    }

    func removeCapsule(_ capsule: TimeCapsule) {
        capsules.removeAll { $0.id == capsule.id }
        capsuleStore.save(capsules)
        triggerSync()
    }

    // MARK: - 操作：留言
    func addNote(_ note: StickyNote) {
        stickyNotes.append(note)
        noteStore.save(stickyNotes)
        triggerSync()
    }

    func removeNote(_ note: StickyNote) {
        stickyNotes.removeAll { $0.id == note.id }
        noteStore.save(stickyNotes)
        triggerSync()
    }

    // MARK: - 操作：爱好
    func addHobby(_ hobby: Hobby) {
        hobbies.append(hobby)
        hobbyStore.save(hobbies)
        triggerSync()
    }

    func removeHobby(_ hobby: Hobby) {
        hobbies.removeAll { $0.id == hobby.id }
        hobbyStore.save(hobbies)
        triggerSync()
    }

    // MARK: - 操作：位置
    func updateLocation(_ record: LocationRecord) {
        locations.removeAll { $0.userId == record.userId }
        locations.append(record)
        locationStore.save(locations)
        triggerSync()
    }


    // MARK: - 同步触发
    /// 写操作后调用，触发 GitHub 同步（防抖 5 秒）
    func triggerSync() {
        SyncManager.shared.schedulePushAll()
    }

    // MARK: - 每日寄语
    func refreshDailyQuote() {
        let quotes: [(String, String?)] = [
            ("和你在一起的每一天，都是最好的时光。", nil),
            ("遇见你，是所有故事里最美的那一页。", nil),
            ("愿我们携手，从心动走到古稀。", nil),
            ("最浪漫的事，是和你一起慢慢变老。", nil),
            ("世界再大，也不过是两个人的小家。", nil),
            ("你在哪里，哪里就是家。", nil),
            ("爱就是和心爱的人，一起做无聊的事。", nil),
            ("谢谢你，陪我走过每一个春夏秋冬。", nil),
            ("My love for you is a journey, not a destination.", nil),
            ("In you, I've found the love of my life and my closest friend.", nil),
        ]
        let today = Calendar.current.startOfDay(for: Date())
        let seed = Int(today.timeIntervalSince1970 / 86400)
        let pick = quotes[seed % quotes.count]
        todayQuote = Quote(content: pick.0, author: pick.1, date: today)
    }

    // MARK: - 示例数据
    private func seedDemoData() {
        guard let startDate = Calendar.current.date(byAdding: .day, value: -128, to: Date()) else { return }
        let user = User(name: "我", role: .him)
        let partner = User(name: "她", role: .her)
        let pairing = Pairing(
            code: "888888",
            status: .connected,
            initiatorId: user.id,
            partnerId: partner.id,
            startDate: startDate
        )
        self.currentUser = user
        self.partner = partner
        self.pairing = pairing

        Prefs.set(user.id.uuidString, forKey: Prefs.Key.currentUserId)
        Prefs.set(partner.id.uuidString, forKey: Prefs.Key.partnerUserId)
        Prefs.set(pairing.id.uuidString, forKey: Prefs.Key.pairingId)
        Prefs.set(startDate, forKey: Prefs.Key.startDate)

        // 默认纪念日
        let together = Anniversary(
            title: "在一起纪念日",
            date: startDate,
            isRecurring: true,
            icon: "💕"
        )
        let firstDate = Anniversary(
            title: "第一次见面",
            date: Calendar.current.date(byAdding: .day, value: -135, to: Date())!,
            isRecurring: true,
            icon: "🌸"
        )
        anniversaries = [together, firstDate]
        anniversaryStore.save(anniversaries)

        // 示例待办
        todos = [
            TodoItem(title: "周末去看展", assignee: .her, dueDate: Calendar.current.date(byAdding: .day, value: 3, to: Date())),
            TodoItem(title: "买一束花", assignee: .him, dueDate: Date()),
            TodoItem(title: "回她爸妈家吃饭", assignee: nil, dueDate: Calendar.current.date(byAdding: .day, value: 7, to: Date())),
        ]
        todoStore.save(todos)

        // 愿望清单
        bucketItems = [
            BucketItem(title: "一起去冰岛看极光", description: "2026 年冬天的约定"),
            BucketItem(title: "养一只猫", description: "英短 or 布偶？"),
            BucketItem(title: "潜水考证", description: "想去帕劳看水母湖"),
            BucketItem(title: "开一家小店", description: "咖啡 + 花艺"),
        ]
        bucketStore.save(bucketItems)

        // 时光胶囊
        if let oneYearLater = Calendar.current.date(byAdding: .year, value: 1, to: Date()) {
            capsules = [
                TimeCapsule(
                    authorId: user.id,
                    title: "写给一年后的我们",
                    content: "希望那时候的我们，依然手牵手走在夕阳下。",
                    unlockDate: oneYearLater
                ),
            ]
            capsuleStore.save(capsules)
        }

        // 留言
        stickyNotes = [
            StickyNote(authorId: user.id, content: "记得早点睡 ❤️", color: .cream, rotation: -3),
            StickyNote(authorId: partner.id, content: "爱你哟", color: .blush, rotation: 2),
            StickyNote(authorId: user.id, content: "今天辛苦了～", color: .gold, rotation: -1),
            StickyNote(authorId: partner.id, content: "下次去那家咖啡店", color: .sage, rotation: 4),
        ]
        noteStore.save(stickyNotes)

        // 爱好
        hobbies = [
            Hobby(userId: user.id, title: "咖啡", subtitle: "美式，少冰", emoji: "☕️"),
            Hobby(userId: user.id, title: "摄影", subtitle: "胶片玩家", emoji: "📷"),
            Hobby(userId: partner.id, title: "烘焙", subtitle: "蛋糕面包", emoji: "🍰"),
            Hobby(userId: partner.id, title: "旅行", subtitle: "已打卡 12 国", emoji: "✈️"),
        ]
        hobbyStore.save(hobbies)

        // 示例聊天
        messages = [
            Message(senderId: partner.id, receiverId: user.id, content: "下班了吗？", createdAt: Date().addingTimeInterval(-7200)),
            Message(senderId: user.id, receiverId: partner.id, content: "马上，路上买杯咖啡给你", createdAt: Date().addingTimeInterval(-7100)),
            Message(senderId: partner.id, receiverId: user.id, content: "好嘞 ☕️ 顺便买点面包当早餐", createdAt: Date().addingTimeInterval(-7000)),
            Message(senderId: user.id, receiverId: partner.id, content: "OK，想吃什么口味？", createdAt: Date().addingTimeInterval(-6900)),
            Message(senderId: partner.id, receiverId: user.id, content: "全麦的就好 🥐", createdAt: Date().addingTimeInterval(-6800)),
            Message(senderId: user.id, receiverId: partner.id, content: "收到～马上到家", createdAt: Date().addingTimeInterval(-6700)),
            Message(senderId: partner.id, receiverId: user.id, content: "❤️", createdAt: Date().addingTimeInterval(-6600)),
        ]
        messagesStore.save(messages)

        // 每日寄语
        refreshDailyQuote()
    }
}