package com.jvziyaoyao.scale.decoder.kmp

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap

interface RegionDecoder {

    fun width(): Int

    fun height(): Int

    fun recycle()

    fun isRecycled(): Boolean

    fun rotate(imageBitmap: ImageBitmap, degree: Float): ImageBitmap

    suspend fun decodeRegion(
        inSampleSize: Int,
        rect: Rect,
    ): ImageBitmap?

}