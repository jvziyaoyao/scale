package com.origeek.viewerDemo

import android.graphics.BitmapRegionDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.EventListener
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.origeek.imageViewer.*
import com.origeek.ui.common.ScaleGrid
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-05 14:31
 **/
class NetActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ViewerDemoTheme {
                NetBody()
            }
        }
    }

}

const val IMAGE_FULL = "http://5gar.cygdl.com:8986/image.jpg"
const val IMAGE_TEMP = "http://5gar.cygdl.com:8986/image_temp.jpg"

//const val IMAGE_FULL = "/storage/emulated/0/DCIM/Camera/IMG_20221009_211925.jpg"
//const val IMAGE_TEMP = "/storage/emulated/0/DCIM/Camera/IMG_20221009_211925.jpg"

@Composable
fun NetBody() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val previewerState = rememberPreviewerState()
        val key = "2887"
        val itemState = rememberTransformItemState()
        if (previewerState.canClose || previewerState.animating) BackHandler {
            if (previewerState.canClose) scope.launch {
//                previewerState.closeTransform(key)
                previewerState.close()
            }
        }
        previewerState.enableVerticalDrag { key }
        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            ScaleGrid(onTap = {
                scope.launch {
                    previewerState.open(0)
//                    previewerState.openTransform(0, itemState)
                }
            }) {
                val painter = rememberAsyncImagePainter(model = IMAGE_TEMP)
                TransformImageView(
                    painter = painter,
                    key = key,
                    itemState = itemState,
                    previewerState = previewerState,
                )
            }
        }
        ImagePreviewer(
            count = 1,
            imageLoader = {
                val inputStream = remember { context.assets.open("a350.jpg") }
                rememberHugeImagePainter(inputStream = inputStream)
                    ?: rememberAsyncImagePainter(model = IMAGE_TEMP)
            },
            state = previewerState,
        )
    }
}

@Composable
fun rememberHugeImagePainter(inputStream: InputStream): Any? {
    val defaultPainter = painterResource(id = R.drawable.ic_dark_bg)
    var painter by remember { mutableStateOf<Any?>(defaultPainter) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            val imageDecoder = try {
                val decoder = BitmapRegionDecoder.newInstance(inputStream, false)
                if (decoder== null) {
                    null
                } else {
                    ImageDecoder(decoder = decoder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            launch(Dispatchers.Main) {
                painter = imageDecoder
            }
        }
    }
    return painter
}