package com.origeek.imageViewer.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.origeek.imageViewer.zoomable.ZoomableGestureScope
import com.origeek.imageViewer.zoomable.ZoomableView

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-11 20:01
 **/

/**
 * model支持Painter、ImageBitmap、ImageVector、ImageDecoder、ComposeModel
 */
@Composable
fun ImageViewer01(
    // 修改参数
    modifier: Modifier = Modifier,
    // 图片数据
    model: Any?,
    // viewer状态
    state: ImageViewerState = rememberViewerState(),
    // 检测手势
    detectGesture: ViewerGestureScope = ViewerGestureScope(),
    // 超出容器是否显示
    boundClip: Boolean = true,
    // 调试模式
    debugMode: Boolean = false,
) {

//    ZoomableView(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Blue.copy(0.2F)),
//        state = ,
//        boundClip = false,
//        detectGesture = ZoomableGestureScope(
//            onTap = {
//
//            },
//            onDoubleTap = {
//
//            },
//            onLongPress = {
//
//            },
//        ),
//    ) {
//        /**
//         * 根据不同类型的model进行不同的渲染
//         */
//        when (model) {
//            is Painter,
//            is ImageVector,
//            is ImageBitmap,
//            -> {
//
//            }
//            is ImageDecoder -> {
//
//            }
//        }
//    }

}