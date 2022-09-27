package com.origeek.viewerDemo

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.error.copy(0.2F))
        .pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = {},
                onDragEnd = {},
                onDragCancel = {},
                onVerticalDrag = { _, _ ->

                }
            )
            detectVerticalDragGestures { change, dragAmount ->
                Log.i("TAG", "NormalBody: detectVerticalDragGestures ${change.position}")
            }
        }) {
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
}