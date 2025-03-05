package com.example.jetpackcomposebubble.ui.component

import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jetpackcomposebubble.R

@Composable
fun MultiToolDialog(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onOpenScreenshot: () -> Unit = {},
    onOpenFacecam: () -> Unit = {},
    onOpenBrush: () -> Unit = {},
    onOpenRegional: () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val boxModifier = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        modifier.requiredWidth(screenWidth / 2)
    } else {
        modifier.fillMaxWidth()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = boxModifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .imePadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(shape = CircleShape)
                        .clickable {
                            onClose()
                        }
                        .align(BiasAlignment(1f, 0f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = Color.Black,
                        contentDescription = null,
                        modifier = Modifier
                            .size(15.dp)
                    )
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.app_name),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(31.dp),
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                MultiToolButton(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_record_menu,
                    text = stringResource(R.string.app_name),
                    onClick = {
                        onClose()
                        onOpenScreenshot()
                    }
                )

                MultiToolButton(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_pause_red,
                    text = stringResource(R.string.app_name),
                    onClick = {
                        onClose()
                        onOpenFacecam()
                    }
                )

                MultiToolButton(
                    modifier = Modifier.weight(1f),
                    icon = com.jetpack.menubar.R.drawable.ic_stop_red,
                    text = stringResource(R.string.app_name),
                    onClick = {
                        onClose()
                        onOpenBrush()
                    }
                )

                MultiToolButton(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_play_red,
                    text = stringResource(R.string.app_name),
                    onClick = {
                        onClose()
                    }
                )
            }
        }
    }
}

@Composable
fun MultiToolButton(
    modifier: Modifier,
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp)
                .aspectRatio(1f)
                .clickable { onClick() }
                .size(40.dp)
        )

        Text(
            text = text,
            maxLines = 1,
            modifier = Modifier
                .padding(top = 8.dp)
                .basicMarquee(Int.MAX_VALUE),
        )
    }
}

@Preview
@Composable
private fun PreviewMultiToolDialog() {
    MultiToolDialog()
}