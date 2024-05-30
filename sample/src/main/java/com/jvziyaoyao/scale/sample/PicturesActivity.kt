package com.jvziyaoyao.scale.sample

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.image.viewer.AnyComposable
import com.jvziyaoyao.scale.image.viewer.SamplingDecoder
import com.jvziyaoyao.scale.image.viewer.ModelProcessor
import com.jvziyaoyao.scale.image.viewer.createSamplingDecoder
import com.jvziyaoyao.scale.image.viewer.samplingProcessorPair
import com.jvziyaoyao.scale.sample.base.BaseActivity
import com.jvziyaoyao.scale.sample.base.CommonPermissions
import com.jvziyaoyao.scale.sample.ui.component.loadPainter
import com.jvziyaoyao.scale.zoomable.previewer.PreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.TransformItemView
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.rememberTransformItemState
import com.origeek.ui.common.compose.DetectScaleGridGesture
import com.origeek.ui.common.compose.ScaleGrid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

val requirePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
    )
} else {
    listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
}

class PicturesActivity : BaseActivity() {

    private val imagesFileList = mutableStateListOf<File>()

    // TODO 这里需要换一个更通用的路径
    private fun getStoragePath(): File {
        val picturesFile =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile
        val storageFile = File(picturesFile, "yao")
        if (!storageFile.exists()) storageFile.mkdirs()
        return storageFile
    }

    private fun fetchImages() {
        val yaoDirectory = getStoragePath()
        val fileList = yaoDirectory.listFiles()
            ?.filter { it.isFile }
//            ?.filter { it.length() > 0 }
            ?.toList()?.reversed() ?: emptyList()
        imagesFileList.clear()
        imagesFileList.addAll(fileList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            CommonPermissions(
                permissions = requirePermissions,
                onPermissionChange = {
                    if (it) launch(Dispatchers.IO) {
                        fetchImages()
                    }
                }
            ) {
                PicturesBody(
                    images = imagesFileList,
                )
            }
        }
    }

}

@Composable
fun PicturesBody(
    images: List<File>,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val previewerState = rememberPreviewerState(
            pageCount = { images.size },
            getKey = { images[it].absolutePath },
        )
        PicturesGridLayer(
            images = images,
            previewerState = previewerState,
        )
        PicturesDecoderPreviewLayer(
            images = images,
            previewerState = previewerState,
        )
    }
}

@Composable
fun PicturesDecoderPreviewLayer(
    images: List<File>,
    previewerState: PreviewerState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ImagePreviewer(
        state = previewerState,
        debugMode = true,
        processor = ModelProcessor(samplingProcessorPair),
        imageLoader = { page ->
            val file = images[page]
            val pair = remember { mutableStateOf<Pair<Any?, Size?>>(Pair(null, null)) }
            LaunchedEffect(Unit) {
                scope.launch(Dispatchers.IO) {
                    try {
                        createSamplingDecoder(file)?.let {
                            pair.value = Pair(it, it.intrinsicSize)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (pair.value.first != null) return@launch
                    loadPainter(context, file)?.let {
                        val painter = BitmapPainter(it.toBitmap().asImageBitmap())
                        pair.value = Pair(painter, painter.intrinsicSize)
                    }
                    if (pair.value.first != null) return@launch
                    pair.value = Pair(AnyComposable {
                        ErrorPlaceHolder()
                    }, null)
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    if (pair.value.first is SamplingDecoder) {
                        (pair.value.first as SamplingDecoder).release()
                    }
                }
            }
            pair.value
        },
    )

//    Previewer(
//        state = previewerState,
//        debugMode = true,
//        previewerLayer = TransformLayerScope(
//            background = {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.Black)
//                )
//            }
//        ),
//        zoomablePolicy = { page ->
//            val file = images[page]
//            val imageDecoder = remember { mutableStateOf<SamplingDecoder?>(null) }
//            val painter = remember { mutableStateOf<Painter?>(null) }
//            val error = remember { mutableStateOf(false) }
//            LaunchedEffect(Unit) {
//                scope.launch(Dispatchers.IO) {
//                    try {
//                        imageDecoder.value = createSamplingDecoder(file)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                    if (imageDecoder.value != null) return@launch
//                    painter.value = loadPainter(context, file)?.let {
//                        BitmapPainter(it.toBitmap().asImageBitmap())
//                    }
//                    if (painter.value != null) return@launch
//                    error.value = true
//                }
//            }
//            DisposableEffect(Unit) {
//                onDispose {
//                    imageDecoder.value?.release()
//                }
//            }
//
//            imageDecoder.value?.let { decoder ->
//                ZoomablePolicy(intrinsicSize = decoder.intrinsicSize) {
//                    val viewPort = it.getViewPort()
//                    SamplingCanvas(
//                        imageDecoder = decoder,
//                        viewPort = viewPort,
//                    )
//                    Text(text = "SamplingCanvas")
//                }
//            }
//            painter.value?.let { p ->
//                if (p.intrinsicSize.isSpecified) {
//                    ZoomablePolicy(intrinsicSize = p.intrinsicSize) {
//                        Image(
//                            modifier = Modifier.fillMaxSize(),
//                            painter = p,
//                            contentDescription = null
//                        )
//                        Text(text = "Image")
//                    }
//                }
//            }
//            if (imageDecoder.value == null && painter.value == null) {
//                if (!error.value) {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.align(Alignment.Center),
//                            color = LocalContentColor.current.copy(0.1F),
//                        )
//                    }
//                }
//            }
//            AnimatedVisibility(
//                modifier = Modifier.fillMaxSize(),
//                visible = error.value,
//                enter = fadeIn(),
//                exit = fadeOut(),
//            ) {
//                ErrorPlaceHolder()
//            }
//            imageDecoder.value != null || painter.value?.intrinsicSize?.isSpecified == true || error.value
//        }
//    )
}

@Composable
fun ErrorPlaceHolder() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val color = Color.White.copy(0.4F)
        Icon(
            modifier = Modifier
                .size(40.dp),
            imageVector = Icons.Filled.Error,
            tint = color,
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "图片加载失败", color = color)
    }
}

@Composable
fun PicturesGridLayer(
    images: List<File>,
    previewerState: PreviewerState,
) {
    val scope = rememberCoroutineScope()
    val lineCount = 4
    LazyVerticalGrid(
        modifier = Modifier.statusBarsPadding(),
        columns = GridCells.Fixed(lineCount)
    ) {
        images.forEachIndexed { index, item ->
            item(key = item.absolutePath) {
                val needStart = index % lineCount != 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F)
                        .padding(start = if (needStart) 2.dp else 0.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ScaleGrid(
                        detectGesture = DetectScaleGridGesture(
                            onPress = {
                                scope.launch {
                                    previewerState.enterTransform(index)
                                }
                            }
                        )
                    ) {
                        val painter = rememberAsyncImagePainter(item)
                        val itemState =
                            rememberTransformItemState(
                                intrinsicSize = painter.intrinsicSize
                            )
                        TransformItemView(
                            modifier = Modifier
                                .background(MaterialTheme.colors.background),
                            key = item.absolutePath,
                            itemState = itemState,
                            transformState = previewerState,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painter,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                )
                                if (painter.state is AsyncImagePainter.State.Error) {
                                    Icon(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .align(Alignment.Center),
                                        imageVector = Icons.Filled.Error,
                                        tint = LocalContentColor.current.copy(0.04F),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}