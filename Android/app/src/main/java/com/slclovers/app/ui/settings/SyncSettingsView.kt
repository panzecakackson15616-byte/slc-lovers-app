package com.slclovers.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.sync.SecureStorage
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsView(viewModel: AppViewModel) {
    val syncManager = viewModel.syncManager
    val isConfigured by syncManager.isConfigured.collectAsState()
    val isSyncing by syncManager.isSyncing.collectAsState()
    val lastSyncAt by syncManager.lastSyncAt.collectAsState()
    val lastError by syncManager.lastError.collectAsState()
    val syncEnabled by syncManager.syncEnabled.collectAsState()

    var token by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyResult by remember { mutableStateOf<String?>(null) }
    var showToken by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 加载已保存配置（不回显 token）
    LaunchedEffect(isConfigured) {
        if (isConfigured) {
            owner = SecureStorage.load(viewModel.getApplication(), SecureStorage.Key.GITHUB_OWNER) ?: ""
            repo = SecureStorage.load(viewModel.getApplication(), SecureStorage.Key.GITHUB_REPO) ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据同步", fontWeight = FontWeight.SemiBold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 状态卡
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SLCColor.CreamLight),
                shape = RoundedCornerShape(SLCRadius.lg),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("同步状态", fontSize = 14.sp, color = SLCColor.TextSecondary)
                        Spacer(Modifier.weight(1f))
                        when {
                            isSyncing -> {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("同步中…", fontSize = 12.sp, color = SLCColor.TextSecondary)
                            }
                            isConfigured -> {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SLCColor.Success, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("已配置", fontSize = 12.sp, color = SLCColor.Success)
                            }
                            else -> {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = SLCColor.Warning, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("未配置", fontSize = 12.sp, color = SLCColor.Warning)
                            }
                        }
                    }
                    lastSyncAt?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "上次同步：${DateUtils.friendlyRelative(it)}",
                            fontSize = 12.sp,
                            color = SLCColor.TextSecondary,
                        )
                    }
                    lastError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "错误：$it",
                            fontSize = 12.sp,
                            color = SLCColor.Danger,
                        )
                    }
                }
            }

            // GitHub 配置
            Text(
                "GitHub 配置",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SLCColor.TextSecondary,
            )

            OutlinedTextField(
                value = owner,
                onValueChange = { owner = it },
                label = { Text("GitHub 用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SLCColor.Him,
                    unfocusedBorderColor = SLCColor.CreamDeep,
                ),
            )

            OutlinedTextField(
                value = repo,
                onValueChange = { repo = it },
                label = { Text("仓库名（私有）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SLCColor.Him,
                    unfocusedBorderColor = SLCColor.CreamDeep,
                ),
            )

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Personal Access Token") },
                singleLine = true,
                visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showToken = !showToken }) {
                        Icon(
                            if (showToken) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = SLCColor.TextSecondary,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SLCColor.Him,
                    unfocusedBorderColor = SLCColor.CreamDeep,
                ),
            )

            // 配置说明
            Card(
                colors = CardDefaults.cardColors(containerColor = SLCColor.CreamDeep),
                shape = RoundedCornerShape(SLCRadius.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("配置步骤", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("1. 在 GitHub 创建一个私有仓库", fontSize = 12.sp, color = SLCColor.TextSecondary)
                    Text("2. 访问 github.com/settings/tokens", fontSize = 12.sp, color = SLCColor.TextSecondary)
                    Text("3. 生成 Token (classic)，勾选 repo 权限", fontSize = 12.sp, color = SLCColor.TextSecondary)
                    Text("4. 把 Token 粘贴到上方", fontSize = 12.sp, color = SLCColor.TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⚠️ Token 只存在本机加密存储，绝不上传",
                        fontSize = 12.sp,
                        color = SLCColor.Warning,
                    )
                }
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isVerifying = true
                            syncManager.saveConfig(token, owner, repo)
                            val ok = syncManager.verifyConfig()
                            verifyResult = if (ok) "✓ 仓库可访问" else "✗ 仓库不存在或 Token 无效"
                            isVerifying = false
                        }
                    },
                    enabled = token.isNotEmpty() && owner.isNotEmpty() && repo.isNotEmpty() && !isVerifying,
                    modifier = Modifier.weight(1f),
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("验证")
                    }
                }

                Button(
                    onClick = {
                        syncManager.saveConfig(token, owner, repo)
                        // 同步配对码用于派生密钥
                        viewModel.pairing.value?.let { p ->
                            syncManager.savePairingForCrypto(p.code, p.id)
                        }
                        syncManager.startAutoSync(viewModel)
                        scope.launch { syncManager.pullAll(viewModel) }
                        verifyResult = "✓ 配置已保存"
                    },
                    enabled = token.isNotEmpty() && owner.isNotEmpty() && repo.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SLCColor.Him,
                        contentColor = SLCColor.Cream,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("保存", fontWeight = FontWeight.SemiBold)
                }
            }

            verifyResult?.let {
                Text(it, fontSize = 12.sp, color = if (it.contains("✓")) SLCColor.Success else SLCColor.Danger)
            }

            // 同步开关
            Card(
                colors = CardDefaults.cardColors(containerColor = SLCColor.CreamLight),
                shape = RoundedCornerShape(SLCRadius.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("自动同步", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text(
                            "关闭后 APP 仍可用本地数据，但不会上传",
                            fontSize = 12.sp,
                            color = SLCColor.TextSecondary,
                        )
                    }
                    Switch(
                        checked = syncEnabled,
                        onCheckedChange = { syncManager.setSyncEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = SLCColor.Him,
                        ),
                    )
                }
            }

            // 立即操作
            if (isConfigured) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { scope.launch { syncManager.pullAll(viewModel) } },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("立即拉取")
                    }
                    OutlinedButton(
                        onClick = { scope.launch { syncManager.pushAllNow(viewModel) } },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("立即推送")
                    }
                }
            }

            // 清空配置
            if (isConfigured) {
                Button(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SLCColor.CreamLight,
                        contentColor = SLCColor.Danger,
                    ),
                    shape = RoundedCornerShape(SLCRadius.md),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("清空同步配置")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空同步配置？") },
            text = { Text("清空后本地数据保留，但不再自动同步。") },
            confirmButton = {
                TextButton(onClick = {
                    syncManager.clearConfig()
                    token = ""
                    owner = ""
                    repo = ""
                    verifyResult = null
                    showClearDialog = false
                }) {
                    Text("清空", color = SLCColor.Danger, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消", color = SLCColor.TextSecondary)
                }
            }
        )
    }
}