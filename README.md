# ImageViewer
🖼 ImageViewer for jetpack compose.

中文介绍 | [English](/README_en.md)

一款基于Jekpack Compose开发的图片预览库，支持超大图片的显示

[![](https://www.jitpack.io/v/jvziyaoyao/ImageViewer.svg)](https://www.jitpack.io/#jvziyaoyao/ImageViewer)

👌 特性
--------
- 基于Jetpack Compose开发；
- 符合直觉的手势动效；
- 支持超大图片显示；
- 提供图片列表浏览组件；

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
‼ 仅在model类型为`BitmapRegionDecoder`才会被当做大图进行加载
```kotlin
@Composable
fun HugeBody() {
    val context = LocalContext.current
    val imageDecoder = remember {
        ImageDecoder(
            BitmapRegionDecoder.newInstance(
                context.assets.open("a350.jpg"),
                false
            )!!
        )
    }
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
        count = images.size,
        imageLoader = { index ->
            val image = images[index]
            rememberCoilImagePainter(image = image)
        }
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
val imageViewerState = rememberPreviewerState()
ImagePreviewer(
  count = images.size,
  state = imageViewerState,
  imageLoader = { index -> painterResource(id = images[index]) },
  onTap = {
    imageViewerState.hide()
  }
)
// 弹出Popup
imageViewerState.show()
```
📓 API
--------
## `ImageViewer`
```kotlin
@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    model: Any,
    state: ImageViewerState = rememberViewerState(),
    onTap: (Offset) -> Unit = {},
    onDoubleTap: (Offset) -> Unit = {},
    onLongPress: (Offset) -> Unit = {},
    boundClip: Boolean = true,
    debugMode: Boolean = false,
) { ... }
```
⚖ 参数

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `modifier` | Composable修改参数 | `Modifier` |
| `model` | 传入图片数据，支持 `Painter`、`ImageBitmap`、`ImageVector`、`BitmapRegionDecoder` | `无` |
| `state` | 组件状态对象，可通过其获取图片的位置信息等 | `ImageViewerState` |
| `onTap` | 图片的单击事件，传入参数为点击的坐标 | `{}` |
| `onDoubleTap` | 图片的双击事件，传入参数为点击的坐标 | `{}` |
| `onLongPress` | 图片的长按事件，传入参数为点击的坐标 | `{}` |
| `boundClip` | 图片超出容器部分是否需要裁剪 | `true` |
| `debugMode` | 调试模式，调试模式会显示手指操作的中心坐标 | `false` |

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
| `offsetX` | 图片X轴偏移量 | `0` |
| `offsetY` | 图片Y轴偏移量 | `0` |
| `scale` | 图片放大倍率 | `1` |
| `rotation` | 图片转角度 | `0` |

🛠 方法

| 名称 | 参数 | 描述 |
| --- | --- | --- |
| `reset` | 无 | 图片位置、放大倍率、旋转角度设置回初始值 |
| `scaleToMax` | (offset: Offset) | 图片按中心点放大到最大 |
| `toggleScale` | (offset: Offset) | 图片在显示区域内最大和最小之间切换 |
| `fixToBound` | 无 | 图片如超出显示范围则回到显示范围内 |

## `ImageGallery`
```kotlin
@Composable
fun ImageGallery(
    modifier: Modifier = Modifier,
    count: Int,
    state: PagerState = rememberPagerState(),
    imageLoader: @Composable (Int) -> Any,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    background: @Composable ((Int) -> Unit) = {},
    foreground: @Composable ((Int) -> Unit) = {},
) { ... }
```

⚖ 参数

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `modifier` | Composable修改参数 | `Modifier` |
| `count` | 传入图片数组的长度 | `无` |
| `state` | Pager状态 | `无` |
| `imageLoader` | 图片加载器，入参为当前页码，用户返回 `Painter`、`ImageBitmap`、`ImageVector` 或 `BitmapRegionDecoder` | `无` |
| `itemSpacing` | 相邻两个图片之间的间隔 | `12.dp` |
| `currentViewerState` | 该方法允许用户获取当前预览图片的`ImageViewerState` | `{}` |
| `onTap` | 当前图片的单击事件 | `{}` |
| `onDoubleTap` | 当前图片的双击事件 | `{ false }` |
| `onLongPress` | 当前图片的长按事件 | `{}` |
| `background` | 设置图片浏览器的背景，入参为当前页码 | `{}` |
| `foreground` | 设置图片浏览器的前景，入参为当前页码 | `{}` |

## `ImagePreviewer`
```kotlin
@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    state: ImagePreviewerState = rememberPreviewerState(),
    count: Int,
    imageLoader: @Composable (index: Int) -> Any,
    background: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> DefaultPreviewerBackground() },
    foreground: @Composable ((size: Int, page: Int) -> Unit) = { _, _ -> },
    currentViewerState: (ImageViewerState) -> Unit = {},
    onTap: () -> Unit = {},
    onDoubleTap: () -> Boolean = { false },
    onLongPress: () -> Unit = {},
    backHandlerEnable: Boolean = true,
    enter: EnterTransition = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            + fadeIn(animationSpec = spring(stiffness = 4000f)),
    exit: ExitTransition = fadeOut(animationSpec = spring(stiffness = 2000f))
            + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
) { ... }
```

⚖ 参数

| 名称 | 描述 | 默认值 |
| --- | --- | :---: |
| `modifier` | Composable修改参数 | `Modifier` |
| `state` | 当前组件显示和图片浏览的状态 | `ImagePreviewerState` |
| `count` | 传入图片数组的长度 | `无` |
| `imageLoader` | 图片加载器，入参为当前页码，用户返回 `Painter`、`ImageBitmap`、`ImageVector` 或 `BitmapRegionDecoder` | `无` |
| `background` | 图片浏览器的背景，入参为当前总页数和页码 | `Default` |
| `foreground` | 图片浏览器的前景，入参为当前总页数和页码 | `{ _, _ -> }` |
| `currentViewerState` | 该方法允许用户获取当前预览图片的`ImageViewerState` | `{}` |
| `onTap` | 当前图片的单击事件 | `{}` |
| `onDoubleTap` | 当前图片的双击事件 | `{ false }` |
| `onLongPress` | 当前图片的长按事件 | `{}` |
| `backHandlerEnable` | 传入true，按下返回键时会隐藏组件 | `true` |
| `enter` | 组件的弹出动画 | `Default` |
| `exit` | 组件的隐藏动画 | `Default` |

## `ImagePreviewerState`
```kotlin
val imageViewerState = rememberPreviewerState()
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
| `index` | 当前页码 | `0` |
| `show` | 组件标识显示 | `false` |

🛠 方法

| 名称 | 参数 | 描述 |
| --- | --- | --- |
| `show` | (index: Int = 0) | 显示图片预览组件，参数设置当前页码 |
| `scrollTo` | (index: Int) | 滚动到目标页码 |
| `hide` | 无 | 隐藏组件 |

🕵️‍♀️ 开源许可
--------
MIT License

Copyright (c) 2022 Sebastian Jesson

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
