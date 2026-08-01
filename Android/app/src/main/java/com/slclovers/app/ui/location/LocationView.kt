package com.slclovers.app.ui.location

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import com.slclovers.app.util.LocationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationView(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    // 模拟两个位置（北京坐标附近）
    val myLoc = Pair(39.9042, 116.4074)
    val partnerLoc = Pair(39.9142, 116.4174)
    val distance = LocationUtils.distance(
        myLoc.first, myLoc.second, partnerLoc.first, partnerLoc.second
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("想见你", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.spacedBy(SLCSpace.lg),
        ) {
            // 距离卡
            DistanceCard(distance = distance)

            // 简易地图
            SimpleMapView(
                modifier = Modifier
                    .padding(horizontal = SLCSpace.lg)
                    .fillMaxWidth()
                    .height(320.dp)
            )

            // 状态行
            Column(
                modifier = Modifier.padding(horizontal = SLCSpace.lg),
                verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
            ) {
                LocationStatusRow(
                    role = currentUser?.role ?: UserRole.Him,
                    address = "北京市朝阳区三里屯",
                    isSharing = true,
                )
                LocationStatusRow(
                    role = if (currentUser?.role == UserRole.Him) UserRole.Her else UserRole.Him,
                    address = "北京市海淀区中关村",
                    isSharing = true,
                )
            }
        }
    }
}

@Composable
private fun DistanceCard(distance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SLCSpace.lg)
            .shadow(2.dp, RoundedCornerShape(SLCRadius.xl))
            .clip(RoundedCornerShape(SLCRadius.xl))
            .background(SLCColor.CreamLight)
            .padding(vertical = SLCSpace.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "相距",
                fontSize = 14.sp,
                color = SLCColor.TextSecondary,
            )
            Spacer(Modifier.height(SLCSpace.sm))
            Text(
                LocationUtils.formattedDistance(distance),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraLight,
                ),
                color = SLCColor.Him,
            )
        }
    }
}

@Composable
private fun SimpleMapView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(SLCRadius.xl))
            .clip(RoundedCornerShape(SLCRadius.xl))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 背景
            drawRect(SLCColor.CreamLight)

            // 网格
            val gridColor = SLCColor.TextTertiary.copy(alpha = 0.1f)
            val step = 40f
            for (i in 0..(size.width / step).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(i * step, 0f),
                    end = Offset(i * step, size.height),
                    strokeWidth = 0.5f
                )
            }
            for (i in 0..(size.height / step).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, i * step),
                    end = Offset(size.width, i * step),
                    strokeWidth = 0.5f
                )
            }

            // 路线（虚线）
            drawLine(
                color = SLCColor.HerDeep.copy(alpha = 0.6f),
                start = Offset(size.width * 0.3f, size.height * 0.625f),
                end = Offset(size.width * 0.7f, size.height * 0.375f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )

            // 我的点
            drawCircle(SLCColor.Him, radius = 8f, center = Offset(size.width * 0.3f, size.height * 0.625f))
            drawCircle(SLCColor.Cream, radius = 5f, center = Offset(size.width * 0.3f, size.height * 0.625f), style = Stroke(width = 3f))

            // TA 的点
            drawCircle(SLCColor.Her, radius = 8f, center = Offset(size.width * 0.7f, size.height * 0.375f))
            drawCircle(SLCColor.Cream, radius = 5f, center = Offset(size.width * 0.7f, size.height * 0.375f), style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun LocationStatusRow(role: UserRole, address: String, isSharing: Boolean) {
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
                .background(SLCColor.person(role)),
            contentAlignment = Alignment.Center,
        ) {
            Text(role.displayName, color = SLCColor.Cream, fontSize = 14.sp)
        }
        Spacer(Modifier.width(SLCSpace.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                address,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = SLCColor.TextPrimary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSharing) SLCColor.Success else SLCColor.TextTertiary)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (isSharing) "正在共享位置" else "未共享",
                    fontSize = 12.sp,
                    color = SLCColor.TextSecondary,
                )
            }
        }
        Text(
            "电量 78%",
            fontSize = 12.sp,
            color = SLCColor.TextSecondary,
        )
    }
}