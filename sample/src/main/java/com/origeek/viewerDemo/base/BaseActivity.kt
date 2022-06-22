package com.origeek.viewerDemo.base

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.origeek.viewerDemo.ui.component.BasePage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class BaseActivity: ComponentActivity(), CoroutineScope by MainScope() {

    fun setBasicContent(
        content: @Composable () -> Unit,
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BasePage {
                content()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

}