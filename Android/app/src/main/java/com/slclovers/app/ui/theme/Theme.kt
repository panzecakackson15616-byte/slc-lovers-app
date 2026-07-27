package com.slclovers.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 主题
 * 始终使用浅色（米色背景），符合品牌定位
 */
private val SLCLightColorScheme = lightColorScheme(
    primary = SLCColor.Him,
    onPrimary = SLCColor.TextOnDark,
    primaryContainer = SLCColor.CreamDeep,
    onPrimaryContainer = SLCColor.TextPrimary,

    secondary = SLCColor.HerDeep,
    onSecondary = SLCColor.TextOnDark,
    secondaryContainer = SLCColor.HerSoft,
    onSecondaryContainer = SLCColor.TextPrimary,

    tertiary = SLCColor.Her,
    onTertiary = SLCColor.TextOnDark,

    background = SLCColor.Cream,
    onBackground = SLCColor.TextPrimary,

    surface = SLCColor.CreamLight,
    onSurface = SLCColor.TextPrimary,
    surfaceVariant = SLCColor.CreamDeep,

    error = SLCColor.Danger,
    onError = SLCColor.TextOnDark,

    outline = SLCColor.TextTertiary,
)

@Composable
fun SLCTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SLCColor.Cream.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            window.navigationBarColor = SLCColor.CreamLight.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = SLCLightColorScheme,
        typography = SLCTypography,
        shapes = SLCShapes,
        content = content
    )
}