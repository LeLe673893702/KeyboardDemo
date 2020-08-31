package com.newler.keyboarddemo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 *
 * @what
 * @author 17173
 * @date 2020/8/28
 *
 */
class MorePanel(context: Context, attr: AttributeSet): Panel, FrameLayout(context, attr) {

    init {
    }

    override fun getView(): View {
        return this
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        val lp = layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp.height = getViewHeight()
        layoutParams = lp
    }

    override fun hide() {
        visibility = View.GONE
    }

    override fun show() {
        visibility = View.VISIBLE
    }

    override fun getViewHeight(): Int {
        return DensityUtil.dp2px(context, 100f)
    }
}