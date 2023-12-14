package com.origeek.imageViewer.gallery

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import com.origeek.imageViewer.previewer.DEFAULT_BEYOND_BOUNDS_ITEM_COUNT
import com.origeek.imageViewer.previewer.DEFAULT_ITEM_SPACE
import com.origeek.imageViewer.zoomable.ZoomableGestureScope
import com.origeek.imageViewer.zoomable.ZoomableView
import com.origeek.imageViewer.zoomable.ZoomableViewState
import com.origeek.imageViewer.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-06 20:48
 **/

fun interface GalleryZoomablePolicyScope {
    @Composable
    fun ZoomablePolicy(
        intrinsicSize: Size,
        image: @Composable () -> Unit,
    )
}

/**
 * gallery状态
 */
open class ImageGalleryState01(
    val pagerState: ImagePagerState,
) {

    /**
     * 当前viewer的状态
     */
    var zoomableViewState by mutableStateOf<ZoomableViewState?>(null)
        internal set

    /**
     * 当前页码
     */
    val currentPage: Int
        get() = pagerState.currentPage

    /**
     * 目标页码
     */
    val targetPage: Int
        get() = pagerState.targetPage

    /**
     * interactionSource
     */
    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    /**
     * 滚动到指定页面
     * @param page Int
     * @param pageOffset Float
     */
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    /**
     * 动画滚动到指定页面
     * @param page Int
     * @param pageOffset Float
     */
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}

/**
 * 记录gallery状态
 */
@Composable
fun rememberImageGalleryState01(
    @IntRange(from = 0) initialPage: Int = 0,
    pageCount: () -> Int,
): ImageGalleryState01 {
    val imagePagerState = rememberImagePagerState(initialPage, pageCount)
    return remember { ImageGalleryState01(imagePagerState) }
}

/**
 * 图片gallery,基于Pager实现的一个图片查看列表组件
 */
@Composable
fun ImageGallery01(
    // 编辑参数
    modifier: Modifier = Modifier,
    // gallery状态
    state: ImageGalleryState01,
    // 每张图片之间的间隔
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    // 页面外缓存个数
    beyondBoundsItemCount: Int = DEFAULT_BEYOND_BOUNDS_ITEM_COUNT,
    // 检测手势
    detectGesture: GalleryGestureScope = GalleryGestureScope(),
    // 图层本体
    zoomablePolicy: @Composable GalleryZoomablePolicyScope.(page: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    // 确保不会越界
    val currentPage = state.currentPage
    ImageHorizonPager(
        state = state.pagerState,
        modifier = modifier
            .fillMaxSize(),
        itemSpacing = itemSpacing,
        beyondBoundsItemCount = beyondBoundsItemCount,
    ) { page ->
        GalleryZoomablePolicyScope { intrinsicSize, image ->
            val zoomableState = rememberZoomableState(contentSize = intrinsicSize)
            LaunchedEffect(key1 = state.currentPage) {
                if (state.currentPage != page) {
                    zoomableState.reset()
                }
                if (currentPage == page) state.zoomableViewState = zoomableState
            }
            ZoomableView(
                state = zoomableState,
                boundClip = false,
                detectGesture = ZoomableGestureScope(
                    onTap = { detectGesture.onTap() },
                    onDoubleTap = {
                        val consumed = detectGesture.onDoubleTap()
                        if (!consumed) scope.launch {
                            zoomableState.toggleScale(it)
                        }
                    },
                    onLongPress = { detectGesture.onLongPress() },
                )
            ) {
                image()
            }
        }.zoomablePolicy(page)
    }
}