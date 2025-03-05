package com.example.jetpackcomposebubble

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposebubble.core.CoreLayout
import com.example.jetpackcomposebubble.service.BubbleService
import com.example.jetpackcomposebubble.ui.theme.JetpackComposeBubbleTheme
import com.example.jetpackcomposebubble.util.AppUtil.isServiceRunning
import com.example.jetpackcomposebubble.util.AppUtil.startBubbleService

class MainActivity : ComponentActivity() {
    private val tag = this.javaClass.simpleName


    private val floatingIconLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            //  you will get result here in result.data
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeBubbleTheme {
                val context = LocalContext.current

                MainLayout(
                    onEnableService = {
                        // Open setting "Display over other apps"
                        if (!Settings.canDrawOverlays(this)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            floatingIconLauncher.launch(intent)
                            return@MainLayout
                        }

                        val isServiceRunning = isServiceRunning(BubbleService::class.java)
                        if(isServiceRunning){
                            // stop bubble service
                            val intent = Intent(this, BubbleService::class.java)
                            stopService(intent)
                        } else {
                            // start bubble service if every condition is satisfied
                            startBubbleService()
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