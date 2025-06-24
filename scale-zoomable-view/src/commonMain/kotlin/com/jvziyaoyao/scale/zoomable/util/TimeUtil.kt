package com.jvziyaoyao.scale.zoomable.util

import kotlinx.datetime.Clock

fun getMilliseconds(): Long {
    return Clock.System.now().toEpochMilliseconds()
}