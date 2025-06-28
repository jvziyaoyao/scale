package com.jvziyaoyao.scale.sample.base

import android.util.Log
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled) {
        onBack.invoke()
    }
}

actual fun log(msg: String, tag: String) {
    Log.i(tag, msg)
}