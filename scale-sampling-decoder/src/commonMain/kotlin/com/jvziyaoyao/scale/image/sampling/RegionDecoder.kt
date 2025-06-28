package com.jvziyaoyao.scale.image.sampling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

interface RegionDecoder {

    fun width(): Int

    fun height(): Int

    fun recycle()

    fun isRecycled(): Boolean

    fun rotate(imageBitmap: ImageBitmap, degree: Float): ImageBitmap

    suspend fun decodeRegion(
        inSampleSize: Int,
        rect: Rect,
    ): ImageBitmap?

}

class IllegalDecoderModelException : RuntimeException("illegal region decoder model type!")

expect fun getReginDecoder(model: Any?): RegionDecoder?

@Composable
fun rememberSamplingDecoder(
    model: Any?,
    rotation: SamplingDecoder.Rotation = SamplingDecoder.Rotation.ROTATION_0,
): Pair<SamplingDecoder?, Exception?> {
    val scope = rememberCoroutineScope()
    val samplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
    val expectation = remember { mutableStateOf<Exception?>(null) }
    LaunchedEffect(model) {
        launch(Dispatchers.IO) {
            if (model != null && samplingDecoder.value == null) {
                try {
                    val reginDecoder = getReginDecoder(model = model)
                    if (reginDecoder != null) {
                        samplingDecoder.value = SamplingDecoder(
                            decoder = reginDecoder,
                            rotation = rotation,
                        ).apply {
                            thumbnail = createTempBitmap()
                        }
                    }
                } catch (e: Exception) {
                    expectation.value = e
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                samplingDecoder.value?.release()
            }
        }
    }
    return Pair(samplingDecoder.value, expectation.value)
}