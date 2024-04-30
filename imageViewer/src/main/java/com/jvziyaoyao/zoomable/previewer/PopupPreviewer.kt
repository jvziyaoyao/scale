package com.jvziyaoyao.zoomable.previewer

import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.jvziyaoyao.zoomable.pager.DEFAULT_BEYOND_BOUNDS_ITEM_COUNT
import com.jvziyaoyao.zoomable.pager.DEFAULT_ITEM_SPACE
import com.jvziyaoyao.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.zoomable.pager.PagerZoomablePolicyScope
import com.jvziyaoyao.zoomable.pager.SupportedPagerState
import com.jvziyaoyao.zoomable.pager.ZoomablePager
import com.jvziyaoyao.zoomable.pager.ZoomablePagerState
import com.jvziyaoyao.zoomable.pager.rememberSupportedPagerState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @program: PopupPreviewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-13 11:45
 **/

/**
 * 默认的弹出预览时的动画效果
 */
val DEFAULT_PREVIEWER_ENTER_TRANSITION =
    scaleIn(tween(180)) + fadeIn(tween(240))

/**
 * 默认的关闭预览时的动画效果
 */
val DEFAULT_PREVIEWER_EXIT_TRANSITION =
    scaleOut(tween(320)) + fadeOut(tween(240))

@Composable
fun rememberPopupPreviewerState(
    @IntRange(from = 0) initialPage: Int = 0,
    pageCount: () -> Int,
): PopupPreviewerState {
    val pagerState = rememberSupportedPagerState(initialPage, pageCount)
    return remember {
        PopupPreviewerState(pagerState = pagerState)
    }
}

open class PopupPreviewerState(
    pagerState: SupportedPagerState,
) : ZoomablePagerState(pagerState) {

    // 锁对象
    private var mutex = Mutex()

    // 最外侧animateVisibleState
    internal var animateContainerVisibleState by mutableStateOf(MutableTransitionState(false))

    // 进入转换动画
    internal var enterTransition: EnterTransition? = null

    // 离开的转换动画
    internal var exitTransition: ExitTransition? = null

    // 标记打开动作，执行开始
    internal suspend fun stateOpenStart() =
        updateState(animating = true, visible = false, visibleTarget = true)

    // 标记打开动作，执行结束
    internal suspend fun stateOpenEnd() =
        updateState(animating = false, visible = true, visibleTarget = null)

    // 标记关闭动作，执行开始
    internal suspend fun stateCloseStart() =
        updateState(animating = true, visible = true, visibleTarget = false)

    // 标记关闭动作，执行结束
    internal suspend fun stateCloseEnd() =
        updateState(animating = false, visible = false, visibleTarget = null)

    // 是否正在进行动画
    var animating by mutableStateOf(false)
        internal set

    // 是否可见
    var visible by mutableStateOf(false)
        internal set

    // 是否可见的目标值
    var visibleTarget by mutableStateOf<Boolean?>(null)
        internal set

    // 是否允许执行open操作
    val canOpen: Boolean
        get() = !visible && visibleTarget == null && !animating

    // 是否允许执行close操作
    val canClose: Boolean
        get() = visible && visibleTarget == null && !animating

    /**
     * 更新当前的标记状态
     * @param animating Boolean
     * @param visible Boolean
     * @param visibleTarget Boolean?
     */
    private suspend fun updateState(animating: Boolean, visible: Boolean, visibleTarget: Boolean?) {
        mutex.withLock {
            this.animating = animating
            this.visible = visible
            this.visibleTarget = visibleTarget
        }
    }

    open suspend fun openAction(
        index: Int = 0,
        enterTransition: EnterTransition? = null,
    ) {
        // 设置当前转换动画
        this.enterTransition = enterTransition
        // container动画立即设置为关闭
        animateContainerVisibleState = MutableTransitionState(false)
        // 开启container
        animateContainerVisibleState.targetState = true
        // 滚动到指定页面
        scrollToPage(index)
    }

    suspend fun open(
        index: Int = 0,
        enterTransition: EnterTransition? = null,
    ) {
        // 标记状态
        stateOpenStart()
        // 实际业务发生
        openAction(index, enterTransition)
        // 标记状态
        stateOpenEnd()
    }

    open suspend fun closeAction(
        exitTransition: ExitTransition? = null,
    ) {
        // 设置当前转换动画
        this.exitTransition = exitTransition
        // 这里创建一个全新的state是为了让exitTransition的设置得到响应
        animateContainerVisibleState = MutableTransitionState(true)
        // 开启container关闭动画
        animateContainerVisibleState.targetState = false
    }

    suspend fun close(
        exitTransition: ExitTransition? = null,
    ) {
        // 标记状态
        stateCloseStart()
        // 实际业务发生
        closeAction(exitTransition)
        // 标记状态
        stateCloseEnd()
    }
}

@Composable
fun PopupPreviewer(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: PopupPreviewerState,
    // 图片间的间隔
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    // 页面外缓存个数
    beyondBoundsItemCount: Int = DEFAULT_BEYOND_BOUNDS_ITEM_COUNT,
    // 进入动画
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    // 退出动画
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    // 检测手势
    detectGesture: PagerGestureScope = PagerGestureScope(),
    // 图层修饰
    previewerDecoration: @Composable (innerBox: @Composable () -> Unit) -> Unit =
        @Composable { innerBox -> innerBox() },
    // 图层本体
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Unit,
) {
    state.apply {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visibleState = animateContainerVisibleState,
            enter = enterTransition ?: enter,
            exit = exitTransition ?: exit,
        ) {
            previewerDecoration {
                ZoomablePager(
                    modifier = modifier.fillMaxSize(),
                    state = state,
                    itemSpacing = itemSpacing,
                    beyondBoundsItemCount = beyondBoundsItemCount,
                    detectGesture = detectGesture,
                    zoomablePolicy = zoomablePolicy,
                )
            }
        }
    }
}