package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.origeek.imageViewer.zoomable.ZoomableGestureScope
import com.origeek.imageViewer.zoomable.ZoomableView
import com.origeek.imageViewer.zoomable.ZoomableViewState
import com.origeek.viewerDemo.base.BaseActivity
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-11-24 16:24
 **/
class ZoomableActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ZoomableBody()
//            ZoomableThirdBody()
        }
    }

}

@Composable
fun ZoomableBody() {
    val scope = rememberCoroutineScope()
    val painter = painterResource(id = R.drawable.img_01)
    val zoomableViewState = remember {
        ZoomableViewState(
            contentSize = painter.intrinsicSize,
        )
    }
    ZoomableView(
        state = zoomableViewState,
        debugMode = true,
        detectGesture = ZoomableGestureScope(
            onTap = {

            },
            onDoubleTap = {
                scope.launch {
                    zoomableViewState.toggleScale(it)
                }
            },
            onLongPress = {

            },
        ),
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
        )
    }
}

@Composable
fun ZoomableThirdBody() {
    val painter = painterResource(id = R.drawable.img_01)
    val zoomableState = rememberZoomState(contentSize = painter.intrinsicSize, maxScale = 20F)
    Image(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue.copy(0.2F))
            .zoomable(
                zoomState = zoomableState,
            ),
        painter = painter,
        contentDescription = null,
    )
}