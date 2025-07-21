package com.jvziyaoyao.scale.sample.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import scale.sample_kmp.generated.resources.Res
import scale.sample_kmp.generated.resources.light_02

@Composable
fun NormalBody() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val scope = rememberCoroutineScope()
        val painter = painterResource(Res.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ImageViewer(
            model = painter,
            state = state,
            detectGesture = ZoomableGestureScope(onDoubleTap = {
                scope.launch {
                    state.toggleScale(it)
                }
            })
        )
    }
}