package com.origeek.imageViewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
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

val imageTransformMutex = Mutex()
val transformItemStateMap = mutableStateMapOf<Any, TransformItemState>()

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    painter: Painter,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState = rememberTransformContentState(),
) {
    val scope = rememberCoroutineScope()
    SideEffect {
        scope.launch {
            imageTransformMutex.withLock {
                transformItemStateMap[key] = itemState
            }
        }
    }
    TransformImageView(
        modifier = modifier,
        itemState = itemState,
        contentState = contentState,
    ) {
        LaunchedEffect(key1 = painter.intrinsicSize) {
            if (painter.intrinsicSize.isSpecified) {
                itemState.intrinsicSize = painter.intrinsicSize
            }
        }
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
    DisposableEffect(key1 = painter) {
        scope.launch {
            imageTransformMutex.withLock {
                transformItemStateMap.remove(key)
            }
        }
        onDispose {}
    }
}

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState = rememberTransformContentState(),
) {
    TransformImageView(
        modifier = modifier,
        itemState = itemState,
        contentState = contentState,
    ) {
        itemState.intrinsicSize = Size(
            bitmap.width.toFloat(),
            bitmap.height.toFloat()
        )
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState = rememberTransformContentState(),
) {
    TransformImageView(
        modifier = modifier,
        itemState = itemState,
        contentState = contentState,
    ) {
        LocalDensity.current.run {
            itemState.intrinsicSize = Size(
                imageVector.defaultWidth.toPx(),
                imageVector.defaultHeight.toPx(),
            )
        }
        Image(
            modifier = Modifier.fillMaxSize(),
            imageVector = imageVector,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState = rememberTransformContentState(),
    content: @Composable () -> Unit,
) {
    TransformItemView(
        modifier = modifier,
        itemState = itemState,
        contentState = contentState,
    ) {
        content()
    }
}

@Composable
fun TransformContentView(
    transformContentState: TransformContentState = rememberTransformContentState(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                transformContentState.containerSize = it.size
                transformContentState.containerOffset = it.positionInRoot()
            },
    ) {
        if (
            transformContentState.srcCompose != null
            && transformContentState.onAction
        ) {
            LaunchedEffect(key1 = transformContentState.onActionTarget) {
                if (transformContentState.onActionTarget == true) {
                    transformContentState.enterTransform()
                } else if (transformContentState.onActionTarget == false) {
                    transformContentState.exitTransform()
                }
            }
            Box(
                modifier = Modifier
                    .offset(
                        x = LocalDensity.current.run { (transformContentState.offsetX.value).toDp() },
                        y = LocalDensity.current.run { (transformContentState.offsetY.value).toDp() },
                    )
                    .size(
                        width = LocalDensity.current.run { transformContentState.displayWidth.value.toDp() },
                        height = LocalDensity.current.run { transformContentState.displayHeight.value.toDp() },
                    )
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0F, 0F)
                        scaleX = transformContentState.graphicScaleX.value
                        scaleY = transformContentState.graphicScaleY.value
                    },
            ) {
                transformContentState.srcCompose!!()
            }
        }
    }
}

class TransformContentState(
    private val scope: CoroutineScope,
    var defaultAnimationSpec: AnimationSpec<Float> = SpringSpec()
) {

    var itemState: TransformItemState? by mutableStateOf(null)

    val intrinsicSize: Size
        get() = itemState?.intrinsicSize ?: Size.Zero

    val intrinsicRatio: Float
        get() {
            if (intrinsicSize.height == 0F) return 1F
            return intrinsicSize.width.div(intrinsicSize.height)
        }

    val srcPosition: Offset
        get() {
            val offset = itemState?.blockPosition ?: Offset.Zero
            return offset.copy(x = offset.x - containerOffset.x, y = offset.y - containerOffset.y)
        }

    val srcSize: IntSize
        get() = itemState?.blockSize ?: IntSize.Zero

    val srcCompose: (@Composable () -> Unit)?
        get() = itemState?.blockCompose

    var onAction by mutableStateOf(false)

    var onActionTarget by mutableStateOf<Boolean?>(null)

    var displayWidth = Animatable(0F)

    var displayHeight = Animatable(0F)

    var graphicScaleX = Animatable(1F)

    var graphicScaleY = Animatable(1F)

    var offsetX = Animatable(0F)

    var offsetY = Animatable(0F)

    var containerOffset by mutableStateOf(Offset.Zero)

    var containerSize by mutableStateOf(IntSize.Zero)

    val containerRatio: Float
        get() {
            if (containerSize.height == 0) return 1F
            return containerSize.width.toFloat().div(containerSize.height)
        }

    val widthFixed: Boolean
        get() = intrinsicRatio > containerRatio

    val fitSize: Size
        get() {
            return if (intrinsicRatio > containerRatio) {
                // 宽度一致
                val uW = containerSize.width
                val uH = uW / intrinsicRatio
                Size(uW.toFloat(), uH)
            } else {
                // 高度一致
                val uH = containerSize.height
                val uW = uH * intrinsicRatio
                Size(uW, uH.toFloat())
            }
        }

    val fitOffsetX: Float
        get() {
            return if (widthFixed) {
                0F
            } else {
                (containerSize.width - fitSize.width).div(2).toFloat()
            }
        }

    val fitOffsetY: Float
        get() {
            return if (!widthFixed) {
                0F
            } else {
                (containerSize.height - fitSize.height).div(2).toFloat()
            }
        }

    val fitScale: Float
        get() = fitSize.width.div(displayRatioSize.width)

    val displayRatioSize: Size
        get() {
            return Size(width = srcSize.width.toFloat(), height = srcSize.width.div(intrinsicRatio))
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
            endAsyncCallBack?.invoke()
            endAsyncCallBack = null
            onActionTarget = null
        }

        fun goEndAction(endAction: suspend () -> Unit) {
            scope.launch {
                endAction()
                mutex.withLock {
                    endCount++
                    if (endCount >= 6) {
                        goCallEnd()
                    }
                }
            }
        }
        goEndAction {
            displayWidth.animateTo(srcSize.width.toFloat(), animationSpec)
        }
        goEndAction {
            displayHeight.animateTo(srcSize.height.toFloat(), animationSpec)
        }
        goEndAction {
            graphicScaleX.animateTo(1F, animationSpec)
        }
        goEndAction {
            graphicScaleY.animateTo(1F, animationSpec)
        }
        goEndAction {
            offsetX.animateTo(srcPosition.x, animationSpec)
        }
        goEndAction {
            offsetY.animateTo(srcPosition.y, animationSpec)
        }
    }

    suspend fun enterTransform(
        animationSpec: AnimationSpec<Float> = defaultAnimationSpec
    ) = suspendCoroutine<Unit> { c ->
        val mutex = Mutex()
        var endCount = 0
        fun goCallEnd() {
            c.resume(Unit)
            startAsyncCallBack?.invoke()
            startAsyncCallBack = null
            onActionTarget = null
        }

        fun goEndAction(endAction: suspend () -> Unit) {
            scope.launch {
                endAction()
                mutex.withLock {
                    endCount++
                    if (endCount >= 6) {
                        goCallEnd()
                    }
                }
            }
        }
        goEndAction {
            displayWidth.animateTo(displayRatioSize.width, animationSpec)
        }
        goEndAction {
            displayHeight.animateTo(displayRatioSize.height, animationSpec)
        }
        goEndAction {
            graphicScaleX.animateTo(fitScale, animationSpec)
        }
        goEndAction {
            graphicScaleY.animateTo(fitScale, animationSpec)
        }
        goEndAction {
            offsetX.animateTo(fitOffsetX, animationSpec)
        }
        goEndAction {
            offsetY.animateTo(fitOffsetY, animationSpec)
        }
    }

    fun start(transformItemState: TransformItemState) {
        itemState = transformItemState

        displayWidth = Animatable(srcSize.width.toFloat())
        displayHeight = Animatable(srcSize.height.toFloat())
        graphicScaleX = Animatable(1F)
        graphicScaleY = Animatable(1F)

        offsetX = Animatable(srcPosition.x)
        offsetY = Animatable(srcPosition.y)

        onActionTarget = true
        onAction = true
    }

    fun end() {
        onActionTarget = false
    }

    private var startAsyncCallBack: (() -> Unit)? = null

    suspend fun startAsync(transformItemState: TransformItemState) = suspendCoroutine<Unit> { c ->
        startAsyncCallBack = {
            c.resume(Unit)
        }
        start(transformItemState)
    }

    private var endAsyncCallBack: (() -> Unit)? = null

    suspend fun endAsync() = suspendCoroutine<Unit> { c ->
        endAsyncCallBack = {
            c.resume(Unit)
        }
        end()
    }

}

@Composable
fun rememberTransformContentState(
    scope: CoroutineScope = rememberCoroutineScope(),
    animationSpec: AnimationSpec<Float> = SpringSpec()
): TransformContentState {
    return remember { TransformContentState(scope, animationSpec) }
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
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState,
    content: @Composable () -> Unit,
) {
    itemState.blockCompose = remember {
        movableContentOf {
            content()
        }
    }
    Box(
        modifier = modifier
            .onGloballyPositioned {
                itemState.blockPosition = it.positionInRoot()
                itemState.blockSize = it.size
            }
            .fillMaxSize()
    ) {
        if (
            contentState.itemState != itemState || !contentState.onAction
        ) {
            itemState.blockCompose()
        }
    }
}