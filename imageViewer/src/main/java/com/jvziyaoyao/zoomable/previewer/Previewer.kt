package com.jvziyaoyao.zoomable.previewer

import androidx.annotation.IntRange
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.jvziyaoyao.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import com.jvziyaoyao.zoomable.pager.DEFAULT_ITEM_SPACE
import com.jvziyaoyao.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.zoomable.pager.PagerZoomablePolicyScope
import com.jvziyaoyao.zoomable.pager.SupportedPagerState
import com.jvziyaoyao.zoomable.pager.rememberSupportedPagerState
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberPreviewerState(
    scope: CoroutineScope = rememberCoroutineScope(),
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    @IntRange(from = 0) initialPage: Int = 0,
    verticalDragType: VerticalDragType = VerticalDragType.Down,
    pageCount: () -> Int,
    getKey: (Int) -> Any,
): PreviewerState {
    val pagerState = rememberSupportedPagerState(initialPage = initialPage, pageCount = pageCount)
    val previewerState = remember {
        PreviewerState(
            scope = scope,
            verticalDragType = verticalDragType,
            pagerState = pagerState,
            getKey = getKey,
        )
    }
    previewerState.defaultAnimationSpec = defaultAnimationSpec
    return previewerState
}

class PreviewerState(
    scope: CoroutineScope,
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    verticalDragType: VerticalDragType = VerticalDragType.None,
    scaleToCloseMinValue: Float = DEFAULT_SCALE_TO_CLOSE_MIN_VALUE,
    pagerState: SupportedPagerState,
    getKey: (Int) -> Any,
) : DraggablePreviewerState(
    scope,
    defaultAnimationSpec,
    verticalDragType,
    scaleToCloseMinValue,
    pagerState,
    getKey
)

@Composable
fun Previewer(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: PreviewerState,
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
    DraggablePreviewer(
        modifier = modifier,
        state = state,
        itemSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        enter = enter,
        exit = exit,
        debugMode = debugMode,
        detectGesture = detectGesture,
        previewerLayer = previewerLayer,
        zoomablePolicy = zoomablePolicy,
    )
}