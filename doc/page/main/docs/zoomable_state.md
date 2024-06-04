# ZoomableView State and Gesture

需要从`ZoomableView`获取手势事件的回调，可以使用`ZoomableGestureScope`，目前仅支持`onTap`、`onDoubleTap`、`onLongPress`

## 🍗 ZoomableGestureScope
```kotlin
ZoomableView(
    state = rememberZoomableState(),
    detectGesture = ZoomableGestureScope(
        // 点击事件
        onTap = { offset ->

        },
        // 双击事件
        onDoubleTap = { offset ->

        },
        // 长按事件
        onLongPress = { offset ->

        }
    )
) {  }
```

## 🍖 ZoomableViewState

在`ZoomableView`中展示的内容有一个最大缩放率，可以通过`maxScale`来设置，进行放大、缩小超过极值时会有一个恢复的过程动画，可以配置一个`animationSpec`来修改这个动画的规格

```kotlin
// 获取一张图片
val painter = painterResource(id = R.drawable.light_02)
// 创建一个ZoomableViewState
val state = rememberZoomableState(
    contentSize = painter.intrinsicSize,
    // 设置组件最大缩放率
    maxScale = 4F,
    // 设置组件进行动画时的动画规格
    animationSpec = tween(1200)
)
```

通过`ZoomableViewState`可以获取`ZoomableView`的各种状态参数：

```kotlin
state.isRunning() // 获取组件是否在动画状态
state.displaySize // 获取组件1倍显示的大小
state.scale // 获取组件当前相对于1倍显示大小的缩放率
state.offsetX // 获取组件的X轴位移
state.offsetY // 获取组件的Y轴位移
state.rotation // 获取组件旋转角度
```

通过`ZoomableViewState`控制`ZoomableView`的缩放率在最大值、最小值间切换：

```kotlin
val scope = rememberCoroutineScope()
ZoomableView(
    state = state,
    detectGesture = ZoomableGestureScope(
        onDoubleTap = { offset ->
            scope.launch {
                // 在最大和最小显示倍率间切换，如果当前缩放率即不是最大值，
                // 也不是最小值，会恢复到默认显示大小
                state.toggleScale(offset)
            }
        },
        onLongPress = { _ ->
            // 恢复到默认显示大小
            scope.launch { state.reset() }
        }
    )
) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painter,
        contentDescription = null,
    )
}
```