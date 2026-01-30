package com.gaoshiqi.camera.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.gaoshiqi.camera.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PhotoPreviewScreen(
    photoUri: Uri,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        GlideImage(
            model = photoUri,
            contentDescription = "Captured photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 重拍按钮
            IconButton(
                onClick = onRetake,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Red.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(R.string.camera_retake),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 确认按钮
            IconButton(
                onClick = onConfirm,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Green.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = stringResource(R.string.camera_confirm),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
