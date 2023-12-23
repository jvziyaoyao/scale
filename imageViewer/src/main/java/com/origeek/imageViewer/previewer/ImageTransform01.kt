package com.origeek.imageViewer.previewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import com.origeek.imageViewer.gallery.ImageGalleryState01
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-11 20:21
 **/
class ImageTransformPreviewerState01(
    // 协程作用域
    private val scope: CoroutineScope = MainScope(),
    // 默认动画窗格
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 预览状态
    galleryState: ImageGalleryState01,
    // 获取当前key
    val getKey: (Int) -> Any,
) : ImagePreviewerState01(scope, defaultAnimationSpec, galleryState) {

    val itemContentVisible = mutableStateOf(false)

    val containerSize = mutableStateOf(Size.Zero)

    val displayWidth = Animatable(0F)

    val displayHeight = Animatable(0F)

    val displayOffsetX = Animatable(0F)

    val displayOffsetY = Animatable(0F)

    // 查找key关联的transformItem
    fun findTransformItem(key: Any): TransformItemState? {
        return transformItemStateMap[key]
    }

    // 根据index查询key
    fun findTransformItemByIndex(index: Int): TransformItemState? {
        val key = getKey(index)
        return findTransformItem(key)
    }

    val enterIndex = mutableStateOf<Int?>(null)

    val mounted = MutableStateFlow(false)

    val decorationAlpha = Animatable(0F)

    suspend fun awaitMounted() = suspendCoroutine<Unit> { c ->
        var notConsumed = true
        scope.launch {
            mounted
                .takeWhile { notConsumed }
                .collectLatest {
                    if (it) {
                        notConsumed = false
                        c.resume(Unit)
                    }
                }
        }
    }

    suspend fun enterTransform(index: Int) {
        val itemState = findTransformItemByIndex(index)
        if (itemState != null) {
            itemState.apply {
                stateOpenStart()

                mounted.value = false

                enterIndex.value = index
                // 设置动画开始的位置
                displayWidth.snapTo(blockSize.width.toFloat())
                displayHeight.snapTo(blockSize.height.toFloat())
                displayOffsetX.snapTo(blockPosition.x)
                displayOffsetY.snapTo(blockPosition.y)
                itemContentVisible.value = true

                // TODO: intrinsicSize为空的情况
                val displaySize = getDisplaySize(intrinsicSize ?: Size.Zero, containerSize.value)
                val targetX = (containerSize.value.width - displaySize.width).div(2)
                val targetY = (containerSize.value.height - displaySize.height).div(2)
                val animationSpec = tween<Float>(600)
                listOf(
                    scope.async {
                        decorationAlpha.animateTo(1F, animationSpec)
                    },
                    scope.async {
                        displayWidth.animateTo(displaySize.width, animationSpec)
                    },
                    scope.async {
                        displayHeight.animateTo(displaySize.height, animationSpec)
                    },
                    scope.async {
                        displayOffsetX.animateTo(targetX, animationSpec)
                    },
                    scope.async {
                        displayOffsetY.animateTo(targetY, animationSpec)
                    },
                ).awaitAll()

                // 开启viewer图层
                animateContainerVisibleState = MutableTransitionState(true)
                // 切换页面到index
                galleryState.scrollToPage(index)
                // 等待挂载成功
                awaitMounted()
                // 动画结束，开启预览
                itemContentVisible.value = false
                // 恢复
                enterIndex.value = null

                stateOpenEnd()
            }
        } else {
            open(index)
        }
    }

    suspend fun exitTransform() {
        val index = currentPage
        // 同步动画开始的位置
        val itemState = findTransformItemByIndex(index)
        if (itemState != null) {
            itemState.apply {
                stateCloseStart()
                // TODO: 要判断intrinsicSize为null的情况
                val displaySize = getDisplaySize(intrinsicSize ?: Size.Zero, containerSize.value)
                val targetX = (containerSize.value.width - displaySize.width).div(2)
                val targetY = (containerSize.value.height - displaySize.height).div(2)
                displayWidth.snapTo(displaySize.width)
                displayHeight.snapTo(displaySize.height)
                displayOffsetX.snapTo(targetX)
                displayOffsetY.snapTo(targetY)

                // 动画结束，开启预览
                itemContentVisible.value = true
                // 关闭viewer图层
                animateContainerVisibleState = MutableTransitionState(false)

                // 运动到原来位置
                val animationSpec = tween<Float>(600)
                listOf(
                    scope.async {
                        decorationAlpha.animateTo(0F, animationSpec)
                    },
                    scope.async {
                        displayWidth.animateTo(blockSize.width.toFloat(), animationSpec)
                    },
                    scope.async {
                        displayHeight.animateTo(blockSize.height.toFloat(), animationSpec)
                    },
                    scope.async {
                        displayOffsetX.animateTo(blockPosition.x, animationSpec)
                    },
                    scope.async {
                        displayOffsetY.animateTo(blockPosition.y, animationSpec)
                    },
                ).awaitAll()
                // 关闭图层
                itemContentVisible.value = false

                stateCloseEnd()
            }
        } else {
            close()
        }
    }
}

fun getDisplaySize(contentSize: Size, containerSize: Size): Size {
    val containerRatio = containerSize.run {
        width.div(height)
    }
    val contentRatio = contentSize.run {
        width.div(height)
    }
    val widthFixed = contentRatio > containerRatio
    val scale1x = if (widthFixed) {
        containerSize.width.div(contentSize.width)
    } else {
        containerSize.height.div(contentSize.height)
    }
    return Size(
        width = contentSize.width.times(scale1x),
        height = contentSize.height.times(scale1x),
    )
}
