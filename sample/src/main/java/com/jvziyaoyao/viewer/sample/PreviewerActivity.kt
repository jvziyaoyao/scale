package com.jvziyaoyao.viewer.sample

import android.os.Bundle
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.image.previewer.ImagePreviewer
import com.jvziyaoyao.image.viewer.sample.R
import com.jvziyaoyao.viewer.sample.base.BaseActivity
import com.jvziyaoyao.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.zoomable.previewer.TransformLayerScope
import com.jvziyaoyao.zoomable.previewer.rememberPreviewerState
import com.origeek.ui.common.util.hideSystemUI
import com.origeek.ui.common.util.showSystemUI
import kotlinx.coroutines.launch

const val SYSTEM_UI_VISIBILITY = "SYSTEM_UI_VISIBILITY"

class PreviewerActivity : BaseActivity() {

    private var systemUIVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerSystemUI(savedInstanceState?.getBoolean(SYSTEM_UI_VISIBILITY) ?: true)
        setBasicContent {
            PreviewerBody {
                if (systemUIVisible != !it) {
                    handlerSystemUI(!it)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SYSTEM_UI_VISIBILITY, systemUIVisible)
        super.onSaveInstanceState(outState)
    }

    private fun handlerSystemUI(visible: Boolean) {
        systemUIVisible = visible
        if (systemUIVisible) {
            showSystemUI(window)
        } else {
            hideSystemUI(window)
        }
    }

}

@Composable
fun PreviewerBody(
    onImageViewVisible: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val images = remember {
        listOf(
            R.drawable.img_01,
            R.drawable.img_02,
            R.drawable.img_03,
            R.drawable.img_04,
            R.drawable.img_05,
            R.drawable.img_06,
        )
    }

    val previewerState = rememberPreviewerState(pageCount = { images.size })
//    val previewerState = rememberPopupPreviewerState(pageCount = { images.size })
    if (previewerState.visible) BackHandler {
        scope.launch {
            previewerState.close()
        }
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val horizontal = maxWidth > maxHeight
        val lineCount = if (horizontal) 6 else 3
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
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
                                painter = painterResource(id = item),
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
                val painter = painterResource(id = images[index])
                Pair(painter, painter.intrinsicSize)
            }
        )
//        PopupPreviewer(
//            state = previewerState,
//            detectGesture = PagerGestureScope(
//                onTap = {
//                    scope.launch {
//                        previewerState.close()
//                    }
//                }
//            ),
//            previewerDecoration = { innerBox ->
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.Black.copy(0.8F))
//                ) {
//                    innerBox()
//                }
//            },
//            zoomablePolicy = { index ->
//                val painter = painterResource(id = images[index])
//                ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
//                    Image(
//                        modifier = Modifier.fillMaxSize(),
//                        painter = painterResource(id = images[index]),
//                        contentDescription = null
//                    )
//                }
//            }
//        )
    }
}
