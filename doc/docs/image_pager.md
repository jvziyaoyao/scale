# ImagePager

`ImagePager`是一个展示图片列表的组件

## 🥃 简单使用
```kotlin
val images = remember {
    mutableStateListOf(R.drawable.light_01, R.drawable.light_02)
}
val pagerState = rememberZoomablePagerState { images.size }
ImagePager(
    pagerState = pagerState,
    imageLoader = { page ->
        val painter = painterResource(id = images[page])
        Pair(painter, painter.intrinsicSize)
    }
)
```

## 🍷 结合Coil使用
```kotlin
val images = remember {
    mutableStateListOf(
        "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
        "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
    )
}
val pagerState = rememberZoomablePagerState { images.size }
ImagePager(
    pagerState = pagerState,
    imageLoader = { page ->
        val painter = rememberAsyncImagePainter(model = images[page])
        Pair(painter, painter.intrinsicSize)
    }
)
```

<a id="proceedpresentation"></a>
## 🥂 ProceedPresentation

在`imageLoader`中，要求返回一个`Pair<Any?, Size?>`类型的数据，第一个是图片数据，第二个为图片的固有大小，组件会根据两个数据的状态来判断是显示图片还是`loading`，这个逻辑可以通过复写`proceedPresentation`来修改

```kotlin
// 声明一个ProceedPresentation
val myProceedPresentation: ProceedPresentation =
    { model, size, processor, imageLoading ->
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

// 设置参数proceedPresentation
ImagePager(
    proceedPresentation = myProceedPresentation
)
```

## 🍸 自定义loading

```kotlin
ImagePager(
    imageLoading = {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Blue,
            )
        }
    }
)
```

## 🍹 页面自定义

```kotlin
ImagePager(
    pageDecoration = { page, innerPage ->
        Box(modifier = Modifier.background(Color.LightGray)) {
            innerPage.invoke()

            // 设置每一页的前景图层
            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(8.dp)
                    .align(Alignment.BottomCenter),
            ) {
                Text(text = "${page + 1}/${images.size}")
            }
        }
    }
)
```

## 🍶 类型拓展

`ImagePager`可以通过`ModelProcessor`增加`model`支持的类型，参考文档：[`ImageViewer 类型拓展`](image_viewer.md#imageviewermodelprocessor)

## 🧉 手势回调

ImagePager手势监听与ZoomablePager一样，使用PagerGestureScope，参考文档：[`ZoomablePager PagerGestureScope`](zoomable_pager.md#pagergesturescope)

## 🥛 状态控制

ImagePager通过ZoomablePagerState进行状态控制，请参考文档：[`ZoomablePager ZoomablePagerState`](zoomable_pager.md#zoomablepagerstate)
