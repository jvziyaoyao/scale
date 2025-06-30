package com.jvziyaoyao.scale.image.sampling

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface

actual fun getReginDecoder(bytes: ByteArray?): RegionDecoder? {
    if (bytes == null) return null
    val bitmapRegionDecoder = BitmapRegionDecoder.newInstance(bytes, 0, bytes.size, false)
    return AndroidRegionDecoder(bitmapRegionDecoder)
}

/**
 * 通过Exif接口获取SamplingDecoder的旋转方向
 *
 * @return
 */
fun ExifInterface.getDecoderRotation(): SamplingDecoder.Rotation {
    val orientation = getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> SamplingDecoder.Rotation.ROTATION_90
        ExifInterface.ORIENTATION_ROTATE_180 -> SamplingDecoder.Rotation.ROTATION_180
        ExifInterface.ORIENTATION_ROTATE_270 -> SamplingDecoder.Rotation.ROTATION_270
        else -> SamplingDecoder.Rotation.ROTATION_0
    }
}

fun Rect.toAndroidRect(): android.graphics.Rect {
    return android.graphics.Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}

class AndroidRegionDecoder(
    private val decoder: BitmapRegionDecoder,
) : RegionDecoder {

    override fun width(): Int {
        return decoder.width
    }

    override fun height(): Int {
        return decoder.height
    }

    override fun recycle() {
        decoder.recycle()
    }

    override fun isRecycled(): Boolean {
        return decoder.isRecycled
    }

    override fun rotate(
        imageBitmap: ImageBitmap,
        degree: Float
    ): ImageBitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val bitmap = imageBitmap.asAndroidBitmap()
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
        return result.asImageBitmap()
    }

    override suspend fun decodeRegion(
        inSampleSize: Int,
        rect: Rect
    ): ImageBitmap? {
        val bitmap = try {
            if (decoder.isRecycled) return null
            val ops = BitmapFactory.Options()
            ops.inSampleSize = inSampleSize
            decoder.decodeRegion(rect.toAndroidRect(), ops)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return bitmap?.asImageBitmap()
    }

}