package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import com.jvziyaoyao.scale.sample.base.BaseActivity

class DuplicateActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            DuplicateBody()
        }
    }

}