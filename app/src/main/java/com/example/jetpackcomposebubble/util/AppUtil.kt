package com.example.jetpackcomposebubble.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.jetpack.bubble.FloatingViewManager
import com.example.jetpackcomposebubble.service.BubbleService
import com.example.jetpackcomposebubble.service.BubbleService.Companion.SAFE_AREA

object AppUtil {
    fun logcat(message: String, tag: String = "Jetpack Compose", enableDivider: Boolean = false) {
        if (enableDivider) {
            Log.d(tag, "----------------------------")
        }
        Log.d(tag, "-> message = $message")
    }

    fun Activity.isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)

        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun Activity.startBubbleService() {
        val isServiceRunning = isServiceRunning(BubbleService::class.java)
        if (isServiceRunning) return

        if (!Settings.canDrawOverlays(this)) return

        val intent = Intent(this, BubbleService::class.java).apply {
            val key: String = SAFE_AREA
            val safeArea = FloatingViewManager.findCutoutSafeArea(this@startBubbleService)
            logcat(message = "safe area of screenshot bubble = $safeArea")
            putExtra(key, safeArea)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    fun View.visible() {
        isVisible = true
    }

    fun View.gone() {
        isGone = true
    }

    fun View.invisible() {
        isInvisible = true
    }
}