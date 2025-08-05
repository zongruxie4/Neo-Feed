package com.saulhdev.feeder.ui.views

import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import com.saulhdev.feeder.R

class BaseBottomSheet @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : AbstractSlideInView(context, attrs, defStyleAttr){
    private val mInsets: Rect = Rect()
    init {
        setWillNotDraw(false)
        mContent = this
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setTranslationShift(mTranslationShift)
    }

    fun show(view: View?, animate: Boolean) {
        (findViewById<View>(R.id.sheet_contents) as ViewGroup).addView(view)
        container.addView(this)
        mIsOpen = false
        animateOpen(animate)
    }

    override fun onCloseComplete() {
        super.onCloseComplete()
    }

    private fun animateOpen(animate: Boolean) {
        if (mIsOpen || mOpenCloseAnimator.isRunning) {
            return
        }
        mIsOpen = true
        mOpenCloseAnimator.setValues(
            PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED)
        )
        mOpenCloseAnimator.interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
        if (!animate) {
            mOpenCloseAnimator.duration = 0
        }
        mOpenCloseAnimator.start()
    }

    override fun handleClose(animate: Boolean) {
        handleClose(animate, DEFAULT_CLOSE_DURATION.toLong())
    }

    override fun isOfType(type: Int): Boolean {
        return type and TYPE_FILTER_SHEET == 0
    }

    companion object{
        const val DEFAULT_CLOSE_DURATION = 200

        fun inflate(context: Context): BaseBottomSheet {
            return View.inflate(context, R.layout.base_bottom_sheet,null) as BaseBottomSheet
        }
    }
}