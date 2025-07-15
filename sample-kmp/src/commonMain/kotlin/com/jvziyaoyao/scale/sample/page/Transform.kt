package com.jvziyaoyao.scale.sample.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.sample.base.BackHandler
import com.jvziyaoyao.scale.sample.ui.component.DetectScaleGridGesture
import com.jvziyaoyao.scale.sample.ui.component.ScaleGrid
import com.jvziyaoyao.scale.sample.ui.theme.Layout
import com.jvziyaoyao.scale.sample.ui.theme.getSlideColors
import com.jvziyaoyao.scale.sample.ui.theme.getSwitchColors
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.scale.zoomable.previewer.TransformItemView
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.rememberTransformItemState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import scale.sample_kmp.generated.resources.Res
import scale.sample_kmp.generated.resources.img_01
import scale.sample_kmp.generated.resources.img_02
import scale.sample_kmp.generated.resources.img_03
import scale.sample_kmp.generated.resources.img_04
import scale.sample_kmp.generated.resources.img_05
import scale.sample_kmp.generated.resources.img_06
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DrawableItem(
    val id: String,
    val res: DrawableResource,
)

val drawableItemTempIdMap = mutableMapOf<Int, String>()

private val imageIds = listOf(
    Res.drawable.img_01,
    Res.drawable.img_02,
    Res.drawable.img_03,
    Res.drawable.img_04,
    Res.drawable.img_05,
    Res.drawable.img_06,
)

@OptIn(ExperimentalUuidApi::class)
fun getUUID(): String {
    return Uuid.random().toString()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransformBody() {
    val images = remember { mutableStateListOf<DrawableItem>() }
    fun onRepeatChanged(repeat: Int) {
        images.clear()
        for (i in 0 until repeat) {
            for ((index, res) in imageIds.withIndex()) {
                val currentIndex = i * imageIds.size + index
                var id = drawableItemTempIdMap[currentIndex]
                if (id == null) {
                    id = getUUID()
                    drawableItemTempIdMap[currentIndex] = id
                }
                images.add(
                    DrawableItem(
                        id = id,
                        res = res
                    )
                )
            }
        }
    }

    fun onDeleteItem(item: DrawableItem) {
        images.remove(item)
    }

    val settingState = remember { TransformSettingState() }
    val scope = rememberCoroutineScope()
    val previewerState = rememberPreviewerState(
        scope = scope,
        defaultAnimationSpec = tween(settingState.animationDuration),
        verticalDragType = VerticalDragType.Down,
        pageCount = { images.size },
        getKey = { images[it].id },
    )
    LaunchedEffect(settingState.dataRepeat) {
        onRepeatChanged(settingState.dataRepeat)
    }
    LaunchedEffect(images.size) {
        if (images.isEmpty() && (previewerState.canClose || previewerState.animating)) {
            previewerState.close()
        }
    }
    if (previewerState.canClose || previewerState.animating) BackHandler {
        if (previewerState.canClose) scope.launch {
            if (settingState.transformExit) {
                previewerState.exitTransform()
            } else {
                previewerState.close()
            }
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontal = this.maxWidth > maxHeight
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
                Text(text = "🎈 Transform")
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Layout.padding.pxxl,
                        end = Layout.padding.pxxl,
                        bottom = Layout.padding.pxxl,
                    )
                    .weight(if (horizontal) 6F else 7F)
            ) {
                LazyVerticalGrid(columns = GridCells.Fixed(lineCount)) {
                    images.forEachIndexed { index, item ->
                        item(key = item.id) {
                            val needStart = index % lineCount != 0
                            val painter = painterResource(item.res)
                            val itemState =
                                rememberTransformItemState(
                                    intrinsicSize = painter.intrinsicSize
                                )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1F)
                                    .animateItem()
                                    .padding(start = if (needStart) 2.dp else 0.dp, bottom = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ScaleGrid(
                                    detectGesture = DetectScaleGridGesture(
                                        onPress = {
                                            scope.launch {
                                                if (settingState.transformEnter) {
                                                    previewerState.enterTransform(index)
                                                } else {
                                                    previewerState.open(index)
                                                }
                                            }
                                        }
                                    )
                                ) {
                                    TransformItemView(
                                        key = item.id,
                                        itemState = itemState,
                                        transformState = previewerState,
                                    ) {
                                        Image(
                                            modifier = Modifier.fillMaxSize(),
                                            painter = painter,
                                            contentScale = ContentScale.Crop,
                                            contentDescription = null,
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                    0.4F
                                                )
                                            )
                                            .padding(vertical = Layout.padding.pxs)
                                            .clickable {
                                                onDeleteItem(item)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(16.dp),
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.surface.copy(0.6F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SettingSurface(settingState)

        ImagePreviewer(
            state = previewerState,
            detectGesture = PagerGestureScope(
                onTap = {
                    scope.launch {
                        previewerState.exitTransform()
                    }
                }
            ),
            imageLoader = { index ->
                val painter = if (settingState.loaderError && (index % 2 == 0)) null
                else painterResource(images[index].res)
                return@ImagePreviewer Pair(painter, painter?.intrinsicSize)
            }
        )
    }
}

@Composable
fun SettingItem(
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp)
        Box {
            content()
        }
    }
}

@Composable
fun SettingItemSwitch(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Box(modifier = Modifier.padding(vertical = Layout.padding.pm)) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChanged,
            colors = getSwitchColors()
        )
    }
}

val VERTICAL_DRAG_ENABLE = VerticalDragType.UpAndDown
val VERTICAL_DRAG_DISABLE = VerticalDragType.None
val DEFAULT_VERTICAL_DRAG = VERTICAL_DRAG_ENABLE
const val DEFAULT_LOADER_ERROR = false
const val DEFAULT_TRANSFORM_ENTER = true
const val DEFAULT_TRANSFORM_EXIT = true
const val DEFAULT_ANIMATION_DURATION = 400
const val DEFAULT_DATA_REPEAT = 1

class TransformSettingState {

    var loaderError by mutableStateOf(DEFAULT_LOADER_ERROR)

    var verticalDrag by mutableStateOf(DEFAULT_VERTICAL_DRAG)

    var transformEnter by mutableStateOf(DEFAULT_TRANSFORM_ENTER)

    var transformExit by mutableStateOf(DEFAULT_TRANSFORM_EXIT)

    var animationDuration by mutableStateOf(DEFAULT_ANIMATION_DURATION)

    var dataRepeat by mutableStateOf(DEFAULT_DATA_REPEAT)

    fun reset() {
        loaderError = DEFAULT_LOADER_ERROR
        verticalDrag = DEFAULT_VERTICAL_DRAG
        transformEnter = DEFAULT_TRANSFORM_ENTER
        transformExit = DEFAULT_TRANSFORM_EXIT
        animationDuration = DEFAULT_ANIMATION_DURATION
        dataRepeat = DEFAULT_DATA_REPEAT
    }

//    companion object {
//        val Saver: Saver<TransformSettingState, *> = mapSaver(
//            save = {
//                mapOf<String, Any>(
//                    it::loaderError.name to it.loaderError,
//                    it::verticalDrag.name to it.verticalDrag,
//                    it::transformEnter.name to it.transformEnter,
//                    it::transformExit.name to it.transformExit,
//                    it::animationDuration.name to it.animationDuration,
//                    it::dataRepeat.name to it.dataRepeat,
//                )
//            },
//            restore = {
//                val state = TransformSettingState()
//                state.loaderError = it[state::loaderError.name] as Boolean
//                state.verticalDrag = it[state::verticalDrag.name] as VerticalDragType
//                state.transformEnter = it[state::transformEnter.name] as Boolean
//                state.transformExit = it[state::transformExit.name] as Boolean
//                state.animationDuration = it[state::animationDuration.name] as Int
//                state.dataRepeat = it[state::dataRepeat.name] as Int
//                state
//            }
//        )
//    }
}

//@Composable
//fun rememberSettingState(): TransformSettingState {
//    return rememberSaveable(saver = TransformSettingState.Saver) {
//        TransformSettingState()
//    }
//}

@Composable
fun SettingPanel(state: TransformSettingState, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        SettingPanelBanner(onClose)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8F)
                .weight(1F)
                .verticalScroll(rememberScrollState())
        ) {
            SettingItem(label = "Loader Error") {
                SettingItemSwitch(checked = state.loaderError, onCheckedChanged = {
                    state.loaderError = it
                })
            }
            Spacer(modifier = Modifier.height(Layout.padding.pxs))
            SettingItem(label = "Vertical Drag") {
                SettingItemSwitch(
                    checked = state.verticalDrag == VERTICAL_DRAG_ENABLE,
                    onCheckedChanged = {
                        state.verticalDrag =
                            if (it) VERTICAL_DRAG_ENABLE else VERTICAL_DRAG_DISABLE
                    })
            }
            Spacer(modifier = Modifier.height(Layout.padding.pxs))
            SettingItem(label = "Transform Enter") {
                SettingItemSwitch(checked = state.transformEnter, onCheckedChanged = {
                    state.transformEnter = it
                })
            }
            Spacer(modifier = Modifier.height(Layout.padding.pxs))
            SettingItem(label = "Transform Exit") {
                SettingItemSwitch(checked = state.transformExit, onCheckedChanged = {
                    state.transformExit = it
                })
            }
            Spacer(modifier = Modifier.height(Layout.padding.pm))
            SettingItem(label = "Duration ${state.animationDuration.toLong()}") {
                Slider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp),
                    colors = getSlideColors(),
                    value = state.animationDuration.toFloat(),
                    valueRange = 0F..2000F,
                    onValueChange = {
                        state.animationDuration = it.toInt()
                    },
                )
            }
            Spacer(modifier = Modifier.height(Layout.padding.pl))
            SettingItem(label = "Data Repeat ${state.dataRepeat}") {
                Slider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp),
                    colors = getSlideColors(),
                    value = state.dataRepeat.toFloat(),
                    valueRange = 1F..100F,
                    onValueChange = {
                        state.dataRepeat = it.toInt()
                    },
                )
            }
            Spacer(modifier = Modifier.height(Layout.padding.pxxl))
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
                onClick = {
                    state.reset()
                },
            ) {
                Text(text = "Reset")
            }
            Spacer(modifier = Modifier.height(Layout.padding.pxxl))
        }
    }
}

@Composable
fun SettingPanelBanner(onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(60.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    onClose()
                }
                .padding(Layout.padding.ps)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(0.32F)
            )
        }
        Text(
            text = "Settings",
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
fun SettingSurface(
    settingState: TransformSettingState,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    if (visible) BackHandler {
        visible = false
    }
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = {
                visible = true
            },
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .padding(bottom = 100.dp, end = Layout.padding.pl)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(0.32F)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxWidth(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.6F))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            visible = false
                        }
                    }
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxWidth(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6F)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = Layout.padding.pm)
                ) {
                    SettingPanel(settingState) {
                        visible = false
                    }
                }
            }
        }
    }
}