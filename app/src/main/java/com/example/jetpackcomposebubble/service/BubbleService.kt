package com.example.jetpackcomposebubble.service

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.LifecycleService
import com.apero.bubble.FloatingViewManager

/**
 * Screenshot bubble service plays a role that draws a shotcut overlays other application to capture screen.
 *
 * Screenshot bubble service will start [ScreenshotActivity] if users allow capturing screen
 *
 * @author Phong-Kaster
 */
class BubbleService: LifecycleService() {
    private val tag = this.javaClass.simpleName
    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }


    /**floatingViewManager is used to manage floating views (e.g., floating widgets or bubbles) that can appear on top of other apps*/
    private lateinit var fabManager: FloatingViewManager
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(this) }


    /**fabLayout represents for UI that this service draws on device's screen*/
    //private val fabLayout by lazy { LayoutBubbleScreenshotBinding.inflate(inflater) }

    private val metrics = DisplayMetrics()
    private val overMargin by lazy { (metrics.density).toInt() }
    private var safeArea: Rect? = null

    companion object {
        const val SAFE_AREA = "safeArea"
        private const val SCREENSHOT_BUBBLE_SERVICE_ID = "screenshotBubbleServiceId"
        private const val SCREENSHOT_BUBBLE_SERVICE = "screenshotBubbleService"
        const val SCREENSHOT_BUBBLE_NOTIFICATION_ID = 441
    }
}