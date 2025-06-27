package com.jvziyaoyao.scale.sample.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import coil3.util.Logger

val coilLogger = object : Logger {
    override var minLevel: Logger.Level
        get() = Logger.Level.Info
        set(value) {}

    override fun log(
        tag: String,
        level: Logger.Level,
        message: String?,
        throwable: Throwable?
    ) {
        com.jvziyaoyao.scale.sample.base.log(msg = message ?: "无信息～", tag)
    }

}

@Composable
fun ScaleSampleTheme(
    content: @Composable () -> Unit
) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .logger(coilLogger)
            .crossfade(true)
            .build()
    }
    MaterialTheme {
        content()
    }
}