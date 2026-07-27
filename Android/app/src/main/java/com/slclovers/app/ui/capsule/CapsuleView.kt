package com.slclovers.app.ui.capsule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.TimeCapsuleEntity
import com.slclovers.app.ui.components.SLCCard
import com.slclovers.app.ui.components.SLCEmptyView
import com.slclovers.app.ui.components.SLCPrimaryButton
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleView(viewModel: AppViewModel) {
    val capsules by viewModel.capsules.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("时光胶囊", fontWeight = FontWeight.SemiBold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = SLCColor.Him,
                contentColor = SLCColor.Cream,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = SLCColor.Cream,
    ) { padding ->
        if (capsules.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SLCEmptyView(
                    icon = Icons.Default.HourglassEmpty,
                    title = "还没有胶囊",
                    subtitle = "给未来的你们写封信"
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = SLCSpace.md),
                verticalArrangement = Arrangement.spacedBy(SLCSpace.md),
            ) {
                capsules.forEach { capsule ->
                    Box(modifier = Modifier.padding(horizontal = SLCSpace.md)) {
                        CapsuleCard(capsule = capsule, onClick = { /* TODO */ })
                    }
                }
            }
        }

        if (showCreate) {
            CreateCapsuleDialog(
                onDismiss = { showCreate = false },
                onConfirm = { title, content, unlockDate ->
                    viewModel.addCapsule(title, content, unlockDate)
                    showCreate = false
                }
            )
        }
    }
}

@Composable
private fun CapsuleCard(capsule: TimeCapsuleEntity, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(SLCRadius.lg))
            .clip(RoundedCornerShape(SLCRadius.lg))
            .background(SLCColor.CreamLight)
            .clickable(onClick = onClick)
            .padding(SLCSpace.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SLCSpace.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (capsule.canUnlock()) Icons.Default.EmailOpen else Icons.Default.Email,
                    contentDescription = null,
                    tint = if (capsule.canUnlock()) SLCColor.HerDeep else SLCColor.Him,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(SLCSpace.md))
                Column {
                    Text(
                        capsule.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = SLCColor.TextPrimary,
                    )
                    Text(
                        "解封于 ${DateUtils.fullChinese(capsule.unlockDate)}",
                        fontSize = 12.sp,
                        color = SLCColor.TextSecondary,
                    )
                }
            }
            when {
                capsule.isUnlocked -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = SLCColor.Success,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "已解封",
                        fontSize = 14.sp,
                        color = SLCColor.Success,
                        fontWeight = FontWeight.Medium,
                    )
                }
                capsule.canUnlock() -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SLCColor.HerDeep,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "可以解封了",
                        fontSize = 14.sp,
                        color = SLCColor.HerDeep,
                        fontWeight = FontWeight.Medium,
                    )
                }
                else -> Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SLCColor.HerSoft.copy(alpha = 0.4f))
                        .padding(horizontal = SLCSpace.sm, vertical = SLCSpace.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SLCColor.HerDeep,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        capsule.countdownDescription(),
                        fontSize = 14.sp,
                        color = SLCColor.HerDeep,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCapsuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var unlockDate by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.MONTH, 6) }.timeInMillis)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建时光胶囊") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SLCSpace.md)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("写给未来的你们...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, content, unlockDate) },
                enabled = title.isNotBlank() && content.isNotBlank(),
            ) {
                Text("封存", color = SLCColor.Him, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = SLCColor.TextSecondary)
            }
        }
    )
}