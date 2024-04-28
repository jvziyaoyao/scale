package com.origeek.imageViewer.previewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.toSize
import com.origeek.imageViewer.gallery.GalleryGestureScope
import com.origeek.imageViewer.gallery.GalleryZoomablePolicyScope
import com.origeek.imageViewer.gallery.ImageGalleryState01
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-11 20:21
 **/
open class ImageTransformPreviewerState01(
    // 协程作用域
    private val scope: CoroutineScope,
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

    val mountedFlow = MutableStateFlow(false)

    val decorationAlpha = Animatable(0F)

    val previewerAlpha = Animatable(0F)

    private suspend fun awaitMounted() {
        mountedFlow.takeWhile { !it }.collect { }
    }

    private suspend fun enterTransformInternal(index: Int) {
        val itemState = findTransformItemByIndex(index)
        if (itemState != null) {
            itemState.apply {
                stateOpenStart()

                mountedFlow.value = false

                enterIndex.value = index
                // 设置动画开始的位置
                displayWidth.snapTo(blockSize.width.toFloat())
                displayHeight.snapTo(blockSize.height.toFloat())
                displayOffsetX.snapTo(blockPosition.x)
                displayOffsetY.snapTo(blockPosition.y)
                itemContentVisible.value = true

                // 关闭修饰图层
                decorationAlpha.snapTo(0F)
                previewerAlpha.snapTo(0F)
                // 开启viewer图层
                animateContainerVisibleState = MutableTransitionState(true)

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

                previewerAlpha.snapTo(1F)
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

    private var enterTransformJob: Job? = null

    suspend fun enterTransform(index: Int) {
        enterTransformJob = scope.launch {
            enterTransformInternal(index)
        }
        enterTransformJob?.join()
    }

    internal fun cancelEnterTransform() {
        enterTransformJob?.cancel()
        enterIndex.value = null
    }

    suspend fun exitTransform() {
        // 取消开启动画
        cancelEnterTransform()
        // 获取当前页码
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

                // 启动关闭
                exitFromCurrentState(itemState)

                stateCloseEnd()
            }
        } else {
            close()
        }
    }

    internal suspend fun exitFromCurrentState(itemState: TransformItemState) {
        // 动画结束，开启预览
        itemContentVisible.value = true
        // 关闭viewer图层
        previewerAlpha.snapTo(0F)

        // 运动到原来位置
        val animationSpec = tween<Float>(600)

        itemState.apply {
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
        }

        // 关闭viewer图层
        animateContainerVisibleState = MutableTransitionState(false)
        // 关闭图层
        itemContentVisible.value = false
    }

    override suspend fun openAction(
        index: Int,
        enterTransition: EnterTransition?,
    ) {
        // 显示修饰图层
        decorationAlpha.snapTo(1F)
        previewerAlpha.snapTo(1F)
        super.openAction(index, enterTransition)
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

@Composable
fun TransformContent01(
    state: ImageTransformPreviewerState01,
) {
    val density = LocalDensity.current
    state.apply {
        Box(
            modifier = Modifier
                .size(
                    width = density.run { displayWidth.value.toDp() },
                    height = density.run { displayHeight.value.toDp() }
                )
                .offset(
                    x = density.run { displayOffsetX.value.toDp() },
                    y = density.run { displayOffsetY.value.toDp() },
                )
                .background(Color.Red.copy(0.2F))
        ) {
            val item = findTransformItemByIndex(enterIndex.value ?: currentPage)
            item?.blockCompose?.invoke(item.key)
            Text(text = "Transform")
        }
    }
}

@Composable
fun TransformContentForPage01(
    page: Int,
    state: ImageTransformPreviewerState01,
) {
    state.apply {
        val density = LocalDensity.current
        val item = findTransformItemByIndex(page)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item?.apply {
                intrinsicSize?.run { Size(width, height) }?.let { contentSize ->
                    density.apply {
                        val displaySize = getDisplaySize(
                            containerSize = Size(
                                maxWidth.toPx(),
                                maxHeight.toPx(),
                            ),
                            contentSize = contentSize,
                        )
                        Box(
                            modifier = Modifier
                                .size(
                                    width = displaySize.width.toDp(),
                                    height = displaySize.height.toDp(),
                                )
                                .background(Color.Blue.copy(0.2F))
                                .align(Alignment.Center),
                        ) {
                            blockCompose.invoke(item.key)
                            Text(text = "TransformForPage")
                        }
                    }
                }
            }
        }
    }
}

class TransformLayerScope01(
    // 图层修饰
    var previewerDecoration: @Composable (innerBox: @Composable () -> Unit) -> Unit =
        @Composable { innerBox -> innerBox() },
    // 背景图层
    var background: @Composable () -> Unit = {},
    // 前景图层
    var foreground: @Composable () -> Unit = {},
)

@Composable
fun ImageTransformPreviewer01(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: ImageTransformPreviewerState01,
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
    state.apply {
        Box(modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                containerSize.value = it.toSize()
            }) {
//            val transformContent = remember {
//                movableContentOf {
//                    TransformContent01(state = state)
//                }
//            }
            ImagePreviewer01(
                modifier = modifier.fillMaxSize(),
                state = this@apply,
                detectGesture = detectGesture,
                enter = enter,
                exit = exit,
                itemSpacing = itemSpacing,
                beyondBoundsItemCount = beyondBoundsItemCount,
                zoomablePolicy = { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val zoomableMounted = remember { mutableStateOf(false) }
                        if (!zoomableMounted.value) {
                            TransformContentForPage01(page = page, state = state)
                        }
                        zoomableMounted.value = zoomablePolicy(page)
                        LaunchedEffect(zoomableMounted.value) {
                            if (enterIndex.value == page && zoomableMounted.value) {
                                mountedFlow.emit(true)
                            }
                        }
                    }
                },
                previewerDecoration = { innerBox ->
                    @Composable
                    fun capsuleLayer(content: @Composable () -> Unit) {
                        Box(
                            modifier = Modifier
                                .alpha(decorationAlpha.value)
                        ) { content() }
                    }
                    previewerLayer.apply {
                        capsuleLayer { background() }
                        previewerDecoration {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(previewerAlpha.value)
                            ) {
                                innerBox()
                            }
                        }
                        capsuleLayer { foreground() }
                    }
                }
            )

            if (itemContentVisible.value && previewerAlpha.value != 1F) {
                TransformContent01(state = state)
            }
        }
    }

}

@Composable
fun TransformItemView01(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    transformState: ImageTransformPreviewerState01,
    content: @Composable (Any) -> Unit,
) {
    transformState.apply {
        val currentPageKey = getKey(currentPage)
        val isCurrentPage = currentPageKey != key
        TransformItemView(
            modifier = modifier,
            key = key,
            itemState = itemState,
            itemVisible = if (!itemContentVisible.value) {
                if (previewerAlpha.value == 1F) {
                    isCurrentPage
                } else true
            } else {
                if (previewerAlpha.value == 1F) {
                    isCurrentPage
                } else {
                    if (enterIndex.value != null) {
                        getKey(enterIndex.value!!) != key
                    } else isCurrentPage
                }
            },
            content = content,
        )
    }
}