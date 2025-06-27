package com.jvziyaoyao.scale.decoder.kmp

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toSkiaRect
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import kotlin.math.ceil

class SkiaRegionDecoder(
    private val bytes: ByteArray,
) : RegionDecoder {

    private val image: Image by lazy { Image.makeFromEncoded(bytes) }

    override fun width(): Int {
        return image.width
    }

    override fun height(): Int {
        return image.height
    }

    override fun recycle() {
        image.close()
    }

    override fun isRecycled(): Boolean {
        return image.isClosed
    }

    override fun rotate(
        imageBitmap: ImageBitmap,
        degree: Float
    ): ImageBitmap {
        return imageBitmap
    }

    override suspend fun decodeRegion(
        inSampleSize: Int,
        rect: Rect
    ): ImageBitmap? {
        val widthValue = rect.width / inSampleSize.toDouble()
        val heightValue = rect.height / inSampleSize.toDouble()
        val bitmapWidth: Int = ceil(widthValue).toInt()
        val bitmapHeight: Int = ceil(heightValue).toInt()
        val bitmap = Bitmap().apply {
            allocN32Pixels(bitmapWidth, bitmapHeight)
        }
        val canvas = Canvas(bitmap)
        canvas.drawImageRect(
            image = image,
            src = rect.toSkiaRect(),
            dst = org.jetbrains.skia.Rect.makeWH(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        )
        return bitmap.asComposeImageBitmap()
    }

}
