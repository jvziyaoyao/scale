package com.origeek.imageViewer

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.origeek.ui.common.Ticket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

val DEEP_DARK_FANTASY = Color(0xFF000000)
val DEFAULT_ITEM_SPACE = 12.dp
val DEFAULT_SOFT_ANIMATION_SPEC = tween<Float>(320)

@Composable
fun DefaultPreviewerBackground() {
    Box(
        modifier = Modifier
            .background(DEEP_DARK_FANTASY)
            .fillMaxSize()
    )
}

class ImagePreviewerState internal constructor() {

    lateinit var scope: CoroutineScope

    lateinit var pagerState: ImagePagerState

    lateinit var transformState: TransformContentState

    private var mutex = Mutex()

    internal val ticket = Ticket()

    internal var getKey: ((Int) -> Any)? = null

    internal var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC

    internal var imageViewerState by mutableStateOf<ImageViewerState?>(null)

    internal var animateContainerState by mutableStateOf(MutableTransitionState(false))

    internal var uiAlpha = Animatable(1F)

    internal var transformContentAlpha = Animatable(1F)

    internal var viewerContainerAlpha = Animatable(1F)

    private val viewerContainerVisible: Boolean
        get() = viewerContainerAlpha.value == 1F

    var animating by mutableStateOf(false)

    var visible by mutableStateOf(false)
        internal set

    var visibleTarget by mutableStateOf<Boolean?>(null)

    val viewerMounted: MutableStateFlow<Boolean>
        get() = imageViewerState?.mountedFlow ?: MutableStateFlow(false)

    val canOpen: Boolean
        get() = !visible && visibleTarget == null && !animating

    val canClose: Boolean
        get() = visible && visibleTarget == null && !animating

    val currentPage: Int
        get() = pagerState.currentPage

    val targetPage: Int
        get() = pagerState.targetPage

    val pageCount: Int
        get() = pagerState.pageCount

    val currentPageOffset: Float
        get() = pagerState.currentPageOffset

    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    var allowLoading by mutableStateOf(true)
        private set

    /**
     * TODO
     */

    var ppContainerSize: IntSize by mutableStateOf(IntSize.Zero)

    var ppOffsetX = Animatable(0F)

    var ppOffsetY = Animatable(0F)

    var ppScale = Animatable(1F)

    suspend fun ppReset() {
        scope.apply {
            listOf(
                async {
                    ppOffsetX.animateTo(0F, defaultAnimationSpec)
                },
                async {
                    ppOffsetY.animateTo(0F, defaultAnimationSpec)
                },
                async {
                    ppScale.animateTo(1F, defaultAnimationSpec)
                },
            ).awaitAll()
        }
    }

    suspend fun ppResetImmediately() {
        ppOffsetX.snapTo(0F)
        ppOffsetY.snapTo(0F)
        ppScale.snapTo(1F)
    }

    /**
     * TODO
     */

    private suspend fun updateState(animating: Boolean, visible: Boolean, visibleTarget: Boolean?) {
        mutex.withLock {
            this.animating = animating
            this.visible = visible
            this.visibleTarget = visibleTarget
        }
    }

    private suspend fun stateOpenStart() =
        updateState(animating = true, visible = false, visibleTarget = true)

    private suspend fun stateOpenEnd() =
        updateState(animating = false, visible = true, visibleTarget = null)

    private suspend fun stateCloseStart() =
        updateState(animating = true, visible = true, visibleTarget = false)

    private suspend fun stateCloseEnd() =
        updateState(animating = false, visible = false, visibleTarget = null)

    private suspend fun shrinkDown() {
        stateCloseStart()
        listOf(
            scope.async {
                imageViewerState?.scale?.animateTo(0F, animationSpec = defaultAnimationSpec)
            },
            scope.async {
                uiAlpha.animateTo(0F, animationSpec = defaultAnimationSpec)
            }
        ).awaitAll()
        ticket.awaitNextTicket()
        animateContainerState = MutableTransitionState(false)
        stateCloseEnd()
    }

    private suspend fun awaitViewerLoading() {
        imageViewerState?.mountedFlow?.apply {
            withContext(Dispatchers.Default) {
                takeWhile { !it }.collect()
            }
        }
    }

    private suspend fun transformSnapToViewer(isViewer: Boolean) {
        if (isViewer) {
            if (visibleTarget == false) return
            transformContentAlpha.snapTo(0F)
            viewerContainerAlpha.snapTo(1F)
        } else {
            transformContentAlpha.snapTo(1F)
            viewerContainerAlpha.snapTo(0F)
        }
    }

    internal suspend fun verticalDrag(pointerInputScope: PointerInputScope) {
        pointerInputScope.apply {
            var vStartOffset by mutableStateOf<Offset?>(null)
            var vOrientationDown by mutableStateOf<Boolean?>(null)
            if (getKey != null) detectVerticalDragGestures(
                onDragStart = OnDragStart@{
                    // 如果imageViewerState不存在，无法进行下拉手势
                    if (imageViewerState == null) return@OnDragStart
                    var transformItemState: TransformItemState? = null
                    // 查询当前transformItem
                    getKey?.apply {
                        findTransformItem(invoke(currentPage))?.apply {
                            transformItemState = this
                        }
                    }
                    // 更新当前transformItem
                    transformState.itemState = transformItemState
                    // 只有viewer的缩放率为1时才允许下拉手势
                    if (imageViewerState?.scale?.value == 1F) {
                        vStartOffset = it
                        // 进入下拉手势时禁用viewer的手势
                        imageViewerState?.allowGestureInput = false
                    }
                },
                onDragEnd = OnDragEnd@{
                    if (vStartOffset == null) return@OnDragEnd
                    vStartOffset = null
                    vOrientationDown = null
                    imageViewerState?.allowGestureInput = true
                    // TODO: 0.8这个值要配置
                    if (ppScale.value < 0.8F && getKey != null) {

                    } else {
                        scope.launch {
                            uiAlpha.animateTo(1F, defaultAnimationSpec)
                        }
                        scope.launch {
                            ppReset()
                        }
                    }
//                    imageViewerState?.apply {
//                        allowGestureInput = true
//                        if (scale.value < 0.8F && getKey != null) {
//                            scope.launch {
//                                val key = getKey!!(currentPage)
//                                val transformItem = findTransformItem(key)
//                                if (transformItem != null) {
//                                    closeTransform(key)
//                                } else {
//                                    shrinkDown()
//                                }
//                                uiAlpha.snapTo(1F)
//                            }
//                        } else {
//                            scope.launch {
//                                uiAlpha.animateTo(1F, defaultAnimationSpec)
//                            }
//                            scope.launch {
//                                reset(defaultAnimationSpec)
//                            }
//                        }
//                    }
                },
                onVerticalDrag = OnVerticalDrag@{ change, dragAmount ->
                    if (imageViewerState == null) return@OnVerticalDrag
                    if (vStartOffset == null) return@OnVerticalDrag
                    if (vOrientationDown == null) vOrientationDown = dragAmount > 0
                    if (vOrientationDown == true) {
                        val offsetY = change.position.y - vStartOffset!!.y
                        val offsetX = change.position.x - vStartOffset!!.x
                        val containerHeight = ppContainerSize.height
                        val scale = (containerHeight - offsetY.absoluteValue).div(
                            containerHeight
                        )
                        scope.launch {
                            uiAlpha.snapTo(scale)
                            ppOffsetX.snapTo(offsetX)
                            ppOffsetY.snapTo(offsetY)
                            ppScale.snapTo(scale)
                        }
                    } else {
                        // 如果不是向上，就返还输入权，以免页面卡顿
                        imageViewerState?.allowGestureInput = true
                    }
                }
            )
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

    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

    private var openCallback: (() -> Unit)? = null

    internal var enterTransition: EnterTransition? = null

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

    private var closeCallback: (() -> Unit)? = null

    internal var exitTransition: ExitTransition? = null

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

    private var openTransformJob: Deferred<Unit>? = null

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

    private fun cancelOpenTransform() {
        openTransformJob?.cancel()
    }

    private suspend fun copyViewerPosToContent(itemState: TransformItemState) {
        transformState.itemState = itemState
        transformState.containerSize = imageViewerState!!.containerSize
        val scale = imageViewerState!!.scale
        val offsetX = imageViewerState!!.offsetX
        val offsetY = imageViewerState!!.offsetY
        val rw = transformState.fitSize.width * scale.value
        val rh = transformState.fitSize.height * scale.value
        val goOffsetX =
            (transformState.containerSize.width - rw).div(2) + offsetX.value
        val goOffsetY =
            (transformState.containerSize.height - rh).div(2) + offsetY.value
        val fixScale = transformState.fitScale * scale.value
        transformState.graphicScaleX.snapTo(fixScale)
        transformState.graphicScaleY.snapTo(fixScale)
        transformState.displayWidth.snapTo(transformState.displayRatioSize.width)
        transformState.displayHeight.snapTo(transformState.displayRatioSize.height)
        transformState.offsetX.snapTo(goOffsetX)
        transformState.offsetY.snapTo(goOffsetY)

        animateContainerState = MutableTransitionState(true)
        transformSnapToViewer(false)
        if (uiAlpha.value == 0F) uiAlpha.snapTo(1F)
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
            if (viewerContainerVisible) copyViewerPosToContent(itemState)
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

    fun enableVerticalDrag(getKey: ((Int) -> Any)) {
        this.getKey = getKey
    }

    fun disableVerticalDrag() {
        this.getKey = null
    }

    companion object {
        val Saver: Saver<ImagePreviewerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.animateContainerState.currentState,
                    it.uiAlpha.value,
                    it.transformContentAlpha.value,
                    it.viewerContainerAlpha.value,
                    it.visible,
                )
            },
            restore = {
                val previewerState = ImagePreviewerState()
                previewerState.animateContainerState = MutableTransitionState(it[0] as Boolean)
                previewerState.uiAlpha = Animatable(it[1] as Float)
                previewerState.transformContentAlpha = Animatable(it[2] as Float)
                previewerState.viewerContainerAlpha = Animatable(it[3] as Float)
                previewerState.visible = it[4] as Boolean
                previewerState
            }
        )
    }
}

@Composable
fun rememberPreviewerState(
    pagerState: ImagePagerState = rememberImagePagerState(),
    transformState: TransformContentState = rememberTransformContentState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    animationSpec: AnimationSpec<Float>? = null,
): ImagePreviewerState {
    val previewerState = rememberSaveable(saver = ImagePreviewerState.Saver) {
        ImagePreviewerState()
    }
    previewerState.pagerState = pagerState
    previewerState.transformState = transformState
    previewerState.scope = scope
    if (animationSpec != null) previewerState.defaultAnimationSpec = animationSpec
    return previewerState
}

@OptIn(ExperimentalAnimationApi::class)
val DEFAULT_PREVIEWER_ENTER_TRANSITION =
    scaleIn(tween(180)) + fadeIn(tween(240))

@OptIn(ExperimentalAnimationApi::class)
val DEFAULT_PREVIEWER_EXIT_TRANSITION =
    scaleOut(tween(320)) + fadeOut(tween(240))

class PreviewerLayerScope(
    var viewerContainer: @Composable (viewer: @Composable () -> Unit) -> Unit = { it() },
    var background: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> DefaultPreviewerBackground() },
    var foreground: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> },
)

@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    count: Int,
    state: ImagePreviewerState = rememberPreviewerState(),
    imageLoader: @Composable (Int) -> Any?,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    currentViewerState: (ImageViewerState) -> Unit = {},
    detectGesture: GalleryGestureScope.() -> Unit = {},
    previewerLayer: PreviewerLayerScope.() -> Unit = {},
) {
    // 图层相关
    val layerScope = remember { PreviewerLayerScope() }
    previewerLayer.invoke(layerScope)
    LaunchedEffect(
        key1 = state.animateContainerState,
        key2 = state.animateContainerState.currentState
    ) {
        state.onAnimateContainerStateChanged()
    }
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visibleState = state.animateContainerState,
        enter = state.enterTransition ?: enter,
        exit = state.exitTransition ?: exit,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.getKey) {
                    state.verticalDrag(this)
                }
        ) {
            @Composable
            fun UIContainer(content: @Composable () -> Unit) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(state.uiAlpha.value)
                ) {
                    content()
                }
            }
            ImageGallery(
                modifier = modifier.fillMaxSize(),
                count = count,
                state = state.pagerState,
                imageLoader = imageLoader,
                currentViewerState = {
                    state.imageViewerState = it
                    currentViewerState(it)
                },
                itemSpacing = itemSpacing,
                detectGesture = detectGesture,
                galleryLayer = {
                    this.viewerContainer = {
                        layerScope.viewerContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned {
                                        state.ppContainerSize = it.size
                                    }
                                    .graphicsLayer {
                                        scaleX = state.ppScale.value
                                        scaleY = state.ppScale.value
                                        translationX = state.ppOffsetX.value
                                        translationY = state.ppOffsetY.value
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(state.transformContentAlpha.value)
                                ) {
                                    TransformContentView(state.transformState)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(state.viewerContainerAlpha.value)
                                ) {
                                    it()
                                }
                                /**
                                 * TODO: 后续再考虑如何设计loading
                                 */
                                val viewerMounted by state.viewerMounted.collectAsState(initial = false)
                                AnimatedVisibility(
                                    visible = !viewerMounted && state.allowLoading,
                                    enter = fadeIn(tween(200)),
                                    exit = fadeOut(tween(200))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White.copy(0.2F))
                                    }
                                }
                            }
                        }
                    }
                    this.background = {
                        UIContainer {
                            layerScope.background(count, it)
                        }
                    }
                    this.foreground = {
                        UIContainer {
                            layerScope.foreground(count, it)
                        }
                    }
                },
            )
            if (!state.visible)
                Box(modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { } }) { }
        }
    }
    state.ticket.Next()
}