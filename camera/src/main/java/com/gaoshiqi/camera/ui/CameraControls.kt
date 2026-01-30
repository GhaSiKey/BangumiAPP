package com.gaoshiqi.camera.ui

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.gaoshiqi.camera.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CameraControls(
    modifier: Modifier = Modifier,
    latestPhotoUri: Uri?,
    isCapturing: Boolean,
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    // 快门按钮按下动画状态
    var isShutterPressed by remember { mutableStateOf(false) }

    // 快门缩放动画
    val shutterScale by animateFloatAsState(
        targetValue = if (isShutterPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 400f
        ),
        label = "shutter_scale"
    )

    // 当开始拍照时触发按下动画
    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            isShutterPressed = true
            delay(150)
            isShutterPressed = false
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 相册缩略图
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
                .clickable(onClick = onOpenGallery),
            contentAlignment = Alignment.Center
        ) {
            if (latestPhotoUri != null) {
                GlideImage(
                    model = latestPhotoUri,
                    contentDescription = "Latest photo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gallery),
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 拍照按钮（带缩放动画）
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(shutterScale)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable(enabled = !isCapturing, onClick = onTakePhoto),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCapturing) Color.Gray else Color.White
                    )
                    .border(2.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            )
        }

        // 切换摄像头按钮
        IconButton(
            onClick = onSwitchCamera,
            modifier = Modifier
                .size(48.dp)
                .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_switch_camera),
                contentDescription = "Switch camera",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
