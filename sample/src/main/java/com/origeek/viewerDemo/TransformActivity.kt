package com.origeek.viewerDemo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.*
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.GridLayout
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.launch

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-09-21 18:20
 **/
class TransformActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBasicContent {
            ViewerDemoTheme {
                TransformBody()
//                TransformBody02()
            }
        }
    }

}

@Composable
fun TransformBody02() {
    val defaultBoxSize = 400F
    val defaultOffset = 200F
    val scope = rememberCoroutineScope()
    var onAction by remember { mutableStateOf(false) }
    var intrinsicSize by remember { mutableStateOf(Size.Zero) }
    val boxSizeWidth = remember { Animatable(defaultBoxSize) }
    val boxSizeHeight = remember { Animatable(defaultBoxSize) }
    val boxGraphicScaleX = remember { Animatable(1F) }
    val boxGraphicScaleY = remember { Animatable(1F) }
    val boxOffsetX = remember { Animatable(defaultOffset) }
    val boxOffsetY = remember { Animatable(defaultOffset) }
    val boxSizeHeightUp by remember {
        derivedStateOf {
            boxSizeWidth.value * (intrinsicSize.height / intrinsicSize.width)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(
                    x = LocalDensity.current.run { boxOffsetX.value.toDp() },
                    y = LocalDensity.current.run { boxOffsetY.value.toDp() }
                )
                .size(
                    width = LocalDensity.current.run { boxSizeWidth.value.toDp() },
                    height = LocalDensity.current.run { boxSizeHeight.value.toDp() },
                )
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0F, 0F)
                    scaleX = boxGraphicScaleX.value
                    scaleY = boxGraphicScaleY.value
                }
        ) {
            val painter = painterResource(id = R.drawable.img_01)
            LaunchedEffect(painter.intrinsicSize) {
                if (painter.intrinsicSize.isSpecified) {
                    intrinsicSize = painter.intrinsicSize
                }
            }
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onAction = !onAction
                        if (onAction) {
                            scope.launch {
                                boxSizeHeight.animateTo(boxSizeHeightUp)
                            }
                            scope.launch {
                                boxGraphicScaleX.animateTo(2F)
                            }
                            scope.launch {
                                boxGraphicScaleY.animateTo(2F)
                            }
                            scope.launch {
                                boxOffsetX.animateTo(0F)
                            }
                            scope.launch {
                                boxOffsetY.animateTo(0F)
                            }
                        } else {
                            scope.launch {
                                boxSizeHeight.animateTo(defaultBoxSize)
                            }
                            scope.launch {
                                boxGraphicScaleX.animateTo(1F)
                            }
                            scope.launch {
                                boxGraphicScaleY.animateTo(1F)
                            }
                            scope.launch {
                                boxOffsetX.animateTo(defaultOffset)
                            }
                            scope.launch {
                                boxOffsetY.animateTo(defaultOffset)
                            }
                        }
                    },
                contentScale = ContentScale.Crop,
                painter = painter,
                contentDescription = null
            )
        }
    }
}

@Composable
fun TransformBody() {
    val images = remember {
        listOf(
            R.drawable.img_01,
            R.drawable.img_02,
            R.drawable.img_03,
            R.drawable.img_04,
            R.drawable.img_05,
            R.drawable.img_06,
        )
    }
    val scope = rememberCoroutineScope()
    val imageViewerState = rememberViewerState()
    var imageViewerVisible = remember { Animatable(0.001F) }
    val transformContentState = rememberTransformContentState(animationSpec = tween(1200))
    var selectedPainter by remember { mutableStateOf<Painter?>(null) }
    var contentVisible by remember { mutableStateOf(true) }
    val lineCount = 3
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🎈 Transform")
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
        ) {
            GridLayout(
                columns = lineCount,
                size = images.size,
                padding = 2.dp,
            ) { index ->
                val item = images[index]
                val painter = painterResource(id = item)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F)
                ) {
                    val itemState = rememberTransformItemState()
                    TransformImageView(
                        modifier = Modifier.clickable {
                            if (transformContentState.onAction) {
                                scope.launch {
                                    transformContentState.endAsync()
                                }
                            } else {
                                selectedPainter = painter
                                scope.launch {
                                    imageViewerVisible.snapTo(0F)
                                }
                                scope.launch {
                                    transformContentState.startAsync(itemState)
                                    imageViewerVisible.animateTo(1F)
                                    contentVisible = false
                                }
                            }
                        },
                        painter = painter,
                        itemState = itemState,
                        contentState = transformContentState,
                    )
                }
            }
        }
    }

    BackHandler {
        contentVisible = true
        scope.launch {
            imageViewerVisible.snapTo(0F)
            val scale = imageViewerState.scale
            val offsetX = imageViewerState.offsetX
            val offsetY = imageViewerState.offsetY
            val rw = transformContentState.fitSize.width * scale.value
            val rh = transformContentState.fitSize.height * scale.value
            val goOffsetX = (transformContentState.containerSize.width - rw).div(2) + offsetX.value
            val goOffsetY = (transformContentState.containerSize.height - rh).div(2) + offsetY.value
            val fixScale = transformContentState.fitScale * scale.value
            transformContentState.graphicScaleX.snapTo(fixScale)
            transformContentState.graphicScaleY.snapTo(fixScale)
            transformContentState.offsetX.snapTo(goOffsetX)
            transformContentState.offsetY.snapTo(goOffsetY)
            transformContentState.exitTransform()
            imageViewerState.resetImmediately()
            selectedPainter = null
        }
    }
    if (contentVisible) TransformContentView(transformContentState)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(imageViewerVisible.value)
    ) {
        if (selectedPainter != null) ImageViewer(
            model = selectedPainter!!,
            state = imageViewerState,
            onDoubleTap = {
                scope.launch {
                    imageViewerState.toggleScale(it)
                }
            }
        )
    }
}