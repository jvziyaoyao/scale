# ImagePreviewer

通过`ImagePreviewer`可以很方便地实现一个类似微信朋友圈放大查看图片的组件

## 🌭 基本用法
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
// 创建一个ImagePreviewer
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

## 🍔 过渡动效

使用`TransformImageView`替代`Image`，点击图片，图片放大并进入到预览列表

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    Row(
        modifier = Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
}
```

如上述代码所示，`TransformImageView`的`imageLoader`需要返回一个`Triple`类型的数据，
其中第一个参数为`key`，第二个为显示的图片数据，第三个为图片的固有大小

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
    TransformImageView(
        imageLoader = {
            val painter = painterResource(id = image.second)
            // key model size
            Triple(image.first, painter, painter.intrinsicSize)
        }
    )
}

// index要与key的position一致
state.enterTransform(index)
```

如果`TransformImageView`无法满足功能需求时，可以考虑使用`TransformItemView`，使用方式见文档：[`Previewer 过渡动效`](previewer.md#transformitemview)

## 🥪 编辑图层

在`ImagePreviewer`中，设置`previewerLayer`来编辑`Previewer`的图层，通过`pageDecoration`来控制每一页的显示，
这里需要注意的是，`pageDecoration`要求返回一个`Boolean`类型的值，这个值可以通过调用`pageDecoration`传入的参数`innerPage`来获得

```kotlin
ImagePreviewer(
    state = state,
    imageLoader = { page ->
        val painter = rememberAsyncImagePainter(model = images[page])
        Pair(painter, painter.intrinsicSize)
    },
    pageDecoration = { _, innerPage ->
        var mounted = false
        // 单独设置每一页的背景颜色
        Box(modifier = Modifier.background(Color.Cyan.copy(0.2F))) {
            // 通过调用页面获取imageLoader的状态
            mounted = innerPage()
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
        // 这里需要返回页面的挂载情况
        mounted
    },
    previewerLayer = TransformLayerScope(
        previewerDecoration = { innerPreviewer ->
            // 设置ImagePreviewer的背景颜色
            Box(
                modifier = Modifier
                    .background(Color.Black)
            ) {
                innerPreviewer.invoke()
            }
        }
    ),
)
```

## 🌮 类型拓展

`ImagePreviewer`可以通过`ModelProcessor`增加`model`支持的类型，参考文档：[`ImageViewer 类型拓展`](image_viewer.md#imageviewermodelprocessor)

## 🌯 Previewer

`ImagePreviewer`是基于`Previewer`封装而来的，其参数设置、状态控制、手势回调等用法一致，详情可参考文档：[`Previewer 基本配置`](previewer.md#previewersetting)
