package com.origeek.imageViewer.previewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import com.origeek.imageViewer.gallery.GalleryGestureScope
import com.origeek.imageViewer.gallery.GalleryZoomablePolicyScope
import com.origeek.imageViewer.gallery.ImageGalleryState01
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
 * @create: 2023-12-25 10:29
 **/
class ImageVerticalPreviewerState01(
    // 协程作用域
    private val scope: CoroutineScope,
    // 默认动画窗格
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 开启垂直手势的类型
    verticalDragType: VerticalDragType = VerticalDragType.None,
    // 下拉关闭的缩小的阈值
    scaleToCloseMinValue: Float = DEFAULT_SCALE_TO_CLOSE_MIN_VALUE,
    // 预览状态
    galleryState: ImageGalleryState01,
    // 获取当前key
    getKey: (Int) -> Any,
) : ImageTransformPreviewerState01(
    scope, defaultAnimationSpec, galleryState, getKey
) {

    /**
     * 开启垂直手势的类型
     */
    private var verticalDragType by mutableStateOf(verticalDragType)

    /**
     * 下拉关闭的缩放的阈值，当scale小于这个值，就关闭，否则还原
     */
    private var scaleToCloseMinValue by mutableStateOf(scaleToCloseMinValue)

    /**
     * 下拉关闭容器状态
     */
    val verticalDragTransformState = VerticalContainerState(
        defaultAnimationSpec = defaultAnimationSpec
    )

    suspend fun verticalDrag(pointerInputScope: PointerInputScope) {
        pointerInputScope.apply {
            // 记录开始时的位置
            var startOffset by mutableStateOf<Offset?>(null)
            // 标记是否为下拉关闭
            var orientationDown by mutableStateOf<Boolean?>(null)
            // 如果getKay不为空才开始检测手势
            if (verticalDragType != VerticalDragType.None) detectVerticalDragGestures(
                onDragStart = OnDragStart@{
                    if (zoomableViewState == null) return@OnDragStart
                    // 只有viewer的缩放率为1时才允许下拉手势
                    if (zoomableViewState!!.scale.value == 1F) {
                        startOffset = it
                        // 进入下拉手势时禁用viewer的手势
                        zoomableViewState!!.allowGestureInput = false
                    }
                },
                onDragEnd = OnDragEnd@{
                    // 如果开始位置为空，就退出
                    if (startOffset == null) return@OnDragEnd
                    // 重置开始位置和方向
                    startOffset = null
                    orientationDown = null
                    // 解除viewer的手势输入限制
                    zoomableViewState?.allowGestureInput = true
                    // 缩放小于阈值，执行关闭动画，大于就恢复原样
                    if (verticalDragTransformState.scale.value < scaleToCloseMinValue) {
                        scope.launch {
                            val itemState = findTransformItemByIndex(currentPage)
                            if (itemState != null) {
                                dragDownClose(itemState)
                            } else {
                                viewerContainerShrinkDown()
                            }
                        }
                    } else {
                        scope.launch {
                            decorationAlpha.snapTo(1F)
                        }
                        scope.launch {
                            verticalDragTransformState.reset()
                        }
                    }
                },
                onVerticalDrag = OnVerticalDrag@{ change, dragAmount ->
                    if (zoomableViewState == null) return@OnVerticalDrag
                    if (startOffset == null) return@OnVerticalDrag
                    if (orientationDown == null) orientationDown = dragAmount > 0
                    if (orientationDown == true || verticalDragType == VerticalDragType.UpAndDown) {
                        val offsetY = change.position.y - startOffset!!.y
                        val offsetX = change.position.x - startOffset!!.x
                        val containerHeight = containerSize.value.height
                        val scale = (containerHeight - offsetY.absoluteValue).div(
                            containerHeight
                        )
                        scope.launch {
                            decorationAlpha.snapTo(scale)
                            verticalDragTransformState.offsetX.snapTo(offsetX)
                            verticalDragTransformState.offsetY.snapTo(offsetY)
                            verticalDragTransformState.scale.snapTo(scale)
                        }
                    } else {
                        // 如果不是向上，就返还输入权，以免页面卡顿
                        zoomableViewState?.allowGestureInput = true
                    }
                }
            )
        }
    }

    /**
     * 响应下拉关闭
     */
    private suspend fun dragDownClose(itemState: TransformItemState) {
        // 标记动作开始
        stateCloseStart()

        verticalDragTransformState.apply {
            val displaySize =
                getDisplaySize(itemState.intrinsicSize ?: Size.Zero, containerSize.value)

            val centerX = containerSize.value.width.div(2)
            val centerY = containerSize.value.height.div(2)

            val nextSize = displaySize.times(scale.value)
            val nextTargetX = centerX + offsetX.value - nextSize.width.div(2)
            val nextTargetY = centerY + offsetY.value - nextSize.height.div(2)

            displayWidth.snapTo(nextSize.width)
            displayHeight.snapTo(nextSize.height)
            displayOffsetX.snapTo(nextTargetX)
            displayOffsetY.snapTo(nextTargetY)
        }

        // 启动关闭
        exitFromCurrentState(itemState)

        // 恢复原来的状态
        verticalDragTransformState.resetImmediately()

        // 标记动作结束
        stateCloseEnd()
    }

    /**
     * viewer容器缩小关闭
     */
    private suspend fun viewerContainerShrinkDown() {
        // 标记动作开始
        stateCloseStart()

        // TODO: defaultAnimationSpec
        val animationSpec = tween<Float>(320)

        coroutineScope {
            listOf(
                // 缩小容器
                async {
                    verticalDragTransformState.scale.animateTo(0F, animationSpec = animationSpec)
                },
                // 关闭UI
                async {
                    decorationAlpha.animateTo(0F, animationSpec = animationSpec)
                }
            ).awaitAll()

            // 关闭动画组件
            animateContainerVisibleState = MutableTransitionState(false)

            // 恢复原来的状态
            verticalDragTransformState.resetImmediately()

            // 标记动作结束
            stateCloseEnd()
        }

    }
}

class VerticalContainerState(
    // 默认动画窗格
    var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC
) {
    // 容器的偏移量X
    var offsetX = Animatable(0F)

    // 容器的偏移量Y
    var offsetY = Animatable(0F)

    // 容器缩放
    var scale = Animatable(1F)

    /**
     * 重置回原来的状态
     * @param animationSpec AnimationSpec<Float>
     */
    suspend fun reset(animationSpec: AnimationSpec<Float> = defaultAnimationSpec) {
        coroutineScope {
            listOf(
                async {
                    offsetX.animateTo(0F, animationSpec)
                },
                async {
                    offsetY.animateTo(0F, animationSpec)
                },
                async {
                    scale.animateTo(1F, animationSpec)
                },
            ).awaitAll()
        }
    }

    /**
     * 立刻重置
     */
    suspend fun resetImmediately() {
        offsetX.snapTo(0F)
        offsetY.snapTo(0F)
        scale.snapTo(1F)
    }
}

@Composable
fun ImageVerticalPreviewer(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: ImageVerticalPreviewerState01,
    // 图片间的间隔
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    // 页面外缓存个数
    beyondBoundsItemCount: Int = DEFAULT_BEYOND_BOUNDS_ITEM_COUNT,
    // 进入动画
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    // 退出动画
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    // 检测手势
    detectGesture: GalleryGestureScope = GalleryGestureScope(),
    // 图层修饰
    previewerLayer: TransformLayerScope01 = TransformLayerScope01(),
    // 缩放图层
    zoomablePolicy: @Composable GalleryZoomablePolicyScope.(page: Int) -> Boolean,
) {
    ImageTransformPreviewer01(
        modifier = modifier,
        state = state,
        itemSpacing = itemSpacing,
        beyondBoundsItemCount = beyondBoundsItemCount,
        enter = enter,
        exit = exit,
        detectGesture = detectGesture,
        previewerLayer = TransformLayerScope01(
            previewerDecoration = { innerBox ->
                state.apply {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(getKey) {
                                verticalDrag(this)
                            }
                            .graphicsLayer {
                                translationX = verticalDragTransformState.offsetX.value
                                translationY = verticalDragTransformState.offsetY.value
                                scaleX = verticalDragTransformState.scale.value
                                scaleY = verticalDragTransformState.scale.value
                            }
                    ) {
                        previewerLayer.previewerDecoration {
                            innerBox()
                        }
                    }
                }
            },
            background = previewerLayer.background,
            foreground = previewerLayer.foreground,
        ),
        zoomablePolicy = zoomablePolicy,
    )
}