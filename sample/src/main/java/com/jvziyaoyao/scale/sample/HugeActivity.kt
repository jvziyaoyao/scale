package com.jvziyaoyao.scale.sample

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import com.jvziyaoyao.scale.image.viewer.SamplingCanvas
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.image.viewer.ModelProcessor
import com.jvziyaoyao.scale.image.viewer.getViewPort
import com.jvziyaoyao.scale.image.viewer.samplingProcessorPair
import com.jvziyaoyao.scale.image.viewer.rememberSamplingDecoder
import com.jvziyaoyao.scale.sample.base.BaseActivity
import com.jvziyaoyao.scale.sample.ui.component.rememberDecoderImagePainter
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

class HugeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            HugeBody()
//            HugeBody01()
        }
    }

}

@Composable
fun HugeBody() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inputStream = remember { context.assets.open("a350.jpg") }
    val (samplingDecoder) = rememberSamplingDecoder(inputStream = inputStream)
    if (samplingDecoder != null) {
        val state = rememberZoomableState(contentSize = samplingDecoder.intrinsicSize)
        ImageViewer(
            model = samplingDecoder,
            state = state,
            processor = ModelProcessor(samplingProcessorPair),
            detectGesture = ZoomableGestureScope(onDoubleTap = {
                scope.launch {
                    state.toggleScale(it)
                }
            })
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun HugeBody01() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val inputStream = remember { context.assets.open("a350.jpg") }
    val samplingDecoder = rememberDecoderImagePainter(inputStream = inputStream)
    val zoomableState = rememberZoomableState(
        contentSize = if (samplingDecoder == null) Size.Zero else Size(
            width = samplingDecoder.decoderWidth.toFloat(),
            height = samplingDecoder.decoderHeight.toFloat()
        )
    )
    ZoomableView(
        modifier = Modifier
            .fillMaxSize(),
        state = zoomableState,
        boundClip = false,
        detectGesture = ZoomableGestureScope(
            onDoubleTap = {
                scope.launch {
                    zoomableState.toggleScale(it)
                }
            },
        ),
    ) {
        if (samplingDecoder != null) {
            val viewPort = zoomableState.getViewPort()
            SamplingCanvas(
                samplingDecoder = samplingDecoder,
                viewPort = viewPort,
            )
        }
    }
}