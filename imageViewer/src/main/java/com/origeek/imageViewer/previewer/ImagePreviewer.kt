package com.origeek.imageViewer.previewer

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.gallery.GalleryGestureScope
import com.origeek.imageViewer.gallery.ImageGallery
import com.origeek.imageViewer.gallery.ImageGalleryState
import com.origeek.imageViewer.gallery.rememberImageGalleryState
import com.origeek.imageViewer.viewer.ImageViewerState
import kotlinx.coroutines.CoroutineScope

val DEEP_DARK_FANTASY = Color(0xFF000000)
val DEFAULT_ITEM_SPACE = 12.dp
val DEFAULT_SOFT_ANIMATION_SPEC = tween<Float>(320)

@Composable
fun DefaultPreviewerBackground() {
    Box(
        modifier = Modifier
            .background(DEEP_DARK_FANTASY)
            .fillMaxSize()
    )
}

class ImagePreviewerState internal constructor() : PreviewerVerticalDragState() {
    companion object {
        val Saver: Saver<ImagePreviewerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.animateContainerVisibleState.currentState,
                    it.uiAlpha.value,
                    it.transformContentAlpha.value,
                    it.viewerContainerAlpha.value,
                    it.visible,
                )
            },
            restore = {
                val previewerState = ImagePreviewerState()
                previewerState.animateContainerVisibleState = MutableTransitionState(it[0] as Boolean)
                previewerState.uiAlpha = Animatable(it[1] as Float)
                previewerState.transformContentAlpha = Animatable(it[2] as Float)
                previewerState.viewerContainerAlpha = Animatable(it[3] as Float)
                previewerState.visible = it[4] as Boolean
                previewerState
            }
        )
    }
}

@Composable
fun rememberPreviewerState(
    scope: CoroutineScope = rememberCoroutineScope(),
    animationSpec: AnimationSpec<Float>? = null,
): ImagePreviewerState {
    val galleryState = rememberImageGalleryState()
    val transformState = rememberTransformContentState()
    val viewerContainerState = rememberViewerContainerState()
    val previewerState = rememberSaveable(saver = ImagePreviewerState.Saver) {
        ImagePreviewerState()
    }
    previewerState.galleryState = galleryState
    previewerState.transformState = transformState
    previewerState.viewerContainerState = viewerContainerState
    previewerState.scope = scope
    if (animationSpec != null) previewerState.defaultAnimationSpec = animationSpec
    return previewerState
}

@OptIn(ExperimentalAnimationApi::class)
val DEFAULT_PREVIEWER_ENTER_TRANSITION =
    scaleIn(tween(180)) + fadeIn(tween(240))

@OptIn(ExperimentalAnimationApi::class)
val DEFAULT_PREVIEWER_EXIT_TRANSITION =
    scaleOut(tween(320)) + fadeOut(tween(240))

val DEFAULT_CROSS_FADE_ANIMATE_SPEC: AnimationSpec<Float> = tween(80)

val DEFAULT_PLACEHOLDER_ENTER_TRANSITION = fadeIn(tween(200))
val DEFAULT_PLACEHOLDER_EXIT_TRANSITION = fadeOut(tween(200))

val DEFAULT_PREVIEWER_PLACEHOLDER_CONTENT = @Composable {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White.copy(0.2F))
    }
}

class PreviewerPlaceholder(
    var enterTransition: EnterTransition = DEFAULT_PLACEHOLDER_ENTER_TRANSITION,
    var exitTransition: ExitTransition = DEFAULT_PLACEHOLDER_EXIT_TRANSITION,
    var content: @Composable () -> Unit = DEFAULT_PREVIEWER_PLACEHOLDER_CONTENT,
)

class PreviewerLayerScope(
    var viewerContainer: @Composable (
        page: Int, viewerState: ImageViewerState, viewer: @Composable () -> Unit
    ) -> Unit = { _, _, viewer -> viewer() },
    var background: @Composable ((page: Int) -> Unit) = { _ -> DefaultPreviewerBackground() },
    var foreground: @Composable ((page: Int) -> Unit) = { _ -> },
    var placeholder: PreviewerPlaceholder = PreviewerPlaceholder()
)

@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    count: Int,
    state: ImagePreviewerState = rememberPreviewerState(),
    imageLoader: @Composable (Int) -> Any?,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    detectGesture: GalleryGestureScope.() -> Unit = {},
    previewerLayer: PreviewerLayerScope.() -> Unit = {},
) {
    state.apply {
        // 图层相关
        val layerScope = remember { PreviewerLayerScope() }
        previewerLayer.invoke(layerScope)
        LaunchedEffect(
            key1 = animateContainerVisibleState,
            key2 = animateContainerVisibleState.currentState
        ) {
            onAnimateContainerStateChanged()
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visibleState = animateContainerVisibleState,
            enter = enterTransition ?: enter,
            exit = exitTransition ?: exit,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(getKey) {
                        verticalDrag(this)
                    }
            ) {
                @Composable
                fun UIContainer(content: @Composable () -> Unit) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(uiAlpha.value)
                    ) {
                        content()
                    }
                }
                ImageGallery(
                    modifier = modifier.fillMaxSize(),
                    count = count,
                    state = galleryState,
                    imageLoader = imageLoader,
                    itemSpacing = itemSpacing,
                    detectGesture = detectGesture,
                    galleryLayer = {
                        this.viewerContainer = { page, viewerState, viewer ->
                            layerScope.viewerContainer(page, viewerState) {
                                ImageViewerContainer(
                                    containerState = viewerContainerState,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(transformContentAlpha.value)
                                    ) {
                                        TransformContentView(transformState)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(viewerContainerAlpha.value)
                                    ) {
                                        viewer()
                                    }
                                    val viewerMounted by viewerState.mountedFlow.collectAsState(initial = false)
                                    if (allowLoading) AnimatedVisibility(
                                        visible = !viewerMounted,
                                        enter = layerScope.placeholder.enterTransition,
                                        exit = layerScope.placeholder.exitTransition,
                                    ) {
                                        layerScope.placeholder.content()
                                    }
                                }
                            }
                        }
                        this.background = {
                            UIContainer {
                                layerScope.background(it)
                            }
                        }
                        this.foreground = {
                            UIContainer {
                                layerScope.foreground(it)
                            }
                        }
                    },
                )
                if (!visible)
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { } }) { }
            }
        }
        ticket.Next()
    }
}