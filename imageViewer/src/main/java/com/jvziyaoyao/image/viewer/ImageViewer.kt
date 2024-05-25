package com.jvziyaoyao.image.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.jvziyaoyao.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.zoomable.zoomable.ZoomableViewState

/**
 * 单个图片预览组件
 *
 * @param modifier 图层修饰
 * @param model 需要显示的图像，仅支持Painter、ImageBitmap、ImageVector、ImageDecoder、AnyComposable,如果需要支持其他类型的数据可以自定义imageContent
 * @param state 组件状态和控制类
 * @param imageContent 用于解析图像数据的方法，可以自定义
 * @param detectGesture 检测组件的手势交互
 */
@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    model: Any?,
    state: ZoomableViewState,
    imageContent: ImageContent = defaultImageContent,
    detectGesture: ZoomableGestureScope = ZoomableGestureScope(),
) {
    ZoomableView(
        modifier = modifier,
        state = state,
        detectGesture = detectGesture,
    ) {
        imageContent.invoke(model, state)
    }
}

/**
 * 用于解析图像数据给ZoomableView显示的方法
 */
typealias ImageContent = @Composable (Any?, ZoomableViewState) -> Unit

/**
 * 默认处理，当前model仅支持Painter、ImageBitmap、ImageVector、ImageDecoder、AnyComposable
 */
val defaultImageContent: ImageContent = { model, state ->
    when (model) {
        is Painter -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = model,
                contentDescription = null,
            )
        }

        is ImageBitmap -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = model,
                contentDescription = null,
            )
        }

        is ImageVector -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                imageVector = model,
                contentDescription = null,
            )
        }

        is ImageDecoder -> {
            ImageCanvas(
                imageDecoder = model,
                viewPort = state.getViewPort(),
            )
        }

        is AnyComposable -> {
            model.composable.invoke()
        }

    }
}

/**
 * ImageViewer传人的Model参数除了特定图片以外，还支持传人一个Composable函数
 *
 * @property composable
 */
class AnyComposable(val composable: @Composable () -> Unit)