package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.origeek.viewerDemo.base.BaseActivity

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-05-11 17:42
 **/
class PagerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            PageBody()
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageBody() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.error)
                .size(200.dp)
        ) {
            val pageState = rememberPagerState()
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                pageCount = 10,
                state = pageState,
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary)
                        .fillMaxSize()
                ) {
                    Text(text = "好家伙：$it")
                }
            }
        }
    }
}