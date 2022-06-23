# ImageViewer
🖼 ImageViewer for jetpack compose.

[中文介绍](/README.md) | English

An ImageViewer library based on Jekpack Compose, which supports display large Image.

[![](https://www.jitpack.io/v/jvziyaoyao/ImageViewer.svg)](https://www.jitpack.io/#jvziyaoyao/ImageViewer)

👌 Feature
--------
- Development based on Jetpack Compose;
- Consistent gesture movement effect;
- Support large image display;
- Provide image previewer components;

🧐 Preview
--------
<img src="doc/huge_image.gif" height="413" width="200"></img>
<img src="doc/previewer_images.gif" height="413" width="200"></img>

🛒 Install
--------
Add `jitpack` to `settings.gradle`.
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add dependencies to your project's `build.gradle`.
```gradle
implementation 'com.github.jvziyaoyao:ImageViewer:1.0.1-alpha.1'
```

👓 Samples
--------
### 👋 For example code, please refer to [sample](https://github.com/jvziyaoyao/ImageViewer/tree/main/sample).
### Simply use
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
### Displaying large image
‼ Use `BitmapRegionDecoder` if you want to display a large image.
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
### Preview images
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
### Popup and preview images
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
// show popup
imageViewerState.show()
```
📓 API
--------
 ### `ImageViewer`
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
⚖ Parameter

| Name | Discription | Default |
| --- | --- | :---: |
| `modifier` | Composable modifier. | `Modifier` |
| `model` | Image data, request `Painter`, `ImageBitmap`, `ImageVector` or `BitmapRegionDecoder`. | `None` |
| `state` | Component's status, get location of the image, etc. | `ImageViewerState` |
| `onTap` | Tap event listener, parameter is the coordinates of taps. | `{}` |
| `onDoubleTap` | Double tap event listener, parameter is the coordinates of taps. | `{}` |
| `onLongPress` | Long tap event listener, parameter is the coordinates of taps. | `{}` |
| `boundClip` | Cut off the image beyond the container. | `true` |
| `debugMode` | The central coordinates of finger operation will be displayed in the debug mode. | `false` |

### `ImageViewerState`
```kotlin
val state = rememberViewerState()
// using in viewer
ImageViewer(
  state = state,
  ...
)
// reset the image
state.reset()
```
💾 Attributes

| Name | Discription | Default |
| --- | --- | :---: |
| `offsetX` | Image X axis offset. | `0` |
| `offsetY` | Image Y axis offset. | `0` |
| `scale` | Image scale rate. | `1` |
| `rotation` | Image rotation angle. | `0` |

🛠 Function

| Name | Parameter | Discription |
| --- | --- | --- |
| `reset` | None | Setting back to the initial value of image's offset, rotation, scale. |
| `scaleToMax` | (offset: Offset) | Scale the image to the maximum according to the center point. |
| `toggleScale` | (offset: Offset) | Toggle the image between the maximum and minimum. |
| `fixToBound` | None | Image return to the display range. |

### `ImageGallery`
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

⚖ Parameter

| Name | Discription | Default |
| --- | --- | :---: |
| `modifier` | Composable modifier. | `Modifier` |
| `count` | Length of images. | `None` |
| `state` | Pager's status. | `None` |
| `imageLoader` | Image loader, the parameter is current page, returns `Painter`, `ImageBitmap`, `ImageVector` or `BitmapRegionDecoder`. | `None` |
| `itemSpacing` | The space between two adjacent images. | `12.dp` |
| `currentViewerState` | Allows you to obtain the current preview image's `ImageViewersState`. | `{}` |
| `onTap` | Tap event listener of the current preview image. | `{}` |
| `onDoubleTap` | Double tap event listener of the current preview image. | `{ false }` |
| `onLongPress` | Long press event listener of the current preview image. | `{}` |
| `background` | Background of the component, the parameter is current page. | `{}` |
| `foreground` | Foreground of the component, the parameter is current page. | `{}` |

### `ImagePreviewer`
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

⚖ Parameter

| Name | Discription | Default |
| --- | --- | :---: |
| `modifier` | Composable modifier. | `Modifier` |
| `state` | Status of imagePreviewer. | `ImagePreviewerState` |
| `count` | Length of images. | `None` |
| `imageLoader` | Image loader, the parameter is current page, returns `Painter`, `ImageBitmap`, `ImageVector` or `BitmapRegionDecoder`. | `None` |
| `background` | Background of the component, the parameter is count and current page. | `Default` |
| `foreground` | Foreground of the component, the parameter is count and current page. | `{ _, _ -> }` |
| `currentViewerState` | Allows you to obtain the current preview image's `ImageViewerState`. | `{}` |
| `onTap` | Tap event listener of the current preview image. | `{}` |
| `onDoubleTap` | Double tap event listener of the current preview image. | `{ false }` |
| `onLongPress` | Long tap event listener of the current preview image. | `{}` |
| `backHandlerEnable` | Component will be hidden when you press the back button. | `true` |
| `enter` | EnterTransition. | `Default` |
| `exit` | ExitTransition. | `Default` |

### `ImagePreviewerState`
```kotlin
val imageViewerState = rememberPreviewerState()
// usining in component
ImagePreviewer(
  state = imageViewerState,
  ...  
)
// hide this component 
imageViewerState.hide()
```
💾 Attributes

| Name | Discription | Default |
| --- | --- | :---: |
| `index` | Current page index. | `0` |
| `show` | Whether is display. | `false` |

🛠 Function

| Name | Parameter | Discription |
| --- | --- | --- |
| `show` | (index: Int = 0) | Show this component and set index. |
| `scrollTo` | (index: Int) | Scroll to this image. |
| `hide` | None | Hide this component. |

🕵️‍♀️ License
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
