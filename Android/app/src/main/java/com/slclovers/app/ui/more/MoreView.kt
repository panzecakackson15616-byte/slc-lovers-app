package com.slclovers.app.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.components.SLCCard
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreView(
    viewModel: AppViewModel,
    onNavigate: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val pairing by viewModel.pairing.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("更多", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SLCColor.Cream
                )
            )
        },
        containerColor = SLCColor.Cream,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(vertical = SLCSpace.md),
            verticalArrangement = Arrangement.spacedBy(SLCSpace.md),
        ) {
            // 用户卡
            Box(modifier = Modifier.padding(horizontal = SLCSpace.md)) {
                SLCCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SLCColor.person(currentUser?.role ?: UserRole.Him)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                currentUser?.name?.take(1) ?: "?",
                                fontSize = 24.sp,
                                color = SLCColor.Cream,
                            )
                        }
                        Spacer(Modifier.width(SLCSpace.md))
                        Column {
                            Text(
                                currentUser?.name ?: "我",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = SLCColor.TextPrimary,
                            )
                            pairing?.startDate?.let {
                                Text(
                                    DateUtils.fullChinese(it),
                                    fontSize = 12.sp,
                                    color = SLCColor.TextSecondary,
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SLCColor.TextSecondary,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { onNavigate("settings") }
                        )
                    }
                }
            }

            // 功能菜单
            Column(
                modifier = Modifier.padding(horizontal = SLCSpace.md),
                verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
            ) {
                MoreMenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "想见你",
                    subtitle = "看到 TA 在哪",
                    color = SLCColor.Her,
                    onClick = { onNavigate("location") },
                )
                MoreMenuItem(
                    icon = Icons.Default.HourglassBottom,
                    title = "时光胶囊",
                    subtitle = "写给未来的信",
                    color = SLCColor.HerDeep,
                    onClick = { onNavigate("capsule") },
                )
                MoreMenuItem(
                    icon = Icons.Default.NoteAlt,
                    title = "留言板",
                    subtitle = "我们的小纸条",
                    color = SLCColor.Him,
                    onClick = { onNavigate("board") },
                )
                MoreMenuItem(
                    icon = Icons.Default.Sync,
                    title = "数据同步",
                    subtitle = "通过 GitHub 双端同步",
                    color = SLCColor.Info,
                    onClick = { onNavigate("sync") },
                )
                )
                MoreMenuItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "纪念日管理",
                    subtitle = "重要的日子",
                    color = SLCColor.Warning,
                    onClick = { /* TODO */ },
                )
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SLCRadius.lg))
            .background(SLCColor.CreamLight)
            .clickable(onClick = onClick)
            .padding(SLCSpace.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(SLCSpace.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = SLCColor.TextPrimary,
            )
            Text(subtitle, fontSize = 12.sp, color = SLCColor.TextSecondary)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SLCColor.TextTertiary,
            modifier = Modifier.size(14.dp),
        )
    }
}