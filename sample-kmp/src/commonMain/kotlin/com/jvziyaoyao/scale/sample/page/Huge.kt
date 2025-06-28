package com.jvziyaoyao.scale.sample.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import com.jvziyaoyao.scale.decoder.kmp.SamplingCanvas
import com.jvziyaoyao.scale.decoder.kmp.getViewPort
import com.jvziyaoyao.scale.decoder.kmp.rememberSamplingDecoder
import com.jvziyaoyao.scale.decoder.kmp.samplingProcessorPair
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.image.viewer.ModelProcessor
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
import kotlinx.coroutines.launch
import scale.sample_kmp.generated.resources.Res

@Composable
fun HugeBody() {
    val scope = rememberCoroutineScope()
    val bytes = remember { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(Unit) {
        bytes.value = Res.readBytes("files/a350.jpg")
    }
    val (samplingDecoder) = rememberSamplingDecoder(model = bytes.value)
    if (samplingDecoder != null) {
        val state = rememberZoomableState(contentSize = samplingDecoder.intrinsicSize)
        ImageViewer(
            model = samplingDecoder,
            state = state,
            processor = ModelProcessor(samplingProcessorPair),
            detectGesture = ZoomableGestureScope(
                onDoubleTap = {
                    scope.launch {
                        state.toggleScale(it)
                    }
                }
            )
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
    val bytes = remember { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(Unit) {
        bytes.value = Res.readBytes("files/a350.jpg")
    }
    val (samplingDecoder) = rememberSamplingDecoder(model = bytes.value)
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