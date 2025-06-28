package com.jvziyaoyao.scale.image.sampling

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @program: SamplingCanvas
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-01 22:02
 **/

/**
 * 渲染的视口对象
 *
 * @property scale 图片相对显示倍率，相对于1倍屏幕显示倍率
 * @property visualRect 在视口内的归一化坐标
 */
data class SamplingCanvasViewPort(
    val scale: Float,
    val visualRect: Rect,
)

/**
 * 从ZoomableView状态直接获取当前视口对象
 *
 * @return
 */
fun ZoomableViewState.getViewPort(): SamplingCanvasViewPort {
    val realWidth = realSize.width
    val realHeight = realSize.height
    val containerCenterX = containerWidth.div(2)
    val containerCenterY = containerHeight.div(2)
    val displayLeft = containerCenterX - realWidth.div(2)
    val displayTop = containerCenterY - realHeight.div(2)
    val left = displayLeft + offsetX.value
    val top = displayTop + offsetY.value
    val right = left + realWidth
    val bottom = top + realHeight
    val realRect = Rect(left, top, right, bottom)
    val containerRect = Rect(0F, 0F, containerWidth, containerHeight)
    val intersectRect = intersectRect(realRect, containerRect)
    val rectInViewPort = Rect(
        left = (intersectRect.left - realRect.left).div(realWidth),
        top = (intersectRect.top - realRect.top).div(realHeight),
        right = (intersectRect.right - realRect.left).div(realWidth),
        bottom = (intersectRect.bottom - realRect.top).div(realHeight),
    )
    return SamplingCanvasViewPort(
        scale = scale.value,
        visualRect = rectInViewPort,
    )
}

internal fun intersectRect(rect1: Rect, rect2: Rect): Rect {
    val left = java.lang.Float.max(rect1.left, rect2.left)
    val top = java.lang.Float.max(rect1.top, rect2.top)
    val right = java.lang.Float.min(rect1.right, rect2.right)
    val bottom = java.lang.Float.min(rect1.bottom, rect2.bottom)

    return if (left < right && top < bottom) {
        Rect(left, top, right, bottom)
    } else {
        Rect(0F, 0F, 0F, 0F)
    }
}

fun calculateInSampleSize(
    srcWidth: Int,
    reqWidth: Int,
): Int {
    var inSampleSize = 1
    while (true) {
        val iss = inSampleSize * 2
        if (srcWidth.toFloat().div(iss) < reqWidth) break
        inSampleSize = iss
    }
    return inSampleSize
}

fun checkRectInBound(
    stX1: Float, stY1: Float, edX1: Float, edY1: Float,
    stX2: Float, stY2: Float, edX2: Float, edY2: Float,
): Boolean {
    if (edY1 < stY2) return false
    if (stY1 > edY2) return false
    if (edX1 < stX2) return false
    if (stX1 > edX2) return false
    return true
}

internal infix fun Rect.same(other: Rect): Boolean {
    return this.left == other.left
            && this.right == other.right
            && this.top == other.top
            && this.bottom == other.bottom
}

/**
 * 用于ImageViewer/ZoomableView进行分块显示大型图片的配套组件
 *
 * @param samplingDecoder 图片加载器
 * @param viewPort 视口
 */
@Composable
fun SamplingCanvas(
    samplingDecoder: SamplingDecoder,
    viewPort: SamplingCanvasViewPort,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val maxWidthPx = density.run { this@BoxWithConstraints.maxWidth.toPx() }
        val maxHeightPx = density.run { this@BoxWithConstraints.maxHeight.toPx() }

        val realWidth = maxWidthPx.times(viewPort.scale)
        val realHeight = maxHeightPx.times(viewPort.scale)

        // 判断是否需要高画质渲染
        val needRenderHeightTexture by remember(maxWidthPx, maxHeightPx) {
            derivedStateOf {
                // 目前策略：原图的面积大于容器面积，就要渲染高画质
                BigDecimal(samplingDecoder.decoderWidth)
                    .multiply(BigDecimal(samplingDecoder.decoderHeight)) > BigDecimal(maxHeightPx.toDouble())
                    .multiply(BigDecimal(maxWidthPx.toDouble()))
            }
        }
        // 标识当前是否开启高画质渲染，如果需要高画质渲染，并且缩放大于1
        val renderHeightTexture by remember(key1 = viewPort.scale) { derivedStateOf { needRenderHeightTexture && viewPort.scale > 1 } }
        // 当前采样率
        val inSampleSize by remember(realWidth) {
            derivedStateOf {
                calculateInSampleSize(
                    srcWidth = samplingDecoder.decoderWidth,
                    reqWidth = realWidth.toInt()
                )
            }
        }
        // 最小图的采样率
        val zeroInSampleSize by remember {
            derivedStateOf {
                calculateInSampleSize(
                    srcWidth = samplingDecoder.decoderWidth,
                    reqWidth = maxWidthPx.toInt(),
                )
            }
        }

        val backgroundInputSample by remember(
            zeroInSampleSize,
            inSampleSize,
            needRenderHeightTexture
        ) {
            derivedStateOf {
                if (needRenderHeightTexture) zeroInSampleSize else inSampleSize
            }
        }
        var bitmap by remember { mutableStateOf(samplingDecoder.thumbnail) }
        LaunchedEffect(backgroundInputSample) {
            scope.launch(Dispatchers.IO) {
                bitmap = samplingDecoder.decodeRegion(
                    backgroundInputSample, android.graphics.Rect(
                        0,
                        0,
                        samplingDecoder.decoderWidth,
                        samplingDecoder.decoderHeight
                    )
                )
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                bitmap?.recycle()
                bitmap = null
            }
        }

        // 更新时间戳，用于通知canvas更新方块
        var renderUpdateTimeStamp by remember { mutableStateOf(0L) }
        // 开启解码队列的循环
        LaunchedEffect(key1 = Unit) {
            samplingDecoder.startRenderQueue {
                // 解码器解码一个，就更新一次时间戳
                renderUpdateTimeStamp = System.currentTimeMillis()
            }
        }
        // 切换到不需要高画质渲染时，需要清除解码队列，清除全部的bitmap
        LaunchedEffect(key1 = renderHeightTexture) {
            if (!renderHeightTexture) {
                samplingDecoder.renderQueue.clear()
                samplingDecoder.clearAllBitmap()
            }
        }

        /**
         * 更新渲染队列
         */
        var calcMaxCountPending by remember { mutableStateOf(false) }
        // 先前的缩放比
        var previousScale by remember { mutableStateOf<Float?>(null) }
        // 先前的偏移量
        var previousVisualRect by remember { mutableStateOf<Rect?>(null) }

        // 记录最长边的最大方块数
        var blockDividerCount by remember { mutableStateOf(1) }
        // 用来标识这个参数是否有改变
        var preBlockDividerCount by remember { mutableStateOf(blockDividerCount) }

        val stX = realWidth.times(viewPort.visualRect.left)
        val stY = realHeight.times(viewPort.visualRect.top)
        val edX = realWidth.times(viewPort.visualRect.right)
        val edY = realHeight.times(viewPort.visualRect.bottom)

        val visualRectWidth = maxWidth.times(viewPort.visualRect.width)
        val visualRectHeight = maxHeight.times(viewPort.visualRect.height)


        // 更新渲染方块的信息
        fun updateRenderList() {
            // 如果此时正在重新计算渲染方块的数目，就退出
            if (calcMaxCountPending) return
            // 更新的时候如果缩放和偏移量没有变化，方块数量也没变，就没有必要计算了
            if (
                previousVisualRect?.same(viewPort.visualRect) == true
                && previousScale == viewPort.scale
                && preBlockDividerCount == blockDividerCount
            ) return
            previousVisualRect = viewPort.visualRect
            previousScale = viewPort.scale
            // 计算当前渲染方块大小
            val renderBlockSize =
                samplingDecoder.blockSize * (realWidth.div(samplingDecoder.decoderWidth))
            var tlx: Int
            var tly: Int
            var startX: Float
            var startY: Float
            var endX: Float
            var endY: Float
            var eh: Int
            var ew: Int
            var needUpdate: Boolean
            var previousInBound: Boolean
            var previousInSampleSize: Int
            var lastX: Int?
            var lastY: Int? = null
            var lastXDelta: Int
            var lastYDelta: Int
            val insertList = ArrayList<RenderBlock>()
            val removeList = ArrayList<RenderBlock>()
            for ((column, list) in samplingDecoder.renderList.withIndex()) {
                startY = column * renderBlockSize
                endY = (column + 1) * renderBlockSize
                tly = startY.toInt()
                eh = (if (endY > realHeight) realHeight - startY else renderBlockSize).toInt()
                // 由于计算的精度问题，需要确保每一个区块都要严丝合缝
                lastY?.let {
                    if (it < tly) {
                        lastYDelta = tly - it
                        tly = it
                        eh += lastYDelta
                    }
                }
                lastY = tly + eh
                lastX = null
                for ((row, block) in list.withIndex()) {
                    startX = row * renderBlockSize
                    tlx = startX.toInt()
                    endX = (row + 1) * renderBlockSize
                    ew = (if (endX > realWidth) realWidth - startX else renderBlockSize).toInt()
                    previousInSampleSize = block.inSampleSize
                    previousInBound = block.inBound
                    // 记录当前区块的采用率
                    block.inSampleSize = inSampleSize
                    // 判断区块是否在可视范围内
                    block.inBound = checkRectInBound(
                        startX, startY, endX, endY,
                        stX, stY, edX, edY
                    )
                    // 由于计算的精度问题，需要确保每一个区块都要严丝合缝
                    lastX?.let {
                        if (it < tlx) {
                            lastXDelta = tlx - it
                            tlx = it
                            ew += lastXDelta
                        }
                    }
                    lastX = tlx + ew
                    // 记录区块的实际偏移量
                    block.renderOffset = IntOffset(tlx, tly)
                    // 记录区块的实际大小
                    block.renderSize = IntSize(
                        width = ew,
                        height = eh,
                    )
                    // 如果参数跟之前的一样，就没有必要更新bitmap
                    needUpdate = previousInBound != block.inBound
                            || previousInSampleSize != block.inSampleSize
                    if (!needUpdate) continue
                    if (!renderHeightTexture) continue
                    // 解码队列操作时是有锁的，会对性能造成影响
                    if (block.inBound) {
                        if (!samplingDecoder.renderQueue.contains(block)) {
                            insertList.add(block)
                        }
                    } else {
                        removeList.add(block)
                        block.release()
                    }
                }
            }
            scope.launch(Dispatchers.IO) {
                synchronized(samplingDecoder.renderQueue) {
                    insertList.forEach {
                        samplingDecoder.renderQueue.putFirst(it)
                    }
                    removeList.forEach {
                        samplingDecoder.renderQueue.remove(it)
                    }
                }
            }
        }

        LaunchedEffect(realWidth, realHeight, viewPort.visualRect) {
            // 可视区域面积
            val rectArea = BigDecimal(visualRectWidth.value.toDouble())
                .multiply(BigDecimal(visualRectHeight.value.toDouble()))
            // 实际大小面积
            val realArea =
                BigDecimal(realWidth.toDouble()).multiply(BigDecimal(realHeight.toDouble()))
            // 被除数不能为0
            if (realArea.toFloat() == 0F) return@LaunchedEffect
            // 计算实际面积的可视率
            val renderAreaPercentage =
                rectArea.divide(realArea, 2, RoundingMode.HALF_EVEN).toFloat()
            // 根据不同可视率，匹配合适的方块数，最大只能到8
            val goBlockDividerCount = when {
                renderAreaPercentage > 0.6F -> 1
                renderAreaPercentage > 0.025F -> 4
                else -> 8
            }
            // 如果没变，就不要修改
            if (goBlockDividerCount == blockDividerCount) return@LaunchedEffect
            preBlockDividerCount = blockDividerCount
            blockDividerCount = goBlockDividerCount
            scope.launch(Dispatchers.IO) {
                // 清空解码队列
                samplingDecoder.renderQueue.clear()
                // 进入修改区间
                calcMaxCountPending = true
                samplingDecoder.setMaxBlockCount(blockDividerCount)
                calcMaxCountPending = false
                // 离开修改区间

                // 更新一下界面
                updateRenderList()
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            val backScale = 1F.div(viewPort.scale)
            withTransform({
                scale(backScale, backScale, pivot = Offset(0.5F, 0.5F))
            }) {
                if (bitmap != null) {
                    drawImage(
                        image = bitmap!!.asImageBitmap(),
                        dstSize = IntSize(realWidth.toInt(), realHeight.toInt()),
                    )
                }
                // 更新渲染队列
                if (renderUpdateTimeStamp >= 0) updateRenderList()
                if (renderHeightTexture && !calcMaxCountPending) {
                    samplingDecoder.forEachBlock { block, _, _ ->
                        block.getBitmap()?.let {
                            drawImage(
                                image = it.asImageBitmap(),
                                dstSize = block.renderSize,
                                dstOffset = block.renderOffset
                            )
                        }
                    }
                }
            }
        }
    }
}