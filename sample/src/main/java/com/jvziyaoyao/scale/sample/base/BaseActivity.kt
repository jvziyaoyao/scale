package com.jvziyaoyao.scale.sample.base

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.jvziyaoyao.scale.sample.ui.theme.ScaleSampleTheme
import com.jvziyaoyao.scale.sample.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class BaseActivity : ComponentActivity(), CoroutineScope by MainScope() {

    fun setBasicContent(
        content: @Composable () -> Unit,
    ) {
        val systemBarStyle =
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )

                Configuration.UI_MODE_NIGHT_YES -> SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else -> SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )
        setContent {
            ScaleSampleTheme {
                content()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

}