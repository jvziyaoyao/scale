package com.jvziyaoyao.zoomable.pager

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-05 21:41
 **/

/**
 * 基于HorizonPager封装的pager组件
 */
open class SupportedPagerState @OptIn(ExperimentalFoundationApi::class) constructor(
    val pagerState: PagerState,
) {

    /**
     * 当前页码
     */
    @OptIn(ExperimentalFoundationApi::class)
    val currentPage: Int
        get() = pagerState.currentPage

    /**
     * 目标页码
     */
    @OptIn(ExperimentalFoundationApi::class)
    val targetPage: Int
        get() = pagerState.targetPage

    /**
     * 当前页数
     */
    @OptIn(ExperimentalFoundationApi::class)
    val pageCount: Int
        get() = pagerState.pageCount

    /**
     * interactionSource
     */
    @OptIn(ExperimentalFoundationApi::class)
    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    /**
     * 滚动到指定页面
     */
    @OptIn(ExperimentalFoundationApi::class)
    suspend fun scrollToPage(
        // 指定的页码
        @IntRange(from = 0) page: Int,
        // 滚动偏移量
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    /**
     * 动画滚动到指定页面
     */
    @OptIn(ExperimentalFoundationApi::class)
    suspend fun animateScrollToPage(
        // 指定的页码
        @IntRange(from = 0) page: Int,
        // 滚动偏移量
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}

/**
 * 记录pager状态
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSupportedPagerState(
    // 默认显示的页码
    @IntRange(from = 0) initialPage: Int = 0,
    pageCount: () -> Int,
): SupportedPagerState {
    val pageState = rememberPagerState(initialPage = initialPage, pageCount = pageCount)
    return remember {
        SupportedPagerState(pageState)
    }
}

@Composable
fun defaultFlingBehavior(pagerState: SupportedPagerState): TargetedFlingBehavior {
    return PagerDefaults.flingBehavior(
        state = pagerState.pagerState,
    )
//    return PagerDefaults.flingBehavior(
//        state = pagerState.pagerState,
//        lowVelocityAnimationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
//        highVelocityAnimationSpec = rememberSplineBasedDecay(),
//    )
}

/**
 * pager组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SupportedHorizonPager(
    // 编辑参数
    modifier: Modifier = Modifier,
    // pager状态
    state: SupportedPagerState,
    // 每个item之间的间隔
    itemSpacing: Dp = 0.dp,
    // 页面外缓存个数
    beyondViewportPageCount: Int = 0,
    // 页面内容
    content: @Composable (page: Int) -> Unit,
) {
    HorizontalPager(
        state = state.pagerState,
        modifier = modifier,
        pageSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        flingBehavior = defaultFlingBehavior(pagerState = state),
    ) { page ->
        content(page)
    }
}
