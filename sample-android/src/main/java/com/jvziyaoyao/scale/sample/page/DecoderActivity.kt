package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import com.jvziyaoyao.scale.sample.base.BaseActivity

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

//@Composable
//fun DecoderBody() {
//    BoxWithConstraints(
//        modifier = Modifier
//            .fillMaxSize(),
//    ) {
//        val key = "2887"
//        val context = LocalContext.current
//        val scope = rememberCoroutineScope()
//        val previewerState = rememberPreviewerState(
//            verticalDragType = VerticalDragType.Down,
//            pageCount = { 1 },
//            getKey = { key },
//        )
//        val inputStream = remember { context.assets.open("a350.jpg") }
//        val painter = painterResource(R.drawable.a350_temp)
//        val itemState = rememberTransformItemState(
//            intrinsicSize = painter.intrinsicSize,
//        )
//        val horizontal = this@BoxWithConstraints.maxWidth > maxHeight
//        // Save
//        var transformEnable by rememberSaveable { mutableStateOf(true) }
//        var loadDelay by rememberSaveable { mutableStateOf(0F) }
//        var rotation by rememberSaveable { mutableStateOf(0F) }
//        if (previewerState.canClose || previewerState.animating) BackHandler {
//            if (previewerState.canClose) scope.launch {
//                if (transformEnable) {
//                    previewerState.exitTransform()
//                } else {
//                    previewerState.close()
//                }
//            }
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
//        ) {
//            Box(
//                Modifier.size(120.dp), contentAlignment = Alignment.Center
//            ) {
//                ScaleGrid(
//                    detectGesture = DetectScaleGridGesture(
//                        onPress = {
//                            scope.launch {
//                                if (transformEnable) {
//                                    previewerState.enterTransform(0)
//                                } else {
//                                    previewerState.open(0)
//                                }
//                            }
//                        }
//                    )
//                ) {
//                    TransformItemView(
//                        key = key,
//                        itemState = itemState,
//                        transformState = previewerState,
//                    ) {
//                        Image(
//                            modifier = Modifier.fillMaxSize(),
//                            painter = painter,
//                            contentScale = ContentScale.Crop,
//                            contentDescription = null,
//                        )
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(if (horizontal) 12.dp else 24.dp))
//            Text(text = "👆 Clickable")
//            Spacer(modifier = Modifier.height(if (horizontal) 24.dp else 72.dp))
//            Column(modifier = Modifier.fillMaxWidth(if (horizontal) 0.4F else 0.6F)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                ) {
//                    Text(text = "Transform:")
//                    Switch(
//                        checked = transformEnable,
//                        onCheckedChange = {
//                            transformEnable = it
//                        },
//                        colors = SwitchDefaults.colors(
//                            checkedThumbColor = MaterialTheme.colors.primary
//                        )
//                    )
//                }
//                Spacer(modifier = Modifier.height(if (horizontal) 10.dp else 20.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                ) {
//                    Text(text = "Delay ${loadDelay.toLong()}:")
//                    Slider(
//                        modifier = Modifier
//                            .height(48.dp)
//                            .width(100.dp),
//                        value = loadDelay,
//                        valueRange = 0F..4000F,
//                        onValueChange = {
//                            loadDelay = it
//                        },
//                    )
//                }
//                Spacer(modifier = Modifier.height(if (horizontal) 10.dp else 20.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                ) {
//                    Text(text = "Rotation ${ceil(rotation).toInt()}:")
//                    Slider(
//                        modifier = Modifier
//                            .height(48.dp)
//                            .width(100.dp),
//                        value = rotation,
//                        valueRange = 0F..270F,
//                        steps = 2,
//                        onValueChange = {
//                            rotation = it
//                        },
//                    )
//                }
//            }
//        }
//
//        ImagePreviewer(
//            state = previewerState,
//            processor = ModelProcessor(samplingProcessorPair),
//            imageLoader = { _ ->
//                val decoderRotation = when (rotation.toInt()) {
//                    SamplingDecoder.Rotation.ROTATION_90.radius -> SamplingDecoder.Rotation.ROTATION_90
//                    SamplingDecoder.Rotation.ROTATION_180.radius -> SamplingDecoder.Rotation.ROTATION_180
//                    SamplingDecoder.Rotation.ROTATION_270.radius -> SamplingDecoder.Rotation.ROTATION_270
//                    else -> SamplingDecoder.Rotation.ROTATION_0
//                }
//                val (samplingDecoder, error) = rememberSamplingDecoder(
//                    inputStream = inputStream,
//                    rotation = decoderRotation
//                )
//                val realSamplingDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
//                LaunchedEffect(samplingDecoder) {
//                    if (samplingDecoder != null) {
//                        delay(loadDelay.toLong())
//                        realSamplingDecoder.value = samplingDecoder
//                    }
//                }
//                return@ImagePreviewer Pair(
//                    realSamplingDecoder.value,
//                    realSamplingDecoder.value?.intrinsicSize
//                )
//            }
//        )
//    }
//}