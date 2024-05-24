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
import com.jvziyaoyao.image.pager.ImageModelProcessor
import com.jvziyaoyao.image.pager.defaultImageLoading
import com.jvziyaoyao.image.pager.defaultImageModelProcessor
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
    imageModelProcessor: ImageModelProcessor = defaultImageModelProcessor,
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
                imageModelProcessor.invoke(this, model, size, imageContent, imageLoading)
            }
        }
    )
}