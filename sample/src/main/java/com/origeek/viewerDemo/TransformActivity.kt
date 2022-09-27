package com.origeek.viewerDemo

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.origeek.imageViewer.*
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.LazyGridLayout
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-09-21 18:20
 **/
class TransformActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBasicContent {
            ViewerDemoTheme {
                TransformBody()
            }
        }
    }

}

data class DrawableItem(
    val id: String,
    val res: Int,
)

@JvmName("getItemList1")
fun getItemList(): List<DrawableItem> {
    val srcList = listOf(
        R.drawable.img_01,
        R.drawable.img_02,
        R.drawable.img_03,
        R.drawable.img_04,
        R.drawable.img_05,
        R.drawable.img_06,
    )
    val resList = mutableListOf<Int>()
    for (i in 0..10) {
        resList.addAll(srcList)
    }
    return resList.stream().map {
        DrawableItem(
            id = UUID.randomUUID().toString(),
            res = it
        )
    }.collect(Collectors.toList())
}

val itemList = getItemList()

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TransformBody() {
    val images = remember { itemList }
    val animationSpec = tween<Float>(durationMillis = 1400)
    val transformContentState = rememberTransformContentState(animationSpec = animationSpec)
    val previewerState =
        rememberPreviewerState(contentState = transformContentState)
    val lineCount = 3

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyGridLayout(
            modifier = Modifier.fillMaxSize(),
            columns = lineCount,
            size = images.size,
            padding = 2.dp,
        ) { index ->
            val item = itemList[index]
            val painter = painterResource(id = item.res)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1F)
            ) {
                val itemState = rememberTransformItemState()
                TransformImageView(
                    modifier = Modifier.clickable {
//                        previewerState.openTransform(
//                            index = index,
//                            itemState = itemState,
//                        )
                        previewerState.open(index)
                    },
                    painter = painter,
                    itemState = itemState,
                    key = item.id,
                    contentState = transformContentState,
                )
            }
        }
    }

    BackHandler {
//        previewerState.close()
        previewerState.closeTransform()
    }

    TransformImagePreviewer(
        modifier = Modifier.fillMaxSize(),
        count = images.size,
        state = previewerState,
        imageLoader = { index ->
            val image = images[index].res
            rememberCoilImagePainter(image = image)
        },
        currentViewerState = {},
        enter = fadeIn(tween(1200)),
        exit = fadeOut(tween(1200)),
    )
}

class PreviewerState @OptIn(ExperimentalPagerApi::class) constructor(
    val pagerState: PagerState,
    val contentState: TransformContentState,
    private val scope: CoroutineScope,
) {

    var transformEnable by mutableStateOf(false)

    internal var imageViewerState by mutableStateOf<ImageViewerState?>(null)

    internal val imageViewerVisible = Animatable(0F)

    internal var outOfBound by mutableStateOf(false)

    internal var contentVisible by mutableStateOf(false)

    internal var galleryVisible by mutableStateOf(false)

    @OptIn(ExperimentalPagerApi::class)
    fun open(index: Int = 0) {
        transformEnable = false
        scope.launch {
            galleryVisible = false
            delay(20)
            galleryVisible = true
            delay(20)
            pagerState.scrollToPage(index)

            outOfBound = false
            contentVisible = false
            imageViewerVisible.snapTo(1F)
        }
    }

    fun close() {
        scope.launch {
            contentVisible = false
            contentState.onActionTarget = null
            contentState.onAction = false

            transformEnable = false
            galleryVisible = true
            delay(20)
            galleryVisible = false
        }

    }

    @OptIn(ExperimentalPagerApi::class)
    fun openTransform(
        index: Int,
        itemState: TransformItemState,
    ) {
        transformEnable = true
        outOfBound = false
        contentVisible = true
        galleryVisible = true
        scope.launch {
            imageViewerVisible.snapTo(0F)
        }
        scope.launch {
            contentState.startAsync(itemState)
            pagerState.scrollToPage(index)
            imageViewerVisible.animateTo(1F)
            contentVisible = false
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    fun closeTransform() {
        transformEnable = true
        contentState.onAction = true
        scope.launch {
            val index = pagerState.currentPage
            val item = itemList[index]
            val id = item.id
            val itemState = transformItemStateMap[id]
            if (itemState != null) {
                contentVisible = true
                contentState.itemState = itemState
                contentState.containerSize = imageViewerState!!.containerSize
                val scale = imageViewerState!!.scale
                val offsetX = imageViewerState!!.offsetX
                val offsetY = imageViewerState!!.offsetY
                val rw = contentState.fitSize.width * scale.value
                val rh = contentState.fitSize.height * scale.value
                val goOffsetX =
                    (contentState.containerSize.width - rw).div(2) + offsetX.value
                val goOffsetY =
                    (contentState.containerSize.height - rh).div(2) + offsetY.value
                val fixScale = contentState.fitScale * scale.value
                contentState.graphicScaleX.snapTo(fixScale)
                contentState.graphicScaleY.snapTo(fixScale)
                contentState.displayWidth.snapTo(contentState.displayRatioSize.width)
                contentState.displayHeight.snapTo(contentState.displayRatioSize.height)
                contentState.offsetX.snapTo(goOffsetX)
                contentState.offsetY.snapTo(goOffsetY)
                imageViewerVisible.snapTo(0F)
                contentState.exitTransform()
            } else {
                outOfBound = true
            }
            contentState.onActionTarget = null
            contentState.onAction = false
            imageViewerState!!.resetImmediately()
            delay(20)
            contentVisible = false
            galleryVisible = false
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun rememberPreviewerState(
    pagerState: PagerState = rememberPagerState(),
    contentState: TransformContentState = rememberTransformContentState(),
    scope: CoroutineScope = rememberCoroutineScope(),
): PreviewerState {
    return remember {
        PreviewerState(
            pagerState = pagerState,
            contentState = contentState,
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
fun TransformImagePreviewer(
    modifier: Modifier = Modifier,
    count: Int,
    state: PreviewerState = rememberPreviewerState(),
    imageLoader: @Composable (Int) -> Any,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    background: @Composable ((Int) -> Unit) = {},
    foreground: @Composable ((Int) -> Unit) = {},
) {
    if (state.contentVisible && state.transformEnable) TransformContentView(state.contentState)
    val galleryItems = remember {
        movableContentOf {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (state.transformEnable) state.imageViewerVisible.value else 1F)
            ) {
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
                    background = background,
                    foreground = foreground,
                )
            }
        }
    }
    if (state.transformEnable) {
        if (state.outOfBound) {
            var b01 by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                b01 = false
            }
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = b01,
                enter = enter,
                exit = exit
            ) {
                galleryItems()
            }
        } else {
            if (state.galleryVisible) galleryItems()
        }
    } else {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = state.galleryVisible,
            enter = enter,
            exit = exit
        ) {
            galleryItems()
        }
    }

}