package com.origeek.viewerDemo

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import com.origeek.viewerDemo.base.BaseActivity

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            HomeBody()
        }

//        goActivity(GalleryActivity::class.java)
//        goActivity(ZoomableActivity::class.java)
//        goActivity(TransformActivity::class.java)
    }

    private fun goActivity(cls: Class<*>) {
        val intent = Intent(this@HomeActivity, cls)
        startActivity(intent)
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
//    ComposeActivity::class.java,
)

@Composable
fun HomeBody() {
    val context = LocalContext.current
    val state = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = state)
            .systemBarsPadding()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
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