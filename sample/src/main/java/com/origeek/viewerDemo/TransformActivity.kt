package com.origeek.viewerDemo

import android.content.ContentValues.TAG
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.*
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.GridLayout
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.delay
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
            }
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
    val transformContentState = rememberTransformContentState()
    var selectedPainter by remember { mutableStateOf<Painter?>(null) }
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
                                    transformContentState.contentVisible = false
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
        transformContentState.contentVisible = true
        scope.launch {
            imageViewerVisible.snapTo(0F)
            val scale = imageViewerState.scale
            val offsetX = imageViewerState.offsetX
            val offsetY = imageViewerState.offsetY
//            val uSize = transformContentState.uSize
//            val bSize = transformContentState.bSize
            val rw = imageViewerState.defaultSize.width * scale.value
            val rh = imageViewerState.defaultSize.height * scale.value
//            val rx = (bSize.width - rw).div(2) + offsetX.value
//            val ry = (bSize.height - rh).div(2) + offsetY.value
            transformContentState.bw.snapTo(rw)
            transformContentState.bh.snapTo(rh)
            transformContentState.bx.snapTo(0F)
            transformContentState.by.snapTo(0F)
            Log.i(TAG, "TransformBody: ${scale.value} - ${offsetX.value} - ${offsetY.value}")
//            transformContentState.endAsync()
            imageViewerState.resetImmediately()
            selectedPainter = null
        }
    }
    TransformContentView(transformContentState)
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