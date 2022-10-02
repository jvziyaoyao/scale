package com.origeek.viewerDemo

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.origeek.imageViewer.*
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.LazyGridLayout
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.theme.ViewerDemoTheme
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors
import kotlin.math.log

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-09-21 18:20
 **/
class TransformActivity : BaseActivity() {

    private fun getItemList(time: Int = 1): List<DrawableItem> {
        val srcList = listOf(
            R.drawable.img_01,
            R.drawable.img_02,
            R.drawable.img_03,
            R.drawable.img_04,
            R.drawable.img_05,
            R.drawable.img_06,
        )
        val resList = mutableListOf<Int>()
        for (i in 0 until time) {
            resList.addAll(srcList)
        }
        return resList.stream().map {
            DrawableItem(
                id = UUID.randomUUID().toString(),
                res = it
            )
        }.collect(Collectors.toList())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val images = getItemList(10)
        setBasicContent {
            ViewerDemoTheme {
                TransformBody(images)
            }
        }
    }

}

data class DrawableItem(
    val id: String,
    val res: Int,
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TransformBody(images: List<DrawableItem>) {
    val scope = rememberCoroutineScope()
    val transformContentState = rememberTransformContentState()
    val previewerState = rememberPreviewerState(
        transformState = transformContentState,
        animationSpec = tween(1200)
    )
    var openVerticalDrag by rememberSaveable { mutableStateOf(true) }
    if (openVerticalDrag) {
        previewerState.enableVerticalDrag { images[it].id }
    } else {
        previewerState.disableVerticalDrag()
    }
    if (previewerState.canClose) BackHandler {
        scope.launch {
            val id = images[previewerState.currentPage].id
            previewerState.closeTransform(id)
//            previewerState.close()
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontal = maxWidth > 400.dp
        val lineCount = if (horizontal) 6 else 3
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3F),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (!horizontal) Spacer(modifier = Modifier.height(24.dp))
                Text(text = "🎈 Transform")
                Spacer(modifier = Modifier.height(if (horizontal) 12.dp else 24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👋 VerticalDrag:")
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (openVerticalDrag) MaterialTheme.colors.error else MaterialTheme.colors.primary
                        ),
                        onClick = {
                            openVerticalDrag = !openVerticalDrag
                        }) {
                        if (openVerticalDrag) {
                            Text(text = "OFF")
                        } else {
                            Text(text = "ON")
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(if (horizontal) 6F else 7F)
            ) {
                LazyGridLayout(
                    modifier = Modifier.fillMaxSize(),
                    columns = lineCount,
                    size = images.size,
                    padding = 2.dp,
                ) { index ->
                    val item = images[index]
                    val painter = painterResource(id = item.res)
                    val itemState = rememberTransformItemState()
                    val itemScale = remember { Animatable(1F) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1F)
                            .scale(itemScale.value)
                            .pointerInput(Unit) {
                                forEachGesture {
                                    awaitPointerEventScope {
                                        awaitFirstDown()
                                        // 这里开始
                                        scope.launch {
                                            itemScale.animateTo(0.84F)
                                        }
                                        var move = false
                                        do {
                                            val event = awaitPointerEvent()
                                            if (!move) {
                                                move = event.type == PointerEventType.Move
                                                break
                                            }
                                        } while (event.changes.any { it.pressed })
                                        // 这里结束
                                        scope.launch {
                                            itemScale.animateTo(1F)
                                        }
                                        if (move) return@awaitPointerEventScope
                                        scope.launch {
//                                            previewerState.open(index)
                                            previewerState.openTransform(
                                                index = index,
                                                itemState = itemState,
                                            )
                                        }
                                    }
                                }
                            }
                    ) {
                        TransformImageView(
                            painter = painter,
                            itemState = itemState,
                            key = item.id,
                            contentState = transformContentState,
                        )
                    }
                }
            }
        }
        ImagePreviewer(
            modifier = Modifier.fillMaxSize(),
            count = images.size,
            state = previewerState,
            imageLoader = { index ->
                val image = images[index].res
                rememberCoilImagePainter(image = image)
            },
            currentViewerState = {},
        )
    }
}