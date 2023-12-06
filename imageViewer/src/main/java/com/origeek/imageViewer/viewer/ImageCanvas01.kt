package com.origeek.imageViewer.viewer

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-01 22:02
 **/

data class ImageCanvas01ViewPort(
    val size: Size,
    val scale: Float,
    val rectInViewPort: Rect,
)

infix fun Rect.same(other: Rect): Boolean {
    return this.left == other.left
            && this.right == other.right
            && this.top == other.top
            && this.bottom == other.bottom
}

@Composable
fun ImageCanvas01(
    imageDecoder: ImageDecoder,
    viewPort: ImageCanvas01ViewPort,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan.copy(0.4F))
    ) {
        val maxWidthPx = density.run { maxWidth.toPx() }
        val maxHeightPx = density.run { maxHeight.toPx() }

        val realWidth = maxWidthPx.times(viewPort.scale)
        val realHeight = maxHeightPx.times(viewPort.scale)

        // 判断是否需要高画质渲染
        val needRenderHeightTexture by remember(maxWidthPx, maxHeightPx) {
            derivedStateOf {
                // 目前策略：原图的面积大于容器面积，就要渲染高画质
                BigDecimal(imageDecoder.decoderWidth)
                    .multiply(BigDecimal(imageDecoder.decoderHeight)) > BigDecimal(maxHeightPx.toDouble())
                    .multiply(BigDecimal(maxWidthPx.toDouble()))
            }
        }
        // 标识当前是否开启高画质渲染，如果需要高画质渲染，并且缩放大于1
        val renderHeightTexture by remember(key1 = viewPort.scale) { derivedStateOf { needRenderHeightTexture && viewPort.scale > 1 } }
        // 当前采样率
        val inSampleSize by remember(realWidth) {
            derivedStateOf {
                calculateInSampleSize(
                    srcWidth = imageDecoder.decoderWidth,
                    reqWidth = realWidth.toInt()
                )
            }
        }
        // 最小图的采样率
        val zeroInSampleSize = 8
//        val zeroInSampleSize by remember {
//            derivedStateOf {
//                calculateInSampleSize(
//                    srcWidth = imageDecoder.decoderWidth,
//                    reqWidth = maxWidthPx.toInt(),
//                )
//            }
//        }
        // 底图的采样率
        var backGroundInSample by remember { mutableStateOf(0) }
        // 底图bitmap
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }

        // 根据采样率变化，实时更新底图
        LaunchedEffect(
            key1 = zeroInSampleSize,
            key2 = inSampleSize,
            key3 = needRenderHeightTexture
        ) {
            scope.launch(Dispatchers.IO) {
                // 如果不需要渲染高画质，就不需要分块渲染，直接使用当前采样率，用底图来展示
                val iss = if (needRenderHeightTexture) zeroInSampleSize else inSampleSize
                if (iss == backGroundInSample) return@launch
                backGroundInSample = iss
                Log.i("TAG", "ImageCanvas01: backGroundInSample ${backGroundInSample}")
                bitmap = imageDecoder.decodeRegion(
                    iss, android.graphics.Rect(
                        0,
                        0,
                        imageDecoder.decoderWidth,
                        imageDecoder.decoderHeight
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
            imageDecoder.startRenderQueue {
                // 解码器解码一个，就更新一次时间戳
                renderUpdateTimeStamp = System.currentTimeMillis()
            }
        }
        // 切换到不需要高画质渲染时，需要清除解码队列，清除全部的bitmap
        LaunchedEffect(key1 = renderHeightTexture) {
            if (!renderHeightTexture) {
                imageDecoder.renderQueue.clear()
                imageDecoder.clearAllBitmap()
            }
        }

        /**
         * 更新渲染队列
         */
        var calcMaxCountPending by remember { mutableStateOf(false) }
        // 先前的缩放比
        var previousScale by remember { mutableStateOf<Float?>(null) }
        // 先前的偏移量
        var previousRectInViewPort by remember { mutableStateOf<Rect?>(null) }

        // 记录最长边的最大方块数
        var blockDividerCount by remember { mutableStateOf(1) }
        // 用来标识这个参数是否有改变
        var preBlockDividerCount by remember { mutableStateOf(blockDividerCount) }

        val stX = realWidth.times(viewPort.rectInViewPort.left)
        val stY = realHeight.times(viewPort.rectInViewPort.top)
        val edX = realWidth.times(viewPort.rectInViewPort.right)
        val edY = realHeight.times(viewPort.rectInViewPort.bottom)

        val rectInViewPortOffsetX = maxWidth.times(viewPort.rectInViewPort.left)
        val rectInViewPortOffsetY = maxHeight.times(viewPort.rectInViewPort.top)
        val rectInViewPortWidth = maxWidth.times(viewPort.rectInViewPort.width)
        val rectInViewPortHeight = maxHeight.times(viewPort.rectInViewPort.height)

        // 更新渲染方块的信息
        fun updateRenderList() {
            // 如果此时正在重新计算渲染方块的数目，就退出
            if (calcMaxCountPending) return
            // 更新的时候如果缩放和偏移量没有变化，方块数量也没变，就没有必要计算了
            if (
                previousRectInViewPort?.same(viewPort.rectInViewPort) == true
                && previousScale == viewPort.scale
                && preBlockDividerCount == blockDividerCount
            ) return
            previousRectInViewPort = viewPort.rectInViewPort
            previousScale = viewPort.scale
            // 计算当前渲染方块大小
            val renderBlockSize =
                imageDecoder.blockSize * (realWidth.div(imageDecoder.decoderWidth))
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
            for ((column, list) in imageDecoder.renderList.withIndex()) {
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
                        if (!imageDecoder.renderQueue.contains(block)) {
                            insertList.add(block)
                        }
                    } else {
                        removeList.add(block)
                        block.release()
                    }
                }
            }
            scope.launch(Dispatchers.IO) {
                synchronized(imageDecoder.renderQueue) {
                    insertList.forEach {
                        imageDecoder.renderQueue.putFirst(it)
                    }
                    removeList.forEach {
                        imageDecoder.renderQueue.remove(it)
                    }
                }
            }
        }

        LaunchedEffect(realWidth, realHeight, viewPort.rectInViewPort) {
            // 可视区域面积
            val rectArea = BigDecimal(rectInViewPortWidth.value.toDouble())
                .multiply(BigDecimal(rectInViewPortHeight.value.toDouble()))
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
                imageDecoder.renderQueue.clear()
                // 进入修改区间
                calcMaxCountPending = true
                imageDecoder.setMaxBlockCount(blockDividerCount)
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
                    imageDecoder.forEachBlock { block, _, _ ->
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

        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "needRenderHeightTexture -> $needRenderHeightTexture")
            Text(text = "renderHeightTexture -> $renderHeightTexture")
            Text(text = "inSampleSize -> $inSampleSize")
            Text(text = "zeroInSampleSize -> $zeroInSampleSize")
            Text(text = "realWidth,realHeight -> $realWidth,$realHeight")
        }

        // 视口与图片的交集区域
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Cyan.copy(0.4F))
//        )

        // 视口与图片的交集区域
        Box(
            modifier = Modifier
                .size(rectInViewPortWidth, rectInViewPortHeight)
                .offset(rectInViewPortOffsetX, rectInViewPortOffsetY)
                .background(Color.Black.copy(0.2F))
        )
    }
}