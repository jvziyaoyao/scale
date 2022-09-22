package com.origeek.imageViewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-09-22 10:13
 **/

@Composable
fun TransformImageView(
    painter: Painter,
    transformContentState: TransformContentState = rememberTransformContentState(),
) {
    var itemState = rememberTransformItemState()
    TransformItemView(
        contentState = transformContentState,
        itemState = { itemState = it }
    ) {
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

@Composable
fun TransformContentView(
    transformContentState: TransformContentState = rememberTransformContentState(),
) {
    var bSize by remember { mutableStateOf(IntSize.Zero) }
    if (transformContentState.contentVisible) Box(
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
            val uOffset by remember { derivedStateOf { Offset(ux, uy) } }
            LaunchedEffect(key1 = transformContentState.onActionTarget) {
                if (transformContentState.onActionTarget) {
                    transformContentState.enterTransform(uSize, uOffset)
                } else {
                    transformContentState.exitTransform()
                }
            }
            Box(
                modifier = Modifier
                    .size(
                        width = LocalDensity.current.run { transformContentState.bw.value.toDp() },
                        height = LocalDensity.current.run { transformContentState.bh.value.toDp() },
                    )
                    .offset(
                        x = LocalDensity.current.run { transformContentState.bx.value.toDp() },
                        y = LocalDensity.current.run { transformContentState.by.value.toDp() },
                    )
                    .background(MaterialTheme.colors.error.copy(0.2F)),
            ) {
                transformContentState.displayCompose!!()
            }
        }
    }
}

class TransformContentState(
    private val scope: CoroutineScope,
    var defaultAnimationSpec: AnimationSpec<Float> = SpringSpec()
) {

    var itemState: TransformItemState? by mutableStateOf(null)

    val displayIntrinsicSize: Size?
        get() = itemState?.intrinsicSize

    var displayPosition: Offset by mutableStateOf(Offset.Zero)

    var displaySize: IntSize by mutableStateOf(IntSize.Zero)

    var displayCompose: (@Composable () -> Unit)? by mutableStateOf(null)

    var onAction by mutableStateOf(false)

    var onActionTarget by mutableStateOf(false)

    var bw = Animatable(0F)

    var bh = Animatable(0F)

    var bx = Animatable(0F)

    var by = Animatable(0F)

    var contentVisible by mutableStateOf(true)

    private fun setPreStartParams() {
        bw = Animatable(displaySize.width.toFloat())
        bh = Animatable(displaySize.height.toFloat())
        bx = Animatable(displayPosition.x)
        by = Animatable(displayPosition.y)
    }

    private fun callEnd() {
        onAction = false
    }

    suspend fun exitTransform(
        animationSpec: AnimationSpec<Float> = defaultAnimationSpec
    ) = suspendCoroutine<Unit> { c ->
        val mutex = Mutex()
        var endCount = 0
        fun goCallEnd() {
            callEnd()
            c.resume(Unit)
        }

        fun goEndAction(endAction: suspend () -> Unit) {
            scope.launch {
                endAction()
                mutex.withLock {
                    endCount++
                    if (endCount >= 4) {
                        goCallEnd()
                    }
                }
            }
        }
        goEndAction {
            bw.animateTo(
                displaySize.width.toFloat(),
                animationSpec = animationSpec
            )
        }
        goEndAction {
            bh.animateTo(
                displaySize.height.toFloat(),
                animationSpec = animationSpec
            )
        }
        goEndAction {
            bx.animateTo(
                displayPosition.x,
                animationSpec = animationSpec
            )
        }
        goEndAction {
            by.animateTo(
                displayPosition.y,
                animationSpec = animationSpec
            )
        }
    }

    suspend fun enterTransform(
        uSize: IntSize,
        uOffset: Offset,
        animationSpec: AnimationSpec<Float> = defaultAnimationSpec
    ) = suspendCoroutine<Unit> { c ->
        val mutex = Mutex()
        var endCount = 0
        fun goCallEnd() {
            c.resume(Unit)
        }

        fun goEndAction(endAction: suspend () -> Unit) {
            scope.launch {
                endAction()
                mutex.withLock {
                    endCount++
                    if (endCount >= 4) {
                        goCallEnd()
                    }
                }
            }
        }
        goEndAction {
            bw.animateTo(
                uSize.width.toFloat(),
                animationSpec = animationSpec
            )
        }
        goEndAction {
            bh.animateTo(
                uSize.height.toFloat(),
                animationSpec = animationSpec
            )
        }
        goEndAction {
            bx.animateTo(uOffset.x, animationSpec = animationSpec)
        }
        goEndAction {
            by.animateTo(uOffset.y, animationSpec = animationSpec)
        }
    }

    fun start(transformItemState: TransformItemState) {
        itemState = transformItemState
        displayPosition = transformItemState.blockPosition.copy()
        displaySize = IntSize(
            width = transformItemState.blockSize.width,
            height = transformItemState.blockSize.height
        )
        displayCompose = transformItemState.blockCompose
        setPreStartParams()
        onActionTarget = true
        onAction = true
    }

    fun end() {
        onActionTarget = false
    }

}

@Composable
fun rememberTransformContentState(
    scope: CoroutineScope = rememberCoroutineScope()
): TransformContentState {
    return remember { TransformContentState(scope) }
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
fun TransformItemView(
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