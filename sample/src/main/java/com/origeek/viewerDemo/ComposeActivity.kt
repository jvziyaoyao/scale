package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.origeek.imageViewer.viewer.ComposeModel
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
 * @create: 2023-05-22 21:58
 **/
class ComposeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBasicContent {
            ComposeBody()
        }
    }

}

@Composable
fun ComposeBody() {
    val scope = rememberCoroutineScope()
    val state = rememberViewerState()
    Column(modifier = Modifier.fillMaxSize()) {
        ImageViewer(
            model = ComposeModel {
                val painter = painterResource(id = R.drawable.light_02)
                LaunchedEffect(painter.intrinsicSize) {
                    if (painter.intrinsicSize.isSpecified) {
                        painter.intrinsicSize.apply {
                            // 更新控件的固有大小
                            updateIntrinsicSize(IntSize(width.toInt(), height.toInt()))
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painter,
                        contentDescription = null
                    )
                }
            },
            state = state,
            detectGesture = {
                onDoubleTap = {
                    scope.launch {
                        state.toggleScale(it)
                    }
                }
            })
    }
}