# ZoomablePager

`ZoomablePager`基于`ZoomableView`和`Jetpack Compose Pager`实现，提供对横向列表类型界面的支持，简化了手势处理和`ZoomableView`状态的持有

## 🍙 简单使用
```kotlin
// 准备一个图片列表
val images = remember {
    mutableStateListOf(R.drawable.light_01, R.drawable.light_02)
}
// 创建一个PagerState
val pagerState = rememberZoomablePagerState { images.size }
// Pager组件
ZoomablePager(state = pagerState) { page ->
    val painter = painterResource(id = images[page])
    // 必须要调用的Composable函数
    ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null
        )
    }
}
```

`ZoomablePolicy`方法对`ZoomableView`以及`ZoomableViewState`进行了一层封装，与`ZoomableView`的使用方式类似，必须要为`ZoomablePolicy`提供其中展示内容的固有大小，并且`ZoomablePolicy`的`content`中放置的`Composable`需要设置`Modifier.fillMaxSize()`

## 🍥 通过Coil展示网络图片
```kotlin
val images = remember {
    mutableStateListOf(
        "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
        "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
    )
}
val pagerState = rememberZoomablePagerState { images.size }
ZoomablePager(state = pagerState) { page ->
    val painter = rememberAsyncImagePainter(model = images[page])
    ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null
        )
    }
    if (!painter.intrinsicSize.isSpecified) {
        // 未加载成功时可以先显示一个loading占位
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

在使用`Coil`的过程中，某些特殊的写法可能会导致组件不可用：

```kotlin
// ❌ 错误示范
ZoomablePager(state = pagerState) { page ->
    val painter = rememberAsyncImagePainter(model = images[page])
    if (painter.intrinsicSize.isSpecified) {
        // 以下代码将永远不会被执行
        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null
            )
        }
    }
}

// ⭕️ 正确写法
ZoomablePager(state = pagerState) { page ->
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(images[page])
        .size(coil.size.Size.ORIGINAL) // 指定获取图片的大小
        .build()
    val painter = rememberAsyncImagePainter(imageRequest)
    if (painter.intrinsicSize.isSpecified) {
        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null
            )
        }
    }
}
```


## 🍘 对页面自定义
```kotlin
ZoomablePager(state = pagerState) { page ->
    val painter = painterResource(id = images[page])
    // 设置背景色奇偶页不同
    val backgroundColor = if (page % 2 == 0) Color.Cyan else Color.Gray
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.copy(0.2F))
    ) {
        ZoomablePolicy(intrinsicSize = painter.intrinsicSize) { _ ->
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null
            )
        }
        // 设置每一页的前景图层
        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
                .padding(8.dp)
                .align(Alignment.BottomCenter),
        ) {
            Text(text = "${page + 1}/${images.size}")
        }
    }
}
```

与`Pager`一样，可以通过`itemSpacing`设置每一页的间隙，`beyondViewportPageCount`设置预加载的页数：

```kotlin
ZoomablePager(
    itemSpacing = 20.dp, // 设置页面的间隙
    beyondViewportPageCount = 2, // 除当前页面外，预先加载其他页面的数量
) {  }
```

----

`ZoomablePager`通过`PagerGestureScope`获取手势事件的回调，与`ZoomableView`类似，目前仅支持`onTap`、`onDoubleTap`、`onLongPress`

<a id="pagergesturescope"></a>
## 🍣 PagerGestureScope
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

<a id="zoomablepagerstate"></a>
## 🍤 ZoomablePagerState

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