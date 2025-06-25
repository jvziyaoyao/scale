package com.jvziyaoyao.scale.image.pager

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.scale.image.viewer.AnyComposable
import com.jvziyaoyao.scale.image.viewer.ModelProcessor
import com.jvziyaoyao.scale.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import com.jvziyaoyao.scale.zoomable.pager.DEFAULT_ITEM_SPACE
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.scale.zoomable.pager.PagerZoomablePolicyScope
import com.jvziyaoyao.scale.zoomable.pager.ZoomablePager
import com.jvziyaoyao.scale.zoomable.pager.ZoomablePagerState
import com.jvziyaoyao.scale.zoomable.pager.defaultFlingBehavior

/**
 * 基于Pager实现的图片浏览器
 *
 * @param modifier 图层修饰
 * @param pagerState 控件状态与控制对象
 * @param itemSpacing 每一页的间隔
 * @param beyondViewportPageCount 超出视口的页面缓存的个数
 * @param userScrollEnabled 是否允许页面滚动
 * @param detectGesture 手势监听对象
 * @param processor 用于解析图像数据的方法，可以自定义
 * @param imageLoader 图像加载器，支持的图像类型与ImageViewer一致，如果需要支持其他类型的数据可以自定义processor
 * @param imageLoading 图像未完成加载时的占位
 * @param proceedPresentation 用于控制ZoomableView、Loading等图层的切换逻辑，可以自定义
 * @param pageDecoration 每一页的图层修饰，可以用来设置页面的前景、背景等
 */
@Composable
fun ImagePager(
    modifier: Modifier = Modifier,
    pagerState: ZoomablePagerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    flingBehavior: TargetedFlingBehavior = defaultFlingBehavior(pagerState.pagerState),
    userScrollEnabled: Boolean = true,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    processor: ModelProcessor = ModelProcessor(),
    imageLoader: @Composable (Int) -> Pair<Any?, Size?>,
    imageLoading: ImageLoading? = defaultImageLoading,
    proceedPresentation: ProceedPresentation = defaultProceedPresentation,
    pageDecoration: @Composable (page: Int, innerPage: @Composable () -> Unit) -> Unit
    = { _, innerPage -> innerPage() },
) {
    ZoomablePager(
        modifier = modifier,
        state = pagerState,
        itemSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        detectGesture = detectGesture,
    ) { page ->
        pageDecoration.invoke(page) {
            val (model, size) = imageLoader.invoke(page)
            proceedPresentation.invoke(this, model, size, processor, imageLoading)
        }
    }
}

/**
 * 用于控制ZoomableView、Loading等图层的切换
 */
typealias ProceedPresentation = @Composable PagerZoomablePolicyScope.(
    model: Any?,
    size: Size?,
    processor: ModelProcessor,
    imageLoading: ImageLoading?,
) -> Boolean

/**
 * 默认ImageModelProcessor
 */
val defaultProceedPresentation: ProceedPresentation = { model, size, processor, imageLoading ->
    // TODO 这里是否要添加渐变动画?
    if (model != null && model is AnyComposable && size == null) {
        model.composable.invoke()
        true
    } else if (model != null && size != null) {
        ZoomablePolicy(intrinsicSize = size) {
            processor.Deploy(model = model, state = it)
        }
        size.isSpecified
    } else {
        imageLoading?.invoke()
        false
    }
}

/**
 * 图像未完成加载时的占位
 */
typealias ImageLoading = @Composable () -> Unit

/**
 * 默认ImageLoading
 */
val defaultImageLoading: ImageLoading = {
    Box(modifier = Modifier.fillMaxSize()) {
        CanvasCircularLoadingIndicator(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            color = Color.LightGray,
        )
    }
}

@Composable
fun CanvasCircularLoadingIndicator(
    modifier: Modifier = Modifier.size(48.dp),
    strokeWidth: Dp = 4.dp,
    color: Color = Color.Blue
) {
    val infiniteTransition = rememberInfiniteTransition()

    // 头部旋转角度
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 尾部旋转角度
    val sweep = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val reverse = remember { mutableStateOf(false) }
    val lastSweep = remember { mutableStateOf(0F) }
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val diameter = size.minDimension
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        if (sweep.value - lastSweep.value < 0) {
            reverse.value = !reverse.value
        }
        lastSweep.value = sweep.value

        val startAngle = if (!reverse.value) 0F else sweep.value
        val sweepAngle = if (!reverse.value) sweep.value else 360F - sweep.value

        withTransform({ rotate(rotation.value) }) {
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
    }
}