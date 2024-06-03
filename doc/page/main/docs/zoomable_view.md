#ZoomableView

`ZoomableView`是这个库最基本的组件，通过`ZoomableView`可以对任意Composable进行放大、缩小等操作

## 🥑 简单使用
```kotlin
val painter = painterResource(id = R.drawable.light_02)
val state = rememberZoomableState(contentSize = painter.intrinsicSize)
ZoomableView(state = state) {
    Image(
        modifier = Modifier.fillMaxSize(), // 这里请务必要充满整个图层
        painter = painter,
        contentDescription = null,
    )
}
```

## 
```kotlin
val painter = painterResource(id = R.drawable.light_02)
val state = rememberZoomableState(contentSize = painter.intrinsicSize)
ZoomableView(state = state) {
    Image(
        modifier = Modifier.fillMaxSize(), // 这里请务必要充满整个图层
        painter = painter,
        contentDescription = null,
    )
}
```