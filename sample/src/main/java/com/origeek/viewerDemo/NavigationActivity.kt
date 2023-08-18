package com.origeek.viewerDemo

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.origeek.viewerDemo.base.BaseActivity

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-08-18 14:23
 **/
class NavigationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            NavigationBody()
        }
    }

}

@Composable
fun NavigationBody() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "profile") {
        composable("profile") {
            NavProfile()
        }
        composable("friends") {
            NavFriends()
        }
    }
}

@Composable
fun NavProfile() {
    GalleryBody()
}

@Composable
fun NavFriends() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Friends")
    }
}