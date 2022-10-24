package com.origeek.imageViewer.previewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.origeek.imageViewer.viewer.ImageViewerState
import com.origeek.imageViewer.viewer.rememberViewerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-17 14:45
 **/

class ViewerContainerState(
    // 协程作用域
    var scope: CoroutineScope = MainScope(),
    // 转换图层的状态
    var transformState: TransformContentState = TransformContentState(),
    // viewer的状态
    var imageViewerState: ImageViewerState = ImageViewerState(),
    // 默认动画窗格
    var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC
) {

    /**
     *   +-------------------+
     *         INTERNAL
     *   +-------------------+
     */

    // 转换图层transformContent透明度
    internal var transformContentAlpha = Animatable(0F)

    // viewer容器的透明度
    internal var viewerContainerAlpha = Animatable(1F)

    // 是否允许界面显示loading
    internal var allowLoading by mutableStateOf(true)

    // 打开图片后到加载成功过程的协程任务
    internal var openTransformJob: Deferred<Unit>? = null

    /**
     * 取消打开动作
     */
    internal fun cancelOpenTransform() {
        openTransformJob?.cancel()
        openTransformJob = null
    }

    /**
     * 等待挂载成功
     */
    internal suspend fun awaitOpenTransform() {
        // 这里需要等待viewer挂载，显示loading界面
        openTransformJob = scope.async {
            // 等待viewer加载
            awaitViewerLoading()
            // viewer加载成功后显示viewer
            transformSnapToViewer(true)
        }
        openTransformJob?.await()
        openTransformJob = null
    }

    /**
     * 等待viewer挂载成功
     */
    internal suspend fun awaitViewerLoading() {
        imageViewerState.mountedFlow.apply {
            withContext(Dispatchers.Default) {
                takeWhile { !it }.collect()
            }
        }
    }

    /**
     * 转换图层转viewer图层，true显示viewer，false显示转换图层
     * @param isViewer Boolean
     */
    internal suspend fun transformSnapToViewer(isViewer: Boolean) {
        if (isViewer) {
            transformContentAlpha.snapTo(0F)
            viewerContainerAlpha.snapTo(1F)
        } else {
            transformContentAlpha.snapTo(1F)
            viewerContainerAlpha.snapTo(0F)
        }
    }

    /**
     * 将viewer容器的位置大小复制给transformContent
     */
    internal suspend fun copyViewerContainerStateToTransformState() {
        transformState.apply {
            val targetScale = scale.value * fitScale
            graphicScaleX.snapTo(targetScale)
            graphicScaleY.snapTo(targetScale)
            val centerOffsetY = (containerSize.height - realSize.height).div(2)
            val centerOffsetX = (containerSize.width - realSize.width).div(2)
            offsetY.snapTo(centerOffsetY + this@ViewerContainerState.offsetY.value)
            offsetX.snapTo(centerOffsetX + this@ViewerContainerState.offsetX.value)
        }
    }

    /**
     * 将viewer的位置大小等信息复制给transformContent
     * @param itemState TransformItemState
     */
    internal suspend fun copyViewerPosToContent(itemState: TransformItemState) {
        transformState.apply {
            // 更新itemState，确保itemState一致
            this@apply.itemState = itemState
            // 确保viewer的容器大小与transform的容器大小一致
            containerSize = imageViewerState.containerSize
            val scale = imageViewerState.scale
            val offsetX = imageViewerState.offsetX
            val offsetY = imageViewerState.offsetY
            // 计算transform的实际大小
            val rw = fitSize.width * scale.value
            val rh = fitSize.height * scale.value
            // 计算目标平移量
            val goOffsetX =
                (containerSize.width - rw).div(2) + offsetX.value
            val goOffsetY =
                (containerSize.height - rh).div(2) + offsetY.value
            // 计算缩放率
            val fixScale = fitScale * scale.value

            // 更新值
            graphicScaleX.snapTo(fixScale)
            graphicScaleY.snapTo(fixScale)
            displayWidth.snapTo(displayRatioSize.width)
            displayHeight.snapTo(displayRatioSize.height)
            this@apply.offsetX.snapTo(goOffsetX)
            this@apply.offsetY.snapTo(goOffsetY)
        }
    }

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
        val Saver: Saver<ViewerContainerState, *> = mapSaver(
            save = {
                mapOf<String, Any>(
                    it::offsetX.name to it.offsetX.value,
                    it::offsetY.name to it.offsetY.value,
                    it::scale.name to it.scale.value,
                )
            },
            restore = {
                val viewerContainerState = ViewerContainerState()
                viewerContainerState.offsetX =
                    Animatable(it[viewerContainerState::offsetX.name] as Float)
                viewerContainerState.offsetY =
                    Animatable(it[viewerContainerState::offsetY.name] as Float)
                viewerContainerState.scale =
                    Animatable(it[viewerContainerState::scale.name] as Float)
                viewerContainerState
            }
        )
    }
}

@Composable
fun rememberViewerContainerState(
    scope: CoroutineScope = rememberCoroutineScope(),
    viewerState: ImageViewerState = rememberViewerState(),
    animationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
): ViewerContainerState {
    val transformContentState = rememberTransformContentState()
    val viewerContainerState = rememberSaveable(saver = ViewerContainerState.Saver) {
        ViewerContainerState()
    }
    viewerContainerState.scope = scope
    viewerContainerState.imageViewerState = viewerState
    viewerContainerState.transformState = transformContentState
    viewerContainerState.defaultAnimationSpec = animationSpec
    return viewerContainerState
}

@Composable
internal fun ImageViewerContainer(
    modifier: Modifier = Modifier,
    containerState: ViewerContainerState,
    placeholder: PreviewerPlaceholder = PreviewerPlaceholder(),
    viewer: @Composable () -> Unit,
) {
    containerState.apply {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    containerSize = it.size
                }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                }
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
            val viewerMounted by imageViewerState.mountedFlow.collectAsState(
                initial = false
            )
            if (allowLoading) AnimatedVisibility(
                visible = !viewerMounted,
                enter = placeholder.enterTransition,
                exit = placeholder.exitTransition,
            ) {
                placeholder.content()
            }
        }
    }
}