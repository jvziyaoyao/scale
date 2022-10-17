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

    lateinit var pagerState: ImagePagerState

    val currentPage: Int
        get() = pagerState.currentPage

    val targetPage: Int
        get() = pagerState.targetPage

    val pageCount: Int
        get() = pagerState.pageCount

    val currentPageOffset: Float
        get() = pagerState.currentPageOffset

    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float,
    ) = pagerState.scrollToPage(page, pageOffset)

    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}