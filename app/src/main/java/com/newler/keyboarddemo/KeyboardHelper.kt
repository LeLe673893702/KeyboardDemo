package com.newler.keyboarddemo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * @what
 * @author 17173
 * @date 2020/8/28
 *
 *  思路：用popupWindow监测键盘的高度和是否打开
 *  1. 键盘和面板打开
 *      - 有其他面板或者键盘展开,则上升或下降高度为它和键盘或面板两者高度之差，并且收起面板或键盘
 *      - 如果没有其他面板或者展开,则上升高度为键盘高度,
 *  2. 键盘和面板收起
 *      - 因为其他面板或键盘弹出导致,不变化,直接被覆盖收起
 *      - 因为单独被收起,布局直接向下移动距离
 *      -
 */
class KeyboardHelper(
        private val context: Context,
        private var rootView: View,
        private var recyclerView: RecyclerView,
        private var inputView: View,
        private var morePanel: Panel,
        private var stickerPanel: Panel,
        private var keyboardPanel: KeyboardPanel
) {
    private var onKeyboardStateListener : OnKeyboardStateListener? = null

    private var lastInputType: Int = InputType.NONE
    private var currentInputType: Int = InputType.NONE
    init {
        val keyboardStatePopupWindow = KeyboardStatePopupWindow(context, rootView)
        keyboardStatePopupWindow.setOnKeyboardStateListener(object : OnKeyboardStateListener {
            override fun onOpened(keyboardHeight: Int) {
                Log.d("KeyboardHelper", "onOpened")
                keyboardPanel.setHeight(keyboardHeight)
                onKeyboardStateListener?.onOpened(keyboardHeight)
                convertInputType(InputType.KEYBOARD)
            }

            override fun onClosed() {
                onKeyboardStateListener?.onClosed()
                handelKeyboardClosed()
            }
        })
    }

    private fun handelKeyboardClosed() {
        // 如果是键盘面板下,键盘关闭则说明键盘被收起了,其他情况下键盘被其他面板所替换
        if (currentInputType == InputType.KEYBOARD) {
           convertInputType(InputType.NONE)
        }
    }

    fun setOnKeyboardStateListener(onKeyboardStateListener: OnKeyboardStateListener) {
        this.onKeyboardStateListener = onKeyboardStateListener
    }

    fun convertInputType(@InputType currentInputType: Int) {
        if (lastInputType == currentInputType) return
        this.currentInputType = currentInputType
        playAnim(currentInputType)

//        refreshPanelVisibleState((currentInputType))

        lastInputType = currentInputType
    }

    private fun refreshPanelVisibleState(@InputType currentInputType: Int) {
        when(currentInputType) {
            InputType.MORE -> {
                morePanel.show()
                stickerPanel.hide()
                keyboardPanel.hide()
            }
            InputType.STICKER -> {
                stickerPanel.show()
                morePanel.hide()
                keyboardPanel.hide()
            }
            InputType.KEYBOARD -> {
                keyboardPanel.show()
                morePanel.hide()
                stickerPanel.hide()
            }

            else -> {
                keyboardPanel.hide()
                morePanel.hide()
                stickerPanel.hide()
            }
        }
    }

    private fun playAnim(@InputType currentInputType: Int) {
        val (formHeight, toHeight) = calcMoveHeight(currentInputType)
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY", formHeight, toHeight)
        val inputPanelAnimator = ObjectAnimator.ofFloat(inputView, "translationY", formHeight, toHeight)

        val playPanelAnimator = when(currentInputType) {
            InputType.MORE -> ObjectAnimator.ofFloat(morePanel.getView(), "translationY", formHeight, toHeight)
            InputType.STICKER -> ObjectAnimator.ofFloat(morePanel.getView(), "translationY", formHeight, toHeight)
            else -> null
        }

        val animSet = AnimatorSet()
        animSet.duration = 300
        animSet.interpolator = DecelerateInterpolator()

        playPanelAnimator?.let {
            animSet.playTogether(recyclerViewAnimator, inputPanelAnimator, playPanelAnimator)
        } ?: kotlin.run{
            animSet.playTogether(recyclerViewAnimator, inputPanelAnimator)
        }

        animSet.start()

    }

    private fun calcMoveHeight(@InputType currentInputType: Int): Pair<Float, Float> {
         return when(lastInputType) {
            InputType.STICKER -> Pair(-getHeightByInputType(InputType.STICKER), -getHeightByInputType(currentInputType))

            InputType.KEYBOARD -> Pair(-getHeightByInputType(InputType.KEYBOARD), -getHeightByInputType(currentInputType))

            InputType.MORE -> Pair(-getHeightByInputType(InputType.MORE), -getHeightByInputType(currentInputType))

            else -> Pair(0f, -getHeightByInputType(currentInputType))
        }
    }

    private fun getHeightByInputType(@InputType currentInputType: Int):Float {
        return when(currentInputType) {
            InputType.MORE -> morePanel.getViewHeight().toFloat()
            InputType.STICKER -> stickerPanel.getViewHeight().toFloat()
            InputType.KEYBOARD -> keyboardPanel.getViewHeight().toFloat()
            else -> 0f
        }
    }

}