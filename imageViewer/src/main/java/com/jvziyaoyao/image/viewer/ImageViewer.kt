package com.jvziyaoyao.image.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.jvziyaoyao.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.zoomable.zoomable.ZoomableViewState

@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    model: Any?,
    state: ZoomableViewState,
    content: ImageContent = defaultImageContent,
    detectGesture: ZoomableGestureScope = ZoomableGestureScope(),
) {
    ZoomableView(
        modifier = modifier,
        state = state,
        detectGesture = detectGesture,
    ) {
        content.invoke(model, state)
    }
}

typealias ImageContent = @Composable (Any?, ZoomableViewState) -> Unit

val defaultImageContent: ImageContent = { model, state ->
    when (model) {
        is Painter -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = model,
                contentDescription = null,
            )
        }

        is ImageBitmap -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = model,
                contentDescription = null,
            )
        }

        is ImageVector -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                imageVector = model,
                contentDescription = null,
            )
        }

        is ImageDecoder -> {
            ImageCanvas(
                imageDecoder = model,
                viewPort = state.getViewPort(),
            )
        }

        is AnyComposable -> {
            model.composable.invoke()
        }

    }
}

class AnyComposable(val composable: @Composable () -> Unit)