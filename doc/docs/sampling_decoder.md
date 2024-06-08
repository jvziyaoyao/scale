# SamplingDecoder

`Scale`提供了`SamplingDecoder`、`SamplingCanvas`用于实现超级大图的预览，`SamplingDecoder`对`BitmapRegionDecoder`进行了封装，有助于开发者通过简单的`API`调用实现大型图片的加载显示，避免`OOM`

添加`SamplingDecoder`依赖支持：

```kotlin
implementation("com.jvziyaoyao.scale:sampling-decoder:$version")
```

## 🍋 简单使用
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
        // 添加SamplingDecoder的支持
        processor = ModelProcessor(samplingProcessorPair)
    )
}
```

`SamplingDecoder`支持常见位图，如：`JPEG`、`PNG`、`HEIF`等，`RAW`、`GIF`这些并不支持，在使用`rememberSamplingDecoder`方法时，格式无法解析时会返回异常

```kotlin
// exception为报错信息
val (samplingDecoder,exception) = 
    rememberSamplingDecoder(inputStream = inputStream)
```

也可以自行创建一个`SamplingDecoder`，不过在组件销毁的时候务必要把`SamplingDecoder`移除，否则将导致内存泄漏

```kotlin
val bitmapRegionDecoder = // 创建一个BitmapRegionDecoder
val samplingDecoder = 
    createSamplingDecoder(decoder, SamplingDecoder.Rotation.ROTATION_0)

// 组件退出的时候release
DisposableEffect(Unit) {
    onDispose {
        samplingDecoder.release()
    }
}
```

`SamplingDecoder`支持对图片进行旋转操作，例如某些文件会将旋转信息写到`Exif`中，请参考以下代码：

```kotlin
val file = // 图片文件
val inputStream = FileInputStream(file)
val exifInterface = ExifInterface(file)
val rotation = exifInterface.getDecoderRotation()
val samplingDecoder = rememberSamplingDecoder(inputStream, rotation)
```

## 🍊 在ZoomableView中使用
```kotlin
val state = rememberZoomableState(contentSize = samplingDecoder.intrinsicSize)
ZoomableView(state = state) {
    SamplingCanvas(
        samplingDecoder = samplingDecoder,
        viewPort = state.getViewPort()
    )
}
```

## 🍐 直接使用SamplingCanvas
```kotlin
val context = LocalContext.current
val inputStream = remember { context.assets.open("a350.jpg") }
val (samplingDecoder) = rememberSamplingDecoder(inputStream = inputStream)
if (samplingDecoder != null) {
    val offset = remember { mutableStateOf(Offset.Zero) }
    val scale = remember { mutableStateOf(1F) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _, _ ->
                    offset.value += pan
                    scale.value *= zoom
                    true
                }
            }
    ) {
        val ratio = samplingDecoder.intrinsicSize.run {
            width.div(height)
        }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offset.value.x
                    translationY = offset.value.y
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .fillMaxWidth()
                .aspectRatio(ratio)
                .align(Alignment.Center)
        ) {
            SamplingCanvas(
                samplingDecoder = samplingDecoder,
                viewPort = SamplingCanvasViewPort(
                    scale = 8F,
                    visualRect = Rect(0.4F, 0.4F, 0.6F, 0.8F)
                )
            )
        }
    }
}
```