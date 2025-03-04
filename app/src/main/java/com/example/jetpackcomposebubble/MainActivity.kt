package com.example.jetpackcomposebubble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposebubble.core.CoreLayout
import com.example.jetpackcomposebubble.ui.theme.JetpackComposeBubbleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeBubbleTheme {
                MainLayout(
                    onEnableService = {}
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