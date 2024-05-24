package com.jvziyaoyao.viewer.sample

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.jvziyaoyao.image.viewer.ImageViewer
import com.jvziyaoyao.image.viewer.sample.R
import com.jvziyaoyao.viewer.sample.base.BaseActivity
import com.jvziyaoyao.viewer.sample.ui.component.rememberCoilImagePainter
import com.jvziyaoyao.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.zoomable.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

class NormalActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            NormalBody()
        }
    }

}

@Composable
fun NormalBody() {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val painter = rememberCoilImagePainter(image = R.drawable.light_02)
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