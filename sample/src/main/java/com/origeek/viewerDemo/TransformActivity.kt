package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.rememberPreviewerState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.GridLayout
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme

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
    val imageViewerState = rememberPreviewerState()
    val transformContentState = rememberTransformContentState()
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
                var itemState = rememberTransformItemState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F)
                ) {
                    TransformContent(
                        contentState = transformContentState,
                        itemState = { itemState = it }
                    ) {
                        Image(
                            modifier = Modifier
                                .clickable {
                                    transformContentState.start(itemState)
                                }
                                .fillMaxWidth()
                                .aspectRatio(1F),
                            painter = painterResource(id = item),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (
            transformContentState.displayCompose != null
            && transformContentState.onAction
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = LocalDensity.current.run { transformContentState.displaySize.width.toDp() },
                        height = LocalDensity.current.run { transformContentState.displaySize.height.toDp() },
                    )
                    .offset(
                        x = LocalDensity.current.run { transformContentState.displayPosition.x.toDp() + 10.dp },
                        y = LocalDensity.current.run { transformContentState.displayPosition.y.toDp() + 10.dp },
                    )
                    .background(MaterialTheme.colors.error.copy(0.8F))
            ) {
                transformContentState.displayCompose!!()
            }
        }
    }
}

class TransformContentState {

    var itemState: TransformItemState? by mutableStateOf(null)

    var displayPosition: Offset by mutableStateOf(Offset.Zero)

    var displaySize: IntSize by mutableStateOf(IntSize.Zero)

    var displayCompose: (@Composable () -> Unit)? by mutableStateOf(null)

    var onAction by mutableStateOf(false)

    fun start(transformItemState: TransformItemState) {
        itemState = transformItemState
        displayPosition = transformItemState.blockPosition.copy()
        displaySize = transformItemState.blockSize
        displayCompose = transformItemState.blockCompose
        onAction = true
    }

    fun end() {
        onAction = false
    }

}

@Composable
fun rememberTransformContentState(): TransformContentState {
    return remember { TransformContentState() }
}

class TransformItemState(
    var blockPosition: Offset = Offset.Zero,
    var blockSize: IntSize = IntSize.Zero,
    var blockCompose: (@Composable () -> Unit) = {},
)

@Composable
fun rememberTransformItemState(): TransformItemState {
    return remember { TransformItemState() }
}

@Composable
fun TransformContent(
    modifier: Modifier = Modifier,
    contentState: TransformContentState,
    itemState: (TransformItemState) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val transformItemState = rememberTransformItemState()
    itemState(transformItemState)
    transformItemState.blockCompose = remember {
        movableContentOf {
            content()
        }
    }
    Box(
        modifier = modifier
            .onGloballyPositioned {
                transformItemState.blockPosition = it.positionInRoot()
                transformItemState.blockSize = it.size
            }
            .fillMaxSize()
    ) {
        if (
            contentState.itemState != transformItemState || !contentState.onAction
        ) {
            transformItemState.blockCompose()
        }
    }
}