package com.origeek.imageViewer.previewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.origeek.imageViewer.viewer.ImageViewerState
import com.origeek.ui.common.Ticket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-17 14:41
 **/

open class PreviewerTransformState: PreviewerPagerState() {

    private var mutex = Mutex()

    private var openCallback: (() -> Unit)? = null

    private var closeCallback: (() -> Unit)? = null

    private suspend fun updateState(animating: Boolean, visible: Boolean, visibleTarget: Boolean?) {
        mutex.withLock {
            this.animating = animating
            this.visible = visible
            this.visibleTarget = visibleTarget
        }
    }

    private var openTransformJob: Deferred<Unit>? = null

    private fun cancelOpenTransform() {
        openTransformJob?.cancel()
    }

    private suspend fun copyViewerPosToContent(itemState: TransformItemState) {
        // 更新itemState，确保itemState一致
        transformState.itemState = itemState
        // 确保viewer的容器大小与transform的容器大小一致
        transformState.containerSize = imageViewerState!!.containerSize
        val scale = imageViewerState!!.scale
        val offsetX = imageViewerState!!.offsetX
        val offsetY = imageViewerState!!.offsetY
        // 计算transform的实际大小
        val rw = transformState.fitSize.width * scale.value
        val rh = transformState.fitSize.height * scale.value
        // 计算目标平移量
        val goOffsetX =
            (transformState.containerSize.width - rw).div(2) + offsetX.value
        val goOffsetY =
            (transformState.containerSize.height - rh).div(2) + offsetY.value
        // 计算缩放率
        val fixScale = transformState.fitScale * scale.value
        // 更新值
        transformState.graphicScaleX.snapTo(fixScale)
        transformState.graphicScaleY.snapTo(fixScale)
        transformState.displayWidth.snapTo(transformState.displayRatioSize.width)
        transformState.displayHeight.snapTo(transformState.displayRatioSize.height)
        transformState.offsetX.snapTo(goOffsetX)
        transformState.offsetY.snapTo(goOffsetY)
    }

    private suspend fun awaitViewerLoading() {
        imageViewerState?.mountedFlow?.apply {
            withContext(Dispatchers.Default) {
                takeWhile { !it }.collect()
            }
        }
    }

    /**
     * override
     */

    lateinit var scope: CoroutineScope

    lateinit var transformState: TransformContentState

    internal val ticket = Ticket()

    internal var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC

    internal var animateContainerState by mutableStateOf(MutableTransitionState(false))

    internal var imageViewerState by mutableStateOf<ImageViewerState?>(null)

    internal var uiAlpha = Animatable(1F)

    internal var transformContentAlpha = Animatable(1F)

    internal var viewerContainerAlpha = Animatable(1F)

    internal val viewerContainerVisible: Boolean
        get() = viewerContainerAlpha.value == 1F

    val viewerMounted: MutableStateFlow<Boolean>
        get() = imageViewerState?.mountedFlow ?: MutableStateFlow(false)

    var allowLoading by mutableStateOf(true)

    var animating by mutableStateOf(false)

    var visible by mutableStateOf(false)
        internal set

    var visibleTarget by mutableStateOf<Boolean?>(null)
        internal set

    val canOpen: Boolean
        get() = !visible && visibleTarget == null && !animating

    val canClose: Boolean
        get() = visible && visibleTarget == null && !animating

    internal var enterTransition: EnterTransition? = null

    internal var exitTransition: ExitTransition? = null

    protected suspend fun stateOpenStart() =
        updateState(animating = true, visible = false, visibleTarget = true)

    protected suspend fun stateOpenEnd() =
        updateState(animating = false, visible = true, visibleTarget = null)

    protected suspend fun stateCloseStart() =
        updateState(animating = true, visible = true, visibleTarget = false)

    protected suspend fun stateCloseEnd() =
        updateState(animating = false, visible = false, visibleTarget = null)

    protected suspend fun transformSnapToViewer(isViewer: Boolean) {
        if (isViewer) {
            if (visibleTarget == false) return
            transformContentAlpha.snapTo(0F)
            viewerContainerAlpha.snapTo(1F)
        } else {
            transformContentAlpha.snapTo(1F)
            viewerContainerAlpha.snapTo(0F)
        }
    }

    internal fun onAnimateContainerStateChanged() {
        if (animateContainerState.currentState) {
            openCallback?.invoke()
            transformState.setEnterState()
        } else {
            closeCallback?.invoke()
        }
    }

    fun findTransformItem(key: Any) = transformState.findTransformItem(key)

    fun clearTransformItems() = transformState.clearTransformItems()

    suspend fun open(
        index: Int = 0,
        itemState: TransformItemState? = null,
        enterTransition: EnterTransition? = null
    ) =
        suspendCoroutine<Unit> { c ->
            // 设置当前转换动画
            this.enterTransition = enterTransition
            // 设置转换回调
            openCallback = {
                c.resume(Unit)
                // 清除转换回调
                openCallback = null
                // 清除转换动画
                this.enterTransition = null
                // 标记结束
                scope.launch {
                    stateOpenEnd()
                }
            }
            scope.launch {
                // 标记开始
                stateOpenStart()
                // container动画立即设置为关闭
                animateContainerState = MutableTransitionState(false)
                // 允许显示loading
                allowLoading = true
                // 开启UI
                uiAlpha.snapTo(1F)
                // 开启viewer
                viewerContainerAlpha.snapTo(1F)
                // 如果输入itemState，则用itemState做为背景
                if (itemState != null) {
                    scope.launch {
                        transformContentAlpha.snapTo(1F)
                        transformState.awaitContainerSizeSpecifier()
                        transformState.enterTransform(itemState, animationSpec = tween(0))
                    }
                }
                // 等待下一帧
                ticket.awaitNextTicket()
                // 开启container
                animateContainerState.targetState = true
                // 可能要跳两次才行，否则会闪退
                ticket.awaitNextTicket()
                ticket.awaitNextTicket()
                // 跳转到index
                pagerState.scrollToPage(index)
                // 等待viewer加载
                awaitViewerLoading()
                // viewer加载成功后显示viewer
                transformSnapToViewer(true)
            }
        }

    suspend fun close(exitTransition: ExitTransition? = null) = suspendCoroutine<Unit> { c ->
        // 设置当前退出动画
        this.exitTransition = exitTransition
        // 设置退出结束的回调方法
        closeCallback = {
            c.resume(Unit)
            // 将回调设置为空
            closeCallback = null
            // 将退出动画设置为空
            this.exitTransition = null
            // 标记结束
            scope.launch {
                stateCloseEnd()
            }
        }
        scope.launch {
            // 标记开始
            stateCloseStart()
            // 关闭正在进行的开启操作
            cancelOpenTransform()
            // 这里创建一个全新的state是为了让exitTransition的设置得到响应
            animateContainerState = MutableTransitionState(true)
            // 开启container关闭动画
            animateContainerState.targetState = false
            // 等待下一帧
            ticket.awaitNextTicket()
            // transformState标记退出
            transformState.setExitState()
        }
    }

    suspend fun openTransform(
        index: Int,
        itemState: TransformItemState,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        // 动画开始
        stateOpenStart()
        // 设置当前动画窗格
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        // 关闭loading
        allowLoading = false
        // 设置新的container状态立刻设置为true
        animateContainerState = MutableTransitionState(true)
        // 关闭viewer。打开transform
        transformSnapToViewer(false)
        // 关闭UI
        uiAlpha.snapTo(0F)
        // 等待下一帧
        ticket.awaitNextTicket()
        // pager跳转到index页
        pagerState.scrollToPage(index)
        // 这两个一起执行
        listOf(
            scope.async {
                // 开启动画
                transformState.enterTransform(itemState, animationSpec = currentAnimationSpec)
                // 开启loading
                allowLoading = true
            },
            scope.async {
                // UI慢慢显示
                uiAlpha.animateTo(1F, animationSpec = currentAnimationSpec)
            }
        ).awaitAll()

        // 执行完成后的回调
        stateOpenEnd()

        // 这里需要等待viewer挂载，显示loading界面
        openTransformJob = scope.async {
            // 等待viewer加载
            awaitViewerLoading()
            // viewer加载成功后显示viewer
            transformSnapToViewer(true)
        }
        openTransformJob?.await()
    }

    suspend fun closeTransform(
        key: Any,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        // 设置当前动画窗格
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        // 标记开始
        stateCloseStart()
        // 关闭可能正在进行的open操作
        cancelOpenTransform()
        // 关闭loading的显示
        allowLoading = false
        // 查询item是否存在
        val itemState = findTransformItem(key)
        // 如果存在，就transform退出，否则就普通退出
        if (itemState != null) {
            // 如果viewer在显示的状态，退出时将viewer的pose复制给content
            if (viewerContainerVisible) {
                // 复制viewer的pos给transform
                copyViewerPosToContent(itemState)
                // 切换为transform
                transformSnapToViewer(false)
            }
            // 等待下一帧
            ticket.awaitNextTicket()
            listOf(
                scope.async {
                    // transform动画退出
                    transformState.exitTransform(animationSpec = currentAnimationSpec)
                    // 退出结束后隐藏content
                    transformContentAlpha.snapTo(0F)
                },
                scope.async {
                    // 动画隐藏UI
                    uiAlpha.animateTo(0F, animationSpec = currentAnimationSpec)
                }
            ).awaitAll()
            // 等待下一帧
            ticket.awaitNextTicket()
            // 彻底关闭container
            animateContainerState = MutableTransitionState(false)
        } else {
            // transform标记退出
            transformState.setExitState()
            // container动画退出
            animateContainerState.targetState = false
        }
        // 允许使用loading
        allowLoading = true
        // 标记结束
        stateCloseEnd()
    }

}