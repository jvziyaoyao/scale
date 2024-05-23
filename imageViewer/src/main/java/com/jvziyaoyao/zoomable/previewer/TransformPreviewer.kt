package com.jvziyaoyao.zoomable.previewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.jvziyaoyao.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import com.jvziyaoyao.zoomable.pager.DEFAULT_ITEM_SPACE
import com.jvziyaoyao.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.zoomable.pager.PagerZoomablePolicyScope
import com.jvziyaoyao.zoomable.pager.SupportedPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

// 比较轻柔的动画窗格
val DEFAULT_SOFT_ANIMATION_SPEC = tween<Float>(400)

/**
 * @program: TransformPreviewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-11 20:21
 **/
open class TransformPreviewerState(
    // 协程作用域
    private val scope: CoroutineScope,
    // 默认动画窗格
    var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 预览状态
    pagerState: SupportedPagerState,
    // 获取当前key
    val getKey: (Int) -> Any,
) : PopupPreviewerState(pagerState) {

    val itemContentVisible = mutableStateOf(false)

    val containerSize = mutableStateOf(Size.Zero)

    val displayWidth = Animatable(0F)

    val displayHeight = Animatable(0F)

    val displayOffsetX = Animatable(0F)

    val displayOffsetY = Animatable(0F)

    // 查找key关联的transformItem
    private fun findTransformItem(key: Any): TransformItemState? {
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

    private suspend fun enterTransformInternal(
        index: Int,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
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
                val displaySize = if (intrinsicSize != null && intrinsicSize!!.isSpecified) {
                    getDisplaySize(intrinsicSize!!, containerSize.value)
                } else {
                    getDisplaySize(containerSize.value, containerSize.value)
                }
//                val displaySize = getDisplaySize(intrinsicSize ?: Size.Zero, containerSize.value)
                val targetX = (containerSize.value.width - displaySize.width).div(2)
                val targetY = (containerSize.value.height - displaySize.height).div(2)
//                val animationSpec = tween<Float>(600)f
                listOf(
                    scope.async {
                        decorationAlpha.animateTo(1F, currentAnimationSpec)
                    },
                    scope.async {
                        displayWidth.animateTo(displaySize.width, currentAnimationSpec)
                    },
                    scope.async {
                        displayHeight.animateTo(displaySize.height, currentAnimationSpec)
                    },
                    scope.async {
                        displayOffsetX.animateTo(targetX, currentAnimationSpec)
                    },
                    scope.async {
                        displayOffsetY.animateTo(targetY, currentAnimationSpec)
                    },
                ).awaitAll()

                previewerAlpha.snapTo(1F)
                // 切换页面到index
                pagerState.scrollToPage(index)
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

    suspend fun enterTransform(
        index: Int,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        enterTransformJob = scope.launch {
            enterTransformInternal(index, animationSpec)
        }
        enterTransformJob?.join()
    }

    internal fun cancelEnterTransform() {
        enterTransformJob?.cancel()
        enterIndex.value = null
    }

    suspend fun exitTransform(animationSpec: AnimationSpec<Float>? = null) {
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
                exitFromCurrentState(itemState, animationSpec)

                stateCloseEnd()
            }
        } else {
            close()
        }
    }

    internal suspend fun exitFromCurrentState(
        itemState: TransformItemState,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        // 动画结束，开启预览
        itemContentVisible.value = true
        // 关闭viewer图层
        previewerAlpha.snapTo(0F)

        itemState.apply {
            listOf(
                scope.async {
                    decorationAlpha.animateTo(0F, currentAnimationSpec)
                },
                scope.async {
                    displayWidth.animateTo(blockSize.width.toFloat(), currentAnimationSpec)
                },
                scope.async {
                    displayHeight.animateTo(blockSize.height.toFloat(), currentAnimationSpec)
                },
                scope.async {
                    displayOffsetX.animateTo(blockPosition.x, currentAnimationSpec)
                },
                scope.async {
                    displayOffsetY.animateTo(blockPosition.y, currentAnimationSpec)
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
fun TransformContentLayer(
    state: TransformPreviewerState,
    debugMode: Boolean = false,
) {
    val density = LocalDensity.current
    state.apply {
        Box(modifier = Modifier.fillMaxSize()) {
            val actionColor = Color.Green
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
                    .run {
                        if (debugMode) border(width = 2.dp, color = actionColor) else this
                    }
            ) {
                val item = findTransformItemByIndex(enterIndex.value ?: currentPage)
                item?.blockCompose?.invoke(item.key)
                if (debugMode) Text(text = "Transform", color = actionColor)
            }
        }
    }
}

@Composable
fun TransformContentForPage(
    page: Int,
    state: TransformPreviewerState,
    debugMode: Boolean = false,
) {
    state.apply {
        val density = LocalDensity.current
        val item = findTransformItemByIndex(page)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            density.apply {
                item?.apply {
                    val containerSize = Size(maxWidth.toPx(), maxHeight.toPx())
                    intrinsicSize?.run {
                        if (isSpecified) Size(width, height) else containerSize
                    }?.let { contentSize ->
                        val displaySize = getDisplaySize(
                            containerSize = containerSize,
                            contentSize = contentSize,
                        )
                        val actionColor = Color.Cyan
                        Box(
                            modifier = Modifier
                                .size(
                                    width = displaySize.width.toDp(),
                                    height = displaySize.height.toDp(),
                                )
                                .run {
                                    if (debugMode) border(
                                        width = 2.dp,
                                        color = actionColor
                                    ) else this
                                }
                                .align(Alignment.Center),
                        ) {
                            blockCompose.invoke(item.key)
                            if (debugMode) Text(text = "TransformForPage", color = actionColor)
                        }
                    }
                }
            }
        }
    }
}

class TransformLayerScope(
    // 图层修饰
    var previewerDecoration: @Composable (innerBox: @Composable () -> Unit) -> Unit =
        @Composable { innerBox -> innerBox() },
    // 背景图层
    var background: @Composable () -> Unit = {},
    // 前景图层
    var foreground: @Composable () -> Unit = {},
)

@Composable
fun TransformPreviewer(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: TransformPreviewerState,
    // 图片间的间隔
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    // 页面外缓存个数
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    // 进入动画
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    // 退出动画
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    // 调试模式
    debugMode: Boolean = false,
    // 检测手势
    detectGesture: PagerGestureScope = PagerGestureScope(),
    // 图层修饰
    previewerLayer: TransformLayerScope = TransformLayerScope(),
    // 缩放图层
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Boolean,
) {
    state.apply {
        Box(modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                containerSize.value = it.toSize()
            }) {
            PopupPreviewer(
                modifier = modifier.fillMaxSize(),
                state = this@apply,
                detectGesture = detectGesture,
                enter = enter,
                exit = exit,
                itemSpacing = itemSpacing,
                beyondViewportPageCount = beyondViewportPageCount,
                zoomablePolicy = { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // TODO 优化闪烁的问题
                        val zoomableMounted = remember { mutableStateOf(false) }
                        if (!zoomableMounted.value) {
                            TransformContentForPage(page = page, state = state, debugMode = debugMode)
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
                TransformContentLayer(state = state, debugMode = debugMode)
            }
        }
    }

}

@Composable
fun TransformItemView(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    transformState: TransformPreviewerState,
    content: @Composable (Any) -> Unit,
) {
    transformState.apply {
        val currentPageKey = try {
            getKey(currentPage)
        } catch (e: Exception) {
            null
        }
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