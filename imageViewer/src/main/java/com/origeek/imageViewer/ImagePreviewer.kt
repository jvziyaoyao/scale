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
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.origeek.ui.common.Ticket
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    var viewerLoading by mutableStateOf(false)
        internal set

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
        viewerLoading = true
        imageViewerState?.mountedFlow?.collectOne(scope)
        viewerLoading = false
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
//                    if (imageViewerState == null) return@OnDragStart
//                    if (imageViewerState?.modelType?.name == ComposeModel::class.java.name) return@OnDragStart
//                    var transformItemState: TransformItemState? = null
//                    getKey?.apply {
//                        findTransformItem(invoke(currentPage))?.apply {
//                            transformItemState = this
//                        }
//                    }
//                    transformState.itemState = transformItemState
//                    if (imageViewerState!!.scale.value == 1F) {
//                        vStartOffset = it
//                        imageViewerState!!.allowGestureInput = false
//                    }
                },
                onDragEnd = OnDragEnd@{
//                    if (vStartOffset == null) return@OnDragEnd
//                    vStartOffset = null
//                    vOrientationDown = null
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
//                    if (imageViewerState == null) return@OnVerticalDrag
//                    if (vStartOffset != null) {
//                        if (vOrientationDown == null) vOrientationDown = dragAmount > 0
//                        if (vOrientationDown == true) {
//                            val offsetY = change.position.y - vStartOffset!!.y
//                            val offsetX = change.position.x - vStartOffset!!.x
//                            val containerHeight = imageViewerState!!.containerSize.height
//                            val scale = (containerHeight - offsetY.absoluteValue).div(
//                                containerHeight
//                            )
//                            scope.launch {
//                                uiAlpha.snapTo(scale)
//                                imageViewerState?.offsetY?.apply {
//                                    snapTo(offsetY)
//                                }
//                                imageViewerState?.offsetX?.apply {
//                                    snapTo(offsetX)
//                                }
//                                imageViewerState?.scale?.apply {
//                                    snapTo(scale)
//                                }
//                            }
//                        } else {
//                            // 如果不是向上，就返还输入权，以免页面卡顿
//                            imageViewerState?.allowGestureInput = true
//                        }
//                    }
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

    suspend fun open(index: Int = 0, enterTransition: EnterTransition? = null) =
        suspendCoroutine<Unit> { c ->
            this.enterTransition = enterTransition
            openCallback = {
                c.resume(Unit)
                openCallback = null
                this.enterTransition = null
                scope.launch {
                    stateOpenEnd()
                }
            }
            scope.launch {
                stateOpenStart()
                animateContainerState = MutableTransitionState(false)
                uiAlpha.snapTo(1F)
                viewerContainerAlpha.snapTo(1F)
                ticket.awaitNextTicket()
                animateContainerState.targetState = true
                // 可能要跳两次才行，否则会闪退
                ticket.awaitNextTicket()
                ticket.awaitNextTicket()
                pagerState.scrollToPage(index)
                launch {
                    awaitViewerLoading()
                }
            }
        }

    private var closeCallback: (() -> Unit)? = null

    internal var exitTransition: ExitTransition? = null

    suspend fun close(exitTransition: ExitTransition? = null) = suspendCoroutine<Unit> { c ->
        this.exitTransition = exitTransition
        closeCallback = {
            c.resume(Unit)
            closeCallback = null
            this.exitTransition = null
            scope.launch {
                stateCloseEnd()
            }
        }
        scope.launch {
            stateCloseStart()
            cancelOpenTransform()
            // 这里创建一个全新的state是为了让exitTransition的设置得到响应
            animateContainerState = MutableTransitionState(true)
            animateContainerState.targetState = false
            ticket.awaitNextTicket()
            transformState.setExitState()
        }
    }

    private var openTransformJob: Deferred<Unit>? = null

    suspend fun openTransform(
        index: Int,
        itemState: TransformItemState,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        openTransformJob = scope.async {
            stateOpenStart()
            val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
            animateContainerState = MutableTransitionState(true)
            transformSnapToViewer(false)
            uiAlpha.snapTo(0F)
            ticket.awaitNextTicket()
            pagerState.scrollToPage(index)
            listOf(
                scope.async {
                    transformState.enterTransform(itemState, animationSpec = currentAnimationSpec)
                },
                scope.async {
                    uiAlpha.animateTo(1F, animationSpec = currentAnimationSpec)
                }
            ).awaitAll()

            // 执行完成后的回调
            stateOpenEnd()

            // 等待viewer加载
            awaitViewerLoading()
            transformSnapToViewer(true)
        }
        openTransformJob?.await()
    }

    private fun cancelOpenTransform() {
        openTransformJob?.cancel()
        viewerLoading = false
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
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        stateCloseStart()
        cancelOpenTransform()
        val itemState = findTransformItem(key)
        if (itemState != null) {
            if (viewerContainerVisible) copyViewerPosToContent(itemState)
            ticket.awaitNextTicket()
            listOf(
                scope.async {
                    transformState.exitTransform(animationSpec = currentAnimationSpec)
                    transformContentAlpha.snapTo(0F)
                },
                scope.async {
                    uiAlpha.animateTo(0F, animationSpec = currentAnimationSpec)
                }
            ).awaitAll()
            ticket.awaitNextTicket()
            animateContainerState = MutableTransitionState(false)
        } else {
            transformState.setExitState()
            animateContainerState.targetState = false
        }

        // 执行完成后的回调
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(state.transformContentAlpha.value)
                                ) {
                                    TransformContentView(state.transformState)
                                }
                                /**
                                 * TODO: 后续再考虑如何设计loading
                                 */
                                AnimatedVisibility(
                                    visible = state.viewerLoading,
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(state.viewerContainerAlpha.value)
                                ) {
                                    it()
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