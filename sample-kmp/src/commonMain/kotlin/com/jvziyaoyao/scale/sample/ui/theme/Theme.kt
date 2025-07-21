package com.jvziyaoyao.scale.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
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
fun getSwitchColors(): SwitchColors {
    return SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
}

@Composable
fun getSlideColors(): SliderColors {
    return SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
    )
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

    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = Color(0xFFFFB641),
            background = Color(0xFF060B14),
            surface = Color(0xFF14325A),
            onBackground = Color(0xFFFFFFFF),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF325491),
            background = Color(0xFFE5E5E5),
            surface = Color(0xFFFFFFFF),
            onBackground = Color(0xFF000000),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        val contentColor = colorScheme.onBackground.copy(0.8F)
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(color = contentColor),
            LocalContentColor provides contentColor,
        ) {
            content()
        }
    }
}