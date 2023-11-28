package com.origeek.imageViewer.zoomable

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-11-24 16:58
 **/

fun ZoomableViewState.onGestureStart(scope: CoroutineScope) {
    if (allowGestureInput) {
        eventChangeCount = 0
        velocityTracker = VelocityTracker()
        scope.launch {
            offsetX.stop()
            offsetY.stop()
            offsetX.updateBounds(null, null)
            offsetY.updateBounds(null, null)
        }
    }
}

fun ZoomableViewState.onGestureEnd(scope: CoroutineScope, transformOnly: Boolean) {
    if (!transformOnly || isRunning() || !allowGestureInput) return
    var velocity = try {
        velocityTracker.calculateVelocity()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    // 如果缩放比小于1，要自动回到1
    // 如果缩放比大于最大显示缩放比，就设置回去，并且避免加速度
    val nextScale = when {
        scale.value < 1 -> 1F
        scale.value > maxScale -> {
            velocity = null
            maxScale
        }

        else -> null
    }
    scope.launch {
        if (inBound(offsetX.value, boundX) && velocity != null) {
            val vx = sameDirection(lastPan.x, velocity.x)
            offsetX.updateBounds(boundX.first, boundX.second)
            offsetX.animateDecay(vx, decay)
        } else {
            val targetX = if (nextScale != maxScale) {
                limitToBound(offsetX.value, boundX)
            } else {
                panTransformAndScale(
                    center = centroid.x,
                    currentOffset = offsetX.value,
                    displayLength = displayWidth,
                    displayOffset = displayOffsetInContainerX,
                    fromScale = scale.value,
                    toScale = nextScale,
                )
            }
            offsetX.animateTo(targetX)
        }
    }
    scope.launch {
        if (inBound(offsetY.value, boundY) && velocity != null) {
            val vy = sameDirection(lastPan.y, velocity.y)
            offsetY.updateBounds(boundY.first, boundY.second)
            offsetY.animateDecay(vy, decay)
        } else {
            val targetY = if (nextScale != maxScale) {
                limitToBound(offsetY.value, boundY)
            } else {
                panTransformAndScale(
                    center = centroid.y,
                    currentOffset = offsetY.value,
                    displayLength = displayHeight,
                    displayOffset = displayOffsetInContainerY,
                    fromScale = scale.value,
                    toScale = nextScale,
                )
            }
            offsetY.animateTo(targetY)
        }
    }
    scope.launch {
        rotation.animateTo(0F)
    }
    nextScale?.let {
        scope.launch {
            scale.animateTo(nextScale)
        }
    }
}

fun ZoomableViewState.onGesture(
    scope: CoroutineScope,
    center: Offset,
    pan: Offset,
    zoom: Float,
    rotate: Float,
    event: PointerEvent
): Boolean {
    if (allowGestureInput) return false
    // 这里只记录最大手指数
    if (eventChangeCount <= event.changes.size) {
        eventChangeCount = event.changes.size
    } else {
        // 如果手指数从多个变成一个，就结束本次手势操作
        return false
    }

    var checkRotate = rotate
    var checkZoom = zoom
    // 如果是双指的情况下，手指距离小于一定值时，缩放和旋转的值会很离谱，所以在这种极端情况下就不要处理缩放和旋转了
    if (event.changes.size == 2) {
        val fingerDistanceOffset =
            event.changes[0].position - event.changes[1].position
        if (
            fingerDistanceOffset.x.absoluteValue < MIN_GESTURE_FINGER_DISTANCE
            && fingerDistanceOffset.y.absoluteValue < MIN_GESTURE_FINGER_DISTANCE
        ) {
            checkRotate = 0F
            checkZoom = 1F
        }
    }

    gestureCenter.value = center

    val currentOffsetX = offsetX.value
    val currentOffsetY = offsetY.value
    val currentScale = scale.value
    val currentRotation = rotation.value
    val currentWidth = currentScale.times(displayWidth)
    val currentHeight = currentScale.times(displayHeight)

    // 中心点在1倍图片上的落点
    val offsetOn1ScaleX = center.x - displayOffsetInContainerX
    val offsetOn1ScaleY = center.y - displayOffsetInContainerY

    // 中心点在实际图片上的落点
    val offsetOnRealX = offsetOn1ScaleX - currentOffsetX
    val offsetOnRealY = offsetOn1ScaleY - currentOffsetY

    var nextScale = currentScale.times(checkZoom)
    // 检查最小放大倍率
    if (nextScale < MIN_SCALE) nextScale = MIN_SCALE

    val nextWidth = nextScale.times(displayWidth)
    val nextHeight = nextScale.times(displayHeight)

    // 接下来中心点在图片上的落点
    val nextOffsetOnRealX = nextWidth.times(offsetOnRealX.div(currentWidth))
    val nextOffsetOnRealY =
        nextHeight.times(offsetOnRealY.div(currentHeight))

    // 接下来图片的偏移量
    val nextOffsetZoomX = offsetOn1ScaleX - nextOffsetOnRealX
    val nextOffsetZoomY = offsetOn1ScaleY - nextOffsetOnRealY

    var nextOffsetX = nextOffsetZoomX + pan.x
    var nextOffsetY = nextOffsetZoomY + pan.y

    val nextRotation = if (nextScale < 1) {
        currentRotation + checkRotate
    } else currentRotation

    // 如果手指数1，就是拖拽，拖拽受范围限制
    // 如果手指数大于1，即有缩放事件，则支持中心点放大
    if (eventChangeCount == 1) {
        nextOffsetX = limitToBound(nextOffsetX, boundX)
        nextOffsetY = limitToBound(nextOffsetY, boundY)
    }

    // 添加到手势加速度
    velocityTracker.addPosition(
        event.changes[0].uptimeMillis,
        Offset(nextOffsetX, nextOffsetY),
    )
    // 最后一次的偏移量
    lastPan = pan
    // 记录手势的中点
    centroid = center

    // 计算边界，如果目标缩放值超过最大显示缩放值，边界就要用最大缩放值来计算，否则手势结束时会导致无法归位
    boundScale =
        if (nextScale > maxScale) maxScale else nextScale
    boundX =
        getBound(
            boundScale,
            containerWidth,
            displayWidth,
            displayOffsetInContainerX
        )
    boundY =
        getBound(
            boundScale,
            containerHeight,
            displayHeight,
            displayOffsetInContainerY
        )

    if (!isRunning()) scope.launch {
        scale.snapTo(nextScale)
        offsetX.snapTo(nextOffsetX)
        offsetY.snapTo(nextOffsetY)
        rotation.snapTo(nextRotation)
    }
    return true
}

suspend fun ZoomableViewState.scaleTo(
    offset: Offset,
    nextScale: Float,
    animationSpec: AnimationSpec<Float>? = null
) {
    val currentAnimateSpec = animationSpec ?: defaultAnimateSpec

    val centerBoundX = Pair(
        displayOffsetInContainerX,
        displayOffsetInContainerX + displayWidth,
    )
    val centerBoundY = Pair(
        displayOffsetInContainerY,
        displayOffsetInContainerY + displayHeight,
    )

    // 限制落点在图片显示范围内
    val limitX = limitToBound(offset.x, centerBoundX)
    val limitY = limitToBound(offset.y, centerBoundY)

    // 求出图片内的落点
    val displayX = limitX - displayOffsetInContainerX
    val displayY = limitY - displayOffsetInContainerY

    // 图片内落点的比例
    val displayXRatio = displayX.div(displayWidth)
    val displayYRatio = displayY.div(displayHeight)

    // 图片最大尺寸
    val nextWidth = displayWidth.times(nextScale)
    val nextHeight = displayHeight.times(nextScale)

    // 放大后的落点
    val nextX = nextWidth.times(displayXRatio)
    val nextY = nextHeight.times(displayYRatio)

    // 放大后落点在容器中的位置
    val nextInDisplayX = nextX + displayOffsetInContainerX
    val nextInDisplayY = nextY + displayOffsetInContainerY

    // 放大后的容器中心点位置
    val containerCenterX = containerWidth.div(2)
    val containerCenterY = containerHeight.div(2)

    // 求出偏移位置
    var nextOffsetX = containerCenterX - nextInDisplayX
    var nextOffsetY = containerCenterY - nextInDisplayY

    // 求出限制范围
    val boundX =
        getBound(nextScale, containerWidth, displayWidth, displayOffsetInContainerX)
    val boundY =
        getBound(nextScale, containerHeight, displayHeight, displayOffsetInContainerY)

    nextOffsetX = limitToBound(nextOffsetX, boundX)
    nextOffsetY = limitToBound(nextOffsetY, boundY)

    // 启动
    coroutineScope {
        listOf(
            async {
                offsetX.animateTo(nextOffsetX, currentAnimateSpec)
                offsetX.updateBounds(boundX.first, boundX.second)
            },
            async {
                offsetY.animateTo(nextOffsetY, currentAnimateSpec)
                offsetY.updateBounds(boundY.first, boundY.second)
            },
            async {
                scale.animateTo(nextScale, currentAnimateSpec)
            },
        ).awaitAll()
    }
}