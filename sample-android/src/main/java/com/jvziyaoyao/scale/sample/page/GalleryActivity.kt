package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import com.jvziyaoyao.scale.sample.base.BaseActivity

class GalleryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            GalleryBody()
//            GalleryBody01()
//            GalleryBody02()
        }
    }

}
