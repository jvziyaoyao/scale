package com.jvziyaoyao.image.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.jvziyaoyao.zoomable.pager.ZoomablePager
import com.jvziyaoyao.zoomable.pager.ZoomablePagerState

@Composable
fun ImagePager(
    modifier: Modifier = Modifier,
    pagerState: ZoomablePagerState,
    imageLoader: @Composable (Int) -> Painter,
) {
    ZoomablePager(
        modifier = modifier,
        state = pagerState
    ) { page ->
        val painter = imageLoader(page)
        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null,
            )
        }
    }
}