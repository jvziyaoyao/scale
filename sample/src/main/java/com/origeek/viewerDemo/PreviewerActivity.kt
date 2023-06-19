package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.systemBarsPadding
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.ui.common.util.hideSystemUI
import com.origeek.ui.common.util.showSystemUI
import com.origeek.viewerDemo.base.BaseActivity
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

    val imageViewerState = rememberPreviewerState()

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
                                            imageViewerState.open(index = index)
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
        LaunchedEffect(key1 = imageViewerState.visible, block = {
            onImageViewVisible(imageViewerState.visible)
        })
        ImagePreviewer(
            count = images.size,
            state = imageViewerState,
            imageLoader = { index -> painterResource(id = images[index]) },
            detectGesture = {
                onTap = {
                    scope.launch {
                        imageViewerState.close()
                    }
                }
            },
        )
    }
}
