package com.jvziyaoyao.scale.image.viewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.ceil

class RotationIllegalException(msg: String = "Illegal rotation angle.") : RuntimeException(msg)

data class RenderBlock(
    var inBound: Boolean = false,
    var inSampleSize: Int = 1,
    var renderOffset: IntOffset = IntOffset.Zero,
    var renderSize: IntSize = IntSize.Zero,
    var sliceRect: Rect = Rect(0, 0, 0, 0),
    private var bitmap: Bitmap? = null,
) {

    fun release() {
        bitmap?.recycle()
        bitmap = null
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

}

/**
 * 用以提供ImageCanvas显示大型图片，rememberSamplingDecoder，createSamplingDecoder
 *
 * @property decoder 图源BitmapRegionDecoder
 * @property rotation 图片的旋转角度，通过Exif接口获取文件的旋转角度后可以设置rotation确保图像的正确显示
 * @property onRelease 资源缩放事件
 * @constructor
 *
 * @param thumbnails 默认显示的缓存图片，图片未完成加载时可用于显示占位
 */
class SamplingDecoder(
    private val decoder: BitmapRegionDecoder,
    private val rotation: Rotation = Rotation.ROTATION_0,
    private val onRelease: () -> Unit = {},
    thumbnails: Bitmap? = null,
) : CoroutineScope by MainScope() {

    enum class Rotation(val radius: Int) {
        ROTATION_0(0),
        ROTATION_90(90),
        ROTATION_180(180),
        ROTATION_270(270),
        ;
    }

    var thumbnail by mutableStateOf<Bitmap?>(thumbnails)

    // 解码的宽度
    var decoderWidth by mutableStateOf(0)
        private set

    // 解码的高度
    var decoderHeight by mutableStateOf(0)
        private set

    // 解码大小
    val intrinsicSize: Size
        get() {
            return Size(
                width = decoderWidth.toFloat(),
                height = decoderHeight.toFloat(),
            )
        }

    // 解码区块大小
    var blockSize by mutableStateOf(0)
        private set

    // 渲染列表
    var renderList: Array<Array<RenderBlock>> = emptyArray()
        private set

    // 解码渲染队列
    val renderQueue = LinkedBlockingDeque<RenderBlock>()

    // 横向方块数
    private var countW = 0

    // 纵向方块数
    private var countH = 0

    // 最长边的最大方块数
    private var maxBlockCount = 0

    init {
        // 初始化最大方块数
        setMaxBlockCount(1)
    }

    // 构造一个渲染方块队列
    private fun getRenderBlockList(): Array<Array<RenderBlock>> {
        var endX: Int
        var endY: Int
        var sliceStartX: Int
        var sliceStartY: Int
        var sliceEndX: Int
        var sliceEndY: Int
        return Array(countH) { column ->
            sliceStartY = (column * blockSize)
            endY = (column + 1) * blockSize
            sliceEndY = if (endY > decoderHeight) decoderHeight else endY
            Array(countW) { row ->
                sliceStartX = (row * blockSize)
                endX = (row + 1) * blockSize
                sliceEndX = if (endX > decoderWidth) decoderWidth else endX
                RenderBlock(
                    sliceRect = Rect(
                        sliceStartX,
                        sliceStartY,
                        sliceEndX,
                        sliceEndY,
                    )
                )
            }
        }
    }

    // 设置最长边最大方块数
    fun setMaxBlockCount(count: Int): Boolean {
        if (maxBlockCount == count) return false
        if (decoder.isRecycled) return false

        when (rotation) {
            Rotation.ROTATION_0, Rotation.ROTATION_180 -> {
                decoderWidth = decoder.width
                decoderHeight = decoder.height
            }

            Rotation.ROTATION_90, Rotation.ROTATION_270 -> {
                decoderWidth = decoder.height
                decoderHeight = decoder.width
            }
        }

        maxBlockCount = count
        blockSize =
            (decoderWidth.coerceAtLeast(decoderHeight)).toFloat().div(count).toInt()
        countW = ceil(decoderWidth.toFloat().div(blockSize)).toInt()
        countH = ceil(decoderHeight.toFloat().div(blockSize)).toInt()
        renderList = getRenderBlockList()
        return true
    }

    // 遍历每一个渲染方块
    fun forEachBlock(action: (block: RenderBlock, column: Int, row: Int) -> Unit) {
        for ((column, rows) in renderList.withIndex()) {
            for ((row, block) in rows.withIndex()) {
                action(block, column, row)
            }
        }
    }

    // 清除全部bitmap的引用
    fun clearAllBitmap() {
        forEachBlock { block, _, _ ->
            block.release()
        }
    }

    // 释放资源
    fun release() {
        thumbnail?.recycle()
        thumbnail = null
        synchronized(decoder) {
            if (!decoder.isRecycled) {
                // 清除渲染队列
                renderQueue.clear()
                // 回收资源
                decoder.recycle()
                // 发送一个信号停止堵塞的循环
                renderQueue.putFirst(RenderBlock())
            }
            onRelease()
        }
    }

    fun getRotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    /**
     * 解码渲染区域
     */
    fun decodeRegion(inSampleSize: Int, rect: Rect): Bitmap? {
        synchronized(decoder) {
            return try {
                val ops = BitmapFactory.Options()
                ops.inSampleSize = inSampleSize
                if (decoder.isRecycled) return null
                return if (rotation == Rotation.ROTATION_0) {
                    decoder.decodeRegion(rect, ops)
                } else {
                    val newRect = when (rotation) {
                        Rotation.ROTATION_90 -> {
                            val nextX1 = rect.top
                            val nextX2 = rect.bottom
                            val nextY1 = decoderWidth - rect.right
                            val nextY2 = decoderWidth - rect.left
                            Rect(nextX1, nextY1, nextX2, nextY2)
                        }

                        Rotation.ROTATION_180 -> {
                            val nextX1 = decoderWidth - rect.right
                            val nextX2 = decoderWidth - rect.left
                            val nextY1 = decoderHeight - rect.bottom
                            val nextY2 = decoderHeight - rect.top
                            Rect(nextX1, nextY1, nextX2, nextY2)
                        }

                        Rotation.ROTATION_270 -> {
                            val nextX1 = decoderHeight - rect.bottom
                            val nextX2 = decoderHeight - rect.top
                            val nextY1 = rect.left
                            val nextY2 = rect.right
                            Rect(nextX1, nextY1, nextX2, nextY2)
                        }

                        else -> throw RotationIllegalException()
                    }
                    val srcBitmap = decoder.decodeRegion(newRect, ops)
                    getRotateBitmap(bitmap = srcBitmap, rotation.radius.toFloat())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // 开启堵塞队列的循环
    fun startRenderQueue(onUpdate: () -> Unit) {
        launch(Dispatchers.IO) {
            try {
                while (!decoder.isRecycled) {
                    val block = renderQueue.take()
                    if (decoder.isRecycled) break
                    val bitmap = decodeRegion(block.inSampleSize, block.sliceRect)
                    if (bitmap != null) block.setBitmap(bitmap)
                    onUpdate()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun createTempBitmap(targetWidth: Int = 720): Bitmap? {
        val inputSample = calculateInSampleSize(
            srcWidth = decoderWidth,
            reqWidth = targetWidth,
        )
        return decodeRegion(
            inputSample, Rect(
                0,
                0,
                decoderWidth,
                decoderHeight
            )
        )
    }
}

/**
 * 通过文件创建SamplingDecoder
 *
 * @param file
 * @return
 */
fun createSamplingDecoder(file: File): SamplingDecoder? {
    val inputStream = FileInputStream(file)
    val exifInterface = ExifInterface(file)
    val decoder = createBitmapRegionDecoder(inputStream)
    val rotation = exifInterface.getDecoderRotation()
    return decoder?.let { createSamplingDecoder(it, rotation) }
}

/**
 * 通过流创建BitmapRegionDecoder
 *
 * @param inputStream
 * @return
 */
fun createBitmapRegionDecoder(inputStream: InputStream): BitmapRegionDecoder? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BitmapRegionDecoder.newInstance(inputStream)
    } else {
        BitmapRegionDecoder.newInstance(inputStream, false)
    }
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

/**
 * 创建SamplingDecoder的主要方法
 *
 * @param decoder
 * @param rotation 请参考SamplingDecoder.Rotation
 * @return
 */
fun createSamplingDecoder(
    decoder: BitmapRegionDecoder,
    rotation: SamplingDecoder.Rotation = SamplingDecoder.Rotation.ROTATION_0,
): SamplingDecoder {
    return SamplingDecoder(decoder = decoder, rotation = rotation).apply {
        this.thumbnail = createTempBitmap()
    }
}

/**
 * 创建SamplingDecoder的方法
 *
 * @param file
 * @return SamplingDecoder成功创建时不为空，创建过程中出现异常会返回Exception
 */
@Composable
fun rememberSamplingDecoder(file: File): Pair<SamplingDecoder?, Exception?> {
    val samplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
    val expectation = remember { mutableStateOf<Exception?>(null) }
    LaunchedEffect(file) {
        launch(Dispatchers.IO) {
            try {
                samplingDecoder.value = createSamplingDecoder(file)
            } catch (e: Exception) {
                expectation.value = e
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            samplingDecoder.value?.release()
        }
    }
    return Pair(samplingDecoder.value, expectation.value)
}

/**
 * 创建SamplingDecoder的方法
 *
 * @param inputStream
 * @param rotation 请参考SamplingDecoder.Rotation
 * @return SamplingDecoder成功创建时不为空，创建过程中出现异常会返回Exception
 */
@Composable
fun rememberSamplingDecoder(
    inputStream: InputStream,
    rotation: SamplingDecoder.Rotation = SamplingDecoder.Rotation.ROTATION_0,
): Pair<SamplingDecoder?, Exception?> {
    val samplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
    val expectation = remember { mutableStateOf<Exception?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            try {
                val decoder = createBitmapRegionDecoder(inputStream)
                    ?: throw RuntimeException("Can not create bitmap region decoder!")
                samplingDecoder.value = createSamplingDecoder(decoder, rotation)
            } catch (e: Exception) {
                expectation.value = e
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            samplingDecoder.value?.release()
        }
    }
    return Pair(samplingDecoder.value, expectation.value)
}