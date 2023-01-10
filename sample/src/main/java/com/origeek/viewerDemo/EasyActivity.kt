package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.origeek.imageViewer.gallery.ImageGallery
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.PreviewerPlaceholder
import com.origeek.imageViewer.previewer.TransformImageView
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.ImageViewerState
import com.origeek.viewerDemo.base.BaseActivity
import kotlinx.coroutines.launch

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-31 18:03
 **/
class EasyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBasicContent {
            EasyBody()
        }
    }

}

@Composable
fun EasyBody() {
    // 数据列表，key,value形式
    val images = mapOf(
        "001" to R.drawable.img_01,
        "002" to R.drawable.img_02,
    ).entries.toList()
    // 协程作用域
    val scope = rememberCoroutineScope()
    // enableVerticalDrag 开启垂直方向的拖拽手势
    // getKey 指定getKey方法，否则转换效果不会生效
    val previewerState = rememberPreviewerState(enableVerticalDrag = true) { index ->
        images[index].key
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for ((index, imageItem) in images.withIndex()) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(2.dp)
            ) {
                // 使用支持转换效果的ImageView，使用方法与Compose Image一样
                TransformImageView(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch {
                                // 弹出预览，带转换效果
                                previewerState.openTransform(index)
                            }
                        }
                    },
                    // 指定key，得到的key要与
                    key = imageItem.key,
                    painter = painterResource(id = imageItem.value),
                    previewerState = previewerState,
                )
            }
        }
    }
    ImagePreviewer(
        modifier = Modifier.fillMaxSize(),
        count = images.size,
        state = previewerState,
        // 图片加载器
        imageLoader = { index ->
            painterResource(id = images[index].value)
        },
        detectGesture = {
            // 点击手势
            onTap = {
                scope.launch {
                    // 关闭预览，带转换效果
                    previewerState.closeTransform()
                }
            }
        }
    )
}