package com.slclovers.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.slclovers.app.ui.RootView
import com.slclovers.app.ui.theme.SLCColor
import com.slclovers.app.ui.theme.SLCTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SLCTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SLCColor.Cream),
                    color = SLCColor.Cream
                ) {
                    RootView(viewModel = viewModel)
                }
            }
        }
    }
}