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
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.jetpackcomposebubble.R
import com.example.jetpackcomposebubble.databinding.LayoutBubbleScreenshotBinding
import com.example.jetpackcomposebubble.ui.component.MultiToolDialog
import com.example.jetpackcomposebubble.util.AppUtil
import com.example.jetpackcomposebubble.util.AppUtil.visible
import com.jetpack.bubble.FloatingViewListener
import com.jetpack.bubble.FloatingViewManager
import com.jetpack.menubar.FoldingTabBar

/**
 * Screenshot bubble service plays a role that draws a shotcut overlays other application to capture screen.
 *
 * Screenshot bubble service will start [ScreenshotActivity] if users allow capturing screen
 *
 * @author Phong-Kaster
 */
class BubbleService() : LifecycleService(), SavedStateRegistryOwner {

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

    var recordBubbleXPosition: Int = 0
    var recordBubbleYPosition: Int = 0

    private val constraintSetRTL = ConstraintSet()
    private val constraintSetLTR = ConstraintSet()

    private var composeView: ComposeView? = null


    companion object {
        const val SAFE_AREA = "safeArea"
        private const val SCREENSHOT_BUBBLE_SERVICE_ID = "screenshotBubbleServiceId"
        private const val SCREENSHOT_BUBBLE_SERVICE = "screenshotBubbleService"
        const val SCREENSHOT_BUBBLE_NOTIFICATION_ID = 441
    }


    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        popupNotification()
        registryComposeView()
    }

    private fun registryComposeView() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
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
        if (composeView != null) {
            composeView = null
        }

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
        initConstraintSetLTR()
        initConstraintSetRTL()
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
                recordBubbleXPosition = x
                recordBubbleYPosition = y
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
                AppUtil.logcat(tag = tag, message = "Recorder Bubble Service on click")
                if (!fabLayout.menuBar.isShowing) {
                    openMenu()
                } else {
                    closeMenu()
                }
                //lifecycleScope.launch { AppEventBus.emitEvent(AppEvents.ClickedBubbleEvent()) }
            }

            fabLayout.menuBar.onFoldingItemClickListener =
                object : FoldingTabBar.OnFoldingItemSelectedListener {
                    override fun onFoldingItemSelected(item: MenuItem): Boolean {
                        //Toast.makeText(this@BubbleService, "onFoldingItemSelected", Toast.LENGTH_SHORT).show()
                        return true
                    }

                    override fun onOpened() {
                        //Toast.makeText(this@BubbleService, "open", Toast.LENGTH_SHORT).show()
                    }

                    override fun onClosed() {
                        AppUtil.logcat(tag = tag, message = "onClosed")
                    }

                    override fun onOpenSetting() {
                        AppUtil.logcat(tag = tag, message = "onOpenSetting")
                    }

                    override fun onOpenHome() {
                        AppUtil.logcat(tag = tag, message = "onOpenHome")
                    }

                    override fun onStartStop() {
                        AppUtil.logcat(tag = tag, message = "onStartStop")
                    }

                    override fun onPauseResume() {
                        AppUtil.logcat(tag = tag, message = "onPauseResume")
                    }

                    override fun onOpenTool() {
                        showMultiToolDialog()
                    }

                }

            fabManager.removeTrashView()
        } catch (ex: Exception) {
            AppUtil.logcat(tag = tag, message = "exception = ${ex.message}")
            ex.printStackTrace()
        }
    }


    private fun isRTL() = recordBubbleXPosition > metrics.widthPixels / 2
    private fun openMenu() {
        if (isRTL()) {
            constraintSetRTL.clear(R.id.layout, ConstraintSet.START)
            constraintSetRTL.clear(R.id.menuBar, ConstraintSet.START)
            constraintSetRTL.applyTo(fabLayout.root as ConstraintLayout)

            fabLayout.menuBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginEnd = 20
            }
        } else {
            constraintSetLTR.clear(R.id.layout, ConstraintSet.END)
            constraintSetLTR.clear(R.id.menuBar, ConstraintSet.END)
            constraintSetLTR.applyTo(fabLayout.root as ConstraintLayout?)

            fabLayout.menuBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = 20
            }
        }

        fabLayout.menuBar.expand(isRTL())
        //fabLayout.chronometer.gone()
        fabLayout.ivRecorder.apply {
            visible()
            setImageResource(R.drawable.ic_close)
        }
    }

    private fun closeMenu() {
        fabLayout.apply {
            menuBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = 0
                marginEnd = 0
            }

            fabLayout.ivRecorder.visible()
            fabLayout.ivRecorder.setImageResource(R.drawable.ic_camera)
            menuBar.rollUp()
            //menuBar.gone()
        }
    }

    private fun initConstraintSetLTR() {
        constraintSetLTR.apply {
            clone(fabLayout.root as ConstraintLayout)
            connect(
                R.id.layout,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(
                R.id.menuBar,
                ConstraintSet.START,
                R.id.layout,
                ConstraintSet.END
            )
            connect(
                R.id.layout,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            connect(
                R.id.layout,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }
    }

    private fun initConstraintSetRTL() {
        constraintSetRTL.apply {
            clone(fabLayout.root as ConstraintLayout)
            connect(
                R.id.layout,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            connect(
                R.id.menuBar,
                ConstraintSet.END,
                R.id.layout,
                ConstraintSet.START
            )

            connect(
                R.id.layout,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            connect(
                R.id.layout,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }
    }

    private fun showMultiToolDialog() {
        if (composeView != null) {
            windowManager.removeView(composeView)
            composeView = null
        }

        composeView = ComposeView(this)
        composeView?.setViewTreeSavedStateRegistryOwner(this@BubbleService)
        composeView?.setViewTreeLifecycleOwner(this@BubbleService)
        composeView?.setContent {
            MultiToolDialog(
                onClose = { windowManager.removeView(composeView) },
                onOpenScreenshot = {},
                onOpenFacecam = {},
                onOpenBrush = {},
                onOpenRegional = {},
            )
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        try {
            windowManager.addView(composeView, layoutParams)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}