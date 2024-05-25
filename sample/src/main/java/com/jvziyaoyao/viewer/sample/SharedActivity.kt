package com.jvziyaoyao.viewer.sample

import android.os.Bundle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.image.viewer.sample.R
import com.jvziyaoyao.viewer.sample.base.BaseActivity
import com.jvziyaoyao.zoomable.previewer.getDisplaySize

class SharedActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            SharedBody()
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedBody() {
    val density = LocalDensity.current
    var showDetails by remember { mutableStateOf(false) }
    val key = "好家伙"
    SharedTransitionLayout {
        density.apply {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(key = key),
                        showDetails
                    )
                    .clickable {
                        showDetails = false
                    }
            ) {
                val maxWidthPx = maxWidth.toPx()
                val maxHeightPx = maxHeight.toPx()
                val painter = painterResource(id = R.drawable.img_01)
                val displaySize = remember(painter.intrinsicSize, maxWidthPx, maxHeightPx) {
                    getDisplaySize(
                        contentSize = painter.intrinsicSize,
                        containerSize = Size(maxWidthPx, maxHeightPx),
                    )
                }
                Image(
                    modifier = Modifier
                        .size(
                            width = displaySize.width.toDp(),
                            height = displaySize.height.toDp(),
                        )
                        .align(Alignment.Center)
                        .background(Color.Cyan),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(key = key),
                        !showDetails
                    )
                    .clickable {
                        showDetails = true
                    }
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    painter = painterResource(id = R.drawable.img_01),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MainContent(
    onShowDetails: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        Image(
            painter = painterResource(id = R.drawable.img_01),
            contentDescription = "Cupcake",
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(key = "image"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .size(100.dp)
                .clickable {
                    onShowDetails()
                },
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DetailsContent(
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .sharedElement(
                    rememberSharedContentState(key = "image"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .clickable {
                    onBack()
                },
            painter = painterResource(id = R.drawable.img_01),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}

