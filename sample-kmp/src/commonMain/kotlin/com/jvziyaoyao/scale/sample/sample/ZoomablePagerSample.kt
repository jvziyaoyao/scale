package com.jvziyaoyao.scale.sample.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jvziyaoyao.scale.zoomable.pager.ZoomablePager
import com.jvziyaoyao.scale.zoomable.pager.rememberZoomablePagerState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import scale.sample_kmp.generated.resources.Res
import scale.sample_kmp.generated.resources.light_01
import scale.sample_kmp.generated.resources.light_02

object ZoomablePagerSample {

    // 简单使用
    @Composable
    fun BasicSample() {
        val images = remember {
            mutableStateListOf(Res.drawable.light_01, Res.drawable.light_02)
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ZoomablePager(state = pagerState) { page ->
            val painter = painterResource(images[page])
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
        }
    }

    // Coil加载网络图片
    @Composable
    fun CoilSample() {
        val images = remember {
            mutableStateListOf(
                "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
                "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
            )
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ZoomablePager(state = pagerState) { page ->
            val painter = rememberAsyncImagePainter(model = images[page])
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
            if (!painter.intrinsicSize.isSpecified) {
                // 未加载成功时可以先显示一个loading占位
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    // 对页面进行自定义
    @Composable
    fun DecorationSample() {
        val images = remember {
            mutableStateListOf(Res.drawable.light_01, Res.drawable.light_02)
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ZoomablePager(state = pagerState) { page ->
            val painter = painterResource(images[page])
            // 设置背景色奇偶页不同
            val backgroundColor = if (page % 2 == 0) Color.Cyan else Color.Gray
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(0.2F))
            ) {
                ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painter,
                        contentDescription = null
                    )
                }
                // 设置每一页的前景图层
                Box(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .padding(8.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    Text(text = "${page + 1}/${images.size}")
                }
            }
        }
    }

    @Composable
    fun StateSample() {
        val scope = rememberCoroutineScope()
        val images = remember {
            mutableStateListOf(Res.drawable.light_01, Res.drawable.light_02)
        }
        val pagerState = rememberZoomablePagerState { images.size }
        ZoomablePager(
            state = pagerState,
            itemSpacing = 20.dp, // 设置页面的间隙
            beyondViewportPageCount = 2, // 除当前页面外，预先加载其他页面的数量
            detectGesture = PagerGestureScope(
                onTap = {
                    // 点击事件
                    scope.launch {
                        // 获取当前页面的页码
                        if (pagerState.currentPage == 0) {
                            // 滚动到下一个页面
                            pagerState.animateScrollToPage(1)
                            // pagerState.scrollToPage(1)
                        }
                    }
                },
                onDoubleTap = {
                    // 双击事件
                    // 如果返回false，会执行默认操作，把当前页面放大到最大
                    // 如果返回true，则不会有任何操作
                    return@PagerGestureScope true
                },
                onLongPress = {
                    // 长按事件
                }
            )
        ) { page ->
            val painter = painterResource(images[page])
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
        }
    }

}