package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.origeek.imageViewer.ImageViewer
import com.origeek.imageViewer.rememberViewerState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.launch

class NormalActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ViewerDemoTheme {
                NormalBody()
            }
        }
    }

}

@Composable
fun NormalBody() {
    val scope = rememberCoroutineScope()
    val state = rememberViewerState()
    ImageViewer(
        state = state,
        model = painterResource(id = R.drawable.light_02),
        modifier = Modifier.fillMaxSize(),
        onDoubleTap = {
            scope.launch {
                state.toggleScale(it)
            }
        }
    )
}