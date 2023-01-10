package com.origeek.viewerDemo.ui.component

import android.graphics.BitmapRegionDecoder
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.origeek.imageViewer.viewer.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream

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

@Composable
fun rememberDecoderImagePainter(
    inputStream: InputStream,
    delay: Long? = null,
): Any? {
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            if (delay != null) delay(delay)
            imageDecoder = try {
                val decoder = BitmapRegionDecoder.newInstance(inputStream, false)
                if (decoder == null) {
                    null
                } else {
                    ImageDecoder(decoder = decoder)
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