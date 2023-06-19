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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import com.origeek.viewerDemo.base.BaseActivity

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            HomeBody(goSample = {
                goActivity(NormalActivity::class.java)
            }, goHuge = {
                goActivity(HugeActivity::class.java)
            }, goGallery = {
                goActivity(GalleryActivity::class.java)
            }, goPreviewer = {
                goActivity(PreviewerActivity::class.java)
            }, goTransform = {
                goActivity(TransformActivity::class.java)
            }, goDecoder = {
                goActivity(DecoderActivity::class.java)
            }, goCompose = {
                goActivity(ComposeActivity::class.java)
            })
        }
    }

    private fun goActivity(cls: Class<*>) {
        val intent = Intent(this@HomeActivity, cls)
        startActivity(intent)
    }

}

@Composable
fun HomeBody(
    goSample: () -> Unit,
    goHuge: () -> Unit,
    goGallery: () -> Unit,
    goPreviewer: () -> Unit,
    goTransform: () -> Unit,
    goDecoder: () -> Unit,
    goCompose: () -> Unit,
) {
    val state = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = state)
            .systemBarsPadding()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        HomeLargeButton(title = "Sample", onClick = goSample)
        Spacer(modifier = Modifier.height(24.dp))
        HomeLargeButton(title = "Huge image", onClick = goHuge)
        Spacer(modifier = Modifier.height(24.dp))
        HomeLargeButton(title = "ImageGallery", onClick = goGallery)
        Spacer(modifier = Modifier.height(24.dp))
        HomeLargeButton(title = "ImagePreviewer", onClick = goPreviewer)
        Spacer(modifier = Modifier.height(24.dp))
        HomeLargeButton(title = "TransformPreviewer", onClick = goTransform)
        Spacer(modifier = Modifier.height(24.dp))
        HomeLargeButton(title = "ImageDecoder", onClick = goDecoder)
        Spacer(modifier = Modifier.height(24.dp))
        HomeLargeButton(title = "Compose", onClick = goCompose)
        Spacer(modifier = Modifier.height(24.dp))
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