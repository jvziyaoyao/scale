package com.origeek.viewerDemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.TransformImageView
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.ui.common.compose.ScaleGrid
import com.origeek.viewerDemo.base.BaseActivity
import com.origeek.viewerDemo.ui.component.rememberCoilImagePainter
import com.origeek.viewerDemo.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

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
        animationSpec = tween(settingState.animationDuration),
        enableVerticalDrag = settingState.verticalDrag,
    ) {
        images[it].id
    }
    LaunchedEffect(settingState.dataRepeat) {
        onRepeatChanged(settingState.dataRepeat)
    }
    if (previewerState.canClose || previewerState.animating) BackHandler {
        if (previewerState.canClose) scope.launch {
            if (settingState.transformExit) {
                previewerState.closeTransform()
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
                            val itemState = rememberTransformItemState()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1F)
                                    .animateItemPlacement()
                                    .padding(start = if (needStart) 2.dp else 0.dp, bottom = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ScaleGrid(
                                    detectGesture = {
                                        onPress = {
                                            scope.launch {
                                                if (settingState.transformEnter) {
                                                    previewerState.openTransform(
                                                        index = index,
                                                        itemState = itemState,
                                                    )
                                                } else {
                                                    previewerState.open(index)
                                                }

                                            }
                                        }
                                    }
                                ) {
                                    TransformImageView(
                                        painter = painter,
                                        key = item.id,
                                        itemState = itemState,
                                        previewerState = previewerState,
                                    )
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
            modifier = Modifier.fillMaxSize(),
            count = images.size,
            state = previewerState,
            imageLoader = { index ->
                if (settingState.loaderError && (index % 2 == 0)) {
                    null
                } else {
                    val image = images[index].res
                    rememberCoilImagePainter(image = image)
                }
            },
            previewerLayer = {
                foreground = { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp, end = pl),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.surface.copy(0.2F))
                                .clickable {
                                    onDeleteItem(images[index])
                                }
                                .padding(pl)
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colors.surface
                            )
                        }
                    }
                }
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

const val DEFAULT_LOADER_ERROR = false
const val DEFAULT_VERTICAL_DRAG = true
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
                state.verticalDrag = it[state::verticalDrag.name] as Boolean
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
                SettingItemSwitch(checked = state.verticalDrag, onCheckedChanged = {
                    state.verticalDrag = it
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