import Foundation

/// 通用工具方法
enum DateUtils {

    /// 友好的日期格式（"今天"、"昨天"、"3 天前"）
    static func friendlyRelative(from date: Date) -> String {
        let calendar = Calendar.current
        let now = Date()
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")

        if calendar.isDateInToday(date) {
            return "今天"
        }
        if calendar.isDateInYesterday(date) {
            return "昨天"
        }

        let days = calendar.dateComponents([.day], from: date, to: now).day ?? 0
        if days < 7 {
            return "\(days) 天前"
        }

        // 本年内
        let sameYear = calendar.component(.year, from: date) == calendar.component(.year, from: now)
        formatter.dateFormat = sameYear ? "MM-dd" : "yyyy-MM-dd"
        return formatter.string(from: date)
    }

    /// 时间格式（HH:mm）
    static func timeOnly(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }

    /// 计算两个日期之间相隔的天数
    static func daysBetween(_ from: Date, _ to: Date) -> Int {
        let calendar = Calendar.current
        let fromDay = calendar.startOfDay(for: from)
        let toDay = calendar.startOfDay(for: to)
        return calendar.dateComponents([.day], from: fromDay, to: toDay).day ?? 0
    }

    /// 计算「在一起 X 天」
    static func togetherDays(since startDate: Date) -> String {
        let days = daysBetween(startDate, Date())
        return "\(days)"
    }

    /// 解封倒计时（X 天 Y 小时）
    static func countdownDescription(until date: Date) -> String {
        let interval = date.timeIntervalSinceNow
        if interval <= 0 { return "可解封" }

        let days = Int(interval / 86400)
        let hours = Int((interval.truncatingRemainder(dividingBy: 86400)) / 3600)
        let minutes = Int((interval.truncatingRemainder(dividingBy: 3600)) / 60)

        if days > 0 {
            return "\(days) 天 \(hours) 小时"
        } else if hours > 0 {
            return "\(hours) 小时 \(minutes) 分"
        } else {
            return "\(minutes) 分钟"
        }
    }

    /// 完整日期（yyyy 年 M 月 d 日）
    static func fullChinese(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.dateFormat = "yyyy 年 M 月 d 日"
        return formatter.string(from: date)
    }

    /// 月日（"5 月 20 日"）
    static func monthDay(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.dateFormat = "M 月 d 日"
        return formatter.string(from: date)
    }
}

/// 计算两点间距离（公里）
enum LocationUtils {
    static func distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) -> Double {
        let R = 6371.0  // 地球半径（公里）
        let dLat = (lat2 - lat1) * .pi / 180
        let dLon = (lon2 - lon1) * .pi / 180
        let a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * .pi / 180) * cos(lat2 * .pi / 180) *
                sin(dLon / 2) * sin(dLon / 2)
        let c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    static func formattedDistance(_ km: Double) -> String {
        if km < 1 {
            return "\(Int(km * 1000)) m"
        } else if km < 100 {
            return String(format: "%.1f km", km)
        } else {
            return String(format: "%.0f km", km)
        }
    }
}