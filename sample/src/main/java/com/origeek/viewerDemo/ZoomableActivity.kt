package com.origeek.viewerDemo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.origeek.imageViewer.gallery.ImageGallery01
import com.origeek.imageViewer.gallery.rememberImageGalleryState01
import com.origeek.imageViewer.previewer.ImagePreviewer01
import com.origeek.imageViewer.previewer.ImagePreviewerState01
import com.origeek.imageViewer.previewer.ImageTransformPreviewer01
import com.origeek.imageViewer.previewer.ImageTransformPreviewerState01
import com.origeek.imageViewer.previewer.ImageVerticalPreviewerState01
import com.origeek.imageViewer.previewer.TransformItemView
import com.origeek.imageViewer.previewer.TransformLayerScope01
import com.origeek.imageViewer.previewer.VerticalDragType
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.imageViewer.viewer.ImageCanvas01
import com.origeek.imageViewer.viewer.getViewPort
import com.origeek.imageViewer.zoomable.ZoomableGestureScope
import com.origeek.imageViewer.zoomable.ZoomableView
import com.origeek.imageViewer.zoomable.rememberZoomableState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.component.rememberDecoderImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable

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
//            ZoomablePreviewerBody()
//            ZoomableTransformPreviewerBody()
            ZoomableVertical()
        }
    }

}

@Composable
fun ZoomableVertical() {
    val scope = rememberCoroutineScope()
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    val galleryState = rememberImageGalleryState01 { images.size }
    val verticalPreviewerState =
        remember {
            ImageVerticalPreviewerState01(
                scope = scope,
                getKey = { images[it] },
                verticalDragType = VerticalDragType.Down,
                galleryState = galleryState
            )
        }

    verticalPreviewerState.apply {
        if (visible) {
            if (galleryState.zoomableViewState != null) {
                if (galleryState.zoomableViewState?.scale?.value != 1F) {
                    BackHandler {
                        scope.launch {
                            galleryState.zoomableViewState?.reset()
                        }
                    }
                } else {
                    BackHandler {
                        scope.launch {
                            close()
                        }
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(7F))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    images.forEachIndexed { index, image ->
                        val painter = rememberCoilImagePainter(image)
                        val itemState = rememberTransformItemState()
                        LaunchedEffect(painter.intrinsicSize) {
                            itemState.intrinsicSize = painter.intrinsicSize
                        }
                        TransformItemView(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    scope.launch {
//                                        open(index)
                                        enterTransform(index)
                                    }
                                },
                            key = getKey(index),
                            itemState = itemState,
                            itemVisible = (!itemContentVisible.value)
                                    || if (enterIndex.value != null) enterIndex.value != index else currentPage != index,
                            content = {
                                Image(
                                    painter = painter,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(3F))
            }

            ImageTransformPreviewer01(
                state = verticalPreviewerState,
                previewerLayer = TransformLayerScope01(
                    previewerDecoration = { innerBox ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(getKey) {
                                    verticalDrag(this)
                                }
                                .graphicsLayer {
                                    translationX = verticalDragTransformState.offsetX.value
                                    translationY = verticalDragTransformState.offsetY.value
                                    scaleX = verticalDragTransformState.scale.value
                                    scaleY = verticalDragTransformState.scale.value
                                }
                        ) {
                            innerBox()
                        }
                    },
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(0.8F))
                        )
                    }
                ),
                zoomablePolicy = { page ->
                    val image = images[page]
                    val painter = rememberCoilImagePainter(image)
                    val mounted = remember { mutableStateOf(false) }
                    LaunchedEffect(painter.intrinsicSize.isSpecified) {
                        if (painter.intrinsicSize.isSpecified) {
//                            delay(4000)
                            mounted.value = true
                        }
                    }
                    if (mounted.value) {
                        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painter,
                                contentDescription = null,
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

//                    painter.intrinsicSize.isSpecified
                    mounted.value
                }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(onClick = {
                    scope.launch {
                        exitTransform()
//                        close()
                    }
                }) {
                    Text(text = "复位-$visibleTarget")
                }
            }
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
    val previewerState =
        remember { ImagePreviewerState01(scope = scope, galleryState = galleryState) }

    previewerState.apply {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(7F))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    images.forEachIndexed { index, image ->
                        val painter = rememberCoilImagePainter(image)
                        val itemState = rememberTransformItemState()
                        LaunchedEffect(painter.intrinsicSize) {
                            itemState.intrinsicSize = painter.intrinsicSize
                        }
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    scope.launch {
                                        open(index)
                                    }
                                },
                        ) {
                            Image(
                                painter = painter,
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(3F))
            }

            ImagePreviewer01(
                state = previewerState,
                previewerDecoration = { innerBox ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.8F))
                    ) {
                        innerBox()
                    }
                },
                zoomablePolicy = { page ->
                    val image = images[page]
                    val painter = rememberCoilImagePainter(image)
                    val mounted = remember { mutableStateOf(false) }
                    val isSpecified = painter.intrinsicSize.isSpecified
                    LaunchedEffect(isSpecified) {
                        if (isSpecified) {
                            delay(2000)
                            mounted.value = true
                        }
                    }
                    AnimatedVisibility(
                        modifier = Modifier.fillMaxSize(),
                        visible = mounted.value,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painter,
                                contentDescription = null,
                            )
                        }
                    }
                    if (!mounted.value) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                },
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(onClick = {
                    scope.launch {
                        close()
                    }
                }) {
                    Text(text = "复位")
                }
            }
        }
    }
}

@Composable
fun ZoomableTransformPreviewerBody() {
    val scope = rememberCoroutineScope()
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    val galleryState = rememberImageGalleryState01 { images.size }
//    val previewerState = remember { ImagePreviewerState01(galleryState = galleryState) }
    val transformPreviewerState =
        remember {
            ImageTransformPreviewerState01(
                scope = scope,
                getKey = { images[it] },
                galleryState = galleryState
            )
        }

    transformPreviewerState.apply {
        if (visible) {
            if (galleryState.zoomableViewState != null) {
                if (galleryState.zoomableViewState?.scale?.value != 1F) {
                    BackHandler {
                        scope.launch {
                            galleryState.zoomableViewState?.reset()
                        }
                    }
                } else {
                    BackHandler {
                        scope.launch {
                            close()
                        }
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(7F))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    images.forEachIndexed { index, image ->
                        val painter = rememberCoilImagePainter(image)
                        val itemState = rememberTransformItemState()
                        LaunchedEffect(painter.intrinsicSize) {
                            itemState.intrinsicSize = painter.intrinsicSize
                        }
                        TransformItemView(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    scope.launch {
//                                        open(index)
                                        enterTransform(index)
                                    }
                                },
                            key = getKey(index),
                            itemState = itemState,
                            itemVisible = (!itemContentVisible.value)
                                    || if (enterIndex.value != null) enterIndex.value != index else currentPage != index,
                            content = {
                                Image(
                                    painter = painter,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(3F))
            }

            ImageTransformPreviewer01(
                state = transformPreviewerState,
                previewerLayer = TransformLayerScope01(
                    previewerDecoration = { innerBox ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(0.8F))
                        ) {
                            innerBox()
                        }
                    }
                ),
                zoomablePolicy = { page ->
                    val image = images[page]
                    val painter = rememberCoilImagePainter(image)
                    val mounted = remember { mutableStateOf(false) }
                    LaunchedEffect(painter.intrinsicSize.isSpecified) {
                        if (painter.intrinsicSize.isSpecified) {
                            delay(2000)
                            mounted.value = true
                        }
                    }
                    if (mounted.value) {
                        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painter,
                                contentDescription = null,
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

//                    painter.intrinsicSize.isSpecified
                    mounted.value
                }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(onClick = {
                    scope.launch {
                        exitTransform()
//                        close()
                    }
                }) {
                    Text(text = "复位-$visibleTarget")
                }
            }
        }
    }
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