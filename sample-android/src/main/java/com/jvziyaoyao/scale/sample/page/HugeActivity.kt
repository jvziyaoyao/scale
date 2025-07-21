package com.jvziyaoyao.scale.sample.page

import android.os.Bundle
import com.jvziyaoyao.scale.sample.base.BaseActivity

class HugeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            HugeBody()
//            HugeBody01()
        }
    }

}