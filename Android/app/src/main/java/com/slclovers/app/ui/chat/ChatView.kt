package com.slclovers.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Send
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
import com.slclovers.app.data.model.MessageEntity
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(viewModel: AppViewModel) {
    val messages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val pairing by viewModel.pairing.collectAsState()

    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("TA", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("在线", fontSize = 11.sp, color = SLCColor.Success)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SLCColor.Cream
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = {
                    if (draft.isNotBlank()) {
                        viewModel.sendMessage(draft.trim())
                        draft = ""
                    }
                }
            )
        },
        containerColor = SLCColor.Cream,
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = SLCSpace.md),
            contentPadding = PaddingValues(vertical = SLCSpace.md),
            verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
        ) {
            // 按日期分组
            val grouped = messages.groupBy { dayKey(it.createdAt) }
            val sortedKeys = grouped.keys.sorted()
            sortedKeys.forEach { day ->
                item(key = "day-$day") {
                    DateDivider(day)
                }
                grouped[day]?.sortedBy { it.createdAt }?.forEach { msg ->
                    item(key = msg.id) {
                        MessageBubble(
                            message = msg,
                            isCurrentUser = msg.senderId == currentUser?.id,
                        )
                    }
                }
            }
        }
    }
}

private fun dayKey(date: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

@Composable
private fun DateDivider(day: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SLCSpace.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SLCColor.TextTertiary.copy(alpha = 0.2f))
        )
        Text(
            DateUtils.fullChinese(day),
            fontSize = 12.sp,
            color = SLCColor.TextSecondary,
            modifier = Modifier.padding(horizontal = SLCSpace.sm)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SLCColor.TextTertiary.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
    ) {
        if (isCurrentUser) Spacer(Modifier.width(60.dp))

        if (!isCurrentUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SLCColor.Her),
                contentAlignment = Alignment.Center,
            ) {
                Text("她", fontSize = 12.sp, color = SLCColor.Cream, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(SLCSpace.sm))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(SLCRadius.lg))
                    .background(if (isCurrentUser) SLCColor.Him else SLCColor.CreamDeep)
                    .padding(horizontal = SLCSpace.md, vertical = SLCSpace.sm + 2.dp),
            ) {
                Text(
                    message.content,
                    color = if (isCurrentUser) SLCColor.Cream else SLCColor.TextPrimary,
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                DateUtils.timeOnly(message.createdAt),
                fontSize = 10.sp,
                color = SLCColor.TextTertiary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        if (!isCurrentUser) Spacer(Modifier.width(60.dp))
    }
}

@Composable
private fun ChatInputBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = SLCColor.CreamLight,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SLCSpace.md, vertical = SLCSpace.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* TODO: 小纸条 */ }) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = SLCColor.HerDeep,
                )
            }

            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = { Text("说点什么…") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SLCColor.Him,
                    unfocusedBorderColor = SLCColor.CreamDeep,
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
            )

            IconButton(
                onClick = onSend,
                enabled = draft.isNotBlank(),
            ) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Send",
                    tint = if (draft.isBlank()) SLCColor.TextTertiary else SLCColor.Him,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}