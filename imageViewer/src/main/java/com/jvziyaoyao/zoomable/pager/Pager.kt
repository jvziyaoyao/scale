package com.jvziyaoyao.zoomable.pager

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.TargetedFlingBehavior
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
 *
 * @property pagerState 可以用来控制页面切换和获取页面状态等
 */
open class SupportedPagerState(
    val pagerState: PagerState,
) {

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
     * 当前页数
     */
    val pageCount: Int
        get() = pagerState.pageCount

    /**
     * interactionSource
     */
    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    /**
     * 滚动到指定页面
     */
    suspend fun scrollToPage(
        // 指定的页码
        @IntRange(from = 0) page: Int,
        // 滚动偏移量
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    /**
     * 动画滚动到指定页面
     */
    suspend fun animateScrollToPage(
        // 指定的页码
        @IntRange(from = 0) page: Int,
        // 滚动偏移量
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}

/**
 * 用于获取pager状态和控制pager
 *
 * @param initialPage 初始页码
 * @param pageCount 总页数
 * @return 返回一个通用对PagerState
 */
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

/**
 * 切换页面对时候默认对手势效果
 *
 * @param pagerState 页面状态对象
 * @return TargetedFlingBehavior
 */
@Composable
fun defaultFlingBehavior(pagerState: SupportedPagerState): TargetedFlingBehavior {
    return PagerDefaults.flingBehavior(
        state = pagerState.pagerState,
    )
}

/**
 * 一个通用pager组件，对底层对pager进行了封装
 *
 * @param modifier 图层修饰
 * @param state pager状态获取与控制
 * @param itemSpacing 每个item之间的间隔
 * @param beyondViewportPageCount 页面外缓存个数
 * @param content 页面内容
 */
@Composable
fun SupportedHorizonPager(
    modifier: Modifier = Modifier,
    state: SupportedPagerState,
    itemSpacing: Dp = 0.dp,
    beyondViewportPageCount: Int = 0,
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
