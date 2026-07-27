package com.slclovers.app.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.ui.components.SLCEmptyView
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryView(viewModel: AppViewModel) {
    val photos by viewModel.photos.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("相册", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SLCColor.Cream
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = SLCColor.Him,
                contentColor = SLCColor.Cream,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = SLCColor.Cream,
    ) { padding ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SLCEmptyView(
                    icon = Icons.Default.Photo,
                    title = "还没有照片",
                    subtitle = "把你们的回忆都装进来吧"
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(SLCSpace.xs),
                verticalArrangement = Arrangement.spacedBy(SLCSpace.xs),
                horizontalArrangement = Arrangement.spacedBy(SLCSpace.xs),
            ) {
                items(photos) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(SLCRadius.sm))
                            .background(SLCColor.CreamDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = null,
                            tint = SLCColor.TextTertiary,
                        )
                    }
                }
            }
        }
    }
}