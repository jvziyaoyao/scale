package com.origeek.viewerDemo

import android.graphics.BitmapRegionDecoder
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

@Composable
fun NetBody() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        val key = "2887"
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val previewerState = rememberPreviewerState(animationSpec = tween(400))
        val itemState = rememberTransformItemState()
        val inputStream = remember { context.assets.open("a350.jpg") }
//        val painter = rememberAsyncImagePainter(model = R.drawable.a350_temp)
        var transformEnable by remember { mutableStateOf(false) }
        val painter = painterResource(R.drawable.a350_temp)

        if (previewerState.canClose || previewerState.animating) BackHandler {
            if (previewerState.canClose) scope.launch {
                if (transformEnable) {
                    previewerState.closeTransform(key)
                } else {
                    previewerState.close()
                }
            }
        }
        previewerState.enableVerticalDrag { key }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                ScaleGrid(onTap = {
                    scope.launch {
                        if (transformEnable) {
                            previewerState.openTransform(0, itemState)
                        } else {
                            previewerState.open(0, itemState)
                        }
                    }
                }) {
                    TransformImageView(
                        painter = painter,
                        key = key,
                        itemState = itemState,
                        previewerState = previewerState,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "👆 Clickable")
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Transform:")
                Spacer(modifier = Modifier.width(12.dp))
                Switch(checked = transformEnable, onCheckedChange = {
                    transformEnable = it
                })
            }
        }

        ImagePreviewer(
            count = 1,
            imageLoader = {
                rememberDecoderImagePainter(inputStream = inputStream)
//                painterResource(R.drawable.a350_temp)
//                rememberAsyncImagePainter(
//                    model = IMAGE_FULL,
//                    placeholder = painterResource(
//                        id = R.drawable.img_01
//                    )
//                )
//                rememberHugeImagePainter(
//                    placeholder = if (previewerState.isOpenTransform) painter else defaultPlaceholder,
//                    inputStream = inputStream,
//                )
            },
            state = previewerState,
        )
    }
}

val defaultPlaceholder = ComposeModel {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colors.surface.copy(0.2F))
    }
}

@Composable
fun rememberHugeImagePainter(
    placeholder: Any = defaultPlaceholder,
    inputStream: InputStream,
    delayTime: Long = 0L,
): Any {
    var painter by remember { mutableStateOf<Any>(placeholder) }
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            delay(delayTime)
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
            launch(Dispatchers.Main) {
                if (imageDecoder != null) painter = imageDecoder!!
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            imageDecoder?.release()
        }
    }
    return painter
}

@Composable
fun rememberDecoderImagePainter(
    inputStream: InputStream,
): Any? {
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        launch(Dispatchers.IO) {
            delay(1200)
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