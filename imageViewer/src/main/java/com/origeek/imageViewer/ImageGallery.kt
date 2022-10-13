package com.origeek.imageViewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-10 11:50
 **/

class GalleryGestureScope(
    var onTap: () -> Unit = {},
    var onDoubleTap: () -> Boolean = { false },
    var onLongPress: () -> Unit = {},
)

class GalleryLayerScope(
    var viewerContainer: @Composable (viewer: @Composable () -> Unit) -> Unit = { it() },
    var background: @Composable ((Int) -> Unit) = {},
    var foreground: @Composable ((Int) -> Unit) = {},
)

@Composable
fun ImageGallery(
    modifier: Modifier = Modifier,
    count: Int,
    state: ImagePagerState = rememberImagePagerState(),
    imageLoader: @Composable (Int) -> Any?,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    currentViewerState: (ImageViewerState) -> Unit = {},
    detectGesture: GalleryGestureScope.() -> Unit = {},
    galleryLayer: GalleryLayerScope.() -> Unit = {},
) {
    require(count >= 0) { "imageCount must be >= 0" }
    val scope = rememberCoroutineScope()
    // 手势相关
    val galleryGestureScope = remember { GalleryGestureScope() }
    detectGesture.invoke(galleryGestureScope)
    // 图层相关
    val galleryLayerScope = remember { GalleryLayerScope() }
    galleryLayer.invoke(galleryLayerScope)
    // 确保不会越界
    val currentPage by remember {
        derivedStateOf {
            if (state.currentPage >= count) {
                if (count > 0) count - 1 else 0
            } else state.currentPage
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        galleryLayerScope.background(currentPage)
        ImageHorizonPager(
            count = count,
            state = state,
            modifier = Modifier
                .fillMaxSize(),
            itemSpacing = itemSpacing,
        ) { page ->
            val imageState = rememberViewerState()
            LaunchedEffect(key1 = currentPage) {
                if (currentPage != page) imageState.reset()
                if (currentPage == page) currentViewerState(imageState)
            }
            galleryLayerScope.viewerContainer {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    key(count, page) {
                        ImageViewer(
                            modifier = Modifier.fillMaxSize(),
                            model = imageLoader(page),
                            state = imageState,
                            boundClip = false,
                            detectGesture = {
                                this.onTap = {
                                    galleryGestureScope.onTap()
                                }
                                this.onDoubleTap = {
                                    val consumed = galleryGestureScope.onDoubleTap()
                                    if (!consumed) scope.launch {
                                        imageState.toggleScale(it)
                                    }
                                }
                                this.onLongPress = { galleryGestureScope.onLongPress() }
                            },
                        )
                    }
                }
            }
        }
        galleryLayerScope.foreground(currentPage)
    }
}