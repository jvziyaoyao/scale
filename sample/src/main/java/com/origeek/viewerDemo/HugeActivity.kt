package com.origeek.viewerDemo

import android.graphics.BitmapRegionDecoder
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
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
    val textColor = MaterialTheme.colors.onBackground.copy(0.4F)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(0.01F))
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 120.dp)
        ) {
            val state = rememberViewerState()
            ImageViewer(
                model = imageDecoder,
                state = state,
                debugMode = true,
                boundClip = false,
                onDoubleTap = {
                    scope.launch {
                        state.toggleScale(it)
                    }
                }
            )

            // 显示容器实际可视区域
            Box(
                modifier = Modifier
                    .background(Color.LightGray.copy(0.2F))
                    .fillMaxSize()
            ) {
                Text(text = "In container", color = textColor)
            }
        }
        Text(text = "Outside the container", color = textColor)
    }

}
