package com.newler.keyboarddemo

import android.view.View

/**
 *
 * @what
 * @author 17173
 * @date 2020/8/28
 *
 */
interface Panel {
    fun getView(): View
    fun getHeight(): Int
    fun reset()
}