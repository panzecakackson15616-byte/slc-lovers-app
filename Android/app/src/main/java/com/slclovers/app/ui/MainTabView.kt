package com.slclovers.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.slclovers.app.AppViewModel
import com.slclovers.app.ui.board.BoardView
import com.slclovers.app.ui.capsule.CapsuleView
import com.slclovers.app.ui.chat.ChatView
import com.slclovers.app.ui.diary.DiaryView
import com.slclovers.app.ui.gallery.GalleryView
import com.slclovers.app.ui.home.HomeView
import com.slclovers.app.ui.location.LocationView
import com.slclovers.app.ui.settings.SettingsView
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabView(viewModel: AppViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("首页", Icons.Default.Home),
        TabItem("聊天", Icons.Default.ChatBubble),
        TabItem("相册", Icons.Default.Photo),
        TabItem("日记", Icons.Default.MenuBook),
        TabItem("更多", Icons.Default.MoreHoriz),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = com.slclovers.app.ui.theme.SLCColor.CreamLight
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.slclovers.app.ui.theme.SLCColor.Him,
                            selectedTextColor = com.slclovers.app.ui.theme.SLCColor.Him,
                            indicatorColor = com.slclovers.app.ui.theme.SLCColor.CreamDeep,
                            unselectedIconColor = com.slclovers.app.ui.theme.SLCColor.TextTertiary,
                            unselectedTextColor = com.slclovers.app.ui.theme.SLCColor.TextTertiary,
                        )
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> HomeView(viewModel)
                1 -> ChatView(viewModel)
                2 -> GalleryView(viewModel)
                3 -> DiaryView(viewModel)
                4 -> MoreTabView(viewModel)
            }
        }
    }
}

private data class TabItem(
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreTabView(viewModel: AppViewModel) {
    var selectedFeature by rememberSaveable { mutableStateOf<String?>(null) }

    when (selectedFeature) {
        null -> com.slclovers.app.ui.more.MoreView(
            viewModel = viewModel,
            onNavigate = { feature -> selectedFeature = feature }
        )
        "location" -> LocationView(viewModel)
        "capsule" -> CapsuleView(viewModel)
        "board" -> BoardView(viewModel)
        "settings" -> SettingsView(viewModel)
        "sync" -> com.slclovers.app.ui.settings.SyncSettingsView(viewModel)
    }
}