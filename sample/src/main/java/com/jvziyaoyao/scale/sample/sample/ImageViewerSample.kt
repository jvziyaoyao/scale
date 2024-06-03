package com.jvziyaoyao.scale.sample.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.sample.R
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState

object ImageViewerSample {

    // 基本使用
    @Composable
    fun BasicSample() {
        val painter = painterResource(id = R.drawable.light_02)
        val state = rememberZoomableState(contentSize = painter.intrinsicSize)
        ImageViewer(model = painter, state = state)
    }

}