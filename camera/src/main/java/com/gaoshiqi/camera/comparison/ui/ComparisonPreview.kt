package com.gaoshiqi.camera.comparison.ui

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gaoshiqi.camera.R
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonCameraViewModel
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonLensFacing
import com.gaoshiqi.camera.comparison.viewmodel.ReferenceImageState

/** 16:9 宽高比 */
private const val ASPECT_RATIO_16_9 = 16f / 9f

/**
 * 对比拍照预览组件
 * 上半部分显示相机预览，下半部分显示参考图
 */
@Composable
fun ComparisonPreview(
    modifier: Modifier = Modifier,
    viewModel: ComparisonCameraViewModel,
    lensFacing: ComparisonLensFacing,
    referenceState: ReferenceImageState,
    isCapturing: Boolean = false,
    isSwitchingCamera: Boolean = false,
    onRetryLoadReference: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var isCameraBound by remember { mutableStateOf(false) }

    // 拍照时的画面定格
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showFrozenFrame by remember { mutableStateOf(false) }

    // 闪白动画
    val flashAlpha = remember { Animatable(0f) }
    var showFlash by remember { mutableStateOf(false) }

    // 拍照时截取画面并显示闪白效果
    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            frozenBitmap = previewView.bitmap
            showFrozenFrame = true
            showFlash = true
            flashAlpha.snapTo(0.9f)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 150)
            )
            showFlash = false
        } else {
            showFrozenFrame = false
            frozenBitmap = null
        }
    }

    // 切换摄像头时定格画面
    LaunchedEffect(isSwitchingCamera) {
        if (isSwitchingCamera) {
            frozenBitmap = previewView.bitmap
            showFrozenFrame = true
        } else {
            showFrozenFrame = false
            frozenBitmap = null
        }
    }

    // 绑定相机
    DisposableEffect(lensFacing) {
        if (isCameraBound) {
            viewModel.rebindCamera(lifecycleOwner, previewView)
            viewModel.onCameraSwitchComplete()
        } else {
            viewModel.bindCamera(lifecycleOwner, previewView)
            isCameraBound = true
        }
        onDispose { }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 上半部分：相机预览（宽度填满，16:9 比例，裁剪超出部分）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ASPECT_RATIO_16_9)
                .clipToBounds()
                .background(Color.DarkGray), // 加载时的背景
            contentAlignment = Alignment.Center
        ) {
            // 相机预览
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // 定格画面
            if (showFrozenFrame && frozenBitmap != null) {
                Image(
                    bitmap = frozenBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 闪白效果
            if (showFlash) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flashAlpha.value))
                )
            }
        }

        // 分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )

        // 下半部分：参考图（宽度填满，16:9 比例）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ASPECT_RATIO_16_9),
            contentAlignment = Alignment.Center
        ) {
            when (referenceState) {
                is ReferenceImageState.Loading -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                is ReferenceImageState.Success -> {
                    Image(
                        bitmap = referenceState.bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.comparison_reference_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                is ReferenceImageState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.comparison_load_failed),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        TextButton(onClick = onRetryLoadReference) {
                            Text(
                                text = stringResource(R.string.comparison_retry),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
