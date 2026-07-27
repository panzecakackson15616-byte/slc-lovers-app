package com.slclovers.app.ui.pairing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slclovers.app.AppViewModel
import com.slclovers.app.data.model.UserRole
import com.slclovers.app.ui.components.SLCPrimaryButton
import com.slclovers.app.ui.components.SLCSecondaryButton
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCRadius
import com.slclovers.app.ui.theme.SLCSpace
import kotlinx.coroutines.launch

/**
 * 配对引导流程
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingFlow(viewModel: AppViewModel) {
    var step by remember { mutableStateOf(Step.INTRO) }
    val scope = rememberCoroutineScope()
    val joinCode = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SLCColor.Cream)
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                fadeIn() + slideInHorizontally(initialOffsetX = { it / 4 }) togetherWith
                        fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 4 })
            },
            label = "step"
        ) { current ->
            when (current) {
                Step.INTRO -> IntroStep(onNext = { step = Step.ROLE })
                Step.ROLE -> RoleStep(
                    viewModel = viewModel,
                    onNext = { step = Step.NAME }
                )
                Step.NAME -> NameStep(
                    viewModel = viewModel,
                    onNext = { step = Step.START_DATE }
                )
                Step.START_DATE -> StartDateStep(
                    viewModel = viewModel,
                    onNext = { step = Step.CREATE_OR_JOIN }
                )
                Step.CREATE_OR_JOIN -> CreateOrJoinStep(
                    onCreate = { step = Step.CREATE },
                    onJoin = { step = Step.JOIN }
                )
                Step.CREATE -> CreateStep(
                    viewModel = viewModel,
                    onSuccess = {
                        scope.launch {
                            viewModel.createPairing(
                                name = viewModel.tempName.value.ifEmpty { "我" },
                                role = viewModel.tempRole.value ?: UserRole.Him,
                                startDate = viewModel.tempStartDate.value
                            )
                        }
                    }
                )
                Step.JOIN -> JoinStep(
                    viewModel = viewModel,
                    code = joinCode,
                    onSuccess = {
                        scope.launch {
                            viewModel.joinPairing(
                                name = viewModel.tempName.value.ifEmpty { "TA" },
                                role = viewModel.tempRole.value ?: UserRole.Her,
                                code = joinCode.value,
                                startDate = viewModel.tempStartDate.value
                            )
                        }
                    }
                )
            }
        }
    }
}

private enum class Step {
    INTRO, ROLE, NAME, START_DATE, CREATE_OR_JOIN, CREATE, JOIN
}

// ============ 介绍 ============
@Composable
private fun IntroStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "S & LC",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                color = SLCColor.Him,
            )
            Spacer(Modifier.height(SLCSpace.md))
            Text(
                "Just for the two of us",
                fontSize = 15.sp,
                color = SLCColor.TextSecondary,
                letterSpacing = 2.sp,
            )
        }
        Spacer(Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SLCSpace.sm),
        ) {
            Text(
                "一个只属于两个人的私密空间",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = SLCColor.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                "记录爱 · 守护时光 · 珍藏回忆",
                fontSize = 14.sp,
                color = SLCColor.TextSecondary,
            )
        }
        Spacer(Modifier.weight(1f))
        SLCPrimaryButton(text = "开始", onClick = onNext)
        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

// ============ 角色选择 ============
@Composable
private fun RoleStep(viewModel: AppViewModel, onNext: () -> Unit) {
    val role by viewModel.tempRole.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("你是？", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SLCSpace.sm))
            Text("选一个属于你的颜色", color = SLCColor.TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.height(SLCSpace.xl))

        Row(
            modifier = Modifier.padding(horizontal = SLCSpace.xl),
            horizontalArrangement = Arrangement.spacedBy(SLCSpace.lg)
        ) {
            RoleCard(
                role = UserRole.Him,
                selected = role == UserRole.Him,
                onTap = { viewModel.setTempRole(UserRole.Him) },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                role = UserRole.Her,
                selected = role == UserRole.Her,
                onTap = { viewModel.setTempRole(UserRole.Her) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.weight(1f))
        SLCPrimaryButton(
            text = "下一步",
            onClick = onNext,
            enabled = role != null,
        )
        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    selected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(SLCRadius.lg))
            .background(SLCColor.CreamLight)
            .clickable(onClick = onTap)
            .padding(vertical = SLCSpace.lg)
            .scale(if (selected) 1.05f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SLCSpace.md)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SLCColor.person(role)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                role.displayName,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                color = SLCColor.Cream,
            )
        }
        Text(
            if (role == UserRole.Him) "沉稳 · 墨黑" else "温柔 · 玫瑰金",
            fontSize = 12.sp,
            color = SLCColor.TextSecondary
        )
    }
}

// ============ 名字输入 ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NameStep(viewModel: AppViewModel, onNext: () -> Unit) {
    val name by viewModel.tempName.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("给自己起个昵称", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SLCSpace.sm))
            Text("对方会看到这个名字", color = SLCColor.TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.height(SLCSpace.xl))

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.setTempName(it) },
            placeholder = { Text("例如：小柚", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SLCSpace.xl),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SLCColor.Him,
                unfocusedBorderColor = SLCColor.CreamDeep,
            ),
        )
        Spacer(Modifier.weight(1f))
        SLCPrimaryButton(
            text = "下一步",
            onClick = onNext,
            enabled = name.isNotEmpty(),
        )
        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

// ============ 在一起日期 ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDateStep(viewModel: AppViewModel, onNext: () -> Unit) {
    val startDate by viewModel.tempStartDate.collectAsState()
    val role by viewModel.tempRole.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.3f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "你们是什么时候在一起的？",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(SLCSpace.sm))
            Text("我们会从这一天开始计算", color = SLCColor.TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.height(SLCSpace.md))

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()
            }
        )

        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = SLCColor.person(role ?: UserRole.Him),
            ),
            modifier = Modifier.padding(horizontal = SLCSpace.lg)
        )

        LaunchedEffect(datePickerState.selectedDateMillis) {
            datePickerState.selectedDateMillis?.let { viewModel.setTempStartDate(it) }
        }

        Spacer(Modifier.weight(1f))
        SLCPrimaryButton(text = "下一步", onClick = onNext)
        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

// ============ 创建/加入选择 ============
@Composable
private fun CreateOrJoinStep(onCreate: () -> Unit, onJoin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("怎么开始？", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SLCSpace.sm))
            Text("选择一方先发起连接", color = SLCColor.TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(SLCSpace.md)) {
            SLCPrimaryButton(text = "我先创建连接", onClick = onCreate)
            SLCSecondaryButton(text = "对方已经创建，我加入", onClick = onJoin)
        }
        Spacer(Modifier.weight(1f))
    }
}

// ============ 创建 ============
@Composable
private fun CreateStep(viewModel: AppViewModel, onSuccess: () -> Unit) {
    var code by remember { mutableStateOf(generateCode()) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("把这个码发给 TA", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SLCSpace.sm))
            Text(
                "对方在另一台手机上输入即可连接",
                color = SLCColor.TextSecondary,
                fontSize = 15.sp,
            )
        }
        Spacer(Modifier.height(SLCSpace.xl))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(SLCRadius.lg))
                .background(SLCColor.CreamLight)
                .padding(horizontal = SLCSpace.lg, vertical = SLCSpace.lg)
        ) {
            Text(
                code,
                fontSize = 56.sp,
                color = SLCColor.Him,
                style = MaterialTheme.typography.displayLarge,
                letterSpacing = 8.sp,
            )
        }
        Spacer(Modifier.height(SLCSpace.md))
        TextButton(onClick = {
            clipboardManager.setText(AnnotatedString(code))
        }) {
            Text("复制配对码", color = SLCColor.HerDeep, fontSize = 14.sp)
        }

        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("等待对方输入...", color = SLCColor.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(SLCSpace.sm))
            SLCPrimaryButton(
                text = "对方已输入，继续",
                onClick = onSuccess,
            )
        }
        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

// ============ 加入 ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinStep(
    viewModel: AppViewModel,
    code: MutableState<String>,
    onSuccess: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(SLCSpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("输入对方的配对码", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SLCSpace.sm))
            Text("6 位数字", color = SLCColor.TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.height(SLCSpace.xl))

        OutlinedTextField(
            value = code.value,
            onValueChange = {
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    code.value = it
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.displayLarge.copy(
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SLCSpace.xl),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SLCColor.Him,
                unfocusedBorderColor = SLCColor.CreamDeep,
            ),
        )

        Spacer(Modifier.weight(1f))
        SLCPrimaryButton(
            text = "连接",
            onClick = onSuccess,
            enabled = code.value.length == 6,
        )
        Spacer(Modifier.height(SLCSpace.xxl))
    }
}

private fun generateCode(): String = (100000..999999).random().toString()