package com.origeek.viewerDemo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.TransformImageView
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.imageViewer.viewer.ROTATION_0
import com.origeek.imageViewer.viewer.ROTATION_180
import com.origeek.imageViewer.viewer.ROTATION_270
import com.origeek.imageViewer.viewer.ROTATION_90
import com.origeek.ui.common.compose.DetectScaleGridGesture
import com.origeek.ui.common.compose.ScaleGrid
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberDecoderImagePainter
import kotlinx.coroutines.launch
import kotlin.math.ceil

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
            NetBody()
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
        val previewerState =
            rememberPreviewerState(enableVerticalDrag = true, pageCount = { 1 }) { key }
        val itemState = rememberTransformItemState()
        val inputStream = remember { context.assets.open("a350.jpg") }
        val painter = painterResource(R.drawable.a350_temp)
        val horizontal = maxWidth > maxHeight
        // Save
        var transformEnable by rememberSaveable { mutableStateOf(true) }
        var loadDelay by rememberSaveable { mutableStateOf(0F) }
        var rotation by rememberSaveable { mutableStateOf(0F) }

        if (previewerState.canClose || previewerState.animating) BackHandler {
            if (previewerState.canClose) scope.launch {
                if (transformEnable) {
                    previewerState.closeTransform()
                } else {
                    previewerState.close()
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(120.dp), contentAlignment = Alignment.Center
            ) {
                ScaleGrid(
                    detectGesture = DetectScaleGridGesture(
                        onPress = {
                            scope.launch {
                                if (transformEnable) {
                                    previewerState.openTransform(0, itemState)
                                } else {
                                    previewerState.open(0, itemState)
                                }
                            }
                        }
                    )
                ) {
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
                Spacer(modifier = Modifier.height(if (horizontal) 10.dp else 20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Rotation ${ceil(rotation).toInt()}:")
                    Slider(
                        modifier = Modifier
                            .height(48.dp)
                            .width(100.dp),
                        value = rotation,
                        valueRange = 0F..270F,
                        steps = 2,
                        onValueChange = {
                            rotation = it
                        },
                    )
                }
            }
        }

        val insetRotation by remember {
            derivedStateOf {
                when (ceil(rotation).toInt()) {
                    ROTATION_0 -> ROTATION_0
                    ROTATION_90 -> ROTATION_90
                    ROTATION_180 -> ROTATION_180
                    ROTATION_270 -> ROTATION_270
                    else -> ROTATION_0
                }
            }
        }
        ImagePreviewer(
            imageLoader = {
                rememberDecoderImagePainter(
                    inputStream = inputStream,
                    rotation = insetRotation,
                    delay = loadDelay.toLong()
                )
            },
            state = previewerState,
        )
    }
}