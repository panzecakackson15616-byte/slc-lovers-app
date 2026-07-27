package com.slclovers.app.ui.diary

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.BucketItemEntity
import com.slclovers.app.data.model.Mood
import com.slclovers.app.data.model.TodoEntity
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.components.SLCCard
import com.slclovers.app.ui.components.SLCEmptyView
import com.slclovers.app.ui.components.SLCPersonBadge
import com.slclovers.app.ui.components.SLCSectionHeader
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.DateUtils

private enum class DiaryTab(val label: String) {
    Mood("心情"), Todo("待办"), Bucket("愿望")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryView(viewModel: AppViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(DiaryTab.Mood) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("日记", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SLCColor.Cream
                ),
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        },
        containerColor = SLCColor.Cream,
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = SLCColor.Cream,
                contentColor = SLCColor.Him,
            ) {
                DiaryTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }

            when (selectedTab) {
                DiaryTab.Mood -> MoodDiarySection(viewModel)
                DiaryTab.Todo -> TodoSection(viewModel)
                DiaryTab.Bucket -> BucketSection(viewModel)
            }
        }
    }
}

@Composable
private fun MoodDiarySection(viewModel: AppViewModel) {
    val entries by viewModel.diaryEntries.collectAsState()

    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SLCEmptyView(
                icon = Icons.Default.MenuBook,
                title = "还没有日记",
                subtitle = "记录心情，留下此刻"
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = SLCSpace.md),
            verticalArrangement = Arrangement.spacedBy(SLCSpace.md),
        ) {
            entries.forEach { entry ->
                Box(modifier = Modifier.padding(horizontal = SLCSpace.md)) {
                    SLCCard {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(entry.mood.emoji, fontSize = 28.sp)
                            Spacer(Modifier.width(SLCSpace.sm))
                            Column {
                                Text(
                                    entry.mood.displayName,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    color = SLCColor.TextPrimary,
                                )
                                Text(
                                    DateUtils.fullChinese(entry.createdAt),
                                    fontSize = 12.sp,
                                    color = SLCColor.TextSecondary,
                                )
                                Spacer(Modifier.height(SLCSpace.xs))
                                entry.title?.let {
                                    Text(
                                        it,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = SLCColor.TextPrimary,
                                    )
                                }
                                Spacer(Modifier.height(SLCSpace.xs))
                                Text(
                                    entry.content,
                                    fontSize = 14.sp,
                                    color = SLCColor.TextSecondary,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoSection(viewModel: AppViewModel) {
    val todos by viewModel.todos.collectAsState()
    val pending = todos.filter { !it.isCompleted }
    val completed = todos.filter { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = SLCSpace.md),
    ) {
        if (pending.isEmpty() && completed.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SLCEmptyView(
                    icon = Icons.Default.Checklist,
                    title = "还没有待办",
                    subtitle = "把想做的事记下来"
                )
            }
        } else {
            if (pending.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                    verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                ) {
                    pending.forEach { todo ->
                        TodoRow(todo = todo, onToggle = { viewModel.toggleTodo(todo) })
                    }
                }
            }
            if (completed.isNotEmpty()) {
                Spacer(Modifier.height(SLCSpace.lg))
                SLCSectionHeader(title = "已完成")
                Column(
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                    verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                ) {
                    completed.forEach { todo ->
                        TodoRow(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo) },
                            dimmed = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoRow(todo: TodoEntity, onToggle: () -> Unit, dimmed: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SLCRadius.md))
            .background(if (dimmed) SLCColor.CreamDeep else SLCColor.CreamLight)
            .padding(SLCSpace.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle",
                tint = if (todo.isCompleted) SLCColor.Success else SLCColor.TextTertiary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                todo.title,
                fontSize = 16.sp,
                color = if (todo.isCompleted) SLCColor.TextSecondary else SLCColor.TextPrimary,
                textDecoration = if (todo.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            )
            todo.dueDate?.let {
                Text(
                    DateUtils.monthDay(it),
                    fontSize = 12.sp,
                    color = SLCColor.TextTertiary,
                )
            }
        }
        todo.assignee?.let { SLCPersonBadge(role = it, compact = true) }
    }
}

@Composable
private fun BucketSection(viewModel: AppViewModel) {
    val buckets by viewModel.bucketItems.collectAsState()
    val active = buckets.filter { !it.isAchieved }
    val done = buckets.filter { it.isAchieved }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = SLCSpace.md),
    ) {
        if (active.isEmpty() && done.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SLCEmptyView(
                    icon = Icons.Default.Star,
                    title = "还没有愿望",
                    subtitle = "写下想一起做的事"
                )
            }
        } else {
            if (active.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                    verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                ) {
                    active.forEach { item ->
                        BucketRow(item = item, onToggle = { viewModel.toggleBucket(item) })
                    }
                }
            }
            if (done.isNotEmpty()) {
                Spacer(Modifier.height(SLCSpace.lg))
                SLCSectionHeader(title = "已实现")
                Column(
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                    verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                ) {
                    done.forEach { item ->
                        BucketRow(item = item, onToggle = { viewModel.toggleBucket(item) }, dimmed = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun BucketRow(item: BucketItemEntity, onToggle: () -> Unit, dimmed: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SLCRadius.md))
            .background(if (dimmed) SLCColor.CreamDeep else SLCColor.CreamLight)
            .padding(SLCSpace.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                if (item.isAchieved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle",
                tint = if (item.isAchieved) SLCColor.Success else SLCColor.TextTertiary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = SLCColor.TextPrimary,
                textDecoration = if (item.isAchieved) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            )
            item.description?.let {
                Text(
                    it,
                    fontSize = 12.sp,
                    color = SLCColor.TextSecondary,
                )
            }
        }
    }
}