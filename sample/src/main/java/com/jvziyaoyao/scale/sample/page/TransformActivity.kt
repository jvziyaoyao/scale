package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.sample.R
import com.jvziyaoyao.scale.sample.base.BaseActivity
import com.jvziyaoyao.scale.sample.ui.component.rememberCoilImagePainter
import com.jvziyaoyao.scale.sample.ui.theme.pgl
import com.jvziyaoyao.scale.sample.ui.theme.pl
import com.jvziyaoyao.scale.sample.ui.theme.pm
import com.jvziyaoyao.scale.sample.ui.theme.ps
import com.jvziyaoyao.scale.sample.ui.theme.pxs
import com.jvziyaoyao.scale.sample.ui.theme.pxxl
import com.jvziyaoyao.scale.zoomable.previewer.TransformItemView
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.rememberTransformItemState
import com.origeek.ui.common.compose.DetectScaleGridGesture
import com.origeek.ui.common.compose.ScaleGrid
import kotlinx.coroutines.launch
import java.util.UUID

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

    private val imageIds = listOf(
        R.drawable.img_01,
        R.drawable.img_02,
        R.drawable.img_03,
        R.drawable.img_04,
        R.drawable.img_05,
        R.drawable.img_06,
    )

    private val images = mutableStateListOf<DrawableItem>()

    private fun syncImages(repeat: Int) {
        images.clear()
        for (i in 0 until repeat) {
            for ((index, res) in imageIds.withIndex()) {
                val currentIndex = i * imageIds.size + index
                var id = drawableItemTempIdMap[currentIndex]
                if (id == null) {
                    id = UUID.randomUUID().toString()
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

    private fun deleteItem(item: DrawableItem) {
        images.remove(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            TransformBody(
                images = images,
                onRepeatChanged = { repeat ->
                    syncImages(repeat)
                },
                onDeleteItem = { drawableItem ->
                    deleteItem(drawableItem)
                }
            )
        }
    }

}

data class DrawableItem(
    val id: String,
    val res: Int,
)

val drawableItemTempIdMap = mutableMapOf<Int, String>()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransformBody(
    images: List<DrawableItem>,
    onRepeatChanged: (Int) -> Unit = {},
    onDeleteItem: (DrawableItem) -> Unit = {},
) {
    val settingState = rememberSettingState()
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
        val horizontal = maxWidth > maxHeight
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
                    .padding(start = pxxl, end = pxxl, bottom = pxxl)
                    .weight(if (horizontal) 6F else 7F)
            ) {
                LazyVerticalGrid(columns = GridCells.Fixed(lineCount)) {
                    images.forEachIndexed { index, item ->
                        item(key = item.id) {
                            val needStart = index % lineCount != 0
                            val painter = painterResource(id = item.res)
                            val itemState =
                                rememberTransformItemState(
                                    intrinsicSize = painter.intrinsicSize
                                )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1F)
                                    .animateItemPlacement()
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
                                            .background(MaterialTheme.colors.onBackground.copy(0.4F))
                                            .padding(vertical = pxs)
                                            .clickable {
                                                onDeleteItem(item)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(16.dp),
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colors.surface.copy(0.6F)
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
            imageLoader = { index ->
                val painter = if (settingState.loaderError && (index % 2 == 0)) null
                else rememberCoilImagePainter(image = images[index].res)
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
    Box(modifier = Modifier.padding(vertical = pm)) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChanged,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
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

    companion object {
        val Saver: Saver<TransformSettingState, *> = mapSaver(
            save = {
                mapOf<String, Any>(
                    it::loaderError.name to it.loaderError,
                    it::verticalDrag.name to it.verticalDrag,
                    it::transformEnter.name to it.transformEnter,
                    it::transformExit.name to it.transformExit,
                    it::animationDuration.name to it.animationDuration,
                    it::dataRepeat.name to it.dataRepeat,
                )
            },
            restore = {
                val state = TransformSettingState()
                state.loaderError = it[state::loaderError.name] as Boolean
                state.verticalDrag = it[state::verticalDrag.name] as VerticalDragType
                state.transformEnter = it[state::transformEnter.name] as Boolean
                state.transformExit = it[state::transformExit.name] as Boolean
                state.animationDuration = it[state::animationDuration.name] as Int
                state.dataRepeat = it[state::dataRepeat.name] as Int
                state
            }
        )
    }
}

@Composable
fun rememberSettingState(): TransformSettingState {
    return rememberSaveable(saver = TransformSettingState.Saver) {
        TransformSettingState()
    }
}

@Composable
fun SettingPanel(state: TransformSettingState, onClose: () -> Unit) {
    val context = LocalContext.current
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
            Spacer(modifier = Modifier.height(pxs))
            SettingItem(label = "Vertical Drag") {
                SettingItemSwitch(
                    checked = state.verticalDrag == VERTICAL_DRAG_ENABLE,
                    onCheckedChanged = {
                        state.verticalDrag =
                            if (it) VERTICAL_DRAG_ENABLE else VERTICAL_DRAG_DISABLE
                    })
            }
            Spacer(modifier = Modifier.height(pxs))
            SettingItem(label = "Transform Enter") {
                SettingItemSwitch(checked = state.transformEnter, onCheckedChanged = {
                    state.transformEnter = it
                })
            }
            Spacer(modifier = Modifier.height(pxs))
            SettingItem(label = "Transform Exit") {
                SettingItemSwitch(checked = state.transformExit, onCheckedChanged = {
                    state.transformExit = it
                })
            }
            Spacer(modifier = Modifier.height(pm))
            SettingItem(label = "Duration ${state.animationDuration.toLong()}") {
                Slider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp),
                    value = state.animationDuration.toFloat(),
                    valueRange = 0F..2000F,
                    onValueChange = {
                        state.animationDuration = it.toInt()
                    },
                )
            }
            Spacer(modifier = Modifier.height(pl))
            SettingItem(label = "Data Repeat ${state.dataRepeat}") {
                Slider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp),
                    value = state.dataRepeat.toFloat(),
                    valueRange = 1F..100F,
                    onValueChange = {
                        state.dataRepeat = it.toInt()
                    },
                )
            }
            Spacer(modifier = Modifier.height(pgl))
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                state.reset()
                Toast.makeText(context, "👌 Reset", Toast.LENGTH_SHORT)
                    .show()
            }) {
                Text(text = "Reset")
            }
            Spacer(modifier = Modifier.height(pxxl))
        }
    }
}

@Composable
fun SettingPanelBanner(onClose: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        val (closeBtn, bannerTxt) = createRefs()
        Box(modifier = Modifier
            .clip(CircleShape)
            .constrainAs(closeBtn) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
            .clickable {
                onClose()
            }
            .padding(ps)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colors.onSurface.copy(0.32F)
            )
        }
        Text(text = "Settings", modifier = Modifier.constrainAs(bannerTxt) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        })
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
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        val (btn) = createRefs()
        val guideFromBottom = createGuidelineFromBottom(0.28F)
        FloatingActionButton(
            onClick = {
                visible = true
            },
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier.constrainAs(btn) {
                end.linkTo(parent.end, pm)
                bottom.linkTo(guideFromBottom)
            }
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface.copy(0.32F)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
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
                    .background(MaterialTheme.colors.onSurface)
                    .pointerInput(Unit) {
                        detectTapGestures { }
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
                        .background(MaterialTheme.colors.surface)
                        .padding(horizontal = pm)
                ) {
                    SettingPanel(settingState) {
                        visible = false
                    }
                }
            }
        }
    }
}