package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.origeek.imageViewer.ImageGallery
import com.origeek.imageViewer.ViewerResult
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme

class GalleryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ViewerDemoTheme {
                GalleryBody()
            }
        }
    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GalleryBody() {
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    ImageGallery(
        modifier = Modifier.fillMaxSize(),
        count = images.size,
        imageLoader = { index ->
            val image = images[index]
            rememberCoilImagePainter(image = image)
        },
    )
}
