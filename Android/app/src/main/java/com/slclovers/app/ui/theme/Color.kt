package com.slclovers.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.slclovers.app.data.model.UserRole

/**
 * SLC-Lovers 设计系统的核心色板
 * 从设计令牌 [DESIGN-TOKENS.md] 同步，所有颜色都应从此处引用
 */
object SLCColor {

    // 背景
    val Cream = Color(0xFFF5F1E8)
    val CreamLight = Color(0xFFFAF7F0)
    val CreamDeep = Color(0xFFEDE6D5)

    // 角色色（他 / 她）
    val Him = Color(0xFF1A1A1A)
    val HimSoft = Color(0xFF3A3A3A)
    val Her = Color(0xFFC9A961)
    val HerDeep = Color(0xFFB8956A)
    val HerSoft = Color(0xFFE8D4A0)

    // 文字
    val TextPrimary = Color(0xFF2C2826)
    val TextSecondary = Color(0xFF6B6560)
    val TextTertiary = Color(0xFF9C958E)
    val TextOnDark = Color(0xFFF5F1E8)

    // 语义
    val Success = Color(0xFF7A9B6E)
    val Warning = Color(0xFFD4A574)
    val Danger = Color(0xFFC46B5A)
    val Info = Color(0xFF8FA4B5)

    fun person(role: UserRole) = if (role == UserRole.Him) Him else Her
    fun personDeep(role: UserRole) = if (role == UserRole.Him) HimSoft else HerDeep
    fun personSoft(role: UserRole) = if (role == UserRole.Him) HimSoft else HerSoft
}