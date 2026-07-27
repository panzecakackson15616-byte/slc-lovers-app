package com.slclovers.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace

/**
 * 卡片容器
 */
@Composable
fun SLCCard(
    modifier: Modifier = Modifier,
    padding: Dp = SLCSpace.md,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(SLCRadius.lg))
            .clip(RoundedCornerShape(SLCRadius.lg))
            .background(SLCColor.CreamLight)
            .padding(padding),
        content = content
    )
}

/**
 * 主按钮
 */
@Composable
fun SLCPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = SLCColor.Him
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = SLCColor.TextOnDark,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = SLCColor.TextOnDark,
        ),
        shape = RoundedCornerShape(SLCRadius.md),
        enabled = enabled,
    ) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
        )
    }
}

/**
 * 次按钮
 */
@Composable
fun SLCSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SLCColor.TextPrimary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(SLCColor.TextTertiary.copy(alpha = 0.3f))
        ),
        shape = RoundedCornerShape(SLCRadius.md),
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * 头像
 */
@Composable
fun SLCAvatar(
    role: UserRole,
    initial: String,
    size: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(SLCColor.person(role)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initial,
            color = SLCColor.Cream,
            style = TextStyle(
                fontSize = (size.value * 0.45).sp,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
}

/**
 * 角色徽章
 */
@Composable
fun SLCPersonBadge(role: UserRole, compact: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(SLCColor.person(role))
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 3.dp)
    ) {
        Text(
            role.displayName,
            color = SLCColor.Cream,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * 空状态
 */
@Composable
fun SLCEmptyView(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SLCSpace.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SLCSpace.md)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = SLCColor.TextTertiary,
            modifier = Modifier.size(56.dp)
        )
        Text(
            title,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = SLCColor.TextPrimary
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = SLCColor.TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Section 标题
 */
@Composable
fun SLCSectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SLCSpace.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = SLCColor.TextPrimary
        )
        Spacer(Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = SLCColor.HerDeep, fontSize = 14.sp)
            }
        }
    }
}