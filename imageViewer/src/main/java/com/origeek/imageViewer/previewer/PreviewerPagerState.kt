package com.origeek.imageViewer.previewer

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.origeek.imageViewer.gallery.ImagePagerState

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-17 14:41
 **/

open class PreviewerPagerState {

    /**
     * pagerState
     */
    lateinit var pagerState: ImagePagerState

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
     * 总页数
     */
    val pageCount: Int
        get() = pagerState.pageCount

    /**
     * 当前页面的平移量
     */
    val currentPageOffset: Float
        get() = pagerState.currentPageOffset

    /**
     * 滚动到指定页面
     * @param page Int
     * @param pageOffset Float
     */
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0F,
    ) = pagerState.scrollToPage(page, pageOffset)

    /**
     * 带动画滚动到指定页面
     * @param page Int
     * @param pageOffset Float
     */
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0F,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}