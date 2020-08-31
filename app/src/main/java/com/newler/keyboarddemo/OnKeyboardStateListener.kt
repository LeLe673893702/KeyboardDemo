package com.newler.keyboarddemo

/**
 *
 * @what
 * @author 17173
 * @date 2020/8/31
 *
 */
interface OnKeyboardStateListener {
    fun onOpened(keyboardHeight: Int)
    fun onClosed()
}