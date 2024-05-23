package com.jvziyaoyao.image.previewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import com.jvziyaoyao.image.pager.ImageLoading
import com.jvziyaoyao.image.pager.defaultImageLoading
import com.jvziyaoyao.image.viewer.ImageContent
import com.jvziyaoyao.image.viewer.defaultImageContent
import com.jvziyaoyao.zoomable.previewer.Previewer
import com.jvziyaoyao.zoomable.previewer.PreviewerState
import com.jvziyaoyao.zoomable.previewer.TransformLayerScope

val defaultPreviewBackground:(@Composable () -> Unit)  = {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
    )
}

@Composable
fun ImagePreviewer(
    state: PreviewerState,
    imageLoader: @Composable (Int) -> Pair<Any?, Size?>,
    imageContent: ImageContent = defaultImageContent,
    imageLoading: ImageLoading? = defaultImageLoading,
    previewerLayer: TransformLayerScope = TransformLayerScope(background = defaultPreviewBackground),
    pageDecoration: @Composable (page: Int, innerPage: @Composable () -> Boolean) -> Boolean
    = { _, innerPage -> innerPage() },
) {
    Previewer(
        state = state,
        previewerLayer = previewerLayer,
        zoomablePolicy = { page ->
            pageDecoration.invoke(page) decoration@{
                val (model, size) = imageLoader.invoke(page)
                val isSpecified = size != null && size.isSpecified
                if (isSpecified) {
                    ZoomablePolicy(intrinsicSize = size!!) {
                        imageContent.invoke(model, it)
                    }
                } else {
                    imageLoading?.invoke()
                }
                return@decoration isSpecified
            }
        }
    )
}