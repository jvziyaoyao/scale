package com.origeek.viewerDemo.ui.component

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme

@Composable
fun BasePage(content: @Composable () -> Unit) {
    ViewerDemoTheme {
        ProvideWindowInsets {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }
            content()
        }
    }
}