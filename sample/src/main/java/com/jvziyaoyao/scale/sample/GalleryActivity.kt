package com.jvziyaoyao.scale.sample

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.jvziyaoyao.scale.image.pager.ImagePager
import com.jvziyaoyao.scale.sample.base.BaseActivity
import com.jvziyaoyao.scale.sample.ui.component.rememberCoilImagePainter
import com.jvziyaoyao.scale.zoomable.pager.ZoomablePager
import com.jvziyaoyao.scale.zoomable.pager.rememberZoomablePagerState

class GalleryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            GalleryBody()
//            GalleryBody01()
//            GalleryBody02()
        }
    }

}

@Composable
fun GalleryBody() {
//    val images = remember {
//        mutableStateListOf(
//            "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
//            "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
//            "https://t7.baidu.com/it/u=1423490396,3473826719&fm=193&f=GIF",
//            "https://t7.baidu.com/it/u=938052523,709452322&fm=193&f=GIF",
//        )
//    }
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
//    ImagePager(
//        modifier = Modifier.fillMaxSize(),
//        pagerState = rememberZoomablePagerState { images.size },
//        imageLoader = { index ->
//            val painter = rememberCoilImagePainter(image = images[index])
//            val pair = remember { mutableStateOf<Pair<Any?, Size?>>(Pair(null, null)) }
//            LaunchedEffect(painter) {
//                delay(1000)
//                pair.value = Pair(painter, painter.intrinsicSize)
//            }
//            return@ImagePager pair.value
//        },
//    )
    ImagePager(
        modifier = Modifier.fillMaxSize(),
        pagerState = rememberZoomablePagerState { images.size },
        imageLoader = { index ->
            val painter = painterResource(images[index])
            return@ImagePager Pair(painter, painter.intrinsicSize)
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
    val galleryState =
        rememberZoomablePagerState { images.size }
    ZoomablePager(state = galleryState) { page ->
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
