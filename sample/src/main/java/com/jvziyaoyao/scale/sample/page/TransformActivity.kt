package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import com.jvziyaoyao.scale.samLayout.padding.ple.page.TransformBody
import com.jvziyaoyao.scale.sample.base.BaseActivity

/**
 * @program: ImageViewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-09-21 18:20
 **/
class TransformActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            TransformBody()
        }
    }

}

