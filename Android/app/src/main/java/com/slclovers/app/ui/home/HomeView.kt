package com.slclovers.app.ui.home

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.AnniversaryEntity
import com.slclovers.app.data.model.TodoEntity
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.components.SLCCard
import com.slclovers.app.ui.components.SLCPersonBadge
import com.slclovers.app.ui.components.SLCSectionHeader
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.DateUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val pairing by viewModel.pairing.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val todos by viewModel.todos.collectAsState()
    val bucketItems by viewModel.bucketItems.collectAsState()
    val anniversaries by viewModel.anniversaries.collectAsState()

    // 每分钟刷新一次
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SLCColor.Cream)
            .verticalScroll(rememberScrollState())
            .padding(vertical = SLCSpace.md),
    ) {
        // 顶部
        HeaderSection(userName = currentUser?.name ?: "我")

        Spacer(Modifier.height(SLCSpace.lg))

        // 在一起大数字
        pairing?.startDate?.let { TogetherCounter(startDate = it, now = now) }

        Spacer(Modifier.height(SLCSpace.lg))

        // 每日寄语
        QuoteCard()

        Spacer(Modifier.height(SLCSpace.lg))

        // 下一个纪念日
        nextAnniversary(anniversaries)?.let { ann ->
            NextAnniversaryCard(anniversary = ann)
        }

        Spacer(Modifier.height(SLCSpace.lg))

        // 待办预览
        TodoPreviewSection(todos = todos.filter { !it.isCompleted }.take(3)) {
            todo -> viewModel.toggleTodo(todo)
        }

        Spacer(Modifier.height(SLCSpace.lg))

        // 愿望清单预览
        BucketPreviewSection(buckets = bucketItems.filter { !it.isAchieved }.take(5))

        Spacer(Modifier.height(SLCSpace.lg))

        // 最近聊天
        if (messages.isNotEmpty()) {
            RecentChatSection(lastMessage = messages.last(), partnerName = "TA")
        }

        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

private fun nextAnniversary(anniversaries: List<AnniversaryEntity>): AnniversaryEntity? {
    return anniversaries
        .filter { !it.title.contains("在一起") }
        .minByOrNull { it.daysUntilNext() }
}

// ============ 顶部 ============
@Composable
private fun HeaderSection(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SLCSpace.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                greeting(),
                color = SLCColor.TextSecondary,
                fontSize = 13.sp,
            )
            Text(
                userName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = SLCColor.TextPrimary,
            )
        }
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = "Profile",
            tint = SLCColor.Him,
            modifier = Modifier.size(36.dp)
        )
    }
}

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..10 -> "早安，"
        in 11..13 -> "中午好，"
        in 14..17 -> "下午好，"
        in 18..21 -> "晚上好，"
        else -> "夜深了，"
    }
}

// ============ 在一起大数字 ============
@Composable
private fun TogetherCounter(startDate: Long, now: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SLCSpace.lg)
            .shadow(2.dp, RoundedCornerShape(SLCRadius.xl))
            .clip(RoundedCornerShape(SLCRadius.xl))
            .background(
                Brush.verticalGradient(
                    listOf(SLCColor.CreamLight, SLCColor.Cream)
                )
            )
            .padding(vertical = SLCSpace.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "在一起",
                color = SLCColor.TextSecondary,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(SLCSpace.sm))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    DateUtils.togetherDays(startDate),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 96.sp,
                        fontWeight = FontWeight.UltraLight,
                    ),
                    color = SLCColor.Him,
                )
                Text(
                    " 天",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = SLCColor.Him,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(Modifier.height(SLCSpace.sm))
            Text(
                DateUtils.fullChinese(startDate),
                color = SLCColor.TextSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

// ============ 每日寄语 ============
@Composable
private fun QuoteCard() {
    val quotes = listOf(
        "和你在一起的每一天，都是最好的时光。",
        "遇见你，是所有故事里最美的那一页。",
        "愿我们携手，从心动走到古稀。",
        "最浪漫的事，是和你一起慢慢变老。",
        "世界再大，也不过是两个人的小家。",
        "你在哪里，哪里就是家。",
        "爱就是和心爱的人，一起做无聊的事。",
        "谢谢你，陪我走过每一个春夏秋冬。",
    )
    val today = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    val seed = (today / 86400000L).toInt()
    val quote = quotes[seed % quotes.size]

    Box(modifier = Modifier.padding(horizontal = SLCSpace.lg)) {
        SLCCard {
            Row {
                Icon(
                    Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = SLCColor.HerDeep.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(SLCSpace.md))
                Text(
                    quote,
                    fontSize = 16.sp,
                    color = SLCColor.TextPrimary,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

// ============ 下一个纪念日 ============
@Composable
private fun NextAnniversaryCard(anniversary: AnniversaryEntity) {
    Box(modifier = Modifier.padding(horizontal = SLCSpace.lg)) {
        SLCCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(anniversary.icon, fontSize = 44.sp)
                Spacer(Modifier.width(SLCSpace.md))
                Column {
                    Text(
                        anniversary.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = SLCColor.TextPrimary,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "还有 ",
                            fontSize = 13.sp,
                            color = SLCColor.TextSecondary,
                        )
                        Text(
                            "${anniversary.daysUntilNext()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = SLCColor.HerDeep,
                        )
                        Text(
                            " 天",
                            fontSize = 13.sp,
                            color = SLCColor.TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SLCColor.TextTertiary,
                )
            }
        }
    }
}

// ============ 待办预览 ============
@Composable
private fun TodoPreviewSection(todos: List<TodoEntity>, onToggle: (TodoEntity) -> Unit) {
    Column {
        SLCSectionHeader(title = "待办", action = "全部", onAction = { /* TODO */ })
        if (todos.isEmpty()) {
            Text(
                "暂无待办，去记下今天想做的事吧",
                color = SLCColor.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SLCSpace.lg)
                    .clip(RoundedCornerShape(SLCRadius.lg))
                    .background(SLCColor.CreamLight)
                    .padding(SLCSpace.lg),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = SLCSpace.md),
                verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
            ) {
                todos.forEach { todo ->
                    TodoRow(todo = todo, onToggle = { onToggle(todo) })
                }
            }
        }
    }
}

@Composable
private fun TodoRow(todo: TodoEntity, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SLCRadius.md))
            .background(SLCColor.CreamLight)
            .padding(SLCSpace.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle",
                tint = SLCColor.TextTertiary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(todo.title, fontSize = 16.sp, color = SLCColor.TextPrimary)
            todo.dueDate?.let {
                Text(
                    DateUtils.monthDay(it),
                    fontSize = 12.sp,
                    color = SLCColor.TextSecondary,
                )
            }
        }
        todo.assignee?.let { SLCPersonBadge(role = it, compact = true) }
    }
}

// ============ 愿望预览 ============
@Composable
private fun BucketPreviewSection(buckets: List<com.slclovers.app.data.model.BucketItemEntity>) {
    Column {
        SLCSectionHeader(title = "愿望清单")
        if (buckets.isEmpty()) {
            Text(
                "还没有愿望，去记下想做的事吧",
                color = SLCColor.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SLCSpace.lg)
                    .clip(RoundedCornerShape(SLCRadius.lg))
                    .background(SLCColor.CreamLight)
                    .padding(SLCSpace.lg),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = SLCSpace.md),
                horizontalArrangement = Arrangement.spacedBy(SLCSpace.sm),
            ) {
                items(buckets) { bucket ->
                    BucketCard(bucket)
                }
            }
        }
    }
}

@Composable
private fun BucketCard(bucket: com.slclovers.app.data.model.BucketItemEntity) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(SLCRadius.lg))
            .background(SLCColor.CreamLight)
            .padding(SLCSpace.md),
    ) {
        Text(
            bucket.title,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = SLCColor.TextPrimary,
            maxLines = 2,
        )
        Spacer(Modifier.weight(1f))
        bucket.description?.let {
            Text(
                it,
                fontSize = 12.sp,
                color = SLCColor.TextSecondary,
                maxLines = 1,
            )
        }
    }
}

// ============ 最近聊天 ============
@Composable
private fun RecentChatSection(lastMessage: com.slclovers.app.data.model.MessageEntity, partnerName: String) {
    Column {
        SLCSectionHeader(title = "最近的消息")
        Box(modifier = Modifier.padding(horizontal = SLCSpace.md)) {
            SLCCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = SLCColor.HerDeep,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(SLCSpace.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            lastMessage.content,
                            fontSize = 16.sp,
                            color = SLCColor.TextPrimary,
                            maxLines = 1,
                        )
                        Text(
                            DateUtils.friendlyRelative(lastMessage.createdAt),
                            fontSize = 12.sp,
                            color = SLCColor.TextSecondary,
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SLCColor.TextTertiary,
                    )
                }
            }
        }
    }
}

// LazyRow helper - end of file