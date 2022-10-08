package com.origeek.viewerDemo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.origeek.imageViewer.ImagePreviewer
import com.origeek.imageViewer.TransformImageView
import com.origeek.imageViewer.rememberPreviewerState
import com.origeek.imageViewer.rememberTransformItemState
import com.origeek.ui.common.ScaleGrid
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.launch

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
            .systemBarsPadding()
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val previewerState = rememberPreviewerState()
        val key = "2887"
        val itemState = rememberTransformItemState()
        if (previewerState.canClose || previewerState.animating) BackHandler {
            if (previewerState.canClose) scope.launch {
                previewerState.closeTransform(key)
            }
        }
        previewerState.enableVerticalDrag { key }
        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            ScaleGrid(onTap = {
                scope.launch {
                    previewerState.openTransform(0, itemState)
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
                val imageRequest = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.img_01)
                    .size(coil.size.Size.ORIGINAL)
                    .build()
                rememberAsyncImagePainter(imageRequest)
            },
            state = previewerState,
        )
    }
}