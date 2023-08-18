# ImageViewer
🖼 ImageViewer for jetpack compose.

一款基于Jetpack Compose开发的图片预览库，支持超大图片的显示

[![](https://www.jitpack.io/v/jvziyaoyao/ImageViewer.svg)](https://www.jitpack.io/#jvziyaoyao/ImageViewer)

### 📝 更新日志 👉 [CHANGELOG](/CHANGELOG.md)
<br/>

👌 特性
--------
- 基于Jetpack Compose开发；
- 符合直觉的手势动效；
- 支持超大图片显示；
- 提供图片列表浏览组件；
- 支持图片预览组件弹出时的过渡动画；

🧐 预览
--------
<img src="doc/huge_image.gif" height="413" width="200"></img>
<img src="doc/previewer_images.gif" height="413" width="200"></img>

🛒 引入
--------
在`settings.gradle`增加jitpack的地址
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
在`build.gradle`增加依赖的引入
```gradle
// 从releases里面选一个版本
implementation 'com.github.jvziyaoyao:ImageViewer:VERSION'
```

👓 示例
--------
### 👋 示例代码请参考[sample](https://github.com/jvziyaoyao/ImageViewer/tree/main/sample)
### 一般使用
```kotlin
@Composable
fun NormalBody() {
    val scope = rememberCoroutineScope()
    val state = rememberViewerState()
    ImageViewer(
        state = state,
        model = painterResource(id = R.drawable.light_02),
        modifier = Modifier.fillMaxSize(),
        onDoubleTap = {
            scope.launch {
                state.toggleScale(it)
            }
        }
    )
}
```
### 加载超大图
‼ 仅在model类型为`ImageDecoder`才会被当做大图进行加载
```kotlin
/**
 * 声明一个方法用于加载ImageDecoder
 * @param inputStream InputStream
 * @return ImageDecoder?
 */
@Composable
fun rememberDecoderImagePainter(inputStream: InputStream): ImageDecoder? {
    var imageDecoder by remember { mutableStateOf<ImageDecoder?>(null) }
    LaunchedEffect(inputStream) {
        // 尽可能在IO线程上进行该操作
        launch(Dispatchers.IO) {
            imageDecoder = try {
                val decoder = BitmapRegionDecoder.newInstance(inputStream, false) 
                    ?: throw Exception()
                ImageDecoder(decoder = decoder)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    // 释放资源
    DisposableEffect(Unit) {
        onDispose {
            imageDecoder?.release()
        }
    }
    return imageDecoder
}

/**
 * 在界面中加载大图
 */
@Composable
fun HugeBody() {
    val context = LocalContext.current
    val inputStream = remember { context.assets.open("a350.jpg") }
    val imageDecoder = rememberDecoderImagePainter(inputStream = inputStream)
    val scope = rememberCoroutineScope()
    val state = rememberViewerState()
    ImageViewer(
        model = imageDecoder,
        state = state,
        boundClip = false,
        onDoubleTap = {
            scope.launch {
                state.toggleScale(it)
            }
        }
    )
}
```
### 图片列表浏览
```kotlin
@OptIn(ExperimentalPagerApi::class)
@Composable
fun GalleryBody() {
    val images = remember {
        mutableStateListOf(
            R.drawable.light_01,
            R.drawable.light_02,
            R.drawable.light_03,
        )
    }
    ImageGallery(
        modifier = Modifier.fillMaxSize(),
        state = rememberImageGalleryState { images.size },
        imageLoader = { index ->
            val image = images[index]
            rememberCoilImagePainter(image = image)
        },
    )
}
```
### 图片弹出预览
```kotlin
val images = remember {
  listOf(
    R.drawable.img_01,
    R.drawable.img_02,
  )
}
val imageViewerState = rememberPreviewerState(pageCount = { images.size })
ImagePreviewer(
  state = imageViewerState,
  imageLoader = { index -> painterResource(id = images[index]) },
  onTap = {
    // 关闭Popup
    imageViewerState.close()
  }
)

// 弹出Popup
imageViewerState.open()
```

### 图片弹出预览（带转换效果）
```kotlin
@Composable
fun TransformBody() {
    // 数据列表，key,value形式
    val images = mapOf(
        "001" to R.drawable.img_01,
        "002" to R.drawable.img_02,
    ).entries.toList()
    // 协程作用域
    val scope = rememberCoroutineScope()
    // enableVerticalDrag 开启垂直方向的拖拽手势
    // getKey 指定getKey方法，否则转换效果不会生效
    val previewerState = rememberPreviewerState(
      enableVerticalDrag = true,
      pageCount = { images.size },
    ) { index ->
        images[index].key
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for ((index, imageItem) in images.withIndex()) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(2.dp)
            ) {
                // 使用支持转换效果的TransformImageView，使用方法与Compose Image一样
                TransformImageView(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch {
                                // 弹出预览，带转换效果
                                previewerState.openTransform(index)
                            }
                        }
                    },
                    // 指定key，得到的key要与前面指定的getKey方法获得的一致
                    key = imageItem.key,
                    painter = painterResource(id = imageItem.value),
                    previewerState = previewerState,
                )
            }
        }
    }
    ImagePreviewer(
        modifier = Modifier.fillMaxSize(),
        state = previewerState,
        // 图片加载器
        imageLoader = { index ->
            painterResource(id = images[index].value)
        },
        detectGesture = {
            // 点击手势
            onTap = {
                scope.launch {
                    // 关闭预览，带转换效果
                    previewerState.closeTransform()
                }
            }
        }
    )
}
```

📓 API
--------
## `ImageViewer`
```kotlin
@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    model: Any?,
    state: ImageViewerState = rememberViewerState(),
    detectGesture: ViewerGestureScope.() -> Unit = {},
    boundClip: Boolean = true,
    debugMode: Boolean = false,
) { ... }
```
⚖ 参数

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `modifier` | Composable修改参数 | `Modifier` |
| `model` | 传入图片数据，仅支持 `Painter`、`ImageBitmap`、`ImageVector`、`ImageDecoder`、`ComposeModel` | `无` |
| `state` | 组件状态对象，可通过其获取图片的位置信息等 | `ImageViewerState` |
| `detectGesture` | 监听手势事件 | `{}` |
| `boundClip` | 图片超出容器部分是否需要裁剪 | `true` |
| `debugMode` | 调试模式，调试模式会显示手指操作的中心坐标 | `false` |

```kotlin
// detectGesture的使用
ImageViewer(
    ...
    detectGesture = {
        onTap = { /* 点击事件 */ }
        onDoubleTap = { /* 双击事件 */ }
        onLongPress = { /* 长按事件 */ }
    }
)
```

## `ImageViewerState`
```kotlin
val state = rememberViewerState()
// 在viewer中使用
ImageViewer(
  state = state,
  ...
)
// 设置图片归位
state.reset()
```
💾 属性

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `offsetX` | 图片X轴偏移量 | `0F` |
| `offsetY` | 图片Y轴偏移量 | `0F` |
| `scale` | 图片放大倍率 | `1F` |
| `rotation` | 图片转角度 | `0F` |
| `defaultSize` | 默认显示大小 | `IntSize(0, 0)` |
| `allowGestureInput` | 是否允许手势输入 | `true` |
| `defaultAnimateSpec` | 默认动画窗格 | `true` |
| `crossfadeAnimationSpec` | 挂载成功后显示时的动画窗格 | `true` |

🛠 方法

| 名称 | 参数 | 描述 |
| --- | --- | --- |
| `resetImmediately` | 无 | 图片位置、缩放率、角度立刻变换回初始值 |
| `reset` | (AnimationSpec\<Float>) | 图片位置、缩放率、角度动画变换回初始值 |
| `scaleToMax` | (offset: Offset) | 图片按中心点放大到最大 |
| `toggleScale` | (offset: Offset) | 图片在显示区域内最大和最小之间切换 |
| `fixToBound` | 无 | 图片如超出显示范围则回到显示范围内 |

## `ImageGallery`
```kotlin
@Composable
fun ImageGallery(
    modifier: Modifier = Modifier,
    state: ImageGalleryState,
    imageLoader: @Composable (Int) -> Any?,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    detectGesture: GalleryGestureScope.() -> Unit = {},
    galleryLayer: GalleryLayerScope.() -> Unit = {},
) { ... }
```

⚖ 参数

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `modifier` | Composable修改参数 | `Modifier` |
| `state` | Gallery状态ImageGalleryState | `无` |
| `imageLoader` | 图片加载器，入参为页码，须返回ImageViewer可接受的model | `无` |
| `itemSpacing` | 图片间的间隔 | `12.dp` |
| `detectGesture` | 监听手势事件 | `{}` |
| `galleryLayer` | 支持自定义viewer的前景、背景、viewer容器图层 | `{}` |

```kotlin
// detectGesture,galleryLayer的使用
ImageGallery(
    ...
    detectGesture = {
        onTap = { /* 单击事件 */ }
        onDoubleTap = { 
          // 双击事件
          // ImageGallery默认双击时会放大或缩小当前查看的图片
          // 返回true则不会执行上述操作
          false 
        }
        onLongPress = { /* 长按事件 */ }
    },
    galleryLayer = {
        background = { /** 自定义背景 */ }
        foreground = { /** 自定义前景 */ }
        viewerContainer = { page, viewerState, viewer -> 
          // 在这里你可以自定义一个view来包裹住viewer
          // 请务必要执行这个方法
          viewer()
        }
    },
)
```

## `ImageGalleryState`
```kotlin
// 须指定图片列表的长度
val state = rememberImageGalleryState { images.size }
// 在gallery中使用
ImageGallery(
  state = state,
  ...
)
// 滚动到第0页
state.animateScrollToPage(0)
```
💾 属性

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `imageViewerState` | 当前页码中ImageViewer的状态 | `null` |
| `currentPage` | 当前页码 | `0` |
| `targetPage` | 目标页码 | `0` |
| `pageCount` | 总页数 | `0` |
| `currentPageOffset` | 当前页面的偏移量 | `0F` |

🛠 方法

| 名称 | 参数 | 描述 |
| --- | --- | --- |
| `scrollToPage` | (page: Int, pageOffset: Float) | 滚动到指定页面 |
| `animateScrollToPage` | (page: Int, pageOffset: Float) | 动画滚动到指定页面 |

## `ImagePreviewer`
```kotlin
@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    state: ImagePreviewerState,
    imageLoader: @Composable (Int) -> Any?,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    detectGesture: GalleryGestureScope.() -> Unit = {},
    previewerLayer: PreviewerLayerScope.() -> Unit = {},
) { ... }
```

⚖ 参数

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `modifier` | Composable修改参数 | `Modifier` |
| `state` | 当前组件显示和图片浏览的状态ImagePreviewerState | `无` |
| `imageLoader` | 图片加载器，入参为页码，须返回ImageViewer可接受的model | `无` |
| `itemSpacing` | 图片间的间隔 | `12.dp` |
| `enter` | 组件的弹出动画 | `Default` |
| `exit` | 组件的隐藏动画 | `Default` |
| `detectGesture` | 监听手势事件 | `{}` |
| `previewerLayer` | 支持自定义viewer的前景、背景、viewer容器图层 | `{}` |

```kotlin
// detectGesture,previewerLayer的使用
ImagePreviewer(
    ...
    detectGesture = {
        onTap = { /* 单击事件 */ }
        onDoubleTap = { 
          // 双击事件
          // ImagePreviewer默认双击时会放大或缩小当前查看的图片
          // 返回true则不会执行上述操作
          false 
        }
        onLongPress = { /* 长按事件 */ }
    },
    previewerLayer = {
        foreground = { /** 自定义前景 */ }
        background = { /** 自定义背景 */ }
        viewerContainer = { page, viewerState, viewer ->
            // 在这里你可以自定义一个view来包裹住viewer
            // 请务必要执行这个方法
            viewer()
        }
        placeholder = PreviewerPlaceholder(
            enterTransition = fadeIn(),
            exitTransition = fadeOut(),
        ) {
            /** 自定义placeholder */
        }
    }
)
```

## `ImagePreviewerState`
```kotlin
// 须指定图片列表的长度
val imageViewerState = rememberPreviewerState(pageCount = { images.size })
// 组件中引用
ImagePreviewer(
  state = imageViewerState,
  ...  
)
// 隐藏组件 
imageViewerState.hide()
```
💾 属性

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `galleryState` | ImageGallery组件状态 | `ImageGalleryState` |
| `imageViewerState` | 当前页面的ImageViewer的状态对象 | `null` |
| `currentPage` | 当前页码 | `0` |
| `targetPage` | 目标页码 | `0` |
| `pageCount` | 总页数 | `0` |
| `currentPageOffset` | 当前页面的偏移量 | `0F` |
| `animating` | 是否正在进行动画 | `false` |
| `visible` | 是否可见 | `false` |
| `visibleTarget` | 是否可见的目标值 | `null` |
| `canOpen` | 是否允许执行open操作 | `false` |
| `canClose` | 是否允许执行close操作 | `false` |
| `getKey` | 用户提供的获取当前页码所属的key的方法 | `null` |
| `enableVerticalDrag` | 是否开启垂直下拉手势 | `false` |
| `scaleToCloseMinValue` | 下拉手势结束的时，判断是否关闭的阈值 | `0.8F` |

🛠 方法

| 名称 | 参数 | 描述 |
| --- | --- | --- |
| `scrollToPage` | (page: Int, pageOffset: Float) | 滚动到指定页面 |
| `animateScrollToPage` | (page: Int, pageOffset: Float) | 动画滚动到指定页面 |
| `findTransformItem` | (key: Any) | 查找key关联的transformItem |
| `findTransformItemByIndex` | (index: Int) | 根据页码查询关联的transformItem |
| `clearTransformItems` | 无 | 清除全部已缓存的transformItems |
| `open` | (Int, TransformItemState, EnterTransition) | 开启图片预览 |
| `close` | (ExitTransition) | 关闭图片预览 |
| `openTransform` | (Int, TransformItemState, AnimationSpec\<Float>) | 开启图片预览，带转换效果 |
| `closeTransform` | (AnimationSpec\<Float>) | 关闭图片预览，带转换效果 |


🕵️‍♀️ 开源许可
--------
MIT License

Copyright (c) 2022 JVZIYAOYAO

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
