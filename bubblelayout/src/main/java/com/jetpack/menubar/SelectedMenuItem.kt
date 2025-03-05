package com.jetpack.menubar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorRes

class SelectedMenuItem : ImageView {

    private var mCirclePaint: Paint
    private var radius: Float = 0f
    var menuItemId: Int = 0

    constructor(context: Context, @ColorRes color: Int) : this(context, null, color)

    constructor(context: Context, attrs: AttributeSet?, @ColorRes color: Int) : this(context, attrs, 0, color)

    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int, @ColorRes color: Int)
            : super(context, attrs, defStyleRes) {
        mCirclePaint = Paint(ANTI_ALIAS_FLAG).apply {
            this.color = ResourcesCompat.getColor(resources, color, null)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isActivated) {
            drawCircleIcon(canvas)
        }
    }

    /**
     * Here we are making scale drawing of selection
     * */
    private fun drawCircleIcon(canvas: Canvas) {
        canvas.drawCircle(canvas.width / 2.0f, canvas.height - paddingBottom / 1.5f, radius, mCirclePaint)
        if (radius <= canvas.width / 20.0f) {
            radius++
            invalidate()
        }
    }
}