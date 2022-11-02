package com.origeek.imageViewer.gallery

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

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
    @OptIn(ExperimentalPagerApi::class)
    internal var pagerState: PagerState = PagerState(currentPage)

    /**
     * 当前页码
     */
    @OptIn(ExperimentalPagerApi::class)
    val currentPage: Int
        get() = pagerState.currentPage

    /**
     * 目标页码
     */
    @OptIn(ExperimentalPagerApi::class)
    val targetPage: Int
        get() = pagerState.targetPage

    /**
     * 总页数
     */
    @OptIn(ExperimentalPagerApi::class)
    val pageCount: Int
        get() = pagerState.pageCount

    /**
     * 当前页面的偏移量
     */
    @OptIn(ExperimentalPagerApi::class)
    val currentPageOffset: Float
        get() = pagerState.currentPageOffset

    /**
     * interactionSource
     */
    @OptIn(ExperimentalPagerApi::class)
    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    /**
     * 滚动到指定页面
     */
    @OptIn(ExperimentalPagerApi::class)
    suspend fun scrollToPage(
        // 指定的页码
        @IntRange(from = 0) page: Int,
        // 滚动偏移量
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    /**
     * 动画滚动到指定页面
     */
    @OptIn(ExperimentalPagerApi::class)
    suspend fun animateScrollToPage(
        // 指定的页码
        @IntRange(from = 0) page: Int,
        // 滚动偏移量
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

    companion object {
        @OptIn(ExperimentalPagerApi::class)
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
@OptIn(ExperimentalPagerApi::class)
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
        count = count,
        state = state.pagerState,
        modifier = modifier,
        itemSpacing = itemSpacing,
    ) { page ->
        content(page)
    }
}
