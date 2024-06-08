package com.jvziyaoyao.scale.sample.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.jvziyaoyao.scale.image.sampling.SamplingCanvas
import com.jvziyaoyao.scale.image.sampling.SamplingCanvasViewPort
import com.jvziyaoyao.scale.image.sampling.getViewPort
import com.jvziyaoyao.scale.image.sampling.rememberSamplingDecoder
import com.jvziyaoyao.scale.image.sampling.samplingProcessorPair
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.image.viewer.ModelProcessor
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.scale.zoomable.zoomable.detectTransformGestures
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState

object SamplingDecoderSample {

    @Composable
    fun BasicSample() {
        val context = LocalContext.current
        val inputStream = remember { context.assets.open("a350.jpg") }
        val (samplingDecoder) = rememberSamplingDecoder(inputStream = inputStream)
        if (samplingDecoder != null) {
            val state = rememberZoomableState(contentSize = samplingDecoder.intrinsicSize)
            ImageViewer(
                model = samplingDecoder,
                state = state,
                processor = ModelProcessor(samplingProcessorPair)
            )
        }
    }

    @Composable
    fun ZoomableSample() {
        val context = LocalContext.current
        val inputStream = remember { context.assets.open("a350.jpg") }
        val (samplingDecoder) = rememberSamplingDecoder(inputStream = inputStream)
        if (samplingDecoder != null) {
            val state = rememberZoomableState(contentSize = samplingDecoder.intrinsicSize)
            ZoomableView(state = state) {
                SamplingCanvas(
                    samplingDecoder = samplingDecoder,
                    viewPort = state.getViewPort()
                )
            }
        }
    }

    @Composable
    fun RawSample() {
        val context = LocalContext.current
        val inputStream = remember { context.assets.open("a350.jpg") }
        val (samplingDecoder) = rememberSamplingDecoder(inputStream = inputStream)
        if (samplingDecoder != null) {
            val offset = remember { mutableStateOf(Offset.Zero) }
            val scale = remember { mutableStateOf(1F) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _, _ ->
                            offset.value += pan
                            scale.value *= zoom
                            true
                        }
                    }
            ) {
                val ratio = samplingDecoder.intrinsicSize.run {
                    width.div(height)
                }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = offset.value.x
                            translationY = offset.value.y
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                        .align(Alignment.Center)
                ) {
                    SamplingCanvas(
                        samplingDecoder = samplingDecoder,
                        viewPort = SamplingCanvasViewPort(
                            scale = 8F,
                            visualRect = Rect(0.4F, 0.4F, 0.6F, 0.8F)
                        )
                    )
                }
            }
        }
    }

}