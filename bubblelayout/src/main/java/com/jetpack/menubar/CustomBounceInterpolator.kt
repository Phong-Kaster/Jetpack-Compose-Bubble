package com.jetpack.menubar

import android.view.animation.Interpolator

internal class CustomBounceInterpolator(val amplitude: Double = 0.1,
                                        val frequency: Double = 0.8) : Interpolator {

    override fun getInterpolation(time: Float): Float {
        /*return (-1.0 * exp(-time / amplitude) *
                cos(frequency * time) + 1).toFloat()*/
        return 1f
    }
}