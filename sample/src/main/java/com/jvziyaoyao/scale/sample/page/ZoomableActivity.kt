package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import com.jvziyaoyao.scale.sample.base.BaseActivity

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-11-24 16:24
 **/
class ZoomableActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ZoomableBody()
        }
    }

}

//@Composable
//fun ZoomableBody() {
//    val scope = rememberCoroutineScope()
//
//    val url = "https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF"
//    val painter = rememberCoilImagePainter(url)
//    val zoomableState = rememberZoomableState(contentSize = painter.intrinsicSize)
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(40.dp)
//    ) {
//        zoomableState.apply {
//            ZoomableView(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Blue.copy(0.2F)),
//                state = zoomableState,
//                boundClip = false,
//                detectGesture = ZoomableGestureScope(
//                    onTap = {
//
//                    },
//                    onDoubleTap = {
//                        scope.launch {
//                            zoomableState.toggleScale(it)
//                        }
//                    },
//                    onLongPress = {
//
//                    },
//                ),
//            ) {
//                Image(
//                    modifier = Modifier.fillMaxSize(),
//                    painter = painter,
//                    contentDescription = null,
//                )
//            }
//            Box(
//                modifier = Modifier
//                    .graphicsLayer {
//                        translationX = gestureCenter.value.x - 6.dp.toPx()
//                        translationY = gestureCenter.value.y - 6.dp.toPx()
//                    }
//                    .clip(CircleShape)
//                    .background(Color.Red.copy(0.4f))
//                    .size(12.dp)
//            )
//            Box(
//                modifier = Modifier
//                    .clip(CircleShape)
//                    .background(Color.Cyan)
//                    .size(12.dp)
//                    .align(Alignment.Center)
//            )
//        }
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Yellow.copy(0.2F))
//        )
//    }
//}