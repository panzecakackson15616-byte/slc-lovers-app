package com.slclovers.app.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.HobbyEntity
import com.slclovers.app.data.model.NoteColor
import com.slclovers.app.data.model.StickyNoteEntity
import com.slclovers.app.ui.components.SLCEmptyView
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardView(viewModel: AppViewModel) {
    val hobbies by viewModel.hobbies.collectAsState()
    val notes by viewModel.stickyNotes.collectAsState()
    var showAddNote by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("我们", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { /* 返回 */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SLCColor.Cream
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNote = true },
                containerColor = SLCColor.Him,
                contentColor = SLCColor.Cream,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
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
            // 爱好
            Column {
                Text(
                    "我们喜欢",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = SLCColor.TextPrimary,
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                )
                Spacer(Modifier.height(SLCSpace.sm))
                FlowRow(
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                    horizontalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                    verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                ) {
                    hobbies.forEach { hobby ->
                        HobbyChip(hobby)
                    }
                }
            }

            // 留言板
            Column {
                Text(
                    "留言板",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = SLCColor.TextPrimary,
                    modifier = Modifier.padding(horizontal = SLCSpace.md),
                )
                Spacer(Modifier.height(SLCSpace.sm))
            }

            if (notes.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SLCEmptyView(
                        icon = Icons.Default.NoteAlt,
                        title = "还没有留言",
                        subtitle = "贴一张小纸条吧"
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = SLCSpace.md),
                    horizontalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                    verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
                    modifier = Modifier.height(((notes.size + 1) / 2 * 110).dp),
                ) {
                    items(notes) { note ->
                        StickyNoteCard(note = note)
                    }
                }
            }
        }

        if (showAddNote) {
            AddNoteDialog(
                onDismiss = { showAddNote = false },
                onConfirm = { content, color ->
                    viewModel.addNote(content, color)
                    showAddNote = false
                }
            )
        }
    }
}

@Composable
private fun HobbyChip(hobby: HobbyEntity) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(SLCRadius.md))
            .background(SLCColor.CreamLight)
            .padding(horizontal = SLCSpace.sm, vertical = 6.dp)
            .border(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(hobby.emoji, fontSize = 16.sp)
        Spacer(Modifier.width(4.dp))
        Column {
            Text(
                hobby.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = SLCColor.TextPrimary,
            )
            hobby.subtitle?.let {
                Text(it, fontSize = 10.sp, color = SLCColor.TextSecondary)
            }
        }
    }
}

@Composable
private fun StickyNoteCard(note: StickyNoteEntity) {
    val bgColor = Color(android.graphics.Color.parseColor(note.color.hex))
    val textColor = if (note.color == NoteColor.Black) SLCColor.Cream else SLCColor.TextPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .rotate(note.rotation.toFloat())
            .shadow(2.dp, RoundedCornerShape(SLCRadius.sm))
            .clip(RoundedCornerShape(SLCRadius.sm))
            .background(bgColor)
            .padding(SLCSpace.md),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            note.content,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor,
        )
    }
}

@Composable
private fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, NoteColor) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(NoteColor.Cream) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("贴留言") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SLCSpace.md)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("写点什么...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(SLCSpace.sm)) {
                    NoteColor.values().forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(android.graphics.Color.parseColor(c.hex)))
                                .clickable { color = c }
                                .then(
                                    if (color == c) Modifier
                                        .shadow(2.dp, RoundedCornerShape(999.dp))
                                    else Modifier
                                ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(content, color) },
                enabled = content.isNotBlank(),
            ) {
                Text("贴上", color = SLCColor.Him, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = SLCColor.TextSecondary)
            }
        }
    )
}

// 简易 FlowRow 实现（避免依赖 foundation:foundation-layout）
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
    ) {
        content()
    }
}

// border 扩展
private fun Modifier.border() = this