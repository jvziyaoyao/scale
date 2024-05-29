package com.jvziyaoyao.scale.sample.ui.component

import android.content.Context
import android.graphics.BitmapRegionDecoder
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.jvziyaoyao.scale.image.viewer.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun rememberCoilImagePainter(image: Any): Painter {
    // 加载图片
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(image)
        .size(coil.size.Size.ORIGINAL)
        .build()
    // 获取图片的初始大小
    return rememberAsyncImagePainter(imageRequest)
}

suspend fun loadPainter(context: Context, data: Any) = suspendCoroutine<Drawable?> { c ->
    val imageRequest = ImageRequest.Builder(context)
        .data(data)
        .size(coil.size.Size.ORIGINAL)
        .target(
            onSuccess = {
                c.resume(it)
            },
            onError = {
                c.resume(null)
            }
        )
        .build()
    context.imageLoader.enqueue(imageRequest)
}

@Composable
fun rememberBitmapRegionDecoder(
    inputStream: InputStream,
): BitmapRegionDecoder? {
    val decoder = remember { mutableStateOf<BitmapRegionDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            decoder.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BitmapRegionDecoder.newInstance(inputStream)
            } else {
                BitmapRegionDecoder.newInstance(inputStream,false)
            }
        }
    }
    return decoder.value
}

@Composable
fun rememberDecoderImagePainter(
    inputStream: InputStream,
    rotation: Int = 0,
    delay: Long? = null,
): ImageDecoder? {
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            if (delay != null) delay(delay)
            imageDecoder = try {
                val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BitmapRegionDecoder.newInstance(inputStream)
                } else {
                    BitmapRegionDecoder.newInstance(inputStream,false)
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
                    ImageDecoder(decoder = decoder, rotation = decoderRotation)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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