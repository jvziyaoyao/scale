package com.jvziyaoyao.viewer.sample

import android.content.res.AssetManager.AssetInputStream
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.image.previewer.ImagePreviewer
import com.jvziyaoyao.image.viewer.ImageCanvas
import com.jvziyaoyao.image.viewer.ImageDecoder
import com.jvziyaoyao.image.viewer.getViewPort
import com.jvziyaoyao.image.viewer.rememberImageDecoder
import com.jvziyaoyao.image.viewer.sample.R
import com.jvziyaoyao.viewer.sample.base.BaseActivity
import com.jvziyaoyao.viewer.sample.ui.component.rememberCoilImagePainter
import com.jvziyaoyao.zoomable.previewer.Previewer
import com.jvziyaoyao.zoomable.previewer.TransformItemView
import com.jvziyaoyao.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.zoomable.previewer.rememberPreviewerState
import com.jvziyaoyao.zoomable.previewer.rememberTransformItemState
import com.origeek.ui.common.compose.DetectScaleGridGesture
import com.origeek.ui.common.compose.ScaleGrid
import kotlinx.coroutines.delay
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
            DecoderBody()
        }
    }

}

@Composable
fun DecoderBody() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        val key = "2887"
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val previewerState = rememberPreviewerState(
            verticalDragType = VerticalDragType.Down,
            pageCount = { 1 },
            getKey = { key },
        )
        val inputStream = remember { context.assets.open("a350.jpg") }
        val painter = painterResource(R.drawable.a350_temp)
        val itemState = rememberTransformItemState(
            intrinsicSize = painter.intrinsicSize,
        )
        val horizontal = maxWidth > maxHeight
        // Save
        var transformEnable by rememberSaveable { mutableStateOf(true) }
        var loadDelay by rememberSaveable { mutableStateOf(0F) }
        var rotation by rememberSaveable { mutableStateOf(0F) }
        if (previewerState.canClose || previewerState.animating) BackHandler {
            if (previewerState.canClose) scope.launch {
                if (transformEnable) {
                    previewerState.exitTransform()
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
                                    previewerState.enterTransform(0)
                                } else {
                                    previewerState.open(0)
                                }
                            }
                        }
                    )
                ) {
                    TransformItemView(
                        key = key,
                        itemState = itemState,
                        transformState = previewerState,
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painter,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                        )
                    }
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

//        val insetRotation by remember {
//            derivedStateOf { ceil(rotation).toInt() }
//        }
        ImagePreviewer(
            state = previewerState,
            imageLoader = { _ ->
                val decoderRotation = when (rotation.toInt()) {
                    ImageDecoder.Rotation.ROTATION_90.radius -> ImageDecoder.Rotation.ROTATION_90
                    ImageDecoder.Rotation.ROTATION_180.radius -> ImageDecoder.Rotation.ROTATION_180
                    ImageDecoder.Rotation.ROTATION_270.radius -> ImageDecoder.Rotation.ROTATION_270
                    else -> ImageDecoder.Rotation.ROTATION_0
                }
                val (imageDecoder, error) = rememberImageDecoder(inputStream = inputStream, rotation = decoderRotation)
                val realImageDecoder = remember { mutableStateOf<ImageDecoder?>(null) }
                LaunchedEffect(imageDecoder) {
                    if (imageDecoder != null) {
                        delay(loadDelay.toLong())
                        realImageDecoder.value = imageDecoder
                    }
                }
                return@ImagePreviewer Pair(
                    realImageDecoder.value,
                    realImageDecoder.value?.intrinsicSize
                )
            }
        )
//        Previewer(
//            state = previewerState,
//            zoomablePolicy = {
//                Log.i("TAG", "DecoderBody: inputStream $inputStream")
//                val imageDecoder = rememberImageDecoder(
//                    inputStream = inputStream,
//                    rotation = insetRotation
//                )
//                if (imageDecoder != null) {
//                    ZoomablePolicy(intrinsicSize = imageDecoder.intrinsicSize) {
//                        val viewPort = it.getViewPort()
//                        ImageCanvas(
//                            imageDecoder = imageDecoder,
//                            viewPort = viewPort,
//                        )
//                    }
//                } else {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                    }
//                }
//                imageDecoder != null
//            }
//        )
    }
}