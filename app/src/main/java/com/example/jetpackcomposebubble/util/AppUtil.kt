package com.example.jetpackcomposebubble.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.util.Log

object AppUtil {
    fun logcat(message: String, tag: String = "Jetpack Compose", enableDivider: Boolean = false) {
        if(enableDivider){
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
}