package com.jvziyaoyao.viewer.sample

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.jvziyaoyao.image.viewer.R
import com.jvziyaoyao.viewer.sample.base.BaseActivity
import com.jvziyaoyao.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.zoomable.zoomable.rememberZoomableState
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.rememberViewerState
import kotlinx.coroutines.launch

class NormalActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
//            NormalBody()
            NormalBody1()
        }
    }

}

@Composable
fun NormalBody() {
    val scope = rememberCoroutineScope()
    val state = rememberViewerState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ImageViewer(
            state = state,
            model = painterResource(id = R.drawable.light_02),
            modifier = Modifier.fillMaxSize(),
            detectGesture = {
                onDoubleTap = {
                    scope.launch {
                        state.toggleScale(it)
                    }
                }
            },
        )
    }
}

@Composable
fun NormalBody1() {
    val scope = rememberCoroutineScope()
    val painter = painterResource(id = R.drawable.light_02)
    val zoomableState = rememberZoomableState(contentSize = painter.intrinsicSize)
    ZoomableView(
        state = zoomableState,
        detectGesture = ZoomableGestureScope(
            onDoubleTap = {
                scope.launch {
                    zoomableState.toggleScale(it)
                }
            }
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
        )
    }
}