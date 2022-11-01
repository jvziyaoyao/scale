package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.TransformImageView
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.rememberViewerState
import com.origeek.viewerDemo.base.BaseActivity
import kotlinx.coroutines.launch

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-31 18:03
 **/
class EasyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBasicContent {
            EasyBody()
        }
    }

}

@Composable
fun EasyBody() {
    val images = mapOf(
        "001" to R.drawable.img_01,
        "002" to R.drawable.img_02,
    ).entries.toList()
    val scope = rememberCoroutineScope()
    val previewerState = rememberPreviewerState(verticalDragEnable = true) { index ->
        images[index].key
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for ((index, imageItem) in images.withIndex()) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(2.dp)
            ) {
                TransformImageView(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch {
                                previewerState.openTransform(index)
                            }
                        }
                    },
                    key = imageItem.key,
                    painter = painterResource(id = imageItem.value),
                    previewerState = previewerState,
                )
            }
        }
    }
    ImagePreviewer(
        modifier = Modifier.fillMaxSize(),
        count = images.size,
        state = previewerState,
        imageLoader = { index ->
            painterResource(id = images[index].value)
        },
        detectGesture = {
            onTap = {
                scope.launch {
                    previewerState.closeTransform()
                }
            }
        }
    )
}