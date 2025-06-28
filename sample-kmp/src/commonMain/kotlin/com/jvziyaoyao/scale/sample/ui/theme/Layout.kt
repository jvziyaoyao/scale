package com.jvziyaoyao.scale.sample.ui.theme

/**
 * @program: ImageGallery
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-07-28 19:50
 **/

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @program: ImageGallery
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-07-28 19:50
 **/

class Padding(
    val pxxs: Dp = 2.dp,
    val pxs: Dp = 4.dp,
    val ps: Dp = 8.dp,
    val pm: Dp = 12.dp,
    val pl: Dp = 20.dp,
    val pxl: Dp = 36.dp,
    val pxxl: Dp = 48.dp,
)

class FontSize(
    val fxxs: TextUnit = 10.sp,
    val fxs: TextUnit = 12.sp,
    val fs: TextUnit = 14.sp,
    val fm: TextUnit = 16.sp,
    val fl: TextUnit = 18.sp,
    val fxl: TextUnit = 24.sp,
    val fxxl: TextUnit = 28.sp,
    val fh: TextUnit = 32.sp,
)

class RoundShape(
    val rs_dp: Dp = 6.dp,
    val rm_dp: Dp = 12.dp,
    val rl_dp: Dp = 18.dp,
    val rxl_dp: Dp = 28.dp,
    val rs: RoundedCornerShape = RoundedCornerShape(rs_dp),
    val rm: RoundedCornerShape = RoundedCornerShape(rm_dp),
    val rl: RoundedCornerShape = RoundedCornerShape(rl_dp),
    val rxl: RoundedCornerShape = RoundedCornerShape(rxl_dp),
)

object Layout {

    private val defaultPadding = Padding()
    private val defaultFontSize = FontSize()
    private val defaultRoundShape = RoundShape()

    val padding: Padding
        @Composable
        get() = defaultPadding

    val fontSize: FontSize
        @Composable
        get() = defaultFontSize

    val roundShape: RoundShape
        @Composable
        get() = defaultRoundShape

}