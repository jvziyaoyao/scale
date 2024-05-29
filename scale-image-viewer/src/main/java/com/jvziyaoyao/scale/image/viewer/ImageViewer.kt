package com.jvziyaoyao.scale.image.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableViewState
import kotlin.reflect.KClass

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
    // TODO 改文档 适配到每一层API
//    imageContent: ImageContent = defaultImageContent,
    processor: ImageLoaderProcessor = defaultLoaderProcessor,
    detectGesture: ZoomableGestureScope = ZoomableGestureScope(),
) {
    ZoomableView(
        modifier = modifier,
        state = state,
        detectGesture = detectGesture,
    ) {
        model?.let {
            processor.Deploy(model = it, state = state)
        }
    }
}

/**
 * 用于解析图像数据给ZoomableView显示的方法
 */
typealias ImageContent = @Composable (Any, ZoomableViewState) -> Unit

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

// TODO 这个代码要移除
val defaultLoaderProcessor = ImageLoaderProcessor(
    ImageRegionDecoderProcessor(),
)

class ImageLoaderProcessor(
    vararg additionalProcessor: Processor,
) {

    // 默认添加的处理器
    private val defaultProcessorList = listOf(ImageProcessor(), ComposableProcessor())

    private val typeMapper = mutableStateMapOf<KClass<out Any>, ImageContent>()

    init {
        listOf(*defaultProcessorList.toTypedArray(), *additionalProcessor).forEach { processor ->
            processor.getPair().forEach { pair ->
                typeMapper[pair.first] = pair.second
            }
        }
    }

    @Composable
    fun Deploy(model: Any, state: ZoomableViewState) {
        val entry = typeMapper.entries.firstOrNull { isSubclassOf(model, it.key) } ?: return
        entry.value.invoke(model, state)
    }

}

interface Processor {
    fun getPair(): List<Pair<KClass<out Any>, ImageContent>>
}

class ImageProcessor : Processor {
    override fun getPair(): List<Pair<KClass<out Any>, ImageContent>> {
        return listOf(
            Painter::class to { model, _ ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = model as Painter,
                    contentDescription = null,
                )
            },
            ImageBitmap::class to { model, _ ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = model as ImageBitmap,
                    contentDescription = null,
                )
            },
            ImageVector::class to { model, _ ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = model as ImageVector,
                    contentDescription = null,
                )
            },
        )
    }
}

class ImageRegionDecoderProcessor : Processor {
    override fun getPair(): List<Pair<KClass<out Any>, ImageContent>> {
        return listOf(
            ImageDecoder::class to { model, state ->
                ImageCanvas(
                    imageDecoder = model as ImageDecoder,
                    viewPort = state.getViewPort(),
                )
            }
        )
    }
}

class ComposableProcessor : Processor {
    override fun getPair(): List<Pair<KClass<out Any>, ImageContent>> {
        return listOf(
            AnyComposable::class to { model, _ ->
                (model as AnyComposable).composable.invoke()
            }
        )
    }
}

/**
 * 判断对象是否为某个类的子类
 *
 * @param T
 * @param instance 当前实例
 * @param kClass 需要匹配的类对象
 * @return
 */
fun <T : Any> isSubclassOf(instance: T, kClass: KClass<out Any>): Boolean {
    return kClass.isInstance(instance)
}

/**
 * ImageViewer传人的Model参数除了特定图片以外，还支持传人一个Composable函数
 *
 * @property composable
 */
class AnyComposable(val composable: @Composable () -> Unit)