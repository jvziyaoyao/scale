package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.origeek.imageViewer.gallery.ImageGallery
import com.origeek.imageViewer.gallery.ImageGallery01
import com.origeek.imageViewer.gallery.rememberImageGalleryState
import com.origeek.imageViewer.gallery.rememberImageGalleryState01
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter

class GalleryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
//            GalleryBody()
            GalleryBody01()
//            GalleryBody02()
        }
    }

}

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
        state = rememberImageGalleryState { images.size },
        imageLoader = { index ->
            val image = images[index]
            rememberCoilImagePainter(image = image)
        },
    )
}

@Composable
fun GalleryBody01() {
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    val galleryState = rememberImageGalleryState01 { images.size }
    ImageGallery01(state = galleryState) { page ->
        val image = images[page]
        val painter = rememberCoilImagePainter(image)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (page % 2 == 0)
                        Color.Red.copy(0.2F) else Color.Blue.copy(0.2F)
                )
        ) {
            ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryBody02() {
    val pagerState = rememberPagerState { 4 }
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            lowVelocityAnimationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
            highVelocityAnimationSpec = rememberSplineBasedDecay(),
        )
    ) { page ->
        val bgColor = if (page % 2 == 0) Color.Red else Color.Blue
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor.copy(0.2F))
        ) {
            Text(modifier = Modifier.align(Alignment.Center), text = "${page + 1}")
        }
    }
}
