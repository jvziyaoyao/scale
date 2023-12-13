package com.origeek.viewerDemo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.origeek.imageViewer.gallery.ImageGallery01
import com.origeek.imageViewer.gallery.rememberImageGalleryState01
import com.origeek.imageViewer.previewer.ImagePreviewer01
import com.origeek.imageViewer.previewer.ImagePreviewerState01
import com.origeek.imageViewer.previewer.TransformItemView
import com.origeek.imageViewer.previewer.rememberTransformContentState
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.imageViewer.viewer.ImageCanvas01
import com.origeek.imageViewer.viewer.getViewPort
import com.origeek.imageViewer.zoomable.ZoomableGestureScope
import com.origeek.imageViewer.zoomable.ZoomableView
import com.origeek.imageViewer.zoomable.rememberZoomableState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.component.rememberDecoderImagePainter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-11-24 16:24
 **/
class ZoomableActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
//            ZoomableBody()
//            ZoomableCanvasBody()
//            ZoomablePagerBody()
//            ZoomableThirdBody()
//            ZoomableTransformBody()
            ZoomablePreviewerBody()
        }
    }

}

@Composable
fun ZoomablePreviewerBody() {
    val scope = rememberCoroutineScope()
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    val galleryState = rememberImageGalleryState01 { images.size }
    val previewerState = remember { ImagePreviewerState01(galleryState = galleryState) }

    galleryState.zoomableViewState?.apply {
        if (scale.value != 1F) {
            BackHandler {
                scope.launch {
                    reset()
                }
            }
        } else if (previewerState.visible) {
            BackHandler {
                scope.launch {
                    previewerState.close()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            images.forEachIndexed { index, image ->
                val painter = rememberCoilImagePainter(image)
                Image(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            scope.launch {
                                previewerState.open(index)
                            }
                        },
                    painter = painter,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        }

        ImagePreviewer01(state = previewerState) { page ->
            val image = images[page]
            val painter = rememberCoilImagePainter(image)
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun ZoomableTransformBody() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val painter = rememberCoilImagePainter(R.drawable.light_01)
        val intrinsicSize = painter.intrinsicSize
        val position = remember { mutableStateOf(Offset(0F, 0F)) }
        val size = remember { mutableStateOf(IntSize.Zero) }

        val viewerMounted = remember { MutableStateFlow(false) }
        suspend fun onViewerUnmounted() {
            viewerMounted.emit(false)
        }

        suspend fun onViewerMounted() {
            viewerMounted.emit(true)
        }

        suspend fun awaitMounted() = suspendCoroutine<Unit> { c ->
            var notConsumed = true
            scope.launch {
                viewerMounted
                    .takeWhile { notConsumed }
                    .collectLatest {
                        if (it) {
                            notConsumed = false
                            c.resume(Unit)
                        }
                    }
            }
        }

        val itemContentVisible = remember { mutableStateOf(false) }
        val imageViewerVisible = remember { mutableStateOf(false) }

        val displayWidth = remember { Animatable(0F) }
        val displayHeight = remember { Animatable(0F) }
        val displayOffsetX = remember { Animatable(0F) }
        val displayOffsetY = remember { Animatable(0F) }

        @Composable
        fun item() {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        suspend fun enterTransform() {
            // 设置动画开始的位置
            displayWidth.snapTo(size.value.width.toFloat())
            displayHeight.snapTo(size.value.height.toFloat())
            displayOffsetX.snapTo(position.value.x)
            displayOffsetY.snapTo(position.value.y)
            itemContentVisible.value = true

            // 设置动画结束的位置
            val containerSize = Size(
                width = density.run { maxWidth.toPx() },
                height = density.run { maxHeight.toPx() }
            )
            val displaySize = getDisplaySize(intrinsicSize, containerSize)
            val targetX = (containerSize.width - displaySize.width).div(2)
            val targetY = (containerSize.height - displaySize.height).div(2)
            val animationSpec = tween<Float>(1000)
            listOf(
                scope.async {
                    displayWidth.animateTo(displaySize.width, animationSpec)
                },
                scope.async {
                    displayHeight.animateTo(displaySize.height, animationSpec)
                },
                scope.async {
                    displayOffsetX.animateTo(targetX, animationSpec)
                },
                scope.async {
                    displayOffsetY.animateTo(targetY, animationSpec)
                },
            ).awaitAll()

            // 开启viewer图层
            imageViewerVisible.value = true
            // 等待挂载成功
            awaitMounted()
            // 动画结束，开启预览
            itemContentVisible.value = false
        }

        suspend fun exitTransform() {
            // 同步动画开始的位置
            val containerSize = Size(
                width = density.run { maxWidth.toPx() },
                height = density.run { maxHeight.toPx() }
            )
            val displaySize = getDisplaySize(intrinsicSize, containerSize)
            val targetX = (containerSize.width - displaySize.width).div(2)
            val targetY = (containerSize.height - displaySize.height).div(2)
            displayWidth.snapTo(displaySize.width)
            displayHeight.snapTo(displaySize.height)
            displayOffsetX.snapTo(targetX)
            displayOffsetY.snapTo(targetY)

            // 动画结束，开启预览
            itemContentVisible.value = true
            // 开启viewer图层
            imageViewerVisible.value = false

            // 运动到原来位置
            val animationSpec = tween<Float>(1000)
            listOf(
                scope.async {
                    displayWidth.animateTo(size.value.width.toFloat(), animationSpec)
                },
                scope.async {
                    displayHeight.animateTo(size.value.height.toFloat(), animationSpec)
                },
                scope.async {
                    displayOffsetX.animateTo(position.value.x, animationSpec)
                },
                scope.async {
                    displayOffsetY.animateTo(position.value.y, animationSpec)
                },
            ).awaitAll()

            // 关闭图层
            itemContentVisible.value = false
        }

        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 200.dp)
                .size(160.dp)
                .clickable {
                    scope.launch {
                        enterTransform()
                    }
                }
        ) {
            // 这个是中间的内容的容器
            Box(
                modifier = Modifier
                    .onGloballyPositioned {
                        position.value = it.positionInRoot()
                        size.value = it.size
                    }
                    .fillMaxSize()
            ) {
                if (!itemContentVisible.value && !imageViewerVisible.value) {
                    item()
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (itemContentVisible.value) {
                Box(
                    modifier = Modifier
                        .size(
                            width = density.run { displayWidth.value.toDp() },
                            height = density.run { displayHeight.value.toDp() }
                        )
                        .offset(
                            x = density.run { displayOffsetX.value.toDp() },
                            y = density.run { displayOffsetY.value.toDp() },
                        )
                        .background(Color.Red.copy(0.2F))
                ) {
                    item()
                    Text(text = "transform")
                }
            }
            if (imageViewerVisible.value) {
                val zoomableState = rememberZoomableState(contentSize = painter.intrinsicSize)
                val isMounted = viewerMounted.collectAsState()
                LaunchedEffect(Unit) {
                    onViewerUnmounted()
                }
                LaunchedEffect(painter.intrinsicSize.isSpecified, isMounted.value) {
                    if (painter.intrinsicSize.isSpecified) {
                        if (isMounted.value) {
                            onViewerMounted()
                        }
                    }
                }
                ZoomableView(
                    state = zoomableState,
                    detectGesture = ZoomableGestureScope(
                        onDoubleTap = {
                            scope.launch {
                                zoomableState.toggleScale(it)
                            }
                        }
                    )
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painter,
                        contentDescription = null,
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = {
                scope.launch {
                    exitTransform()
                }
            }) {
                Text(text = "复位")
            }
        }
    }
}

fun getDisplaySize(contentSize: Size, containerSize: Size): Size {
    val containerRatio = containerSize.run {
        width.div(height)
    }
    val contentRatio = contentSize.run {
        width.div(height)
    }
    val widthFixed = contentRatio > containerRatio
    val scale1x = if (widthFixed) {
        containerSize.width.div(contentSize.width)
    } else {
        containerSize.height.div(contentSize.height)
    }
    return Size(
        width = contentSize.width.times(scale1x),
        height = contentSize.height.times(scale1x),
    )
}

@Composable
fun ZoomableCanvasBody() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val inputStream = remember { context.assets.open("a350.jpg") }
    val imageDecoder = rememberDecoderImagePainter(inputStream = inputStream)
    val zoomableState = rememberZoomableState(
        contentSize = if (imageDecoder == null) Size.Zero else Size(
            width = imageDecoder.decoderWidth.toFloat(),
            height = imageDecoder.decoderHeight.toFloat()
        )
    )
    Box(
        modifier = Modifier.padding(
            horizontal = 60.dp,
            vertical = 120.dp,
        )
    ) {
        ZoomableView(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue.copy(0.2F)),
            state = zoomableState,
            boundClip = false,
            detectGesture = ZoomableGestureScope(
                onTap = {

                },
                onDoubleTap = {
                    scope.launch {
                        zoomableState.toggleScale(it)
                    }
                },
                onLongPress = {

                },
            ),
        ) {
            if (imageDecoder != null) {
                val viewPort = zoomableState.getViewPort()
                ImageCanvas01(
                    imageDecoder = imageDecoder,
                    viewPort = viewPort,
                )
            }
        }
    }
}

@Composable
fun ZoomablePagerBody() {
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    val galleryState = rememberImageGalleryState01 { images.size }
    ImageGallery01(state = galleryState) { page ->
        val image = images[page]
        val painter = rememberCoilImagePainter(image)
        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun ZoomableBody() {
    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//    val density = LocalDensity.current
//    val painter = painterResource(id = R.drawable.img_01)
//    val zoomableState =
//        rememberZoomableState(contentSize = painter.intrinsicSize)
//    val canvasDp = 400.dp
//    val canvasSize =
//        Size(width = density.run { canvasDp.toPx() }, height = density.run { canvasDp.toPx() })

//    val bitmap = remember {
//        BitmapFactory.decodeResource(context.resources, R.drawable.img_01).asImageBitmap()
//    }
//    val contentSize = remember {
//        Size(
//            width = bitmap.width.toFloat(),
//            height = bitmap.height.toFloat()
//        )
//    }
//    val zoomableState =
//        rememberZoomableState(contentSize = contentSize)

    val url = "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF"
    val painter = rememberCoilImagePainter(url)
    val zoomableState = rememberZoomableState(contentSize = painter.intrinsicSize)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        zoomableState.apply {
            ZoomableView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue.copy(0.2F)),
                state = zoomableState,
                boundClip = false,
                detectGesture = ZoomableGestureScope(
                    onTap = {

                    },
                    onDoubleTap = {
                        scope.launch {
                            zoomableState.toggleScale(it)
                        }
                    },
                    onLongPress = {

                    },
                ),
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                )
//                Image(
//                    modifier = Modifier.fillMaxSize(),
//                    painter = painter,
//                    contentDescription = null,
//                )
//                Canvas(modifier = Modifier.fillMaxSize()) {
//                    drawImage(bitmap)
//                }
//                Canvas(
//                    modifier = Modifier
//                        .fillMaxSize()
//                ) {
//                    drawImage(
//                        image = bitmap,
//                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
//                    )
//                }
            }
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = gestureCenter.value.x - 6.dp.toPx()
                        translationY = gestureCenter.value.y - 6.dp.toPx()
                    }
                    .clip(CircleShape)
                    .background(Color.Red.copy(0.4f))
                    .size(12.dp)
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Cyan)
                    .size(12.dp)
                    .align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Yellow.copy(0.2F))
        )
    }
}

@Composable
fun ZoomableThirdBody() {
//    val painter = painterResource(id = R.drawable.img_01)

    val url = "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF"

    val painter = rememberAsyncImagePainter(model = url)
    val zoomableState = rememberZoomState(contentSize = painter.intrinsicSize, maxScale = 20F)
    Image(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue.copy(0.2F))
            .zoomable(
                zoomState = zoomableState,
                onDoubleTap = {
                    zoomableState.toggleScale(20F, it, tween(1000))
                },
            ),
        painter = painter,
        contentDescription = null,
    )
}