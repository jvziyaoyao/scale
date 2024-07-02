package com.origeek.imageViewer.previewer

import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import com.jvziyaoyao.scale.image.previewer.defaultPreviewBackground
import com.jvziyaoyao.scale.zoomable.pager.DEFAULT_ITEM_SPACE
import com.jvziyaoyao.scale.zoomable.previewer.DEFAULT_PREVIEWER_ENTER_TRANSITION
import com.jvziyaoyao.scale.zoomable.previewer.DEFAULT_PREVIEWER_EXIT_TRANSITION
import com.jvziyaoyao.scale.zoomable.previewer.DEFAULT_SOFT_ANIMATION_SPEC
import com.jvziyaoyao.scale.zoomable.previewer.ItemStateMap
import com.jvziyaoyao.scale.zoomable.previewer.LocalTransformItemStateMap
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.origeek.imageViewer.gallery.GalleryGestureScope
import com.origeek.imageViewer.gallery.ImageGallery
import com.origeek.imageViewer.gallery.ImageGalleryState
import com.origeek.imageViewer.gallery.rememberImageGalleryState
import com.origeek.imageViewer.viewer.ImageViewerState
import com.origeek.imageViewer.viewer.commonDeprecatedText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * 预览组件的状态
 */
@Deprecated(
    message = commonDeprecatedText,
)
class ImagePreviewerState(
    // 协程作用域
    scope: CoroutineScope = MainScope(),
    // 默认动画窗格
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 预览状态
    galleryState: ImageGalleryState,
    // 用于获取transformItemState
    itemStateMap: ItemStateMap,
) : PreviewerVerticalDragState(
    scope,
    defaultAnimationSpec,
    galleryState = galleryState,
    itemStateMap = itemStateMap
) {
    companion object {
        fun getSaver(
            galleryState: ImageGalleryState,
            itemStateMap: ItemStateMap
        ): Saver<ImagePreviewerState, *> {
            return mapSaver(
                save = {
                    mapOf<String, Any>(
                        it::currentPage.name to it.currentPage,
                        it::animateContainerVisibleState.name to it.animateContainerVisibleState.currentState,
                        it::uiAlpha.name to it.uiAlpha.value,
                        it::visible.name to it.visible,
                    )
                },
                restore = {
                    val previewerState = ImagePreviewerState(
                        galleryState = galleryState,
                        itemStateMap = itemStateMap
                    )
                    previewerState.animateContainerVisibleState =
                        MutableTransitionState(it[ImagePreviewerState::animateContainerVisibleState.name] as Boolean)
                    previewerState.uiAlpha =
                        Animatable(it[ImagePreviewerState::uiAlpha.name] as Float)
                    previewerState.visible = it[ImagePreviewerState::visible.name] as Boolean
                    previewerState
                }
            )
        }
    }
}

/**
 * 记录预览组件状态
 */
@Deprecated(
    message = commonDeprecatedText,
)
@Composable
fun rememberPreviewerState(
    // 协程作用域
    scope: CoroutineScope = rememberCoroutineScope(),
    // 动画窗格
    animationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 开启垂直手势的类型
    verticalDragType: VerticalDragType = VerticalDragType.None,
    // 初始页码
    @IntRange(from = 0) initialPage: Int = 0,
    // 获取页数
    pageCount: () -> Int,
    // 提供给组件用于获取key的方法
    getKey: ((Int) -> Any)? = null,
): ImagePreviewerState {
    val itemStateMap = LocalTransformItemStateMap.current
    val galleryState = rememberImageGalleryState(initialPage, pageCount)
    val imagePreviewerState =
        rememberSaveable(saver = ImagePreviewerState.getSaver(galleryState, itemStateMap)) {
            ImagePreviewerState(galleryState = galleryState, itemStateMap = itemStateMap)
        }
    imagePreviewerState.scope = scope
    imagePreviewerState.getKey = getKey
    imagePreviewerState.defaultAnimationSpec = animationSpec
    imagePreviewerState.verticalDragType = verticalDragType
    return imagePreviewerState
}

// 默认淡入淡出动画窗格
val DEFAULT_CROSS_FADE_ANIMATE_SPEC: AnimationSpec<Float> = tween(80)

// 加载占位默认的进入动画
val DEFAULT_PLACEHOLDER_ENTER_TRANSITION = fadeIn(tween(200))

// 加载占位默认的退出动画
val DEFAULT_PLACEHOLDER_EXIT_TRANSITION = fadeOut(tween(200))

// 默认的加载占位
val DEFAULT_PREVIEWER_PLACEHOLDER_CONTENT = @Composable {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White.copy(0.2F))
    }
}

@Deprecated(
    message = commonDeprecatedText,
)
// 加载时的占位内容
class PreviewerPlaceholder(
    // 进入动画
    var enterTransition: EnterTransition = DEFAULT_PLACEHOLDER_ENTER_TRANSITION,
    // 退出动画
    var exitTransition: ExitTransition = DEFAULT_PLACEHOLDER_EXIT_TRANSITION,
    // 占位的内容
    var content: @Composable () -> Unit = DEFAULT_PREVIEWER_PLACEHOLDER_CONTENT,
)

/**
 * 预览图层对象
 */
@Deprecated(
    message = commonDeprecatedText,
)
class PreviewerLayerScope(
    // 包裹viewer的容器图层
    var viewerContainer: @Composable (
        page: Int, viewerState: ImageViewerState, viewer: @Composable () -> Unit
    ) -> Unit = { _, _, viewer -> viewer() },
    // 背景图层
    var background: @Composable ((page: Int) -> Unit) = { _ -> defaultPreviewBackground() },
    // 前景图层
    var foreground: @Composable ((page: Int) -> Unit) = { _ -> },
    // 加载时的占位内容
    var placeholder: PreviewerPlaceholder = PreviewerPlaceholder()
)

/**
 * 图片预览组件
 */
@Deprecated(
    message = "方法已弃用，请使用：com.jvziyaoyao.image.previewer.ImagePreviewer",
)
@Composable
fun ImagePreviewer(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 状态对象
    state: ImagePreviewerState,
    // 图片加载器
    imageLoader: @Composable (Int) -> Any?,
    // 图片间的间隔
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    // 进入动画
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    // 退出动画
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    // 检测手势
    detectGesture: GalleryGestureScope.() -> Unit = {},
    // 自定义previewer的各个图层
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
                    state = galleryState,
                    imageLoader = imageLoader,
                    itemSpacing = itemSpacing,
                    detectGesture = detectGesture,
                    galleryLayer = {
                        this.viewerContainer = { page, viewerState, viewer ->
                            layerScope.viewerContainer(page, viewerState) {
                                val viewerContainerState = rememberViewerContainerState(
                                    viewerState = viewerState,
                                    animationSpec = defaultAnimationSpec
                                )
                                LaunchedEffect(key1 = currentPage) {
                                    if (currentPage == page) {
                                        state.viewerContainerState = viewerContainerState
                                    }
                                }
                                ImageViewerContainer(
                                    modifier = Modifier.alpha(viewerAlpha.value),
                                    containerState = viewerContainerState,
                                    placeholder = layerScope.placeholder,
                                    viewer = viewer,
                                )
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