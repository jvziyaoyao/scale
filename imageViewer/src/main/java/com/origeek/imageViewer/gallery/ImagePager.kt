package com.origeek.imageViewer.gallery

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
open class ImagePagerState(
    @IntRange(from = 0) currentPage: Int = 0,
) {

    /**
     * pager状态
     */
    @OptIn(ExperimentalFoundationApi::class)
    internal var pagerState: PagerState = PagerState(currentPage)

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

    companion object {
        @OptIn(ExperimentalFoundationApi::class)
        val Saver: Saver<ImagePagerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.currentPage,
                )
            },
            restore = {
                val imagePagerState = ImagePagerState()
                imagePagerState.pagerState = PagerState(it[0] as Int)
                imagePagerState
            }
        )
    }

}

/**
 * 记录pager状态
 */
@Composable
fun rememberImagePagerState(
    // 默认显示的页码
    @IntRange(from = 0) currentPage: Int = 0,
): ImagePagerState {
    return rememberSaveable(saver = ImagePagerState.Saver) {
        ImagePagerState(currentPage)
    }
}

/**
 * pager组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageHorizonPager(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 总页数
    count: Int,
    // pager状态
    state: ImagePagerState = rememberImagePagerState(),
    // 每个item之间的间隔
    itemSpacing: Dp = 0.dp,
    // 页面内容
    content: @Composable (page: Int) -> Unit,
) {
    HorizontalPager(
        pageCount = count,
        state = state.pagerState,
        modifier = modifier,
        pageSpacing = itemSpacing,
    ) { page ->
        content(page)
    }
}
