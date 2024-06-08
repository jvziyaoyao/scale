# Getting Started

## 📦 Artifacts
`Scale`使用`mavenCentral()`进行分发，包含以下四个模块：

* `com.jvziyaoyao.scale:image-viewer` 提供了图片放大缩小、列表浏览、弹出预览组件和动效的图片浏览库，开箱即用
* `com.jvziyaoyao.scale:zoomable-view` `ImageViewer`的基础库，包含`ZoomableView`、`ZoomablePager`、`Previewer`，具有较高的扩展性
* `com.jvziyaoyao.scale:sampling-decoder` 提供了对大型图片进行二次采样显示的支持
* `com.jvziyaoyao.scale:image-viewer-classic` 老版本`ImageViewer`

## 🖼️ ImageViewer

对一张图片进行放大缩小：

```kotlin
val painter = painterResource(id = R.drawable.light_02)
val state = rememberZoomableState(contentSize = painter.intrinsicSize)
ImageViewer(model = painter, state = state)
```

## 🔎 ZoomableView

对任意一个Composable进行放大缩小：

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

## 💽 SamplingDecoder

对大型图片进行二次采样：

```kotlin
val context = LocalContext.current
val inputStream = remember { context.assets.open("a350.jpg") }
val (samplingDecoder) = rememberSamplingDecoder(inputStream = inputStream)
if (samplingDecoder != null) {
    val state = rememberZoomableState(
        contentSize = samplingDecoder.intrinsicSize
    )
    ImageViewer(
        model = samplingDecoder,
        state = state,
        processor = ModelProcessor(samplingProcessorPair)
    )
}
```

## 🎞️ ImagePager

展示图片列表：

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

## 📖 ImagePreviewer

图片弹出预览：

```kotlin
val images = remember {
    mutableStateListOf(
        "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
        "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
    )
}
val state = rememberPreviewerState(pageCount = { images.size }) { images[it] }
ImagePreviewer(
    state = state,
    imageLoader = { page ->
        val painter = rememberAsyncImagePainter(model = images[page])
        Pair(painter, painter.intrinsicSize)
    }
)

// 展开
state.open()
// 关闭
state.close()
```

支持过渡动画：

```kotlin
Row {
    images.forEachIndexed { index, url ->
        TransformImageView(
            modifier = Modifier
                .size(120.dp)
                .clickable {
                    scope.launch {
                        state.enterTransform(index)
                    }
                },
            imageLoader = {
                val painter = rememberAsyncImagePainter(model = url)
                Triple(url, painter, painter.intrinsicSize)
            },
            transformState = state,
        )
    }
}
```