package com.slclovers.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 日期工具
 */
object DateUtils {

    private val cnLocale = Locale("zh", "CN")

    /** 友好的相对日期（今天/昨天/N 天前） */
    fun friendlyRelative(date: Long): String {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dateCal = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return when {
            dateCal == today -> "今天"
            dateCal == today - 86400000L -> "昨天"
            now - dateCal < 7 * 86400000L -> "${(now - dateCal) / 86400000L} 天前"
            else -> {
                val fmt = SimpleDateFormat(if (sameYear(date)) "MM-dd" else "yyyy-MM-dd", cnLocale)
                fmt.format(Date(date))
            }
        }
    }

    /** 仅时间 HH:mm */
    fun timeOnly(date: Long): String =
        SimpleDateFormat("HH:mm", cnLocale).format(Date(date))

    /** 在一起 X 天 */
    fun togetherDays(startDate: Long): String {
        val diff = Calendar.getInstance().timeInMillis - startDate
        return "${diff / (1000 * 60 * 60 * 24)}"
    }

    /** 完整日期 */
    fun fullChinese(date: Long): String =
        SimpleDateFormat("yyyy 年 M 月 d 日", cnLocale).format(Date(date))

    /** 月日 */
    fun monthDay(date: Long): String =
        SimpleDateFormat("M 月 d 日", cnLocale).format(Date(date))

    private fun sameYear(date: Long): Boolean {
        val now = Calendar.getInstance().get(Calendar.YEAR)
        return Calendar.getInstance().apply { timeInMillis = date }.get(Calendar.YEAR) == now
    }
}

/**
 * 位置工具
 */
object LocationUtils {
    private const val EARTH_RADIUS = 6371.0

    /** 计算两点间距离（km） */
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return EARTH_RADIUS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun formattedDistance(km: Double): String = when {
        km < 1 -> "${(km * 1000).toInt()} m"
        km < 100 -> String.format("%.1f km", km)
        else -> String.format("%.0f km", km)
    }
}