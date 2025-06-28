package com.jvziyaoyao.scale.image.sampling

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.ceil

class RotationIllegalException(msg: String = "Illegal rotation angle.") : RuntimeException(msg)

data class RenderBlock(
    var inBound: Boolean = false,
    var inSampleSize: Int = 1,
    var renderOffset: IntOffset = IntOffset.Zero,
    var renderSize: IntSize = IntSize.Zero,
    var sliceRect: Rect = Rect(Offset.Zero, Size.Zero),
    private var bitmap: ImageBitmap? = null,
) {

    fun release() {
//        bitmap?.recycle()
        bitmap = null
    }

    fun getBitmap(): ImageBitmap? {
        return bitmap
    }

    fun setBitmap(bitmap: ImageBitmap) {
        this.bitmap = bitmap
    }

}

fun <E> Channel<E>.clear() {
    while (true) {
        val result = this.tryReceive()
        if (result.isFailure) break
    }
}

/**
 * 用以提供SamplingCanvas显示大型图片，rememberSamplingDecoder，createSamplingDecoder
 *
 * @property decoder 图源BitmapRegionDecoder
 * @property rotation 图片的旋转角度，通过Exif接口获取文件的旋转角度后可以设置rotation确保图像的正确显示
 * @property onRelease 资源缩放事件
 * @constructor
 *
 * @param thumbnails 默认显示的缓存图片，图片未完成加载时可用于显示占位
 */
class SamplingDecoder(
    private val decoder: RegionDecoder,
    private val rotation: Rotation = Rotation.ROTATION_0,
    private val onRelease: () -> Unit = {},
    thumbnails: ImageBitmap? = null,
) : CoroutineScope by MainScope() {

    private val mutex = Mutex()

    enum class Rotation(val radius: Int) {
        ROTATION_0(0),
        ROTATION_90(90),
        ROTATION_180(180),
        ROTATION_270(270),
        ;
    }

    var thumbnail by mutableStateOf<ImageBitmap?>(thumbnails)

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
//    val renderQueue = LinkedBlockingDeque<RenderBlock>()
    val renderQueue = BlockingDeque<RenderBlock>()

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
                        sliceStartX.toFloat(),
                        sliceStartY.toFloat(),
                        sliceEndX.toFloat(),
                        sliceEndY.toFloat(),
                    )
                )
            }
        }
    }

    // 设置最长边最大方块数
    fun setMaxBlockCount(count: Int): Boolean {
        if (maxBlockCount == count) return false
        if (decoder.isRecycled()) return false

        when (rotation) {
            Rotation.ROTATION_0, Rotation.ROTATION_180 -> {
                decoderWidth = decoder.width()
                decoderHeight = decoder.height()
            }

            Rotation.ROTATION_90, Rotation.ROTATION_270 -> {
                decoderWidth = decoder.height()
                decoderHeight = decoder.width()
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
    @OptIn(InternalCoroutinesApi::class)
    suspend fun release() {
//        thumbnail?.recycle()
        thumbnail = null

        mutex.withLock {
            if (!decoder.isRecycled()) {
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

//    fun getRotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
//        val matrix = Matrix()
//        matrix.postRotate(degree)
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
//    }

    fun getRotationSize(rotation: Rotation): IntSize {
        return when (rotation) {
            Rotation.ROTATION_0, Rotation.ROTATION_180 -> {
                IntSize(decoder.width(), decoder.height())
            }

            Rotation.ROTATION_90, Rotation.ROTATION_270 -> {
                IntSize(decoder.height(), decoder.width())
            }
        }
    }

//    fun getRealDecodeRect(rect: Rect): Rect {
//        return if (rotation == Rotation.ROTATION_0) rect else {
//            val size = getRotationSize(rotation)
//            val decoderWidth = size.width
//            val decoderHeight = size.height
//            val newRect = when (rotation) {
//                Rotation.ROTATION_90 -> {
//                    val nextX1 = rect.top
//                    val nextX2 = rect.bottom
//                    val nextY1 = decoderWidth - rect.right
//                    val nextY2 = decoderWidth - rect.left
//                    Rect(nextX1, nextY1, nextX2, nextY2)
//                }
//
//                Rotation.ROTATION_180 -> {
//                    val nextX1 = decoderWidth - rect.right
//                    val nextX2 = decoderWidth - rect.left
//                    val nextY1 = decoderHeight - rect.bottom
//                    val nextY2 = decoderHeight - rect.top
//                    Rect(nextX1, nextY1, nextX2, nextY2)
//                }
//
//                Rotation.ROTATION_270 -> {
//                    val nextX1 = decoderHeight - rect.bottom
//                    val nextX2 = decoderHeight - rect.top
//                    val nextY1 = rect.left
//                    val nextY2 = rect.right
//                    Rect(nextX1, nextY1, nextX2, nextY2)
//                }
//
//                else -> throw RotationIllegalException()
//            }
//            newRect
//        }
//    }

    /**
     * 解码渲染区域
     */
    suspend fun decodeRegion(inSampleSize: Int, rect: Rect): ImageBitmap? {
        return mutex.withLock {
            if (rotation == Rotation.ROTATION_0) {
                decoder.decodeRegion(inSampleSize, rect)
            } else {
                val size = getRotationSize(rotation)
                val decoderWidth = size.width
                val decoderHeight = size.height
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
                val bitmap = decoder.decodeRegion(inSampleSize, newRect)
                if (bitmap == null) bitmap else {
                    decoder.rotate(bitmap, rotation.radius.toFloat())
                }
            }
        }
    }
//    suspend fun decodeRegion(inSampleSize: Int, rect: Rect): ImageBitmap? {
//        return mutex.withLock {
//            val realRect = getRealDecodeRect(rect)
//            if (rect == realRect) {
//                decoder.decodeRegion(inSampleSize, rect)
//            } else {
//                val bitmap = decoder.decodeRegion(inSampleSize, realRect)
//                if (bitmap == null) bitmap else {
//                    decoder.rotate(bitmap, rotation.radius.toFloat())
//                }
//            }
//        }
//    }
//    suspend fun decodeRegion(inSampleSize: Int, rect: Rect): ImageBitmap? {
//        return mutex.withLock {
//            decoder.decodeRegion(inSampleSize, rotation, rect)
//        }
//    }

    // 开启堵塞队列的循环
    fun startRenderQueue(onUpdate: () -> Unit) {
        println("startRenderQueue start")
        launch(Dispatchers.IO) {
            try {
                while (!decoder.isRecycled()) {
                    println("startRenderQueue before take")
                    val block = renderQueue.take()
                    println("startRenderQueue block $block")
                    if (decoder.isRecycled()) break
                    val bitmap = decodeRegion(block.inSampleSize, block.sliceRect)
                    if (bitmap != null) block.setBitmap(bitmap)
                    onUpdate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun createTempBitmap(targetWidth: Int = 720): ImageBitmap? {
        val inputSample = calculateInSampleSize(
            srcWidth = decoderWidth,
            reqWidth = targetWidth,
        )
        return decodeRegion(
            inputSample,
            Rect(
                offset = Offset.Zero,
                size = Size(
                    width = decoderWidth.toFloat(),
                    height = decoderHeight.toFloat(),
                ),
            )
        )
    }
}

