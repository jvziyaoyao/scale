package com.origeek.imageViewer.gallery

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-05 21:41
 **/

open class ImagePagerState {

    @OptIn(ExperimentalPagerApi::class)
    internal lateinit var pagerState: PagerState

    @OptIn(ExperimentalPagerApi::class)
    val currentPage: Int
        get() = pagerState.currentPage

    @OptIn(ExperimentalPagerApi::class)
    val targetPage: Int
        get() = pagerState.targetPage

    @OptIn(ExperimentalPagerApi::class)
    val pageCount: Int
        get() = pagerState.pageCount

    @OptIn(ExperimentalPagerApi::class)
    val currentPageOffset: Float
        get() = pagerState.currentPageOffset

    @OptIn(ExperimentalPagerApi::class)
    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    @OptIn(ExperimentalPagerApi::class)
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    @OptIn(ExperimentalPagerApi::class)
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun rememberImagePagerState(): ImagePagerState {
    val pageState = rememberPagerState()
    val imagePagerState = remember { ImagePagerState() }
    imagePagerState.pagerState = pageState
    return imagePagerState
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageHorizonPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: ImagePagerState = rememberImagePagerState(),
    itemSpacing: Dp = 0.dp,
    content: @Composable (page: Int) -> Unit,
) {
    HorizontalPager(
        count = count,
        state = state.pagerState,
        modifier = modifier,
        itemSpacing = itemSpacing,
    ) { page ->
        content(page)
    }
}
