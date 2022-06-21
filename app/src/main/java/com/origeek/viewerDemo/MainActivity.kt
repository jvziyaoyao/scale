package com.origeek.viewerDemo

import android.os.Bundle
import android.view.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.insets.systemBarsPadding
import com.origeek.imageViewer.ImagePreviewer
import com.origeek.imageViewer.rememberPreviewerState
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.GridLayout
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme

const val SYSTEM_UI_VISIBILITY = "SYSTEM_UI_VISIBILITY"

class MainActivity : BaseActivity() {

    private var systemUIVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerSystemUI(savedInstanceState?.getBoolean(SYSTEM_UI_VISIBILITY) ?: true)
        setBasicContent {
            ViewerDemoTheme {
                MainBody {
                    if (systemUIVisible != !it) {
                        handlerSystemUI(!it)
                    }
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
fun MainBody(
    onImageViewVisible: (Boolean) -> Unit = {},
) {

    val images = listOf(
        painterResource(id = R.drawable.img_01),
        painterResource(id = R.drawable.img_02),
        painterResource(id = R.drawable.img_03),
        painterResource(id = R.drawable.img_04),
        painterResource(id = R.drawable.img_05),
        painterResource(id = R.drawable.img_06),
    )

    val imageViewerState = rememberPreviewerState()

    val lineCount = 3
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        GridLayout(
            columns = lineCount,
            size = images.size,
        ) { index ->
            val item = images[index]
            val lp = index % lineCount == 0
            val rp = index % lineCount == lineCount - 1
            val p = 2.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(
                        bottom = p * 2,
                        start = if (lp) 0.dp else p,
                        end = if (rp) 0.dp else p
                    ),
            ) {
                val gridModifier = Modifier
                    .clickable {
                        imageViewerState.show(index = index)
                    }
                    .fillMaxSize()
                Image(
                    painter = item,
                    modifier = gridModifier,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    LaunchedEffect(key1 = imageViewerState.show, block = {
        onImageViewVisible(imageViewerState.show)
    })
    ImagePreviewer(
        count = images.size,
        state = imageViewerState,
        imageLoader = { index -> images[index] },
        onTap = {
            imageViewerState.hide()
        }
    )
}

fun hideSystemUI(window: Window) {
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun showSystemUI(window: Window) {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}
