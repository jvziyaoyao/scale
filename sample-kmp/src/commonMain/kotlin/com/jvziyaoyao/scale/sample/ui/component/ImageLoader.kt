package com.jvziyaoyao.scale.sample.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size

@Composable
fun rememberCoilImagePainter(image: Any): Painter {
    // 加载图片
    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
        .data(image)
        .size(Size.ORIGINAL)
        .build()
    // 获取图片的初始大小
    return rememberAsyncImagePainter(imageRequest)
}

//suspend fun loadPainter(context: PlatformContext, data: Any) = suspendCoroutine<Drawable?> { c ->
//    val imageRequest = ImageRequest.Builder(context)
//        .data(data)
//        .size(coil.size.Size.ORIGINAL)
//        .target(
//            onSuccess = {
//                c.resume(it)
//            },
//            onError = {
//                c.resume(null)
//            }
//        )
//        .build()
//    context.imageLoader.enqueue(imageRequest)
//}

//@Composable
//fun rememberBitmapRegionDecoder(
//    inputStream: InputStream,
//): BitmapRegionDecoder? {
//    val decoder = remember { mutableStateOf<BitmapRegionDecoder?>(null) }
//    LaunchedEffect(inputStream) {
//        launch(Dispatchers.IO) {
//            decoder.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                BitmapRegionDecoder.newInstance(inputStream)
//            } else {
//                BitmapRegionDecoder.newInstance(inputStream,false)
//            }
//        }
//    }
//    return decoder.value
//}
//
//@Composable
//fun rememberDecoderImagePainter(
//    inputStream: InputStream,
//    rotation: Int = 0,
//    delay: Long? = null,
//): SamplingDecoder? {
//    var samplingDecoder by remember { mutableStateOf<SamplingDecoder?>(null) }
//    LaunchedEffect(inputStream) {
//        launch(Dispatchers.IO) {
//            if (delay != null) delay(delay)
//            samplingDecoder = try {
//                val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    BitmapRegionDecoder.newInstance(inputStream)
//                } else {
//                    BitmapRegionDecoder.newInstance(inputStream,false)
//                }
//                if (decoder == null) {
//                    null
//                } else {
//                    val decoderRotation = when(rotation) {
//                        SamplingDecoder.Rotation.ROTATION_90.radius -> SamplingDecoder.Rotation.ROTATION_90
//                        SamplingDecoder.Rotation.ROTATION_180.radius -> SamplingDecoder.Rotation.ROTATION_180
//                        SamplingDecoder.Rotation.ROTATION_270.radius -> SamplingDecoder.Rotation.ROTATION_270
//                        else -> SamplingDecoder.Rotation.ROTATION_0
//                    }
//                    SamplingDecoder(
//                        decoder = decoder,
//                        rotation = decoderRotation
//                    )
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }
//    DisposableEffect(Unit) {
//        onDispose {
//            samplingDecoder?.release()
//        }
//    }
//    return samplingDecoder
//}