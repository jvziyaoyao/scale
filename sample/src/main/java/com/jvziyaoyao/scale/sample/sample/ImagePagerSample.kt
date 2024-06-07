package com.jvziyaoyao.scale.sample.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.pager.ImagePager
import com.jvziyaoyao.scale.image.pager.defaultProceedPresentation
import com.jvziyaoyao.scale.sample.R
import com.jvziyaoyao.scale.zoomable.pager.rememberZoomablePagerState

object ImagePagerSample {

    // 简单使用
    @Composable
    fun BasicSample() {
        val images = remember {
            mutableStateListOf(R.drawable.light_01, R.drawable.light_02)
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ImagePager(
            pagerState = pagerState,
            imageLoader = { page ->
                val painter = painterResource(id = images[page])
                Pair(painter, painter.intrinsicSize)
            }
        )
    }

    @Composable
    fun CoilSample() {
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ImagePager(
            pagerState = pagerState,
            imageLoader = { page ->
                val painter = rememberAsyncImagePainter(model = images[page])
                Pair(painter, painter.intrinsicSize)
            }
        )
    }

    @Composable
    fun DecorationSample() {
        val images = remember {
            mutableStateListOf(R.drawable.light_01, R.drawable.light_02)
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ImagePager(
            pagerState = pagerState,
            proceedPresentation = defaultProceedPresentation,
            imageLoader = { page ->
                val painter = painterResource(id = images[page])
                Pair(painter, painter.intrinsicSize)
            },
            imageLoading = {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Blue,
                    )
                }
            },
            pageDecoration = { page, innerPage ->
                Box(modifier = Modifier.background(Color.LightGray)) {
                    innerPage.invoke()

                    // 设置每一页的前景图层
                    Box(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp)
                            .align(Alignment.BottomCenter),
                    ) {
                        Text(text = "${page + 1}/${images.size}")
                    }
                }
            }
        )
    }

    @Composable
    fun StateSample() {
        // 参考
        ZoomablePagerSample.StateSample()
    }

}