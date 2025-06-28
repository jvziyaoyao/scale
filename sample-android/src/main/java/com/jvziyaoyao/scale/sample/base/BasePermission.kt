package com.jvziyaoyao.scale.sample.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * @program: TestFocusable
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2024-01-23 20:55
 **/

fun Context.openSetting() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CommonPermissions(
    permissions: List<String>,
    onPermissionChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val permissionState =
        rememberMultiplePermissionsState(
            permissions = permissions,
        )
    LaunchedEffect(key1 = permissionState.allPermissionsGranted, block = {
        onPermissionChange(permissionState.allPermissionsGranted)
    })
    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        val context = LocalContext.current
        CommonPermissionNotGranted(
            launchPermissionRequest = {
                permissionState.launchMultiplePermissionRequest()
            },
            goSetting = {
                context.openSetting()
            }
        )
    }
}

@Composable
fun CommonPermissionNotGranted(
    label: String = "👋 我们需要获取权限才能使用该功能~",
    launchPermissionRequest: () -> Unit,
    goSetting: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.statusBarsPadding())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                launchPermissionRequest()
            }) {
                Text(text = "🛸 授予权限", color = MaterialTheme.colors.surface)
            }
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "如果无法授予权限，请通过下方按钮前往设置~",
                color = LocalContentColor.current.copy(0.8F)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "👇")
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = {
                    goSetting()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.background
                )
            ) {
                Text(text = "🚑 前往设置", color = MaterialTheme.colors.primary)
            }
        }
    }

}