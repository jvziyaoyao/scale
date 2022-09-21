package com.origeek.viewerDemo

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.request.ImageRequest
import com.origeek.imageViewer.rememberPreviewerState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.GridLayout
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
                        val painter = painterResource(id = item)
                        LaunchedEffect(key1 = painter.intrinsicSize) {
                            if (painter.intrinsicSize.isSpecified) {
                                itemState.intrinsicSize = painter.intrinsicSize
                            }
                        }
                        Image(
                            modifier = Modifier
                                .clickable {
                                    if (transformContentState.onAction) {
                                        transformContentState.end()
                                    } else {
                                        transformContentState.start(itemState)
                                    }
                                }
                                .fillMaxSize(),
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    var bSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                bSize = it.size
            },
    ) {
        if (
            transformContentState.displayCompose != null
            && transformContentState.onAction
        ) {
            val bw = remember { Animatable(transformContentState.displaySize.width.toFloat()) }
            val bh = remember { Animatable(transformContentState.displaySize.height.toFloat()) }
            val bx = remember { Animatable(transformContentState.displayPosition.x) }
            val by = remember { Animatable(transformContentState.displayPosition.y) }
            // 容器比例
            val bRatio by remember {
                derivedStateOf {
                    bSize.width.toFloat() / bSize.height.toFloat()
                }
            }
            // 图片原始比例
            val oRatio by remember {
                derivedStateOf {
                    transformContentState.displayIntrinsicSize!!.width / transformContentState.displayIntrinsicSize!!.height
                }
            }
            // 是否宽度与容器大小一致
            var widthFixed by remember { mutableStateOf(false) }
            // 显示大小
            val uSize by remember {
                derivedStateOf {
                    if (oRatio > bRatio) {
                        // 宽度一致
                        val uW = bSize.width
                        val uH = uW / oRatio
                        widthFixed = true
                        IntSize(uW, uH.toInt())
                    } else {
                        // 高度一致
                        val uH = bSize.height
                        val uW = uH * oRatio
                        widthFixed = false
                        IntSize(uW.toInt(), uH)
                    }
                }
            }
            val ux by remember {
                derivedStateOf {
                    if (widthFixed) {
                        0F
                    } else {
                        (bSize.width - uSize.width).div(2).toFloat()
                    }
                }
            }
            val uy by remember {
                derivedStateOf {
                    if (!widthFixed) {
                        0F
                    } else {
                        (bSize.height - uSize.height).div(2).toFloat()
                    }
                }
            }
            val animationSpec: TweenSpec<Float> = tween(400)
            LaunchedEffect(key1 = transformContentState.onActionTarget) {
                if (transformContentState.onActionTarget) {
                    scope.launch {
                        bw.animateTo(
                            uSize.width.toFloat(),
                            animationSpec = animationSpec
                        )
                    }
                    scope.launch {
                        bh.animateTo(
                            uSize.height.toFloat(),
                            animationSpec = animationSpec
                        )
                    }
                    scope.launch { bx.animateTo(ux, animationSpec = animationSpec) }
                    scope.launch { by.animateTo(uy, animationSpec = animationSpec) }
                } else {
                    val mutex = Mutex()
                    var endCount = 0
                    fun goEndAction(endAction: suspend () -> Unit) {
                        scope.launch {
                            endAction()
                            mutex.withLock {
                                endCount++
                                if (endCount >= 4) {
                                    transformContentState.callEnd()
                                }
                            }
                        }
                    }
                    goEndAction {
                        bw.animateTo(
                            transformContentState.displaySize.width.toFloat(),
                            animationSpec = animationSpec
                        )
                    }
                    goEndAction {
                        bh.animateTo(
                            transformContentState.displaySize.height.toFloat(),
                            animationSpec = animationSpec
                        )
                    }
                    goEndAction {
                        bx.animateTo(transformContentState.displayPosition.x, animationSpec = animationSpec)
                    }
                    goEndAction {
                        by.animateTo(transformContentState.displayPosition.y, animationSpec = animationSpec)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(
                        width = LocalDensity.current.run { bw.value.toDp() },
                        height = LocalDensity.current.run { bh.value.toDp() },
                    )
                    .offset(
                        x = LocalDensity.current.run { bx.value.toDp() },
                        y = LocalDensity.current.run { by.value.toDp() },
                    )
                    .background(MaterialTheme.colors.error.copy(0.2F)),
            ) {
                transformContentState.displayCompose!!()
            }
        }
    }
}

class TransformContentState {

    var itemState: TransformItemState? by mutableStateOf(null)

    val displayIntrinsicSize: Size?
        get() = itemState?.intrinsicSize

    var displayPosition: Offset by mutableStateOf(Offset.Zero)

    var displaySize: IntSize by mutableStateOf(IntSize.Zero)

    var displayCompose: (@Composable () -> Unit)? by mutableStateOf(null)

    var onAction by mutableStateOf(false)

    var onActionTarget by mutableStateOf(false)

    fun start(transformItemState: TransformItemState) {
        itemState = transformItemState
        displayPosition = transformItemState.blockPosition.copy()
        displaySize = IntSize(
            width = transformItemState.blockSize.width,
            height = transformItemState.blockSize.height
        )
        displayCompose = transformItemState.blockCompose
        onActionTarget = true
        onAction = true
    }

    fun end() {
        onActionTarget = false
    }

    fun callEnd() {
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
    var intrinsicSize: Size? = null,
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