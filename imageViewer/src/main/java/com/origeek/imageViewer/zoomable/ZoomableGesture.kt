package com.origeek.imageViewer.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.util.fastForEach
import com.origeek.imageViewer.viewer.panTransformAndScale
import kotlinx.coroutines.CoroutineScope
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

fun ZoomableViewState.onGestureStart() {
    if (allowGestureInput) {
        eventChangeCount = 0
        velocityTracker = VelocityTracker()
        offsetX.updateBounds(null, null)
        offsetY.updateBounds(null, null)
    }
}

fun ZoomableViewState.onGestureEnd(scope: CoroutineScope, transformOnly: Boolean) {
    scope.apply {
        if (!transformOnly || !allowGestureInput) return
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
        launch {
            if (inBound(offsetX.value, boundX) && velocity != null) {
                val vx = sameDirection(lastPan.x, velocity.x)
                offsetX.updateBounds(boundX.first, boundX.second)
                offsetX.animateDecay(vx, decay)
            } else {
                val targetX = if (nextScale != maxScale) {
                    limitToBound(offsetX.value, boundX)
                } else {
                    panTransformAndScale(
                        offset = offsetX.value,
                        center = centroid.x,
                        bh = containerWidth,
                        uh = displayWidth,
                        fromScale = scale.value,
                        toScale = nextScale
                    )
                }
                offsetX.animateTo(targetX)
            }
        }
        launch {
            if (inBound(offsetY.value, boundY) && velocity != null) {
                val vy = sameDirection(lastPan.y, velocity.y)
                offsetY.updateBounds(boundY.first, boundY.second)
                offsetY.animateDecay(vy, decay)
            } else {
                val targetY = if (nextScale != maxScale) {
                    limitToBound(offsetY.value, boundY)
                } else {
                    panTransformAndScale(
                        offset = offsetY.value,
                        center = centroid.y,
                        bh = containerHeight,
                        uh = displayHeight,
                        fromScale = scale.value,
                        toScale = nextScale
                    )
                }
                offsetY.animateTo(targetY)
            }
        }
        launch {
            rotation.animateTo(0F)
        }
        nextScale?.let {
            launch {
                scale.animateTo(nextScale)
            }
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
    if (!allowGestureInput) return false
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

    var nextScale = currentScale.times(checkZoom)
    // 检查最小放大倍率
    if (nextScale < MIN_SCALE) nextScale = MIN_SCALE

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
        )
    boundY =
        getBound(
            boundScale,
            containerHeight,
            displayHeight,
        )

    var nextOffsetX = panTransformAndScale(
        offset = currentOffsetX,
        center = center.x,
        bh = containerWidth,
        uh = displayWidth,
        fromScale = currentScale,
        toScale = nextScale
    ) + pan.x
    var nextOffsetY = panTransformAndScale(
        offset = currentOffsetY,
        center = center.y,
        bh = containerHeight,
        uh = displayHeight,
        fromScale = currentScale,
        toScale = nextScale
    ) + pan.y

    // 如果手指数1，就是拖拽，拖拽受范围限制
    // 如果手指数大于1，即有缩放事件，则支持中心点放大
    if (eventChangeCount == 1) {
        nextOffsetX = limitToBound(nextOffsetX, boundX)
        nextOffsetY = limitToBound(nextOffsetY, boundY)
    }

    val nextRotation = if (nextScale < 1) {
        currentRotation + checkRotate
    } else currentRotation

    // 添加到手势加速度
    velocityTracker.addPosition(
        event.changes[0].uptimeMillis,
        Offset(nextOffsetX, nextOffsetY),
    )

    scope.launch {
        scale.snapTo(nextScale)
        offsetX.snapTo(nextOffsetX)
        offsetY.snapTo(nextOffsetY)
        rotation.snapTo(nextRotation)
    }

    // 这里判断是否已运动到边界，如果到了边界，就不消费事件，让上层界面获取到事件
    val onRight = nextOffsetX <= boundX.first
    val onLeft = nextOffsetX >= boundX.second
    val reachSide = !(onLeft && pan.x > 0)
            && !(onRight && pan.x < 0)
            && !(onLeft && onRight)
    if (reachSide || scale.value < 1) {
        event.changes.fastForEach {
            if (it.positionChanged()) {
                it.consume()
            }
        }
    }

    // 返回true，继续下一次手势
    return true
}