package com.jvziyaoyao.scale.decoder.kmp

import android.graphics.BitmapRegionDecoder
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 通过文件创建SamplingDecoder
 *
 * @param file
 * @return
 */
suspend fun createSamplingDecoder(file: File): SamplingDecoder? {
    val inputStream = FileInputStream(file)
    val exifInterface = ExifInterface(file)
    val decoder = createBitmapRegionDecoder(inputStream)
    val rotation = exifInterface.getDecoderRotation()
    return decoder?.let {
        SamplingDecoder(
            decoder = AndroidRegionDecoder(decoder),
            rotation = rotation,
        ).apply {
            thumbnail = createTempBitmap()
        }
    }
}

/**
 * 通过流创建BitmapRegionDecoder
 *
 * @param inputStream
 * @return
 */
fun createBitmapRegionDecoder(inputStream: InputStream): BitmapRegionDecoder? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BitmapRegionDecoder.newInstance(inputStream)
    } else {
        BitmapRegionDecoder.newInstance(inputStream, false)
    }
}

/**
 * 通过Exif接口获取SamplingDecoder的旋转方向
 *
 * @return
 */
fun ExifInterface.getDecoderRotation(): SamplingDecoder.Rotation {
    val orientation = getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> SamplingDecoder.Rotation.ROTATION_90
        ExifInterface.ORIENTATION_ROTATE_180 -> SamplingDecoder.Rotation.ROTATION_180
        ExifInterface.ORIENTATION_ROTATE_270 -> SamplingDecoder.Rotation.ROTATION_270
        else -> SamplingDecoder.Rotation.ROTATION_0
    }
}

/**
 * 创建SamplingDecoder的主要方法
 *
 * @param decoder
 * @param rotation 请参考SamplingDecoder.Rotation
 * @return
 */
//fun createSamplingDecoder(
//    decoder: RegionDecoder,
//    rotation: SamplingDecoder.Rotation = SamplingDecoder.Rotation.ROTATION_0,
//): SamplingDecoder {
//    return SamplingDecoder(decoder = decoder, rotation = rotation).apply {
//        this.thumbnail = createTempBitmap()
//    }
//}

/**
 * 创建SamplingDecoder的方法
 *
 * @param file
 * @return SamplingDecoder成功创建时不为空，创建过程中出现异常会返回Exception
 */
@Composable
fun rememberSamplingDecoder(file: File): Pair<SamplingDecoder?, Exception?> {
    val scope = rememberCoroutineScope()
    val samplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
    val expectation = remember { mutableStateOf<Exception?>(null) }
    LaunchedEffect(file) {
        launch(Dispatchers.IO) {
            try {
                samplingDecoder.value = createSamplingDecoder(file)
            } catch (e: Exception) {
                expectation.value = e
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

/**
 * 创建SamplingDecoder的方法
 *
 * @param inputStream
 * @param rotation 请参考SamplingDecoder.Rotation
 * @return SamplingDecoder成功创建时不为空，创建过程中出现异常会返回Exception
 */
@Composable
fun rememberSamplingDecoder(
    inputStream: InputStream,
    rotation: SamplingDecoder.Rotation = SamplingDecoder.Rotation.ROTATION_0,
): Pair<SamplingDecoder?, Exception?> {
    val scope = rememberCoroutineScope()
    val samplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
    val expectation = remember { mutableStateOf<Exception?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            try {
                val decoder = createBitmapRegionDecoder(inputStream)
                    ?: throw RuntimeException("Can not create bitmap region decoder!")
                samplingDecoder.value = SamplingDecoder(
                    decoder = AndroidRegionDecoder(decoder),
                    rotation = rotation,
                ).apply {
                    thumbnail = createTempBitmap()
                }
            } catch (e: Exception) {
                expectation.value = e
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