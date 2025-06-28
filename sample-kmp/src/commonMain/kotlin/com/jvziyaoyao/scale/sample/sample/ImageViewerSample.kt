package com.jvziyaoyao.scale.sample.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.viewer.AnyComposable
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.image.viewer.ModelProcessor
import com.jvziyaoyao.scale.image.viewer.ModelProcessorPair
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
import org.jetbrains.compose.resources.painterResource
import scale.sample_kmp.generated.resources.Res
import scale.sample_kmp.generated.resources.light_02

object ImageViewerSample {

    // 基本使用
    @Composable
    fun BasicSample() {
        val painter = painterResource(Res.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ImageViewer(model = painter, state = state)
    }

    // 结合Coil使用
    @Composable
    fun CoilSample() {
        val painter = rememberAsyncImagePainter(model = Res.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ImageViewer(model = painter, state = state)
    }

    // 显示一个Composable
    @Composable
    fun ComposableSample() {
        val density = LocalDensity.current
        val rectSize = 100.dp
        val rectSizePx = density.run { rectSize.toPx() }
        val size = Size(rectSizePx, rectSizePx)
        val state = rememberZoomableState(contentSize = size)
        ImageViewer(
            state = state,
            model = AnyComposable {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Cyan)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.6F)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd)
                    )
                    Text(modifier = Modifier.align(Alignment.Center), text = "Hello Compose")
                }
            }
        )
    }

    // 处理手势事件回调
    @Composable
    fun GestureSample() {
        val painter = painterResource(Res.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ImageViewer(
            model = painter,
            state = state,
            detectGesture = ZoomableGestureScope(
                onTap = {},  // 长按事件
                onDoubleTap = {}, // 双击事件
                onLongPress = {},  // 长按事件
            )
        )
    }

    val stringProcessorPair: ModelProcessorPair = String::class to { model, _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                Text(modifier = Modifier.align(Alignment.Center), text = model as String)
            }
        }

    @Composable
    fun ProcessSample() {
        val message = "好家伙"
        val state = rememberZoomableState(contentSize = Size(100F, 100F))
        ImageViewer(
            model = message,
            state = state,
            processor = ModelProcessor(stringProcessorPair)
        )
    }

}