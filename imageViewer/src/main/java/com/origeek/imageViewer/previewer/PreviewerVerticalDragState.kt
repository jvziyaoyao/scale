package com.origeek.imageViewer.previewer

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-17 14:42
 **/

open class PreviewerVerticalDragState : PreviewerTransformState() {

    lateinit var viewerContainerState: ViewerContainerState

    private suspend fun copyViewerContainerStateToTransformState() {
        transformState.apply {
            val targetScale = viewerContainerState.scale.value * fitScale
            graphicScaleX.snapTo(targetScale)
            graphicScaleY.snapTo(targetScale)
            val centerOffsetY = (containerSize.height - realSize.height).div(2)
            val centerOffsetX = (containerSize.width - realSize.width).div(2)
            offsetY.snapTo(centerOffsetY + viewerContainerState.offsetY.value)
            offsetX.snapTo(centerOffsetX + viewerContainerState.offsetX.value)
        }
    }

    private suspend fun viewerContainerShrinkDown() {
        stateCloseStart()
        listOf(
            scope.async {
                viewerContainerState.scale.animateTo(0F, animationSpec = defaultAnimationSpec)
            },
            scope.async {
                uiAlpha.animateTo(0F, animationSpec = defaultAnimationSpec)
            }
        ).awaitAll()
        ticket.awaitNextTicket()
        animateContainerState = MutableTransitionState(false)
        ticket.awaitNextTicket()
        viewerContainerState.reset()
        transformState.setExitState()
        stateCloseEnd()
    }

    internal var getKey: ((Int) -> Any)? = null

    internal suspend fun verticalDrag(pointerInputScope: PointerInputScope) {
        pointerInputScope.apply {
            var vStartOffset by mutableStateOf<Offset?>(null)
            var vOrientationDown by mutableStateOf<Boolean?>(null)
            if (getKey != null) detectVerticalDragGestures(
                onDragStart = OnDragStart@{
                    // 如果imageViewerState不存在，无法进行下拉手势
                    if (imageViewerState == null) return@OnDragStart
                    var transformItemState: TransformItemState? = null
                    // 查询当前transformItem
                    getKey?.apply {
                        findTransformItem(invoke(currentPage))?.apply {
                            transformItemState = this
                        }
                    }
                    // 更新当前transformItem
                    transformState.itemState = transformItemState
                    // 只有viewer的缩放率为1时才允许下拉手势
                    if (imageViewerState?.scale?.value == 1F) {
                        vStartOffset = it
                        // 进入下拉手势时禁用viewer的手势
                        imageViewerState?.allowGestureInput = false
                    }
                },
                onDragEnd = OnDragEnd@{
                    if (vStartOffset == null) return@OnDragEnd
                    vStartOffset = null
                    vOrientationDown = null
                    imageViewerState?.allowGestureInput = true
                    // TODO: 0.8这个值要配置
                    if (viewerContainerState.scale.value < 0.8F) {
                        scope.launch {
                            if (getKey != null) {
                                val key = getKey!!.invoke(currentPage)
                                val transformItem = findTransformItem(key)
                                if (transformItem != null) {
                                    // TODO: 提取方法
                                    transformState.notifyEnterChanged()
                                    ticket.awaitNextTicket()
                                    copyViewerContainerStateToTransformState()
                                    viewerContainerState.resetImmediately()
                                    transformSnapToViewer(false)
                                    ticket.awaitNextTicket()
                                    closeTransform(key, defaultAnimationSpec)
                                } else {
                                    viewerContainerShrinkDown()
                                }
                            } else {
                                viewerContainerShrinkDown()
                            }
                            // 结束动画后需要把关闭的UI打开
                            uiAlpha.snapTo(1F)
                        }
                    } else {
                        scope.launch {
                            uiAlpha.animateTo(1F, defaultAnimationSpec)
                        }
                        scope.launch {
                            viewerContainerState.reset()
                        }
                    }
                },
                onVerticalDrag = OnVerticalDrag@{ change, dragAmount ->
                    if (imageViewerState == null) return@OnVerticalDrag
                    if (vStartOffset == null) return@OnVerticalDrag
                    if (vOrientationDown == null) vOrientationDown = dragAmount > 0
                    if (vOrientationDown == true) {
                        val offsetY = change.position.y - vStartOffset!!.y
                        val offsetX = change.position.x - vStartOffset!!.x
                        val containerHeight = viewerContainerState.containerSize.height
                        val scale = (containerHeight - offsetY.absoluteValue).div(
                            containerHeight
                        )
                        scope.launch {
                            uiAlpha.snapTo(scale)
                            viewerContainerState.offsetX.snapTo(offsetX)
                            viewerContainerState.offsetY.snapTo(offsetY)
                            viewerContainerState.scale.snapTo(scale)
                        }
                    } else {
                        // 如果不是向上，就返还输入权，以免页面卡顿
                        imageViewerState?.allowGestureInput = true
                    }
                }
            )
        }
    }

    fun enableVerticalDrag(getKey: ((Int) -> Any)? = null) {
        this.getKey = getKey
    }

    fun disableVerticalDrag() {
        this.getKey = null
    }
}