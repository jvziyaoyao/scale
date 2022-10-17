package com.origeek.imageViewer.previewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-17 14:45
 **/

class ViewerContainerState {

    internal lateinit var scope: CoroutineScope

    internal var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)

    var offsetX = Animatable(0F)

    var offsetY = Animatable(0F)

    var scale = Animatable(1F)

    suspend fun reset(animationSpec: AnimationSpec<Float>? = null) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        scope.apply {
            listOf(
                async {
                    offsetX.animateTo(0F, currentAnimationSpec)
                },
                async {
                    offsetY.animateTo(0F, currentAnimationSpec)
                },
                async {
                    scale.animateTo(1F, currentAnimationSpec)
                },
            ).awaitAll()
        }
    }

    suspend fun resetImmediately() {
        offsetX.snapTo(0F)
        offsetY.snapTo(0F)
        scale.snapTo(1F)
    }

    companion object {
        val Saver: Saver<ViewerContainerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.offsetX.value,
                    it.offsetY.value,
                    it.scale.value,
                )
            },
            restore = {
                val viewerContainerState = ViewerContainerState()
                viewerContainerState.offsetX = Animatable(it[0] as Float)
                viewerContainerState.offsetY = Animatable(it[1] as Float)
                viewerContainerState.scale = Animatable(it[2] as Float)
                viewerContainerState
            }
        )
    }
}

@Composable
fun rememberViewerContainerState(
    scope: CoroutineScope = rememberCoroutineScope(),
    animationSpec: AnimationSpec<Float>? = null,
): ViewerContainerState {
    val containerState = rememberSaveable(saver = ViewerContainerState.Saver) {
        ViewerContainerState()
    }
    containerState.scope = scope
    if (animationSpec != null) containerState.defaultAnimationSpec = animationSpec
    return containerState
}

@Composable
internal fun ImageViewerContainer(
    modifier: Modifier = Modifier,
    containerState: ViewerContainerState,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                containerState.containerSize = it.size
            }
            .graphicsLayer {
                scaleX = containerState.scale.value
                scaleY = containerState.scale.value
                translationX = containerState.offsetX.value
                translationY = containerState.offsetY.value
            }
    ) {
        content()
    }
}