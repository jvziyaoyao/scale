package com.jvziyaoyao.scale.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.jvziyaoyao.scale.sample.page.TransformBody
import com.jvziyaoyao.scale.sample.page.DecoderBody
import com.jvziyaoyao.scale.sample.page.DuplicateBody
import com.jvziyaoyao.scale.sample.page.GalleryBody
import com.jvziyaoyao.scale.sample.page.HomeBody
import com.jvziyaoyao.scale.sample.page.HugeBody
import com.jvziyaoyao.scale.sample.page.NormalBody
import com.jvziyaoyao.scale.sample.page.PreviewerBody
import com.jvziyaoyao.scale.sample.page.ZoomableBody
import com.jvziyaoyao.scale.sample.ui.theme.ScaleSampleTheme
import platform.UIKit.UIViewController

@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
fun getBaseViewController(
    opaque: Boolean = true,
    onFocusBehavior: OnFocusBehavior = OnFocusBehavior.FocusableAboveKeyboard,
    content: @Composable () -> Unit,
): UIViewController {
    return ComposeUIViewController(
        configure = {
            this.opaque = opaque
            this.delegate = delegate
            this.onFocusBehavior = onFocusBehavior
            this.enforceStrictPlistSanityCheck = false
        }
    ) {
        ScaleSampleTheme {
            content()
        }
    }
}

fun homeViewController(
    onZoomable: () -> Unit,
    onNormal: () -> Unit,
    onHuge: () -> Unit,
    onGallery: () -> Unit,
    onPreviewer: () -> Unit,
    onTransform: () -> Unit,
    onDecoder: () -> Unit,
    onDuplicate: () -> Unit,
) = getBaseViewController {
    HomeBody(
        onZoomable = onZoomable,
        onNormal = onNormal,
        onHuge = onHuge,
        onGallery = onGallery,
        onPreviewer = onPreviewer,
        onTransform = onTransform,
        onDecoder = onDecoder,
        onDuplicate = onDuplicate,
    )
}

fun zoomableViewController() = getBaseViewController {
    ZoomableBody()
}

fun normalViewController() = getBaseViewController {
    NormalBody()
}

fun hugeViewController() = getBaseViewController {
    HugeBody()
}

fun galleryViewController() = getBaseViewController {
    GalleryBody()
}

fun previewerViewController() = getBaseViewController {
    PreviewerBody {

    }
}

fun transformViewController() = getBaseViewController {
    TransformBody()
}

fun decoderViewController() = getBaseViewController {
    DecoderBody()
}

fun duplicateViewController() = getBaseViewController {
    DuplicateBody()
}