package com.jvziyaoyao.scale.sample

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.sample.base.BaseActivity
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
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
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val scope = rememberCoroutineScope()
        val painter = painterResource(id = R.drawable.light_02)
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