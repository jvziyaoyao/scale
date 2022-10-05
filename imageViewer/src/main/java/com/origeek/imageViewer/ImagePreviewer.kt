package com.origeek.imageViewer

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

val DEEP_DARK_FANTASY = Color(0xFF000000)
val DEFAULT_ITEM_SPACE = 12.dp

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

    @OptIn(ExperimentalPagerApi::class)
    lateinit var pagerState: PagerState

    lateinit var transformState: TransformContentState

    private var mutex = Mutex()

    internal val ticket = Ticket()

    internal var getKey: ((Int) -> Any)? = null

    internal var defaultAnimationSpec: AnimationSpec<Float> = SpringSpec()

    internal var imageViewerState by mutableStateOf<ImageViewerState?>(null)

    internal var animateContainerState by mutableStateOf(MutableTransitionState(false))

    internal var uiAlpha = Animatable(1F)

    internal var transformContentAlpha = Animatable(1F)

    internal var viewerContainerAlpha = Animatable(1F)

    var animating by mutableStateOf(false)

    var visible by mutableStateOf(false)
        internal set

    var visibleTarget by mutableStateOf<Boolean?>(null)

    val canOpen: Boolean
        get() = !visible && visibleTarget == null && !animating

    val canClose: Boolean
        get() = visible && visibleTarget == null && !animating

    @OptIn(ExperimentalPagerApi::class)
    val currentPage: Int
        get() = pagerState.currentPage

    @OptIn(ExperimentalPagerApi::class)
    val targetPage: Int
        get() = pagerState.targetPage

    @OptIn(ExperimentalPagerApi::class)
    val pageCount: Int
        get() = pagerState.pageCount

    @OptIn(ExperimentalPagerApi::class)
    val currentPageOffset: Float
        get() = pagerState.currentPageOffset

    @OptIn(ExperimentalPagerApi::class)
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

    private fun findTransformItem(key: Any) = transformItemStateMap[key]

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

    internal suspend fun verticalDrag(pointerInputScope: PointerInputScope) {
        pointerInputScope.apply {
            var vStartOffset by mutableStateOf<Offset?>(null)
            var vOrientationDown by mutableStateOf<Boolean?>(null)
            if (getKey != null) detectVerticalDragGestures(
                onDragStart = {
                    var transformItemState: TransformItemState? = null
                    getKey?.apply {
                        findTransformItem(invoke(currentPage))?.apply {
                            transformItemState = this
                        }
                    }
                    transformState.itemState = transformItemState
                    if (imageViewerState?.scale?.value == 1F) {
                        vStartOffset = it
                        imageViewerState?.allowGestureInput = false
                    }
                },
                onDragEnd = {
                    vStartOffset = null
                    vOrientationDown = null
                    imageViewerState?.apply {
                        allowGestureInput = true
                        if (scale.value < 0.8F && getKey != null) {
                            scope.launch {
                                val key = getKey!!(currentPage)
                                val transformItem = findTransformItem(key)
                                if (transformItem != null) {
                                    closeTransform(key)
                                } else {
                                    shrinkDown()
                                }
                                uiAlpha.snapTo(1F)
                            }
                        } else {
                            scope.launch {
                                uiAlpha.animateTo(1F, defaultAnimationSpec)
                            }
                            scope.launch {
                                reset(defaultAnimationSpec)
                            }
                        }
                    }
                },
                onVerticalDrag = { change, dragAmount ->
                    if (vStartOffset != null) {
                        if (vOrientationDown == null) vOrientationDown = dragAmount > 0
                        if (vOrientationDown == true) {
                            val offsetY = change.position.y - vStartOffset!!.y
                            val offsetX = change.position.x - vStartOffset!!.x
                            val containerHeight =
                                imageViewerState?.containerSize?.height
                                    ?: transformState.containerSize.height
                            val scale = (containerHeight - offsetY.absoluteValue).div(
                                containerHeight
                            )
                            scope.launch {
                                uiAlpha.snapTo(scale)
                                imageViewerState?.offsetY?.apply {
                                    snapTo(offsetY)
                                }
                                imageViewerState?.offsetX?.apply {
                                    snapTo(offsetX)
                                }
                                imageViewerState?.scale?.apply {
                                    snapTo(scale)
                                }
                            }
                        } else {
                            // 如果不是向上，就返还输入权，以免页面卡顿
                            imageViewerState?.allowGestureInput = true
                        }
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

    @OptIn(ExperimentalPagerApi::class)
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    @OptIn(ExperimentalPagerApi::class)
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

    private var openCallback: (() -> Unit)? = null

    internal var enterTransition: EnterTransition? = null

    @OptIn(ExperimentalPagerApi::class)
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
            // 这里创建一个全新的state是为了让exitTransition的设置得到响应
            animateContainerState = MutableTransitionState(true)
            animateContainerState.targetState = false
            ticket.awaitNextTicket()
            transformState.setExitState()
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    suspend fun openTransform(
        index: Int,
        itemState: TransformItemState,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        stateOpenStart()
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        animateContainerState = MutableTransitionState(true)
        viewerContainerAlpha.snapTo(0F)
        transformContentAlpha.snapTo(1F)
        uiAlpha.snapTo(0F)
        ticket.awaitNextTicket()
        pagerState.scrollToPage(index)
        listOf(
            scope.async {
                transformState.enterTransform(itemState, animationSpec = currentAnimationSpec)
                transformContentAlpha.snapTo(0F)
                viewerContainerAlpha.snapTo(1F)
            },
            scope.async {
                uiAlpha.animateTo(1F, animationSpec = currentAnimationSpec)
            }
        ).awaitAll()

        // 执行完成后的回调
        stateOpenEnd()
    }

    suspend fun closeTransform(
        key: Any,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        stateCloseStart()
        val itemState = findTransformItem(key)
        if (itemState != null) {
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
            viewerContainerAlpha.snapTo(0F)
            transformContentAlpha.snapTo(1F)
            if (uiAlpha.value == 0F) uiAlpha.snapTo(1F)
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun rememberPreviewerState(
    pagerState: PagerState = rememberPagerState(),
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
    scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(
        animationSpec = spring(
            stiffness = 4000f
        )
    )

@OptIn(ExperimentalAnimationApi::class)
val DEFAULT_PREVIEWER_EXIT_TRANSITION =
    fadeOut(animationSpec = spring(stiffness = 2000f)) + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    count: Int,
    state: ImagePreviewerState = rememberPreviewerState(),
    imageLoader: @Composable (Int) -> Any,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    viewerContainer: @Composable (viewer: @Composable () -> Unit) -> Unit = { it() },
    background: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> DefaultPreviewerBackground() },
    foreground: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> },
) {
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
                onTap = onTap,
                onDoubleTap = onDoubleTap,
                onLongPress = onLongPress,
                viewerContainer = {
                    viewerContainer {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                        }
                    }
                },
                background = {
                    UIContainer {
                        background(count, it)
                    }
                },
                foreground = {
                    UIContainer {
                        foreground(count, it)
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageGallery(
    modifier: Modifier = Modifier,
    count: Int,
    state: PagerState = rememberPagerState(),
    imageLoader: @Composable (Int) -> Any,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    viewerContainer: @Composable (viewer: @Composable () -> Unit) -> Unit = { it() },
    background: @Composable ((Int) -> Unit) = {},
    foreground: @Composable ((Int) -> Unit) = {},
) {
    require(count >= 0) { "imageCount must be >= 0" }
    val scope = rememberCoroutineScope()
    // 确保不会越界
    val currentPage by remember {
        derivedStateOf {
            if (state.currentPage >= count) {
                if (count > 0) count - 1 else 0
            } else state.currentPage
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        background(currentPage)
        HorizontalPager(
            count = count,
            state = state,
            modifier = Modifier
                .fillMaxSize(),
            itemSpacing = itemSpacing,
        ) { page ->
            val imageState = rememberViewerState()
            LaunchedEffect(key1 = currentPage) {
                if (currentPage != page) imageState.reset()
                if (currentPage == page) currentViewerState(imageState)
            }
            viewerContainer {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    key(count, page) {
                        ImageViewer(
                            model = imageLoader(page),
                            state = imageState,
                            boundClip = false,
                            onTap = {
                                onTap()
                            },
                            onDoubleTap = {
                                val consumed = onDoubleTap()
                                if (!consumed) scope.launch {
                                    imageState.toggleScale(it)
                                }
                            },
                            onLongPress = { onLongPress() },
                        )
                    }
                }
            }
        }
        foreground(currentPage)
    }
}