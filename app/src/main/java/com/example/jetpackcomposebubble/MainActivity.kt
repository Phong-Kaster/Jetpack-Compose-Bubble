package com.example.jetpackcomposebubble

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apero.bubble.FloatingViewManager
import com.example.jetpackcomposebubble.core.CoreLayout
import com.example.jetpackcomposebubble.service.BubbleService
import com.example.jetpackcomposebubble.service.BubbleService.Companion.SAFE_AREA
import com.example.jetpackcomposebubble.ui.theme.JetpackComposeBubbleTheme
import com.example.jetpackcomposebubble.util.AppUtil
import com.example.jetpackcomposebubble.util.AppUtil.isServiceRunning

class MainActivity : ComponentActivity() {
    private val tag = this.javaClass.simpleName



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeBubbleTheme {
                val context = LocalContext.current
                val floatingIconLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->

                }

                MainLayout(
                    onEnableService = {
                        if (!Settings.canDrawOverlays(this)) {
                            Log.d("TAG", "canDrawOverlays NOT = ${Settings.canDrawOverlays(context)}")
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            floatingIconLauncher.launch(intent)
                            return@MainLayout
                        }

                        if (!isServiceRunning(BubbleService::class.java) && Settings.canDrawOverlays(this)) {
                            val intent = Intent(this, BubbleService::class.java).apply {
                                val key: String = SAFE_AREA
                                val safeArea = FloatingViewManager.findCutoutSafeArea(this@MainActivity)
                                AppUtil.logcat(tag = tag, message = "safe area of screenshot bubble = $safeArea")
                                putExtra(key, safeArea)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                application.startForegroundService(intent)
                            } else {
                                application.startService(intent)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainLayout(
    onEnableService: () -> Unit = {},
) {
    CoreLayout(
        content = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = onEnableService,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Blue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(text = "Enable Bubble Service", color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetpackComposeBubbleTheme {
        MainLayout(onEnableService = {})
    }
}