package com.jvziyaoyao.scale.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.jvziyaoyao.scale.sample.page.ZoomableBody
import com.jvziyaoyao.scale.sample.ui.theme.ScaleSampleTheme
import platform.UIKit.UIViewController

fun mainViewController(): UIViewController {
    return ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        }
    ) {
        ScaleSampleTheme {
            ZoomableBody()
        }
    }
}