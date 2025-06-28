package com.jvziyaoyao.scale.sample.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.sample.base.BackHandler
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.scale.zoomable.previewer.TransformLayerScope
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import scale.sample_kmp.generated.resources.Res
import scale.sample_kmp.generated.resources.img_01
import scale.sample_kmp.generated.resources.img_02
import scale.sample_kmp.generated.resources.img_03
import scale.sample_kmp.generated.resources.img_04
import scale.sample_kmp.generated.resources.img_05
import scale.sample_kmp.generated.resources.img_06

@Composable
fun PreviewerBody(
    onImageViewVisible: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val images = remember {
        listOf(
            Res.drawable.img_01,
            Res.drawable.img_02,
            Res.drawable.img_03,
            Res.drawable.img_04,
            Res.drawable.img_05,
            Res.drawable.img_06,
        )
    }

    val previewerState = rememberPreviewerState(pageCount = { images.size })
    if (previewerState.visible) BackHandler {
        scope.launch {
            previewerState.close()
        }
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val horizontal = this@BoxWithConstraints.maxWidth > this@BoxWithConstraints.maxHeight
        val lineCount = if (horizontal) 6 else 3
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 100.dp)
        ) {
            LazyVerticalGrid(columns = GridCells.Fixed(lineCount)) {
                images.forEachIndexed { index, item ->
                    item {
                        val needStart = index % lineCount != 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1F)
                                .padding(start = if (needStart) 2.dp else 0.dp, bottom = 2.dp)
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        scope.launch {
                                            previewerState.open(index = index)
                                        }
                                    },
                                painter = painterResource(item),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(key1 = previewerState.visible, block = {
            onImageViewVisible(previewerState.visible)
        })
        ImagePreviewer(
            state = previewerState,
            detectGesture = PagerGestureScope(onTap = {
                scope.launch {
                    previewerState.close()
                }
            }),
            previewerLayer = TransformLayerScope(
                background = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.8F))
                    )
                }
            ),
            imageLoader = { index ->
                val painter = painterResource(images[index])
                Pair(painter, painter.intrinsicSize)
            }
        )
    }
}