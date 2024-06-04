# ZoomableView

`ZoomableView`是这个库最基本的组件，通过`ZoomableView`可以对任意`Composable`进行放大、缩小等操作

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

在使用`ZoomableView`时，必须为组件提供一个`ZoomableViewState`，并且告知组件需要显示的内容的固有大小，否则组件不会正常显示

## 🍑 结合Coil使用
```kotlin
val painter = rememberAsyncImagePainter(model = R.drawable.light_02)
val state = rememberZoomableState(contentSize = painter.intrinsicSize)
ZoomableView(state = state) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painter,
        contentDescription = null,
    )
}
```

`ZoomableView`会根据`ZoomableViewState`中获得的尺寸大小，将内容缩放到刚好能够完全显示，在`ZoomableView`的`content`中放置内容时需要为`Composable`设置`Modifier.fillMaxSize()`,否则会导致显示出问题

## 🍉 展示一个Composable
```kotlin
val density = LocalDensity.current
val rectSize = 100.dp
val rectSizePx = density.run { rectSize.toPx() }
val size = Size(rectSizePx, rectSizePx)
val state = rememberZoomableState(contentSize = size)
ZoomableView(state = state) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.6F)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.BottomEnd)
        )
        Text(modifier = Modifier.align(Alignment.Center), text = "Hello Compose")
    }
}
```

