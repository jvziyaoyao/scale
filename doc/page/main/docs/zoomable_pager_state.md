# ZoomablePager State and Gesture

`ZoomablePager`通过`PagerGestureScope`获取手势事件的回调，与`ZoomableView`类似，目前仅支持`onTap`、`onDoubleTap`、`onLongPress`

## 🥬 PagerGestureScope
```kotlin
ZoomablePager(
    state = pagerState,
    detectGesture = PagerGestureScope(
        onTap = {
            // 点击事件
        },
        onDoubleTap = {
            // 双击事件
            // 如果返回false，会执行默认操作，把当前页面放大到最大
            // 如果返回true，则不会有任何操作
            return@PagerGestureScope false
        },
        onLongPress = {
            // 长按事件
        }
    )
) { }
```

## 🥦 ZoomablePagerState

`ZoomablePagerState`可以获取`ZoomablePager`的各种状态参数，也可以通过代码来切换当前页面：

```kotlin
val pagerState = rememberZoomablePagerState { images.size }
// 获取当前页面的页码
pagerState.currentPage 
// 动画滚动到下一个页面
pagerState.animateScrollToPage(1)
// 滚动到下一个页面
pagerState.scrollToPage(1)
```