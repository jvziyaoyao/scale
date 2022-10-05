package com.origeek.viewerDemo.util

import android.util.Log

/**
 * @program: ImageGallery
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-09-19 17:07
 **/

const val TAG = "CommonUtil"

suspend fun testTimeSuspend(label: String = "nothing", action: suspend () -> Unit) {
    val t0 = System.currentTimeMillis()
    action()
    val t1 = System.currentTimeMillis()
    Log.i(TAG, "testTimeSuspend -> label is $label: ${t1 - t0}")
}

fun testTime(label: String = "nothing", action: () -> Unit) {
    val t0 = System.currentTimeMillis()
    action()
    val t1 = System.currentTimeMillis()
    Log.i(TAG, "testTime -> label is $label: ${t1 - t0}")
}