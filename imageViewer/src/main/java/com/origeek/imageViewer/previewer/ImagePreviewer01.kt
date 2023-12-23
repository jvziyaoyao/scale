package com.origeek.imageViewer.previewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.origeek.imageViewer.gallery.GalleryGestureScope
import com.origeek.imageViewer.gallery.GalleryZoomablePolicyScope
import com.origeek.imageViewer.gallery.ImageGallery01
import com.origeek.imageViewer.gallery.ImageGalleryState01
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-13 11:45
 **/

open class ImagePreviewerState01(
    // 协程作用域
    scope: CoroutineScope = MainScope(),
    // 默认动画窗格
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 预览状态
    galleryState: ImageGalleryState01,
) : PreviewerPager01State(galleryState) {

    // 锁对象
    private var mutex = Mutex()

    // TODO: internal
    // 最外侧animateVisibleState
    var animateContainerVisibleState by mutableStateOf(MutableTransitionState(false))

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

    suspend fun open(
        index: Int = 0,
        enterTransition: EnterTransition? = null
    ) {
        // 标记状态
        stateOpenStart()
        // 设置当前转换动画
        this.enterTransition = enterTransition
        // container动画立即设置为关闭
        animateContainerVisibleState = MutableTransitionState(false)
        // 开启container
        animateContainerVisibleState.targetState = true
        // 滚动到指定页面
        galleryState.scrollToPage(index)
        // 标记状态
        stateOpenEnd()
    }

    suspend fun close(exitTransition: ExitTransition? = null) {
        // 标记状态
        stateCloseStart()
        // 设置当前转换动画
        this.exitTransition = exitTransition
        // 这里创建一个全新的state是为了让exitTransition的设置得到响应
        animateContainerVisibleState = MutableTransitionState(true)
        // 开启container关闭动画
        animateContainerVisibleState.targetState = false
        // 标记状态
        stateCloseEnd()
    }
}

@Composable
fun ImagePreviewer01(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: ImagePreviewerState01,
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
    // 图层装饰
    previewerDecoration: @Composable (innerBox: @Composable () -> Unit) -> Unit =
        @Composable { innerBox -> innerBox() },
    // 图层本体
    zoomablePolicy: @Composable GalleryZoomablePolicyScope.(page: Int) -> Unit,
) {
    state.apply {
        previewerDecoration {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visibleState = animateContainerVisibleState,
                enter = enterTransition ?: enter,
                exit = exitTransition ?: exit,
            ) {
                ImageGallery01(
                    modifier = modifier.fillMaxSize(),
                    state = galleryState,
                    itemSpacing = itemSpacing,
                    beyondBoundsItemCount = beyondBoundsItemCount,
                    detectGesture = detectGesture,
                    zoomablePolicy = zoomablePolicy,
                )
            }
        }
    }
}