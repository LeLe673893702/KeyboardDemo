package com.newler.keyboarddemo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
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
        context: Context,
        rootView: View,
        private var recyclerView: RecyclerView,
        private var inputView: View,
        private var morePanel: Panel,
        private var stickerPanel: Panel,
        private var keyboardPanel: KeyboardPanel
) {
    private var onKeyboardStateListener : OnKeyboardStateListener? = null
    private var keyboardStatePopupWindow: KeyboardStatePopupWindow = KeyboardStatePopupWindow(context, rootView)
    private var lastInputType: Int = InputType.NONE
    private var currentInputType: Int = InputType.NONE
    init {
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

        refreshPanelVisibleState((currentInputType))

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
        val (formHeight, toHeight) = getMoveHeight(currentInputType)
        Log.d("KulaKeyboardHelper", "panelType = $currentInputType, lastPanelType = $lastInputType, fromValue = $formHeight, " +
                "toValue = $toHeight")
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY",
                -getMoveDistance(-formHeight), -getMoveDistance(-toHeight))
        Log.d("KulaKeyboardHelper", "form=${getMoveDistance(-formHeight)}, to=${getMoveDistance(-toHeight)}")
        val inputPanelAnimator = ObjectAnimator.ofFloat(inputView, "translationY",
                formHeight, toHeight)

        val playPanelAnimator = when(currentInputType) {
            InputType.MORE -> ObjectAnimator.ofFloat(morePanel.getView(), "translationY", formHeight, toHeight)
            InputType.STICKER -> ObjectAnimator.ofFloat(stickerPanel.getView(), "translationY", formHeight, toHeight)
            else -> null
        }

        val animSet = AnimatorSet()
        animSet.duration = 200
        animSet.interpolator = DecelerateInterpolator()

        playPanelAnimator?.let {
            animSet.playTogether(recyclerViewAnimator, inputPanelAnimator, playPanelAnimator)
        } ?: kotlin.run{
            animSet.playTogether(recyclerViewAnimator, inputPanelAnimator)
        }

        animSet.start()
    }

    private fun playRecyclerViewMoveAnim() {
        val (formHeight, toHeight) = getMoveHeight(currentInputType)
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY",
                -getMoveDistance(-formHeight), -getMoveDistance(-toHeight))

    }

    private fun getMoveHeight(@InputType currentInputType: Int): Pair<Float, Float> {
         return when(lastInputType) {
            InputType.STICKER -> Pair(-getHeightByInputType(InputType.STICKER), -getHeightByInputType(currentInputType))

            InputType.KEYBOARD -> Pair(-getHeightByInputType(InputType.KEYBOARD), -getHeightByInputType(currentInputType))

            InputType.MORE -> Pair(-getHeightByInputType(InputType.MORE), -getHeightByInputType(currentInputType))

            else -> Pair(0f, -getHeightByInputType(currentInputType))
        }
    }

    /**
     * 1.键盘或面板伸展高度 = 自身高度+输入框高度
     * 2.recyclerView最后一个item距离底部高度 = recyclerView剩余高度+输入框高度
     * 1 - 2 = 面板或键盘高度 - recyclerView剩余高度
     * 如果大于0，说明recyclerView有内容被覆盖，需要上移
     * 如果小于0，说明recyclerView没有内容被覆盖，不需要上移，距离为0
     *
     */
    private fun getMoveDistance(panelHeight: Float):Float {
        return (panelHeight - getRecycleViewRemainHeight()).coerceAtLeast(0f)
    }

    private fun getRecycleViewRemainHeight():Int {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastItemView = layoutManager.findViewByPosition(layoutManager.findLastVisibleItemPosition())
        return (recyclerView.height - (lastItemView?.bottom ?: 0)).coerceAtLeast(0)
    }

    private fun getHeightByInputType(@InputType currentInputType: Int):Float {
        return when(currentInputType) {
            InputType.MORE -> morePanel.getViewHeight().toFloat()
            InputType.STICKER -> stickerPanel.getViewHeight().toFloat()
            InputType.KEYBOARD -> keyboardPanel.getViewHeight().toFloat()
            else -> 0f
        }
    }

    fun release() {
        keyboardStatePopupWindow.release()
        keyboardStatePopupWindow.dismiss()
    }

}