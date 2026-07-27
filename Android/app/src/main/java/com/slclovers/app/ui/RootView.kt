package com.slclovers.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.slclovers.app.AppViewModel
import com.slclovers.app.ui.pairing.PairingFlow
import com.slclovers.app.ui.theme.SLCColor

@Composable
fun RootView(viewModel: AppViewModel) {
    val isPaired by viewModel.isPaired.collectAsState()

    AnimatedContent(
        targetState = isPaired,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "root"
    ) { paired ->
        if (paired) {
            MainTabView(viewModel)
        } else {
            PairingFlow(viewModel)
        }
    }
}