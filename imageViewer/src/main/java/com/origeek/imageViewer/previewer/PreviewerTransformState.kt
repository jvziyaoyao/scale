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
import com.origeek.imageViewer.gallery.ImageGalleryState
import com.origeek.imageViewer.viewer.ImageViewerState
import com.origeek.ui.common.compose.Ticket
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

    /**
     *   +-------------------+
     *          PRIVATE
     *   +-------------------+
     */

    // 锁对象
    private var mutex = Mutex()

    // 打开回调，最外层animateVisible修改时调用
    private var openCallback: (() -> Unit)? = null

    // 关闭回调，最外层animateVisible修改时调用
    private var closeCallback: (() -> Unit)? = null

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

    /**
     * 打开图片后到加载成功过程的协程任务
     */
    private var openTransformJob: Deferred<Unit>? = null

    /**
     * 取消打开动作
     */
    private fun cancelOpenTransform() {
        openTransformJob?.cancel()
    }

    /**
     * 将viewer的位置大小等信息复制给transformContent
     * @param itemState TransformItemState
     */
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

    /**
     * 等待viewer挂载成功
     */
    private suspend fun awaitViewerLoading() {
        imageViewerState?.mountedFlow?.apply {
            withContext(Dispatchers.Default) {
                takeWhile { !it }.collect()
            }
        }
    }

    /**
     *   +-------------------+
     *         LATE INIT
     *   +-------------------+
     */

    // 从外部提供作用域
    lateinit var scope: CoroutineScope

    // 从外部提供transformContentState
    lateinit var transformState: TransformContentState

    /**
     *   +-------------------+
     *         INTERNAL
     *   +-------------------+
     */

    // 等待界面刷新的ticket
    internal val ticket = Ticket()

    // 默认动画窗格
    internal var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC

    // 最外侧animateVisibleState
    internal var animateContainerVisibleState by mutableStateOf(MutableTransitionState(false))

    // UI透明度
    internal var uiAlpha = Animatable(1F)

    // 转换图层transformContent透明度
    internal var transformContentAlpha = Animatable(1F)

    // viewer容器的透明度
    internal var viewerContainerAlpha = Animatable(1F)

    // 是否显示viewer容器的标识
    internal val viewerContainerVisible: Boolean
        get() = viewerContainerAlpha.value == 1F

    // 进入转换动画
    internal var enterTransition: EnterTransition? = null

    // 离开的转换动画
    internal var exitTransition: ExitTransition? = null

    // viewer是否已成功的挂载
    internal val viewerMounted: MutableStateFlow<Boolean>
        get() = imageViewerState?.mountedFlow ?: MutableStateFlow(false)

    // 是否允许界面显示loading
    internal var allowLoading by mutableStateOf(true)

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

    /**
     * 转换图层转viewer图层，true显示viewer，false显示转换图层
     * @param isViewer Boolean
     */
    internal suspend fun transformSnapToViewer(isViewer: Boolean) {
        if (isViewer) {
            if (visibleTarget == false) return
            transformContentAlpha.snapTo(0F)
            viewerContainerAlpha.snapTo(1F)
        } else {
            transformContentAlpha.snapTo(1F)
            viewerContainerAlpha.snapTo(0F)
        }
    }

    /**
     * animateVisable执行完成后调用回调方法
     */
    internal fun onAnimateContainerStateChanged() {
        if (animateContainerVisibleState.currentState) {
            openCallback?.invoke()
            transformState.setEnterState()
        } else {
            closeCallback?.invoke()
        }
    }

    /**
     *   +-------------------+
     *          PUBLIC
     *   +-------------------+
     */

    // 是否正在进行动画
    var animating by mutableStateOf(false)

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

    // imageViewer状态对象
    val imageViewerState: ImageViewerState?
        get() = galleryState.imageViewerState

    // 查找key关联的transformItem
    fun findTransformItem(key: Any) = transformState.findTransformItem(key)

    // 清除全部transformItems
    fun clearTransformItems() = transformState.clearTransformItems()

    /**
     * 打开previewer
     * @param index Int
     * @param itemState TransformItemState?
     * @param enterTransition EnterTransition?
     */
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
                // 跳转到index
                galleryState = ImageGalleryState(index)
                // container动画立即设置为关闭
                animateContainerVisibleState = MutableTransitionState(false)
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
                // 开启container
                animateContainerVisibleState.targetState = true
                // 可能要跳两次才行，否则会闪退
                ticket.awaitNextTicket()
                // 等待viewer加载
                awaitViewerLoading()
                // viewer加载成功后显示viewer
                transformSnapToViewer(true)
            }
        }

    /**
     * 关闭previewer
     * @param exitTransition ExitTransition?
     */
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
            animateContainerVisibleState = MutableTransitionState(true)
            // 开启container关闭动画
            animateContainerVisibleState.targetState = false
            // 等待下一帧
            ticket.awaitNextTicket()
            // transformState标记退出
            transformState.setExitState()
        }
    }

    /**
     * 打开previewer，带转换效果
     * @param index Int
     * @param itemState TransformItemState
     * @param animationSpec AnimationSpec<Float>?
     */
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
        // 跳转到index页
        galleryState = ImageGalleryState(index)
        // 设置新的container状态立刻设置为true
        animateContainerVisibleState = MutableTransitionState(true)
        // 关闭viewer。打开transform
        transformSnapToViewer(false)
        // 关闭UI
        uiAlpha.snapTo(0F)
//        // 等待下一帧
        ticket.awaitNextTicket()
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

    /**
     * 关闭previewer，带转换效果
     * @param key Any
     * @param animationSpec AnimationSpec<Float>?
     */
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
            animateContainerVisibleState = MutableTransitionState(false)
        } else {
            // transform标记退出
            transformState.setExitState()
            // container动画退出
            animateContainerVisibleState.targetState = false
        }
        // 允许使用loading
        allowLoading = true
        // 标记结束
        stateCloseEnd()
    }

}