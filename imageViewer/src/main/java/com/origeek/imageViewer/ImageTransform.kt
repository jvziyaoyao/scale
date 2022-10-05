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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

internal val imageTransformMutex = Mutex()
internal val transformItemStateMap = mutableStateMapOf<Any, TransformItemState>()

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    painter: Painter,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    previewerState: ImagePreviewerState = rememberPreviewerState(),
) {
    TransformImageView(
        modifier = modifier,
        key = key,
        itemState = itemState,
        contentState = previewerState.transformState,
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
}

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    previewerState: ImagePreviewerState = rememberPreviewerState(),
) {
    TransformImageView(
        modifier = modifier,
        key = key,
        itemState = itemState,
        previewerState = previewerState,
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
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    previewerState: ImagePreviewerState = rememberPreviewerState(),
) {
    TransformImageView(
        modifier = modifier,
        key = key,
        itemState = itemState,
        previewerState = previewerState,
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
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    previewerState: ImagePreviewerState = rememberPreviewerState(),
    content: @Composable () -> Unit,
) = TransformImageView(modifier, key, itemState, previewerState.transformState, content)

@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState = rememberTransformContentState(),
    content: @Composable () -> Unit,
) {
    TransformItemView(
        modifier = modifier,
        key = key,
        itemState = itemState,
        contentState = contentState,
    ) {
        content()
    }
}

@Composable
fun rememberTransformItemState(): TransformItemState {
    return remember { TransformItemState() }
}

@Composable
fun TransformItemView(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    contentState: TransformContentState,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    itemState.blockCompose = remember {
        movableContentOf {
            content()
        }
    }
    SideEffect {
        scope.launch {
            imageTransformMutex.withLock {
                transformItemStateMap[key] = itemState
            }
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
    DisposableEffect(key1 = key) {
        scope.launch {
            imageTransformMutex.withLock {
                transformItemStateMap.remove(key)
            }
        }
        onDispose {}
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

class TransformContentState internal constructor() {

    lateinit var scope: CoroutineScope

    var defaultAnimationSpec: AnimationSpec<Float> = SpringSpec()

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

    fun setEnterState() {
        onAction = true
        onActionTarget = null
    }

    fun setExitState() {
        onAction = false
        onActionTarget = null
    }

    suspend fun exitTransform(
        animationSpec: AnimationSpec<Float>? = null
    ) = suspendCoroutine<Unit> { c ->
        val currentAnimateSpec = animationSpec ?: defaultAnimationSpec
        scope.launch {
            listOf(
                scope.async {
                    displayWidth.animateTo(srcSize.width.toFloat(), currentAnimateSpec)
                },
                scope.async {
                    displayHeight.animateTo(srcSize.height.toFloat(), currentAnimateSpec)
                },
                scope.async {
                    graphicScaleX.animateTo(1F, currentAnimateSpec)
                },
                scope.async {
                    graphicScaleY.animateTo(1F, currentAnimateSpec)
                },
                scope.async {
                    offsetX.animateTo(srcPosition.x, currentAnimateSpec)
                },
                scope.async {
                    offsetY.animateTo(srcPosition.y, currentAnimateSpec)
                },
            ).awaitAll()
            onAction = false
            onActionTarget = null
            c.resume(Unit)
        }
    }

    suspend fun enterTransform(
        itemState: TransformItemState,
        animationSpec: AnimationSpec<Float>? = null
    ) = suspendCoroutine<Unit> { c ->
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        this.itemState = itemState

        displayWidth = Animatable(srcSize.width.toFloat())
        displayHeight = Animatable(srcSize.height.toFloat())
        graphicScaleX = Animatable(1F)
        graphicScaleY = Animatable(1F)

        offsetX = Animatable(srcPosition.x)
        offsetY = Animatable(srcPosition.y)

        onActionTarget = true
        onAction = true

        scope.launch {
            listOf(
                scope.async {
                    displayWidth.animateTo(displayRatioSize.width, currentAnimationSpec)
                },
                scope.async {
                    displayHeight.animateTo(displayRatioSize.height, currentAnimationSpec)
                },
                scope.async {
                    graphicScaleX.animateTo(fitScale, currentAnimationSpec)
                },
                scope.async {
                    graphicScaleY.animateTo(fitScale, currentAnimationSpec)
                },
                scope.async {
                    offsetX.animateTo(fitOffsetX, currentAnimationSpec)
                },
                scope.async {
                    offsetY.animateTo(fitOffsetY, currentAnimationSpec)
                },
            ).awaitAll()
            c.resume(Unit)
            onActionTarget = null
        }
    }

    companion object {
        val Saver: Saver<TransformContentState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.onAction,
                )
            },
            restore = {
                val transformContentState = TransformContentState()
                transformContentState.onAction = it[0] as Boolean
                transformContentState
            }
        )
    }

}

@Composable
fun rememberTransformContentState(
    scope: CoroutineScope = rememberCoroutineScope(),
    animationSpec: AnimationSpec<Float> = SpringSpec()
): TransformContentState {
    val transformContentState = rememberSaveable(saver = TransformContentState.Saver) {
        TransformContentState()
    }
    transformContentState.scope = scope
    transformContentState.defaultAnimationSpec = animationSpec
    return transformContentState
}

class TransformItemState(
    var blockPosition: Offset = Offset.Zero,
    var blockSize: IntSize = IntSize.Zero,
    var blockCompose: (@Composable () -> Unit) = {},
    var intrinsicSize: Size? = null,
)