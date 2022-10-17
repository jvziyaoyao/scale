package com.origeek.viewerDemo

import android.graphics.BitmapRegionDecoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.TransformImageView
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.imageViewer.viewer.ImageDecoder
import com.origeek.ui.common.compose.ScaleGrid
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberDecoderImagePainter
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
class DecoderActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ViewerDemoTheme {
                NetBody()
            }
        }
    }

}

@Composable
fun NetBody() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        val key = "2887"
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val previewerState = rememberPreviewerState()
        val itemState = rememberTransformItemState()
        val inputStream = remember { context.assets.open("a350.jpg") }
        var transformEnable by remember { mutableStateOf(true) }
        val painter = painterResource(R.drawable.a350_temp)
        var loadDelay by remember { mutableStateOf(0F) }
        val horizontal = maxWidth > maxHeight

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
            Spacer(modifier = Modifier.height(if (horizontal) 12.dp else 24.dp))
            Text(text = "👆 Clickable")
            Spacer(modifier = Modifier.height(if (horizontal) 24.dp else 72.dp))
            Column(modifier = Modifier.fillMaxWidth(if (horizontal) 0.4F else 0.6F)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Transform:")
                    Switch(
                        checked = transformEnable,
                        onCheckedChange = {
                            transformEnable = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colors.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(if (horizontal) 10.dp else 20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Delay ${loadDelay.toLong()}:")
                    Slider(
                        modifier = Modifier
                            .height(48.dp)
                            .width(100.dp),
                        value = loadDelay,
                        valueRange = 0F..4000F,
                        onValueChange = {
                            loadDelay = it
                        },
                    )
                }
            }
        }

        ImagePreviewer(
            count = 1,
            imageLoader = {
                rememberDecoderImagePainter(inputStream = inputStream, delay = loadDelay.toLong())
            },
            state = previewerState,
        )
    }
}