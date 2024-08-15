# Previewer

`Scale`提供了一个`Previewer`组件，用以帮助开发者实现图片弹出预览的功能，
同时提供了类似微信朋友圈图片放大查看的过渡动画效果

## 🧀 简单使用
```kotlin
// 准备一个图片列表
val images = remember {
    mutableStateListOf(
        "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF",
        "https://t7.baidu.com/it/u=4198287529,2774471735&fm=193&f=GIF",
    )
}
// 声明一个PreviewerState
val state = rememberPreviewerState(pageCount = { images.size }) { images[it] }
// 创建一个Previewer
Previewer(
    state = state,
) { page ->
    val painter = rememberAsyncImagePainter(model = images[page])
    ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null
        )
    }
    painter.intrinsicSize.isSpecified
}

// 展开
state.open()
// 关闭
state.close()
```

<a id="transformitemview"></a>
## 🍞 过渡动效

过渡动效依赖`TransformItemView`，预览组件展开时，会按照 `TransformItemView -> Previewer` 
的顺序进行`UI`变换，请确保`PreviewerState`中提供的`Key`与`TransformItemView`设置的`Key`一致，
通过调用`PreviewerState.enterTransform`展开，`PreviewerState.exitTransform`关闭

```kotlin
val scope = rememberCoroutineScope()
Box(modifier = Modifier.fillMaxSize()) {
    Row(
        modifier = Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEachIndexed { index, url ->
            val painter = rememberAsyncImagePainter(model = url)
            val itemState = rememberTransformItemState(
                intrinsicSize = painter.intrinsicSize
            )
            TransformItemView(
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        scope.launch {
                            state.enterTransform(index)
                        }
                    },
                key = url,
                transformState = state,
                itemState = itemState,
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
```

⚠️‼️ 注意`Key`与`Index`的一致性
```kotlin
val images = remember {
    mutableStateListOf(
        // key to image
        "001" to R.drawable.img_01,
        "002" to R.drawable.img_02,
    )
}

val state = rememberPreviewerState(
    pageCount = { images.size },
    getKey = { index -> images[index].first } // 获取key
)

images.forEachIndexed { index, image ->
    TransformItemView(
        key = image.first, // 设置key
    )
}

// index要与key的position一致
state.enterTransform(index)
```

在同一个界面中，如果存在同一个`key`同时出现在不同的部位时，此时使用弹出动画会导致动画位置不符合预期的情况，可以通过指定`ItemStateMap`的方式来解决
```kotlin
val imageIds = remember { listOf(R.drawable.img_03, R.drawable.img_06) }

val itemStateMap01 = remember { mutableStateMapOf<Any, TransformItemState>() }
val previewerState01 = rememberPreviewerState(
    transformItemStateMap = itemStateMap01,
    pageCount = { imageIds.size },
    getKey = { imageIds[it] },
)

val itemStateMap02 = remember { mutableStateMapOf<Any, TransformItemState>() }
val previewerState02 = rememberPreviewerState(
    transformItemStateMap = itemStateMap02,
    pageCount = { imageIds.size },
    getKey = { imageIds[it] },
)

CompositionLocalProvider(LocalTransformItemStateMap provides itemStateMap01) {
    imageIds.forEach {
        TransformItemView(key = it) {  }
    }
}

CompositionLocalProvider(LocalTransformItemStateMap provides itemStateMap02) {
    imageIds.forEach {
        TransformItemView(key = it) {  }
    }
}
```

## 🥯 编辑图层

在`Previewer`中，设置`previewerLayer`来编辑`Previewer`的图层，通过`zoomablePolicy`来控制每一页的显示

```kotlin
Previewer(
    state = state,
    previewerLayer = TransformLayerScope(
        previewerDecoration = {
            // 设置组件的背景图层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.2F))
            ) {
                // 组件内容本身
                it.invoke()
                // 设置前景图层
                Box(
                    modifier = Modifier
                        .padding(bottom = 48.dp)
                        .size(56.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White)
                        .align(Alignment.BottomCenter),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 36.sp,
                        text = "❤️",
                    )
                }
            }
        },
    ),
) { page ->
    val painter = rememberAsyncImagePainter(model = images[page])
    ZoomablePolicy(intrinsicSize = painter.intrinsicSize) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null
        )
    }
    if (!painter.intrinsicSize.isSpecified) {
        // 加载中
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
    painter.intrinsicSize.isSpecified
}
```

<a id="previewersetting"></a>
## 🥐 基本配置

可以通过`itemSpacing`设置每一页的间隙，`beyondViewportPageCount`设置预加载的页数，展开时，
如果不使用转换动效，可以设置展开和关闭动画，与`AnimatedVisibility`的使用方式一样

```kotlin
Previewer(
    itemSpacing = 20.dp, // 设置页面的间隙
    beyondViewportPageCount = 2, // 除当前页面外，预先加载其他页面的数量

    enter = fadeIn(), // 展开动画
    exit = fadeOut(), // 关闭动画
)
```

展开预览后，在缩放率为`1`的情况下，支持垂直方向上的手势操作，例如上下拖拽关闭预览
```kotlin
val previewerState = rememberPreviewerState(
    verticalDragType = VerticalDragType.Down, // 设置垂直手势类型
    pageCount = { images.size },
    getKey = { images[it] }
)
```

## 🥞 手势回调

Previewer手势监听与ZoomablePager一样，使用PagerGestureScope，参考文档：[`ZoomablePager PagerGestureScope`](zoomable_pager.md#pagergesturescope)

## 🍕 状态控制

`PreviewerState`可以获取`Previewer`的各种状态参数，也可以通过代码来控制展开和关闭
```kotlin
previewerState.open() // 展开
previewerState.close() // 关闭
previewerState.enterTransform(0) // 带转换动画展开
previewerState.exitTransform() // 带转换动画关闭

previewerState.visible // 当前组件是否可见
previewerState.visibleTarget // 当前组件可见状态的目标值
previewerState.animating // 是否正在进行动画
previewerState.canOpen // 是否允许展开
previewerState.canClose // 是否允许关闭
```