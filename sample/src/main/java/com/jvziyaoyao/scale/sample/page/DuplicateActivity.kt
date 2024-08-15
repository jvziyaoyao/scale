package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.sample.R
import com.jvziyaoyao.scale.sample.base.BaseActivity
import com.jvziyaoyao.scale.zoomable.previewer.LocalTransformItemStateMap
import com.jvziyaoyao.scale.zoomable.previewer.PreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.TransformItemState
import com.jvziyaoyao.scale.zoomable.previewer.TransformItemView
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.rememberTransformItemState
import com.origeek.ui.common.compose.DetectScaleGridGesture
import com.origeek.ui.common.compose.ScaleGrid
import kotlinx.coroutines.launch

class DuplicateActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            DuplicateBody()
        }
    }

}

@Composable
fun DuplicateBody() {
    val scope = rememberCoroutineScope()
    val imageIds = remember { listOf(R.drawable.img_03, R.drawable.img_06) }

    val itemStateMap01 = remember { mutableStateMapOf<Any, TransformItemState>() }
    val previewerState01 = rememberPreviewerState(
        verticalDragType = VerticalDragType.UpAndDown,
        transformItemStateMap = itemStateMap01,
        pageCount = { imageIds.size },
        getKey = { imageIds[it] },
    )
    if (previewerState01.canClose || previewerState01.animating) BackHandler {
        if (previewerState01.canClose) scope.launch {
            previewerState01.exitTransform()
        }
    }

    val itemStateMap02 = remember { mutableStateMapOf<Any, TransformItemState>() }
    val previewerState02 = rememberPreviewerState(
        verticalDragType = VerticalDragType.UpAndDown,
        transformItemStateMap = itemStateMap02,
        pageCount = { imageIds.size },
        getKey = { imageIds[it] },
    )
    if (previewerState02.canClose || previewerState02.animating) BackHandler {
        if (previewerState02.canClose) scope.launch {
            previewerState02.exitTransform()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalTransformItemStateMap provides itemStateMap01) {
            DuplicateRow(
                imageIds = imageIds,
                previewerState = previewerState01,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        CompositionLocalProvider(LocalTransformItemStateMap provides itemStateMap02) {
            DuplicateRow(
                imageIds = imageIds,
                previewerState = previewerState02,
            )
        }
    }

    ImagePreviewer(
        state = previewerState01,
        imageLoader = { index ->
            val painter = painterResource(imageIds[index])
            return@ImagePreviewer Pair(painter, painter.intrinsicSize)
        }
    )
    ImagePreviewer(
        state = previewerState02,
        imageLoader = { index ->
            val painter = painterResource(imageIds[index])
            return@ImagePreviewer Pair(painter, painter.intrinsicSize)
        }
    )
}

@Composable
fun DuplicateRow(
    imageIds: List<Int>,
    previewerState: PreviewerState,
) {
    val scope = rememberCoroutineScope()
    Row {
        imageIds.forEachIndexed { index, id ->
            val painter = painterResource(id)
            val itemState =
                rememberTransformItemState(
                    intrinsicSize = painter.intrinsicSize
                )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(2.dp)
            ) {
                ScaleGrid(
                    detectGesture = DetectScaleGridGesture(
                        onPress = {
                            scope.launch {
                                previewerState.enterTransform(index)
                            }
                        }
                    )
                ) {
                    TransformItemView(
                        key = id,
                        itemState = itemState,
                        transformState = previewerState,
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painter,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}