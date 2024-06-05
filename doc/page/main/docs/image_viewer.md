# ImageViewer

`ImageViewer`是一个图片放大缩小查看的组件，提供了默认配置，简化组件的使用流程

## 🍭 基本使用
```kotlin
val painter = painterResource(id = R.drawable.light_02)
val state = rememberZoomableState(contentSize = painter.intrinsicSize)
ImageViewer(model = painter, state = state)
```

⚠️ ‼️ 这里需要注意的是，提供图片的固有尺寸是必须的，没有的话`ImageViewer`不会正常显示

## 🍰 结合Coil使用
```kotlin
val painter = rememberAsyncImagePainter(model = R.drawable.light_02)
val state = rememberZoomableState(contentSize = painter.intrinsicSize)
ImageViewer(model = painter, state = state)
```

## 🍨 自定义内容

`ImageViewer`通过传人的model类型来自动选择使用何种方式进行图片显示，与`Image`类似，默认支持`Painter`、`ImageBitmap`、`ImageVector`，也支持通过`AnyComposable`传入一个`Composable`

```kotlin
// 设定显示内容的固有大小
val rectSize = 100.dp
val density = LocalDensity.current
val rectSizePx = density.run { rectSize.toPx() }
val size = Size(rectSizePx, rectSizePx)
val state = rememberZoomableState(contentSize = size)
ImageViewer(
    state = state,
    model = AnyComposable {
        // 自定义内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Cyan)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.6F)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.BottomEnd)
            )
            Text(
                modifier = Modifier.align(Alignment.Center), 
                text = "Hello Compose"
            )
        }
    }
)
```

但是，事实上这里并不推荐使用`AnyComposable`，`ImageViewer`是对`ZoomableView`进行封装而来，有较高的定制化需求可以考虑直接使用 [`ZoomableView`](zoomable_view.md)

## 🍦 手势与状态

`ImageViewer`手势事件回调为`ZoomableGestureScope`，状态与控制使用`ZoomableViewState`，见文档 [`ZoomableView ZoomableGestureScope ZoomableViewState`](zoomable_view.md#zoomablegesturescope)