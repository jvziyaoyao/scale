package com.jvziyaoyao.scale.sample.page

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.scale.sample.base.BaseActivity

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            HomeBody()
        }
    }

}

val activityList = listOf(
    ZoomableActivity::class.java,
    NormalActivity::class.java,
    HugeActivity::class.java,
    GalleryActivity::class.java,
    PreviewerActivity::class.java,
    TransformActivity::class.java,
    DecoderActivity::class.java,
    PicturesActivity::class.java,
    DuplicateActivity::class.java,
)

@Composable
fun HomeBody() {
    val context = LocalContext.current
    val state = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = state)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        activityList.forEach { activity ->
            HomeLargeButton(
                title = activity.simpleName,
                onClick = {
                    val intent = Intent(context, activity)
                    context.startActivity(intent)
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
            .background(MaterialTheme.colors.secondary)
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        textAlign = TextAlign.Center
    )
}