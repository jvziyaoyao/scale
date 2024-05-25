package com.jvziyaoyao.viewer.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

val DarkColorPalette = darkColors(
    primary = accent_dark_f,
    secondary = second_f.copy(0.1f),
    background = back_dark_f,
    surface = light_f.copy(0.08f),
    onPrimary = light_f.copy(0.8f),
    onSecondary = font_f.copy(0.8f),
    onBackground = light_f.copy(0.72f),
    onSurface = font_f.copy(0.8f),
    error = error_f,
)

val LightColorPalette = lightColors(
    primary = accent_f,
    secondary = stable_f.copy(0.6f),
    background = stable_f,
    surface = light_f,
    onPrimary = light_f.copy(0.8f),
    onSecondary = font_f.copy(0.8f),
    onBackground = font_f.copy(0.8f),
    onSurface = font_f.copy(0.8f),
    error = error_f,
)

@Composable
fun ViewerDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}