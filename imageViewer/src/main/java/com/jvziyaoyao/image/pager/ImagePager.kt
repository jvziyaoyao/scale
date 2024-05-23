package com.jvziyaoyao.image.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import com.jvziyaoyao.image.viewer.ImageContent
import com.jvziyaoyao.image.viewer.defaultImageContent
import com.jvziyaoyao.zoomable.pager.ZoomablePager
import com.jvziyaoyao.zoomable.pager.ZoomablePagerState

@Composable
fun ImagePager(
    modifier: Modifier = Modifier,
    pagerState: ZoomablePagerState,
    imageLoader: @Composable (Int) -> Pair<Any?, Size?>,
    imageContent: ImageContent = defaultImageContent,
    imageLoading: ImageLoading? = defaultImageLoading,
    pageDecoration: @Composable (page: Int, innerPage: @Composable () -> Unit) -> Unit
    = { _, innerPage -> innerPage() },
) {
    ZoomablePager(
        modifier = modifier,
        state = pagerState
    ) { page ->
        pageDecoration.invoke(page) {
            val (model, size) = imageLoader.invoke(page)
            if (size != null && size.isSpecified) {
                ZoomablePolicy(intrinsicSize = size) {
                    imageContent.invoke(model, it)
                }
            } else {
                imageLoading?.invoke()
            }
        }
    }
}

typealias ImageLoading = @Composable () -> Unit

val defaultImageLoading: ImageLoading = {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}