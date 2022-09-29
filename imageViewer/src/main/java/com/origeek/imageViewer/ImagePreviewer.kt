package com.origeek.imageViewer

import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.*
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
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


class ImagePreviewerState @OptIn(ExperimentalPagerApi::class) constructor(
    val pagerState: PagerState,
    val transformState: TransformContentState,
    private val scope: CoroutineScope,
) {

    private var mutex = Mutex()

    internal var imageViewerState by mutableStateOf<ImageViewerState?>(null)

    internal val imageViewerVisible = Animatable(0F)

    internal var animateGalleryItems by mutableStateOf(false)

    internal var contentVisible by mutableStateOf(false)

    internal var galleryVisible by mutableStateOf(false)

    internal val uiAlpha = Animatable(1F)

    internal val ticket = Ticket()

    internal var getKey: ((Int) -> Any)? = null

    var animating by mutableStateOf(false)

    var visible by mutableStateOf(false)

    var visibleTarget by mutableStateOf<Boolean?>(false)

    val canOpen by derivedStateOf { !visible && visibleTarget == null }

    val canClose by derivedStateOf { visible && visibleTarget == null }

    val viewerVisibleOpenAnimateSpec: AnimationSpec<Float> = SpringSpec()

    val viewerVisibleCloseAnimateSpec: AnimationSpec<Float> = SpringSpec()

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

    internal suspend fun verticalDrag(pointerInputScope: PointerInputScope) {
        pointerInputScope.apply {
            var vStartOffset by mutableStateOf<Offset?>(null)
            var vOrientationDown by mutableStateOf<Boolean?>(null)
            if (getKey != null) detectVerticalDragGestures(
                onDragStart = {
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
                                closeTransformAsync(getKey!!(currentPage))
                                uiAlpha.snapTo(1F)
                            }
                        } else {
                            scope.launch {
                                uiAlpha.snapTo(1F)
                            }
                            scope.launch {
                                reset()
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

    suspend fun openAsync(index: Int = 0) = suspendCoroutine<Unit> { c ->
        open(index) { c.resume(Unit) }
    }

    suspend fun closeAsync() = suspendCoroutine<Unit> { c ->
        close { c.resume(Unit) }
    }

    suspend fun openTransformAsync(index: Int, itemState: TransformItemState) =
        suspendCoroutine<Unit> { c ->
            openTransform(index, itemState) { c.resume(Unit) }
        }

    suspend fun closeTransformAsync(key: Any) = suspendCoroutine<Unit> { c ->
        closeTransform(key) { c.resume(Unit) }
    }

    @OptIn(ExperimentalPagerApi::class)
    fun open(index: Int = 0, callback: () -> Unit = {}) {
        scope.launch {
            stateOpenStart()
            animateGalleryItems = true
            galleryVisible = false
            imageViewerVisible.snapTo(1F)
            ticket.awaitNextTicket()
            galleryVisible = true
            ticket.awaitNextTicket()
            delay(20)
            pagerState.scrollToPage(index)
            // 执行完成后的回调
            stateOpenEnd()
            callback()
        }
    }

    fun close(callback: () -> Unit = {}) {
        scope.launch {
            stateCloseStart()
            animateGalleryItems = true
            contentVisible = false
            transformState.onActionTarget = null
            transformState.onAction = false

            galleryVisible = true
            ticket.awaitNextTicket()
            galleryVisible = false

            // 执行完成后的回调
            stateCloseEnd()
            callback()
        }

    }

    @OptIn(ExperimentalPagerApi::class)
    fun openTransform(
        index: Int,
        itemState: TransformItemState,
        callback: () -> Unit = {},
    ) {
        scope.launch {
            stateOpenStart()
            animateGalleryItems = false
            contentVisible = true
            galleryVisible = true
            ticket.awaitNextTicket()
            imageViewerVisible.snapTo(0F)
            transformState.startAsync(itemState)
            pagerState.scrollToPage(index)
            imageViewerVisible.animateTo(
                targetValue = 1F,
                animationSpec = viewerVisibleOpenAnimateSpec
            )
            contentVisible = false

            // 执行完成后的回调
            stateOpenEnd()
            callback()
        }
    }

    fun closeTransform(key: Any, callback: () -> Unit = {}) {
        transformState.onAction = true
        scope.launch {
            stateCloseStart()
            val itemState = transformItemStateMap[key]
            if (itemState != null) {
                contentVisible = true
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
                imageViewerVisible.animateTo(
                    targetValue = 0F,
                    animationSpec = viewerVisibleCloseAnimateSpec
                )
                transformState.exitTransform()
            } else {
                animateGalleryItems = true
            }
            transformState.onActionTarget = null
            transformState.onAction = false
            imageViewerState!!.resetImmediately()
            ticket.awaitNextTicket()
            contentVisible = false
            galleryVisible = false

            // 执行完成后的回调
            stateCloseEnd()
            callback()
        }
    }

    fun enableVerticalDrag(getKey: ((Int) -> Any)) {
        this.getKey = getKey
    }

    fun disableVerticalDrag() {
        this.getKey = null
    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun rememberPreviewerState(
    pagerState: PagerState = rememberPagerState(),
    transformState: TransformContentState = rememberTransformContentState(),
    scope: CoroutineScope = rememberCoroutineScope(),
): ImagePreviewerState {
    return remember {
        ImagePreviewerState(
            pagerState = pagerState,
            transformState = transformState,
            scope = scope,
        )
    }
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
    if (state.contentVisible) TransformContentView(state.transformState)
    val galleryItems = remember {
        movableContentOf {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(state.imageViewerVisible.value)
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
                    viewerContainer = viewerContainer,
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
//                LaunchedEffect(state.imageViewerVisible.value) {
//                    state.imageViewerState?.allowGestureInput = state.imageViewerVisible.value != 1F
//                }
//                if (state.imageViewerVisible.value != 1F)
//                    Box(modifier = Modifier
//                        .fillMaxSize()
//                        .pointerInput(Unit) { detectTapGestures { } }) { }
            }
        }
    }
    if (state.animateGalleryItems) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = state.galleryVisible,
            enter = enter,
            exit = exit
        ) {
            galleryItems()
        }
    } else {
        if (state.galleryVisible) galleryItems()
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