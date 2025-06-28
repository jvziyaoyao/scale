package com.jvziyaoyao.scale.sample.page

import android.content.Intent
import android.os.Bundle
import com.jvziyaoyao.scale.sample.base.BaseActivity

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            HomeBody(
                onZoomable = {
                    val intent = Intent(this, ZoomableActivity::class.java)
                    startActivity(intent)
                },
                onNormal = {
                    val intent = Intent(this, NormalActivity::class.java)
                    startActivity(intent)
                },
                onHuge = {
                    val intent = Intent(this, HugeActivity::class.java)
                    startActivity(intent)
                },
                onGallery = {
                    val intent = Intent(this, GalleryActivity::class.java)
                    startActivity(intent)
                },
                onPreviewer = {
                    val intent = Intent(this, PreviewerActivity::class.java)
                    startActivity(intent)
                },
                onTransform = {
                    val intent = Intent(this, TransformActivity::class.java)
                    startActivity(intent)
                },
                onDecoder = {
                    val intent = Intent(this, DecoderActivity::class.java)
                    startActivity(intent)
                },
                onDuplicate = {
                    val intent = Intent(this, DuplicateActivity::class.java)
                    startActivity(intent)
                },
            )
        }
    }

}