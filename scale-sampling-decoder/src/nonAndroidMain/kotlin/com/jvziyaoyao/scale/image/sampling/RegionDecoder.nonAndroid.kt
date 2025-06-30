package com.jvziyaoyao.scale.image.sampling

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toSkiaRect
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import kotlin.math.ceil
import kotlin.math.roundToInt

actual fun getReginDecoder(model: Any?): RegionDecoder? {
    return if (model is ByteArray) {
        getReginDecoder(model)
    } else {
        throw IllegalDecoderModelException()
    }
}

fun getReginDecoder(byteArray: ByteArray): RegionDecoder {
    return SkiaRegionDecoder(bytes = byteArray)
}

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

    override fun rotate(imageBitmap: ImageBitmap, degree: Float): ImageBitmap {
        val skiaBitmap = imageBitmap.asSkiaBitmap()
        val skiaImage = Image.makeFromBitmap(skiaBitmap)
        val width = skiaImage.width
        val height = skiaImage.height

        val normalizedDegree = (((degree % 360) + 360) % 360).roundToInt()
        val rotation = when (normalizedDegree) {
            in 45..134 -> 90
            in 135..224 -> 180
            in 225..314 -> 270
            else -> 0
        }

        val (outWidth, outHeight) = if (rotation == 90 || rotation == 270) {
            height to width
        } else {
            width to height
        }

        val surface = Surface.makeRasterN32Premul(outWidth, outHeight)
        val canvas = surface.canvas

        // 关键：调整坐标原点后再旋转
        when (rotation) {
            90 -> {
                canvas.translate(outWidth.toFloat(), 0f)
                canvas.rotate(90f)
            }
            180 -> {
                canvas.translate(outWidth.toFloat(), outHeight.toFloat())
                canvas.rotate(180f)
            }
            270 -> {
                canvas.translate(0f, outHeight.toFloat())
                canvas.rotate(270f)
            }
            // 0 -> 不需要变换
        }

        canvas.drawImage(skiaImage, 0f, 0f)

        return surface.makeImageSnapshot().toComposeImageBitmap()
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