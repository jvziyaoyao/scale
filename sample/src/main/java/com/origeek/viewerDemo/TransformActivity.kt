package com.origeek.viewerDemo

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.origeek.imageViewer.*
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.GridLayout
import com.origeek.viewerDemo.ui.component.LazyGridLayout
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
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
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    var imageViewerState by remember { mutableStateOf<ImageViewerState?>(null) }
    val imageViewerVisible = remember { Animatable(0F) }
    val animationSpec = tween<Float>(durationMillis = 400)
    val transformContentState = rememberTransformContentState(animationSpec = animationSpec)
    var outOfBound by remember { mutableStateOf(false) }
    var galleryVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(true) }
    val lineCount = 3

    Box(
        modifier = Modifier.fillMaxSize()
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
                        if (!transformContentState.onAction) {
                            outOfBound = false
                            contentVisible = true
                            galleryVisible = true
                            scope.launch {
                                imageViewerVisible.snapTo(0F)
                            }
                            scope.launch {
                                transformContentState.startAsync(itemState)
                                pagerState.scrollToPage(index)
                                imageViewerVisible.animateTo(1F, animationSpec = animationSpec)
                                contentVisible = false
                            }
                        }
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
        if (imageViewerState == null) return@BackHandler
        scope.launch {
            val index = pagerState.currentPage
            val item = itemList[index]
            val id = item.id
            val itemState = transformItemStateMap[id]
            if (itemState != null) {
                contentVisible = true
                transformContentState.itemState = itemState
                imageViewerVisible.snapTo(0F)
                val scale = imageViewerState!!.scale
                val offsetX = imageViewerState!!.offsetX
                val offsetY = imageViewerState!!.offsetY
                val rw = transformContentState.fitSize.width * scale.value
                val rh = transformContentState.fitSize.height * scale.value
                val goOffsetX =
                    (transformContentState.containerSize.width - rw).div(2) + offsetX.value
                val goOffsetY =
                    (transformContentState.containerSize.height - rh).div(2) + offsetY.value
                val fixScale = transformContentState.fitScale * scale.value
                transformContentState.graphicScaleX.snapTo(fixScale)
                transformContentState.graphicScaleY.snapTo(fixScale)
                transformContentState.displayWidth.snapTo(transformContentState.displayRatioSize.width)
                transformContentState.displayHeight.snapTo(transformContentState.displayRatioSize.height)
                transformContentState.offsetX.snapTo(goOffsetX)
                transformContentState.offsetY.snapTo(goOffsetY)
                transformContentState.exitTransform()
            } else {
                transformContentState.onActionTarget = null
                transformContentState.onAction = false
                outOfBound = true
            }
            imageViewerState!!.resetImmediately()
            delay(20)
            contentVisible = false
            galleryVisible = false
        }
    }
    if (contentVisible) TransformContentView(transformContentState)
    val galleryItems = remember {
        movableContentOf {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageViewerVisible.value)
            ) {
                ImageGallery(
                    modifier = Modifier.fillMaxSize(),
                    count = images.size,
                    state = pagerState,
                    imageLoader = { index ->
                        val image = images[index].res
                        rememberCoilImagePainter(image = image)
                    },
                    currentViewerState = {
                        imageViewerState = it
                    },
                )
            }
        }
    }
    if (outOfBound) {
        var b01 by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            b01 = false
        }
        AnimatedVisibility(modifier = Modifier.fillMaxSize(), visible = b01) {
            galleryItems()
        }
    } else {
        if (galleryVisible) galleryItems()
    }
}

class PreviewerState @OptIn(ExperimentalPagerApi::class) constructor(
    val pagerState: PagerState,
    val contentState: TransformContentState,
) {

    var imageViewerState by mutableStateOf<ImageViewerState?>(null)

    val imageViewerVisible = Animatable(0F)

    var outOfBound by mutableStateOf(false)

    var contentVisible by mutableStateOf(false)

    var galleryVisible by mutableStateOf(false)

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun rememberPreviewerState(
    pagerState: PagerState = rememberPagerState(),
    contentState: TransformContentState = rememberTransformContentState(),
): PreviewerState {
    return remember {
        PreviewerState(pagerState = pagerState, contentState = contentState)
    }
}

@Composable
fun TransformImagePreviewer(
    modifier: Modifier = Modifier,
    count: Int,
    state: PreviewerState = rememberPreviewerState(),
    imageLoader: @Composable (Int) -> Any,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    background: @Composable ((Int) -> Unit) = {},
    foreground: @Composable ((Int) -> Unit) = {},
) {
    if (state.contentVisible) TransformContentView(state.contentState)
    val galleryItems = remember {
        movableContentOf {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(state.imageViewerVisible.value)
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
    if (state.outOfBound) {
        var b01 by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            b01 = false
        }
        AnimatedVisibility(modifier = Modifier.fillMaxSize(), visible = b01) {
            galleryItems()
        }
    } else {
        if (state.galleryVisible) galleryItems()
    }
}