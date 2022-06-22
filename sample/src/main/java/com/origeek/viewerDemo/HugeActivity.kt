package com.origeek.viewerDemo

import android.graphics.BitmapRegionDecoder
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.origeek.imageViewer.ImageDecoder
import com.origeek.imageViewer.ImageViewer
import com.origeek.imageViewer.rememberViewerState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.launch

class HugeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ViewerDemoTheme {
                HugeBody()
            }
        }
    }

}

@Composable
fun HugeBody() {
    val context = LocalContext.current
    val imageDecoder = remember {
        ImageDecoder(
            BitmapRegionDecoder.newInstance(
                context.assets.open("a350.jpg"),
                false
            )!!
        )
    }
    val scope = rememberCoroutineScope()
    val state = rememberViewerState()
    ImageViewer(
        model = imageDecoder,
        state = state,
        boundClip = false,
        onDoubleTap = {
            scope.launch {
                state.toggleScale(it)
            }
        }
    )

}
