package com.origeek.imageViewer

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
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

class ImagePreviewerState(
    index: Int = 0,
    show: Boolean = false,
) {

    // 页面滚动模式
    enum class ScrollActionType {
        ANIMATE,
        SNAP,
        ;
    }

    // 当前页码
    var index by mutableStateOf(index)

    // 显示标识
    var show by mutableStateOf(show)

    // 页面滚动模式，如果为null就不滚动
    internal var scrollActionType by mutableStateOf<ScrollActionType?>(null)

    // recompose之后需要执行的操作
    private var nextTicket: (suspend () -> Unit)? = null

    /**
     * 显示
     */
    fun show(index: Int = 0) {
        if (index < 0) return
        this.index = index
        this.show = true
        scrollActionType = ScrollActionType.SNAP
    }

    /**
     * 滚动到指定页码
     */
    fun scrollTo(index: Int) {
        if (index < 0) return
        this.index = index
        scrollActionType = ScrollActionType.ANIMATE
    }

    /**
     * 隐藏
     */
    fun hide() {
        this.show = false
    }

    @Composable
    internal fun ConsumeTicket() {
        LaunchedEffect(key1 = nextTicket) {
            nextTicket?.invoke()
            nextTicket = null
        }
    }

    companion object {
        val SAVER: Saver<ImagePreviewerState, *> = listSaver(save = {
            listOf(it.index, it.show)
        }, restore = {
            ImagePreviewerState(
                index = it[1] as Int,
                show = it[2] as Boolean,
            )
        })
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun rememberPreviewerState(
    index: Int = 0,
    show: Boolean = false,
): ImagePreviewerState = rememberSaveable(saver = ImagePreviewerState.SAVER) {
    ImagePreviewerState(index, show)
}

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    state: ImagePreviewerState = rememberPreviewerState(),
    count: Int,
    imageLoader: @Composable (index: Int) -> Any,
    background: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> DefaultPreviewerBackground() },
    foreground: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> },
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    backHandlerEnable: Boolean = true,
    enter: EnterTransition = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            + fadeIn(animationSpec = spring(stiffness = 4000f)),
    exit: ExitTransition = fadeOut(animationSpec = spring(stiffness = 2000f))
            + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
) {
    val pagerState = rememberPagerState()
    BackHandler(state.show && backHandlerEnable) {
        state.hide()
    }
    LaunchedEffect(key1 = state.index, key2 = state.scrollActionType) {
        try {
            when (state.scrollActionType) {
                ImagePreviewerState.ScrollActionType.SNAP -> {
                    pagerState.scrollToPage(state.index)
                }
                ImagePreviewerState.ScrollActionType.ANIMATE -> {
                    pagerState.animateScrollToPage(state.index)
                }
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 在同步方法（动画）完成后，才将type设置为null
            state.scrollActionType = null
        }
    }
    LaunchedEffect(key1 = pagerState.currentPage, key2 = state.scrollActionType) {
        // 如果type不为null，currentPage处于动画过程中，currentPage是时刻变化的，所以要过滤掉这种情况
        if (pagerState.currentPage != state.index && state.scrollActionType == null) {
            state.index = pagerState.currentPage
        }
    }
    AnimatedVisibility(
        visible = state.show,
        modifier = modifier
            .fillMaxSize(),
        enter = enter,
        exit = exit,
    ) {
        ImageGallery(
            count = count,
            state = pagerState,
            imageLoader = imageLoader,
            modifier = Modifier.fillMaxSize(),
            background = {
                background(count, it)
            },
            foreground = {
                foreground(count, it)
            },
            currentViewerState = currentViewerState,
            onTap = onTap,
            onDoubleTap = onDoubleTap,
            onLongPress = onLongPress,
        )

        // 这里执行那些需要recompose之后才执行的操作
        state.ConsumeTicket()
    }
}

@OptIn(ExperimentalPagerApi::class, InternalCoroutinesApi::class)
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
                        debugMode = true
                    )
                }
            }
        }
        foreground(currentPage)
    }
}