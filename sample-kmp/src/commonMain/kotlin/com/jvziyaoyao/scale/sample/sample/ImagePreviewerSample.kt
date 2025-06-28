package com.jvziyaoyao.scale.sample.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.image.previewer.TransformImageView
import com.jvziyaoyao.scale.zoomable.previewer.TransformLayerScope
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ImagePreviewerSample {

    @Composable
    fun BasicSample() {
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val state = rememberPreviewerState(pageCount = { images.size }) { images[it] }
        ImagePreviewer(
            state = state,
            imageLoader = { page ->
                val painter = rememberAsyncImagePainter(model = images[page])
                Pair(painter, painter.intrinsicSize)
            }
        )

        LaunchedEffect(Unit) {
            // 展开
            state.open()
            // 等待四秒
            delay(4 * 1000)
            // 关闭
            state.close()
        }
    }

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
                    TransformImageView(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable {
                                scope.launch {
                                    state.enterTransform(index)
                                }
                            },
                        imageLoader = {
                            val painter = rememberAsyncImagePainter(model = url)
                            Triple(url, painter, painter.intrinsicSize)
                        },
                        transformState = state,
                    )
                }
            }
        }

        ImagePreviewer(
            state = state,
            imageLoader = { page ->
                val painter = rememberAsyncImagePainter(model = images[page])
                Pair(painter, painter.intrinsicSize)
            }
        )
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

        ImagePreviewer(
            state = state,
            imageLoader = { page ->
                val painter = rememberAsyncImagePainter(model = images[page])
                Pair(painter, painter.intrinsicSize)
            },
            pageDecoration = { _, innerPage ->
                var mounted = false
                Box(modifier = Modifier.background(Color.Cyan.copy(0.2F))) {
                    mounted = innerPage()

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
                mounted
            },
            previewerLayer = TransformLayerScope(
                previewerDecoration = { innerPreviewer ->
                    Box(
                        modifier = Modifier
                            .background(Color.Black)
                    ) {
                        innerPreviewer.invoke()
                    }
                }
            ),
        )

        LaunchedEffect(Unit) {
            state.open()
        }
    }

    @Composable
    fun StateSample() {
        // 参考
        PreviewerSample.StateSample()
    }

}