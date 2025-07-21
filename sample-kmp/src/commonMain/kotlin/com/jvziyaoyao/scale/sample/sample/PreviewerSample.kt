package com.jvziyaoyao.scale.sample.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.zoomable.previewer.Previewer
import com.jvziyaoyao.scale.zoomable.previewer.TransformItemView
import com.jvziyaoyao.scale.zoomable.previewer.TransformLayerScope
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.rememberTransformItemState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PreviewerSample {

    // 简单使用
    @Composable
    fun BasicSample() {
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val state = rememberPreviewerState(pageCount = { images.size }) { images[it] }
        Previewer(
            state = state,
        ) { page ->
            val painter = rememberAsyncImagePainter(model = images[page])
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
            painter.intrinsicSize.isSpecified
        }

        LaunchedEffect(Unit) {
            // 展开
            state.open()
            // 等待四秒
            delay(4 * 1000)
            // 关闭
            state.close()
        }
    }

    // 带转换动效
    @Composable
    fun TransformSample() {
        val scope = rememberCoroutineScope()
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val state = rememberPreviewerState(pageCount = { images.size }) { images[it] }

        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, url ->
                    val painter = rememberAsyncImagePainter(model = url)
                    val itemState =
                        rememberTransformItemState(intrinsicSize = painter.intrinsicSize)
                    TransformItemView(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable {
                                scope.launch {
                                    state.enterTransform(index)
                                }
                            },
                        key = url,
                        transformState = state,
                        itemState = itemState,
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Previewer(
            state = state,
        ) { page ->
            val painter = rememberAsyncImagePainter(model = images[page])
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
            painter.intrinsicSize.isSpecified
        }
    }

    // 编辑图层示例代码
    @Composable
    fun DecorationSample() {
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val state = rememberPreviewerState(pageCount = { images.size }) { images[it] }

        Previewer(
            state = state,
            previewerLayer = TransformLayerScope(
                previewerDecoration = {
                    // 设置组件的背景图层
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.2F))
                    ) {
                        // 组件内容本身
                        it.invoke()
                        // 设置前景图层
                        Box(
                            modifier = Modifier
                                .padding(bottom = 48.dp)
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.White)
                                .align(Alignment.BottomCenter),
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 36.sp,
                                text = "❤️",
                            )
                        }
                    }
                },
            ),
        ) { page ->
            val painter = rememberAsyncImagePainter(model = images[page])
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
            if (!painter.intrinsicSize.isSpecified) {
                // 加载中
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            painter.intrinsicSize.isSpecified
        }

        LaunchedEffect(Unit) {
            state.open()
        }
    }

    @Composable
    fun StateSample() {
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val state = rememberPreviewerState(
            // 垂直手势类型，支持上下拖拽关闭预览
            verticalDragType = VerticalDragType.Down,
            pageCount = { images.size },
            getKey = { images[it] }
        )

        LaunchedEffect(Unit) {
            state.open() // 展开
            state.close() // 关闭
            state.enterTransform(0) // 带转换动画展开
            state.exitTransform() // 带转换动画关闭

            state.visible // 当前组件是否可见
            state.visibleTarget // 当前组件可见状态的目标值
            state.animating // 是否正在进行动画
            state.canOpen // 是否允许展开
            state.canClose // 是否允许关闭
        }
    }

}