package com.jvziyaoyao.scale.sample.base

import androidx.compose.runtime.Composable

actual fun log(msg: String, tag: String) {
    println("$tag ========> $msg")
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {}