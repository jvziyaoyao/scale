package com.jvziyaoyao.scale.sample.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.scale.sample.ui.theme.Layout

enum class HomeAction() {
    Zoomable,
    Normal,
    Huge,
    Gallery,
    Previewer,
    Transform,
    Decoder,
//    Pictures,
    Duplicate,
}

@Composable
fun HomeBody(
    onZoomable: () -> Unit,
    onNormal: () -> Unit,
    onHuge: () -> Unit,
    onGallery: () -> Unit,
    onPreviewer: () -> Unit,
    onTransform: () -> Unit,
    onDecoder: () -> Unit,
    onDuplicate: () -> Unit,
) {
    val state = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(state = state)
            .systemBarsPadding()
            .padding(Layout.padding.pl),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        HomeAction.entries.forEach { action ->
            HomeLargeButton(
                title = action.name,
                onClick = {
                    when (action) {
                        HomeAction.Zoomable -> onZoomable()
                        HomeAction.Normal -> onNormal()
                        HomeAction.Huge -> onHuge()
                        HomeAction.Gallery -> onGallery()
                        HomeAction.Previewer -> onPreviewer()
                        HomeAction.Transform -> onTransform()
                        HomeAction.Decoder -> onDecoder()
                        HomeAction.Duplicate -> onDuplicate()
                        else -> {}
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeLargeButton(
    title: String,
    onDoubleClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = {
                onClick()
            }, onDoubleClick = {
                onDoubleClick()
            })
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        textAlign = TextAlign.Center
    )
}