package com.jvziyaoyao.image.viewer

import android.graphics.BitmapRegionDecoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.exifinterface.media.ExifInterface
import com.jvziyaoyao.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.zoomable.zoomable.ZoomableViewState
import com.jvziyaoyao.zoomable.zoomable.rememberZoomableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    painter: Painter,
    state: ZoomableViewState = rememberZoomableState(contentSize = painter.intrinsicSize),
    detectGesture: ZoomableGestureScope = ZoomableGestureScope(),
    contentDescription: String? = null,
) {
    ZoomableView(
        modifier = modifier,
        state = state,
        detectGesture = detectGesture,
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = contentDescription,
        )
    }
}

fun createImageDecoder(file: File): ImageDecoder? {
    val inputStream = FileInputStream(file)
    val exifInterface = ExifInterface(file)
    val orientation = exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BitmapRegionDecoder.newInstance(inputStream)
    } else {
        BitmapRegionDecoder.newInstance(inputStream, false)
    }
    return if (decoder != null) {
        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> ImageDecoder.Rotation.ROTATION_90
            ExifInterface.ORIENTATION_ROTATE_180 -> ImageDecoder.Rotation.ROTATION_180
            ExifInterface.ORIENTATION_ROTATE_270 -> ImageDecoder.Rotation.ROTATION_270
            else -> ImageDecoder.Rotation.ROTATION_0
        }
        ImageDecoder(decoder = decoder, rotation = rotation).apply {
            this.thumbnail = createTempBitmap()
        }
    } else null
}

@Composable
fun rememberImageDecoder(file: File): ImageDecoder? {
    val imageDecoder = remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(file) {
        launch(Dispatchers.IO) {
            imageDecoder.value = createImageDecoder(file)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            imageDecoder.value?.release()
        }
    }
    return imageDecoder.value
}

// TODO 待优化/移除
@Composable
fun rememberImageDecoder(
    inputStream: InputStream,
    rotation: Int = 0,
    onError: (Exception) -> Unit = {},
): ImageDecoder? {
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            imageDecoder = try {
                val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BitmapRegionDecoder.newInstance(inputStream)
                } else {
                    BitmapRegionDecoder.newInstance(inputStream, false)
                }
                if (decoder == null) {
                    null
                } else {
                    val decoderRotation = when(rotation) {
                        ImageDecoder.Rotation.ROTATION_90.radius -> ImageDecoder.Rotation.ROTATION_90
                        ImageDecoder.Rotation.ROTATION_180.radius -> ImageDecoder.Rotation.ROTATION_180
                        ImageDecoder.Rotation.ROTATION_270.radius -> ImageDecoder.Rotation.ROTATION_270
                        else -> ImageDecoder.Rotation.ROTATION_0
                    }
                    ImageDecoder(decoder = decoder, rotation = decoderRotation).apply {
                        this.thumbnail = createTempBitmap()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e)
                null
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            imageDecoder?.release()
        }
    }
    return imageDecoder
}

@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    imageDecoder: ImageDecoder,
    state: ZoomableViewState = rememberZoomableState(contentSize = imageDecoder.intrinsicSize),
    detectGesture: ZoomableGestureScope = ZoomableGestureScope(),
) {
    ZoomableView(
        modifier = modifier,
        state = state,
        detectGesture = detectGesture,
    ) {
        ImageCanvas(
            imageDecoder = imageDecoder,
            viewPort = state.getViewPort(),
        )
    }
}

@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    model: Any?,
    state: ZoomableViewState = rememberZoomableState(),
    detectGesture: ZoomableGestureScope = ZoomableGestureScope(),
) {
    ZoomableView(
        modifier = modifier,
        state = state,
        detectGesture = detectGesture,
    ) {
        when(model) {
            is Painter -> {
                // TODO 提取方法
                state.contentSize = model.intrinsicSize
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = model,
                    contentDescription = null,
                )
            }
            is ImageDecoder -> {
                // TODO 提取方法
                state.contentSize = model.intrinsicSize
                ImageCanvas(
                    imageDecoder = model,
                    viewPort = state.getViewPort(),
                )
            }
        }
    }
}