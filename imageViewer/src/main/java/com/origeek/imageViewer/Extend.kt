package com.origeek.imageViewer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-12 10:50
 **/

suspend fun <T> SharedFlow<T>.collectOne(scope: CoroutineScope) = suspendCoroutine<T> { c ->
    var job: Job? = null
    fun callback() {
        job?.cancel()
    }
    val flow = this
    job = scope.launch {
        flow.collect {
            c.resume(it)
            callback()
        }
    }
}