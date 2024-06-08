package com.jvziyaoyao.scale.sample.sample

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.sample.R
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

object ZoomableViewSample {

    // 简单使用
    @Composable
    fun BasicSample() {
        val painter = painterResource(id = R.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ZoomableView(state = state) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null,
            )
        }
    }

    // 结合Coil使用
    @Composable
    fun CoilSample() {
        val painter = rememberAsyncImagePainter(model = R.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ZoomableView(state = state) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null,
            )
        }
    }

    // 显示一个Composable
    @Composable
    fun ComposableSample() {
        val density = LocalDensity.current
        val rectSize = 100.dp
        val rectSizePx = density.run { rectSize.toPx() }
        val size = Size(rectSizePx, rectSizePx)
        val state = rememberZoomableState(contentSize = size)
        ZoomableView(state = state) {
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
    }

    // 处理手势事件回调
    @Composable
    fun GestureSample() {
        val painter = painterResource(id = R.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ZoomableView(
            state = state,
            detectGesture = ZoomableGestureScope(
                // 点击事件
                onTap = { offset ->

                },
                // 双击事件
                onDoubleTap = { offset ->

                },
                // 长按事件
                onLongPress = { offset ->

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

    @Composable
    fun StateSample() {
        val scope = rememberCoroutineScope()
        val painter = painterResource(id = R.drawable.light_02)
        val state = rememberZoomableState(
            contentSize = painter.intrinsicSize,
            // 设置组件最大缩放率
            maxScale = 4F,
            // 设置组件进行动画时的动画规格
            animationSpec = tween(1200)
        )

        state.isRunning() // 获取组件是否在动画状态
        state.displaySize // 获取组件1倍显示的大小
        state.scale // 获取组件当前相对于1倍显示大小的缩放率
        state.offsetX // 获取组件的X轴位移
        state.offsetY // 获取组件的Y轴位移
        state.rotation // 获取组件旋转角度

        ZoomableView(
            state = state,
            detectGesture = ZoomableGestureScope(
                onDoubleTap = { offset ->
                    scope.launch {
                        // 在最大和最小显示倍率间切换，如果当前缩放率即不是最大值，
                        // 也不是最小值，会恢复到默认显示大小
                        state.toggleScale(offset)
                    }
                },
                onLongPress = { _ ->
                    // 恢复到默认显示大小
                    scope.launch { state.reset() }
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

