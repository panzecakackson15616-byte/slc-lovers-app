package com.slclovers.app.ui.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showUnpairDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { /* 返回 */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SLCColor.Cream
                ),
            )
        },
        containerColor = SLCColor.Cream,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(vertical = SLCSpace.md),
            verticalArrangement = Arrangement.spacedBy(SLCSpace.lg),
        ) {
            // 头像 & 资料
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SLCSpace.lg)
                    .clip(RoundedCornerShape(SLCRadius.lg))
                    .background(SLCColor.CreamLight)
                    .padding(vertical = SLCSpace.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SLCSpace.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(SLCColor.person(currentUser?.role ?: UserRole.Him)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        currentUser?.name?.take(1) ?: "?",
                        fontSize = 36.sp,
                        color = SLCColor.Cream,
                    )
                }
                Text(
                    currentUser?.name ?: "我",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SLCColor.TextPrimary,
                )
            }

            // 设置项
            Column(
                modifier = Modifier.padding(horizontal = SLCSpace.md),
                verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
            ) {
                SettingsRow(icon = Icons.Default.Notifications, title = "通知", color = SLCColor.Warning)
                SettingsRow(icon = Icons.Default.Lock, title = "隐私与安全", color = SLCColor.Him)
                SettingsRow(icon = Icons.Default.Cloud, title = "iCloud 同步", color = SLCColor.Info)
                SettingsRow(icon = Icons.Default.Help, title = "帮助与反馈", color = SLCColor.Success)
                SettingsRow(icon = Icons.Default.Info, title = "关于 SLC-Lovers", color = SLCColor.TextSecondary)
            }

            // 危险操作
            Button(
                onClick = { showUnpairDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SLCSpace.md)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SLCColor.CreamLight,
                    contentColor = SLCColor.Danger,
                ),
                shape = RoundedCornerShape(SLCRadius.md),
            ) {
                Text("解除配对", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }

        if (showUnpairDialog) {
            AlertDialog(
                onDismissRequest = { showUnpairDialog = false },
                title = { Text("解除配对？") },
                text = { Text("解除后所有数据将清除，且无法恢复。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.unpair()
                        showUnpairDialog = false
                    }) {
                        Text("解除", color = SLCColor.Danger, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnpairDialog = false }) {
                        Text("取消", color = SLCColor.TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SLCRadius.md))
            .background(SLCColor.CreamLight)
            .padding(SLCSpace.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(SLCSpace.md))
        Text(
            title,
            fontSize = 16.sp,
            color = SLCColor.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SLCColor.TextTertiary,
            modifier = Modifier.size(12.dp),
        )
    }
}