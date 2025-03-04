package com.example.jetpackcomposebubble.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import com.jetpack.bubble.FloatingViewListener
import com.jetpack.bubble.FloatingViewManager
import com.example.jetpackcomposebubble.R
import com.example.jetpackcomposebubble.databinding.LayoutBubbleScreenshotBinding
import com.example.jetpackcomposebubble.util.AppUtil
import com.jetpack.menubar.FoldingTabBar

/**
 * Screenshot bubble service plays a role that draws a shotcut overlays other application to capture screen.
 *
 * Screenshot bubble service will start [ScreenshotActivity] if users allow capturing screen
 *
 * @author Phong-Kaster
 */
class BubbleService : LifecycleService() {
    private val tag = this.javaClass.simpleName

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    /**floatingViewManager is used to manage floating views (e.g., floating widgets or bubbles) that can appear on top of other apps*/
    private lateinit var fabManager: FloatingViewManager
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(this) }


    /**fabLayout represents for UI that this service draws on device's screen*/
    private val fabLayout by lazy { LayoutBubbleScreenshotBinding.inflate(inflater) }

    private val metrics = DisplayMetrics()
    private val overMargin by lazy { (metrics.density).toInt() }
    private var safeArea: Rect? = null

    companion object {
        const val SAFE_AREA = "safeArea"
        private const val SCREENSHOT_BUBBLE_SERVICE_ID = "screenshotBubbleServiceId"
        private const val SCREENSHOT_BUBBLE_SERVICE = "screenshotBubbleService"
        const val SCREENSHOT_BUBBLE_NOTIFICATION_ID = 441
    }

    override fun onCreate() {
        super.onCreate()
        popupNotification()
        //collectScreenshotEvent()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        windowManager.defaultDisplay?.getMetrics(metrics)
        (intent?.getParcelableExtra(SAFE_AREA) as Rect?)?.let { safeArea ->
            this.safeArea = safeArea
            setupScreenshotFabLayout(safeArea)
        }
        //emitScreenshotBubbleBus(enable = true)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fabManager.isInitialized) {
            fabManager.removeAllViewToWindow()
        }
        //emitScreenshotBubbleBus(enable = false)
    }

    /****************************************
     * pop up notification
     */
    private fun popupNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SCREENSHOT_BUBBLE_SERVICE,
                SCREENSHOT_BUBBLE_SERVICE,
                NotificationManager.IMPORTANCE_MIN
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, SCREENSHOT_BUBBLE_SERVICE)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Ready for taking screenshot")
            .setSilent(true)
            .setOngoing(true)
            .build()


//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            AppUtil.logcat(tag = tag, message = "Notification is not enabled. Stop !")
//            return
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                SCREENSHOT_BUBBLE_NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(SCREENSHOT_BUBBLE_NOTIFICATION_ID, notification)
        }
    }

    /****************************************
     * set up screenshot bubble layout
     */
    private fun setupScreenshotFabLayout(safeArea: Rect) {
        AppUtil.logcat(
            tag = tag,
            message = "set up Screenshot Fab Layout with safeArea = $safeArea"
        )
        //val safeArea = (intent?.parcelable<Intent>(SAFE_AREA) as Rect?)
        AppUtil.logcat(tag = tag, message = "set up Screenshot Fab Layout continue")
        val floatingViewListener = object : FloatingViewListener {
            override fun onTouchStarted() {
                try {
                    fabManager.addTrashView()
                } catch (e: WindowManager.BadTokenException) {
                    stopSelf()
                }
            }

            override fun onFinishFloatingView() {
                stopSelf()
            }

            override fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int) {
                fabManager.removeTrashView()
            }
        }
        fabManager = FloatingViewManager(this@BubbleService, floatingViewListener)
        fabManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed)
        fabManager.setActionTrashIconImage(R.drawable.ic_trash_action)
        fabManager.setSafeInsetRect(safeArea)
        fabManager.isTrashViewEnabled = true
        fabManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)


        val options = FloatingViewManager.Options().apply {
            moveDirection = FloatingViewManager.MOVE_DIRECTION_NEAREST
            overMargin = this@BubbleService.overMargin
            floatingViewX = metrics.widthPixels // X is the start of device screen
            floatingViewY =
                (metrics.heightPixels * 0.6).toInt() // Y equals 75% of device screen's height
            usePhysics = true
        }

        try {
            fabManager.addViewToWindow(fabLayout.root, options)
            fabLayout.root.setOnClickListener {
                Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show()
                // Emit that users are able to click on screenshot bubble
                //lifecycleScope.launch { AppEventBus.emitEvent(AppEvents.ClickedBubbleEvent()) }

//                when (RecorderBubbleBus.recordState.value) {
//                    RecordStatus.RECORDING, RecordStatus.PAUSE -> {
//                        lifecycleScope.launch { AppEventBus.emitEvent(RecorderEvent.TakeScreenshotWhileRecording) }
//                    }
//
//                    else -> {
//                        val enableWriteStorageAndroid10 =
//                            this@ScreenshotBubbleService.hasStoragePermissionEnabledAndroid10AndLower()
//                        if (!enableWriteStorageAndroid10) {
//                            openActivity(StorageActivity::class.java)
//                            return@setOnClickListener
//                        }
//
//                        lifecycleScope.launch { AppEventBus.emitEvent(ScreenshotEvent.Capture) }
//                        openActivity(ScreenshotActivity::class.java)
//                    }
//                }
            }

            fabLayout.menuBar.onFoldingItemClickListener = object : FoldingTabBar.OnFoldingItemSelectedListener {
                override fun onFoldingItemSelected(item: MenuItem): Boolean {
                    Toast.makeText(this@BubbleService, "onFoldingItemSelected", Toast.LENGTH_SHORT).show()
                    return true
                }

                override fun onOpened() {
                    Toast.makeText(this@BubbleService, "open", Toast.LENGTH_SHORT).show()
                }

                override fun onClosed() {
                    Toast.makeText(this@BubbleService, "close", Toast.LENGTH_SHORT).show()
                }

                override fun onOpenSetting() {
                    Toast.makeText(this@BubbleService, "open setting", Toast.LENGTH_SHORT).show()
                }

                override fun onOpenHome() {
                    Toast.makeText(this@BubbleService, "on pause resume", Toast.LENGTH_SHORT).show()
                }

                override fun onStartStop() {
                    Toast.makeText(this@BubbleService, "on pause resume", Toast.LENGTH_SHORT).show()
                }

                override fun onPauseResume() {
                    Toast.makeText(this@BubbleService, "on pause resume", Toast.LENGTH_SHORT).show()
                }

                override fun onOpenTool() {
                    Toast.makeText(this@BubbleService, "open tool", Toast.LENGTH_SHORT).show()
                }

            }

            fabManager.removeTrashView()
        } catch (ex: Exception) {
            AppUtil.logcat(tag = tag, message = "exception = ${ex.message}")
            ex.printStackTrace()
        }
    }
}