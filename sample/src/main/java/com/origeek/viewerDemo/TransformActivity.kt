package com.origeek.viewerDemo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
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