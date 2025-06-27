package com.jvziyaoyao.scale.sample.base

actual fun log(msg: String, tag: String) {
    println("$tag ========> $msg")
}