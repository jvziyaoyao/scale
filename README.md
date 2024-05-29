# ImageViewer
🖼 ImageViewer for jetpack compose.

一款基于Jetpack Compose开发的图片预览库，支持超大图片的显示

[![](https://www.jitpack.io/v/jvziyaoyao/ImageViewer.svg)](https://www.jitpack.io/#jvziyaoyao/ImageViewer)

### 🥳 1.1.0 全新版本～

### 📝 更新日志 👉 [CHANGELOG](/CHANGELOG.md)
<br/>

👌 特性
--------
- 基于Jetpack Compose开发；
- 符合直觉的手势动效；
- 支持超大图片显示；
- 提供图片列表浏览组件；
- 支持图片弹出预览组件；
- 支持图片弹出预览的过渡动画；
- 支持定制化可扩展性高；
- 不依赖第三方图片库；

🧐 预览
--------
<img src="doc/image/huge_image.gif" height="413" width="200"></img>
<img src="doc/image/previewer_images.gif" height="413" width="200"></img>

📓 API
--------
 💽 接口文档 👉 [API REFERENCE](https://jvziyaoyao.github.io/ImageViewer)

👓 示例
--------
👋 示例代码请参考 👉 [sample](https://github.com/jvziyaoyao/ImageViewer/tree/dev/sample/src/main/java/com/jvziyaoyao/viewer/sample)

🛒 引入
--------
在`settings.gradle`增加jitpack的地址
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
在`build.gradle`增加依赖的引入
```gradle
// 从releases里面选一个版本
implementation 'com.github.jvziyaoyao:ImageViewer:VERSION'
```

🛵 使用方式
--------
### 一般使用
<img src="doc/image/normal_image.gif" height="444" width="200"></img>
```kotlin
val scope = rememberCoroutineScope()
val state = rememberZoomableState()
ImageViewer(
    state = state,
    model = painterResource(id = R.drawable.light_02),
    modifier = Modifier.fillMaxSize(),
    detectGesture = ZoomableGestureScope(onDoubleTap = {
        scope.launch {
            state.toggleScale(it)
        }
    })
)
```
### 加载超大图
<img src="doc/image/huge_image.gif" height="413" width="200"></img>

‼ 仅在model类型为`ImageDecoder`才会被当做大图进行加载
```kotlin
val context = LocalContext.current
val scope = rememberCoroutineScope()
val inputStream = remember { context.assets.open("a350.jpg") }
val (imageDecoder) = rememberImageDecoder(inputStream = inputStream)
if (imageDecoder != null) {
    val state = rememberZoomableState(contentSize = imageDecoder.intrinsicSize)
    ImageViewer(
        model = imageDecoder,
        state = state
    )
}
```
### 图片列表浏览
<img src="doc/image/pager_image.gif" height="444" width="200"></img>
```kotlin
val images = remember {
    mutableStateListOf(
        R.drawable.light_01,
        R.drawable.light_02,
    )
}
ImagePager(
    modifier = Modifier.fillMaxSize(),
    pagerState = rememberZoomablePagerState { images.size },
    imageLoader = { index ->
        val painter = painterResource(images[index])
        return@ImagePager Pair(painter, painter.intrinsicSize)
    },
)
```
### 图片弹出预览
<img src="doc/image/previewer_image.gif" height="444" width="200"></img>
```kotlin
val images = remember {
  listOf(
    R.drawable.img_01,
    R.drawable.img_02,
  )
}
val previewerState = rememberPreviewerState(pageCount = { images.size })
val scope = rememberCoroutineScope()
ImagePreviewer(
    state = previewerState,
    detectGesture = PagerGestureScope(onTap = {
        scope.launch {
            // 关闭预览组件
            previewerState.close()
        }
    }),
    imageLoader = { index ->
        val painter = painterResource(id = images[index])
        Pair(painter, painter.intrinsicSize)
    }
)

// 显示预览组件
previewerState.open()
```

### 图片弹出预览（带转换效果）
<img src="doc/image/transform_image.gif" height="444" width="200"></img>
```kotlin
val images = remember {
    listOf(
        // 依次声明图片的key、缩略图、原图（实际情况按实际情况来，这里只是示例）
        Triple("001", R.drawable.thumb_01, R.drawable.img_01),
        Triple("002", R.drawable.thumb_02, R.drawable.img_02),
    )
}
// 为组件提供获取数据长度和获取key的方法
val previewerState = rememberPreviewerState(
    pageCount = { images.size },
    getKey = { images[it].first }
)
// 显示缩略图小图的示例代码
val index = 1
val scope = rememberCoroutineScope()
TransformImageView(
    modifier = Modifier
        .size(120.dp)
        .clickable {
            scope.launch {
                // 点击事件触发动效
                previewerState.enterTransform(index)
            }
        },
    imageLoader = {
        val key = images[index].first
        val imageDrawableId = images[index].second
        val painter = painterResource(id = imageDrawableId) // 这里使用的是缩略图
        // 必须依次返回key、图片数据、图片的尺寸
        Triple(key, painter, painter.intrinsicSize)
    },
    transformState = previewerState,
)
// 这里声明图片预览组件
ImagePreviewer(
    state = previewerState,
    detectGesture = PagerGestureScope(onTap = {
        scope.launch {
            // 点击界面后关闭组件
            previewerState.exitTransform()
        }
    }),
    imageLoader = {
        val painter = painterResource(id = images[it].third) // 这里使用的是原图
        // 这里必须依次返回图片数据、图片的尺寸
        return@ImagePreviewer Pair(painter, painter.intrinsicSize)
    }
)
```

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
