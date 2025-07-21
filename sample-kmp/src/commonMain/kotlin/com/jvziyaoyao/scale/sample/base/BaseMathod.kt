package com.jvziyaoyao.scale.sample.base

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

expect fun log(
    msg: String,
    tag: String = "ScaleDefaultTag",
)