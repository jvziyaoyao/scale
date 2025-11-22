package com.jvziyaoyao.scale.zoomable.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getMilliseconds(): Long {
    return Clock.System.now().toEpochMilliseconds()
}