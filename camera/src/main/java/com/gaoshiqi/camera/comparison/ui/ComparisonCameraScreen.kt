package com.gaoshiqi.camera.comparison.ui

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gaoshiqi.camera.R
import com.gaoshiqi.camera.comparison.viewmodel.CaptureState
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonCameraIntent
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonCameraViewModel
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonLensFacing
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonScreenState
import com.gaoshiqi.camera.comparison.viewmodel.ReferenceImageState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay

/**
 * 对比拍照主屏幕
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ComparisonCameraScreen(
    viewModel: ComparisonCameraViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(uiState.shouldClose) {
        if (uiState.shouldClose) {
            onClose()
        }
    }

    BackHandler {
        viewModel.handleIntent(ComparisonCameraIntent.NavigateBack)
    }

    if (cameraPermissionState.status.isGranted) {
        when (val screenState = uiState.screenState) {
            is ComparisonScreenState.Camera -> {
                ComparisonCameraContent(
                    viewModel = viewModel,
                    pointName = uiState.pointName,
                    subjectName = uiState.subjectName,
                    referenceState = uiState.referenceState,
                    lensFacing = uiState.lensFacing,
                    captureState = uiState.captureState,
                    isSwitchingCamera = uiState.isSwitchingCamera,
                    onTakePhoto = { viewModel.handleIntent(ComparisonCameraIntent.TakePhoto) },
                    onSwitchCamera = { viewModel.handleIntent(ComparisonCameraIntent.SwitchCamera) },
                    onRetryLoadReference = { viewModel.handleIntent(ComparisonCameraIntent.RetryLoadReference) },
                    onBack = { viewModel.handleIntent(ComparisonCameraIntent.NavigateBack) }
                )
            }
            is ComparisonScreenState.PhotoPreview -> {
                ComparisonPhotoPreview(
                    composedBitmap = screenState.composedBitmap,
                    onConfirm = { viewModel.handleIntent(ComparisonCameraIntent.ConfirmPhoto) },
                    onRetake = { viewModel.handleIntent(ComparisonCameraIntent.RetakePhoto) }
                )
            }
        }
    } else {
        PermissionRequest(
            shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
        )
    }
}

@Composable
private fun ComparisonCameraContent(
    viewModel: ComparisonCameraViewModel,
    pointName: String,
    subjectName: String,
    referenceState: ReferenceImageState,
    lensFacing: ComparisonLensFacing,
    captureState: CaptureState,
    isSwitchingCamera: Boolean,
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit,
    onRetryLoadReference: () -> Unit,
    onBack: () -> Unit
) {
    val isCapturing = captureState is CaptureState.Capturing || captureState is CaptureState.Composing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 对比预览区域
        ComparisonPreview(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            lensFacing = lensFacing,
            referenceState = referenceState,
            isCapturing = isCapturing,
            isSwitchingCamera = isSwitchingCamera,
            onRetryLoadReference = onRetryLoadReference
        )

        // 顶部：返回按钮和标题
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = stringResource(R.string.camera_back),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = pointName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subjectName,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 底部控制栏
        ComparisonCameraControls(
            modifier = Modifier.align(Alignment.BottomCenter),
            isCapturing = isCapturing,
            canTakePhoto = referenceState is ReferenceImageState.Success,
            onTakePhoto = onTakePhoto,
            onSwitchCamera = onSwitchCamera
        )
    }
}

@Composable
private fun ComparisonCameraControls(
    modifier: Modifier = Modifier,
    isCapturing: Boolean,
    canTakePhoto: Boolean,
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    // 快门按钮动画
    var isShutterPressed by remember { mutableStateOf(false) }
    val shutterScale by animateFloatAsState(
        targetValue = if (isShutterPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 400f
        ),
        label = "shutter_scale"
    )

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
        // 占位（保持布局平衡）
        Box(modifier = Modifier.size(48.dp))

        // 拍照按钮
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(shutterScale)
                .clip(CircleShape)
                .background(if (canTakePhoto) Color.White else Color.Gray)
                .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable(enabled = !isCapturing && canTakePhoto, onClick = onTakePhoto),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCapturing -> Color.Gray
                            canTakePhoto -> Color.White
                            else -> Color.Gray
                        }
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
                contentDescription = stringResource(R.string.camera_switch),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 合成图片预览
 */
@Composable
private fun ComparisonPhotoPreview(
    composedBitmap: Bitmap,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            bitmap = composedBitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.comparison_composed_photo),
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

            // 确认保存按钮
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

@Composable
private fun PermissionRequest(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_camera),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.camera_permission_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (shouldShowRationale) {
                stringResource(R.string.camera_permission_denied)
            } else {
                stringResource(R.string.camera_permission_message)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.camera_permission_grant))
        }
    }
}
