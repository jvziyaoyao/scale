package com.jvziyaoyao.scale.decoder.kmp

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toSkiaRect
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Surface
import kotlin.math.ceil

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

    override fun rotate(
        imageBitmap: ImageBitmap,
        degree: Float
    ): ImageBitmap {
        // 将 Compose 的 ImageBitmap 转为 Skia Image
        val skiaBitmap = imageBitmap.asSkiaBitmap()
        val skiaImage = Image.makeFromBitmap(skiaBitmap)

        val width = skiaImage.width
        val height = skiaImage.height

        // 创建目标 Surface（用来绘制旋转后的图像）
        val surface = Surface.makeRasterN32Premul(width, height)
        val canvas = surface.canvas

        // 设置旋转矩阵：绕中心点旋转
        val rotationMatrix = Matrix33.makeRotate(
            degree,
            width / 2f,
            height / 2f
        )
        canvas.setMatrix(rotationMatrix)

        // 绘制原图到目标 surface 上（旋转后）
        canvas.drawImage(skiaImage, 0f, 0f)

        // 获取绘制后的图像，并转为 Compose 的 ImageBitmap
        val rotatedImage = surface.makeImageSnapshot()
        return rotatedImage.toComposeImageBitmap()
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