package com.jvziyaoyao.scale.image.sampling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

expect fun getReginDecoder(bytes: ByteArray?): RegionDecoder?

@Composable
fun rememberSamplingDecoder(
    bytes: ByteArray?,
    rotation: SamplingDecoder.Rotation = SamplingDecoder.Rotation.ROTATION_0,
): Pair<SamplingDecoder?, Exception?> {
    val scope = rememberCoroutineScope()
    val samplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
    val expectation = remember { mutableStateOf<Exception?>(null) }
    DisposableEffect(bytes, rotation) {
        var currentSamplingDecoder: SamplingDecoder? = null
        scope.launch(Dispatchers.IO) {
            if (bytes != null) {
                try {
                    val reginDecoder = getReginDecoder(bytes = bytes)
                    if (reginDecoder != null) {
                        currentSamplingDecoder = SamplingDecoder(
                            decoder = reginDecoder,
                            rotation = rotation,
                        ).apply {
                            thumbnail = createTempBitmap()
                        }
                        samplingDecoder.value = currentSamplingDecoder
                    }
                } catch (e: Exception) {
                    expectation.value = e
                }
            }
        }
        onDispose {
            scope.launch {
                currentSamplingDecoder?.release()
            }
        }
    }
    return Pair(samplingDecoder.value, expectation.value)
}