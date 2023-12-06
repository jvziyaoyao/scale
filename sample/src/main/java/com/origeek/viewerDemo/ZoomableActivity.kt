package com.origeek.viewerDemo

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.origeek.imageViewer.viewer.ImageCanvas01
import com.origeek.imageViewer.viewer.ImageCanvas01ViewPort
import com.origeek.imageViewer.zoomable.ZoomableGestureScope
import com.origeek.imageViewer.zoomable.ZoomableView
import com.origeek.imageViewer.zoomable.rememberZoomableState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.component.rememberDecoderImagePainter
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable
import java.lang.Float.max
import java.lang.Float.min

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
            ZoomableCanvasBody()
//            ZoomablePagerBody()
//            ZoomableThirdBody()
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
                zoomableState.apply {
//                    val left = (max(0F, realSize.width - containerWidth)).div(2) - offsetX.value
//                    val top = (max(0F, realSize.height - containerHeight)).div(2) - offsetY.value

                    val realWidth = realSize.width
                    val realHeight = realSize.height
                    val containerCenterX = containerWidth.div(2)
                    val containerCenterY = containerHeight.div(2)
                    val displayLeft = containerCenterX - realWidth.div(2)
                    val displayTop = containerCenterY - realHeight.div(2)
                    val left = displayLeft + offsetX.value
                    val top = displayTop + offsetY.value
                    val right = left + realWidth
                    val bottom = top + realHeight
                    val realRect = Rect(left, top, right, bottom)
                    val containerRect = Rect(0F, 0F, containerWidth, containerHeight)
                    val intersectRect = intersectRect(realRect, containerRect)
                    val rectInViewPort = Rect(
                        left = (intersectRect.left - realRect.left).div(realWidth),
                        top = (intersectRect.top - realRect.top).div(realHeight),
                        right = (intersectRect.right - realRect.left).div(realWidth),
                        bottom = (intersectRect.bottom - realRect.top).div(realHeight),
                    )

                    val viewPort = ImageCanvas01ViewPort(
                        size = containerSize.value,
//                        defaultSize = displaySize,
//                        realSize = realSize,
                        scale = scale.value,
                        rectInViewPort = rectInViewPort,
                    )
                    ImageCanvas01(
                        imageDecoder = imageDecoder,
                        viewPort = viewPort,
                    )
                }
            }
        }
    }
}

fun intersectRect(rect1: Rect, rect2: Rect): Rect {
    val left = max(rect1.left, rect2.left)
    val top = max(rect1.top, rect2.top)
    val right = min(rect1.right, rect2.right)
    val bottom = min(rect1.bottom, rect2.bottom)

    return if (left < right && top < bottom) {
        Rect(left, top, right, bottom)
    } else {
        Rect(0F, 0F, 0F, 0F)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerBody() {
    val scope = rememberCoroutineScope()
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    val pagerState = rememberPagerState { images.size }
    VerticalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        pageSpacing = 20.dp,
    ) { index ->
        val image = images[index]
        val painter = rememberCoilImagePainter(image)
        val zoomableState = rememberZoomableState(contentSize = painter.intrinsicSize)
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