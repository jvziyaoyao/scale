package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.origeek.imageViewer.ImageGallery
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.*
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme

class RemakeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ViewerDemoTheme {
                RemakeBody()
            }
        }
    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RemakeBody() {
    val images = remember {
        mutableStateListOf(
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F202003%2F26%2F20200326212002_rxlyj.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1652240723&t=f997f4e20c439a3d8f24c672a33589dc",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F202003%2F26%2F20200326212002_rxlyj.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1652240723&t=f997f4e20c439a3d8f24c672a33589dc",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimglf4.nosdn0.126.net%2Fimg%2FcWRoOUJtSlhiTDVnOUEvS0NjcStWZTR3VWdXQkRIVTc2RUxKSWljbWRDemVDUVA4NXZWcWN3PT0.jpg%3FimageView%26thumbnail%3D2160x0%26quality%3D90%26interlace%3D1%26type%3Djpg&refer=http%3A%2F%2Fimglf4.nosdn0.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1652539409&t=77ee65fc6f82d6d0a0b53502c4a3e279",
        )
    }
    ImageGallery(
        modifier = Modifier.fillMaxSize(),
        count = images.size,
        imageLoader = { index ->
            val image = images[index]
            rememberCoilImagePainter(image = image)
        }
    )
}
