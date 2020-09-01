package com.newler.keyboarddemo

import android.view.View
import android.widget.EditText
import com.qmuiteam.qmui.util.QMUIKeyboardHelper

/**
 *
 * @what
 * @author 17173
 * @date 2020/8/31
 *
 */
class KeyboardPanel(private val inputView: EditText) : Panel {
    private var height = 0
    override fun getViewHeight(): Int {
        return height
    }

    fun setHeight(height: Int) {
        this.height = height
    }

    override fun getView() = inputView

    override fun hide() {
        QMUIKeyboardHelper.hideKeyboard(inputView)
    }

    override fun show() {
        QMUIKeyboardHelper.showKeyboard(inputView, false)
    }
}