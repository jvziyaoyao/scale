package com.jvziyaoyao.image.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import com.jvziyaoyao.image.viewer.AnyComposable
import com.jvziyaoyao.image.viewer.ImageContent
import com.jvziyaoyao.image.viewer.defaultImageContent
import com.jvziyaoyao.zoomable.pager.PagerZoomablePolicyScope
import com.jvziyaoyao.zoomable.pager.ZoomablePager
import com.jvziyaoyao.zoomable.pager.ZoomablePagerState

@Composable
fun ImagePager(
    modifier: Modifier = Modifier,
    pagerState: ZoomablePagerState,
    imageLoader: @Composable (Int) -> Pair<Any?, Size?>,
    imageContent: ImageContent = defaultImageContent,
    imageLoading: ImageLoading? = defaultImageLoading,
    imageModelProcessor: ImageModelProcessor = defaultImageModelProcessor,
    pageDecoration: @Composable (page: Int, innerPage: @Composable () -> Unit) -> Unit
    = { _, innerPage -> innerPage() },
) {
    ZoomablePager(
        modifier = modifier,
        state = pagerState
    ) { page ->
        pageDecoration.invoke(page) {
            val (model, size) = imageLoader.invoke(page)
            imageModelProcessor.invoke(this, model, size, imageContent, imageLoading)
        }
    }
}

typealias ImageModelProcessor = @Composable PagerZoomablePolicyScope.(
    model: Any?,
    size: Size?,
    imageContent: ImageContent,
    imageLoading: ImageLoading?,
) -> Boolean

val defaultImageModelProcessor: ImageModelProcessor = { model, size, imageContent, imageLoading ->
    // TODO 这里要添加渐变动画?
    if (model != null && size != null && size.isSpecified) {
        ZoomablePolicy(intrinsicSize = size) {
            imageContent.invoke(model, it)
        }
        true
    } else if (model != null && model is AnyComposable && size == null) {
        model.composable.invoke()
        true
    } else {
        imageLoading?.invoke()
        false
    }
}

typealias ImageLoading = @Composable () -> Unit

val defaultImageLoading: ImageLoading = {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = Color.LightGray,
        )
    }
}