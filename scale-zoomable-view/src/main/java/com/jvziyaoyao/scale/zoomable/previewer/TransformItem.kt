package com.jvziyaoyao.scale.zoomable.previewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

// 用于操作transformItemStateMap的锁对象
internal val imageTransformMutex = Mutex()

// TODO 限制外部访问
// 用于缓存界面上的transformItemState
val transformItemStateMap = mutableStateMapOf<Any, TransformItemState>()

/**
 * 在compose中获取一个TransformItemState的方式
 *
 * @param scope 协程作用域
 * @param checkInBound 判断当前图片是否在显示范围内，可以为空
 * @param intrinsicSize 图片的固有大小，必须要有值且确定才能正常显示
 * @return TransformItemState
 */
@Composable
fun rememberTransformItemState(
    scope: CoroutineScope = rememberCoroutineScope(),
    checkInBound: (TransformItemState.() -> Boolean)? = null,
    intrinsicSize: Size? = null,
): TransformItemState {
    val transformItemState =
        remember { TransformItemState(scope = scope, checkInBound = checkInBound) }
    transformItemState.intrinsicSize = intrinsicSize
    return transformItemState
}

/**
 * TransformItem的状态与控制对象
 *
 * @property key 当前TransformItem的唯一标识
 * @property blockCompose 实际TransformItem中显示的内容
 * @property scope 协程作用域
 * @property blockPosition TransformItem的绝对位置
 * @property blockSize TransformItem的大小
 * @property intrinsicSize 传入blockCompose的固有大小
 * @property checkInBound 判断当前TransformItem是否在显示范围内的方法
 */
class TransformItemState(
    var key: Any = Unit,
    var blockCompose: (@Composable (Any) -> Unit) = {},
    var scope: CoroutineScope,
    var blockPosition: Offset = Offset.Zero,
    var blockSize: IntSize = IntSize.Zero,
    var intrinsicSize: Size? = null,
    var checkInBound: (TransformItemState.() -> Boolean)? = null,
) {

    private fun checkItemInMap() {
        if (checkInBound == null) return
        if (checkInBound!!.invoke(this)) {
            addItem()
        } else {
            removeItem()
        }
    }

    /**
     * 位置和大小发生变化时
     * @param position Offset
     * @param size IntSize
     */
    internal fun onPositionChange(position: Offset, size: IntSize) {
        blockPosition = position
        blockSize = size
        scope.launch {
            checkItemInMap()
        }
    }

    /**
     * 判断item是否在所需范围内，返回true，则添加该item到map，返回false则移除
     * @param checkInBound Function0<Boolean>
     */
    fun checkIfInBound(checkInBound: () -> Boolean) {
        if (checkInBound()) {
            addItem()
        } else {
            removeItem()
        }
    }

    /**
     * 添加item到map上
     * @param key Any?
     */
    fun addItem(key: Any? = null) {
        val currentKey = key ?: this.key ?: return
        if (checkInBound != null) return
        synchronized(imageTransformMutex) {
            transformItemStateMap[currentKey] = this
        }
    }

    /**
     * 从map上移除item
     * @param key Any?
     */
    fun removeItem(key: Any? = null) {
        synchronized(imageTransformMutex) {
            val currentKey = key ?: this.key ?: return
            if (checkInBound != null) return
            transformItemStateMap.remove(currentKey)
        }
    }
}

/**
 * 用于实现Previewer变换效果的小图装载容器
 *
 * @param modifier 图层修饰
 * @param key 唯一标识
 * @param itemState 该组件的状态与控制对象
 * @param itemVisible 该组件的可见性
 * @param content 需要显示的实际内容
 */
@Composable
fun TransformItemView(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    itemVisible: Boolean,
    content: @Composable (Any) -> Unit,
) {
    val scope = rememberCoroutineScope()
    itemState.key = key
    itemState.blockCompose = content
    DisposableEffect(key) {
        // 这个composable加载时添加到map
        scope.launch {
            itemState.addItem()
        }
        onDispose {
            // composable退出时从map移除
            itemState.removeItem()
        }
    }
    Box(
        modifier = modifier
            .onGloballyPositioned {
                itemState.onPositionChange(
                    position = it.positionInRoot(),
                    size = it.size,
                )
            }
            .fillMaxSize()
    ) {
        if (itemVisible) {
            itemState.blockCompose(key)
        }
    }
}