package com.origeek.viewerDemo.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlin.math.ceil

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

@Composable
fun GridLayout(
    modifier: Modifier = Modifier,
    columns: Int,
    size: Int,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    block: @Composable (Int) -> Unit,
) {
    val line = ceil(size.toDouble() / columns).toInt()
    LazyColumn(
        modifier = modifier,
        state = state,
        content = {
            items(count = line, key = { it }) { c ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (r in 0 until columns) {
                        val index = c * columns + r
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            if (index < size) {
                                block(index)
                            }
                        }
                    }
                }
            }
        }, contentPadding = contentPadding
    )
}