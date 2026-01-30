package com.gaoshiqi.camera.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaoshiqi.camera.R
import com.gaoshiqi.camera.viewmodel.CameraIntent
import com.gaoshiqi.camera.viewmodel.CameraViewModel
import com.gaoshiqi.camera.viewmodel.FocusPoint
import com.gaoshiqi.camera.viewmodel.LensFacing
import com.gaoshiqi.camera.viewmodel.ScreenState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
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
        viewModel.handleIntent(CameraIntent.NavigateBack)
    }

    if (cameraPermissionState.status.isGranted) {
        when (val screenState = uiState.screenState) {
            is ScreenState.Camera -> {
                CameraContent(
                    viewModel = viewModel,
                    lensFacing = uiState.lensFacing,
                    focusPoint = uiState.focusPoint,
                    latestPhotoUri = uiState.latestPhotoUri,
                    isCapturing = uiState.isCapturing,
                    isSwitchingCamera = uiState.isSwitchingCamera,
                    onTakePhoto = { viewModel.handleIntent(CameraIntent.TakePhoto) },
                    onSwitchCamera = { viewModel.handleIntent(CameraIntent.SwitchCamera) },
                    onOpenGallery = { viewModel.handleIntent(CameraIntent.OpenGallery) },
                    onBack = { viewModel.handleIntent(CameraIntent.NavigateBack) }
                )
            }
            is ScreenState.PhotoPreview -> {
                PhotoPreviewScreen(
                    photoUri = screenState.photoUri,
                    onConfirm = { viewModel.handleIntent(CameraIntent.ConfirmPhoto) },
                    onRetake = { viewModel.handleIntent(CameraIntent.RetakePhoto) }
                )
            }
            is ScreenState.Gallery -> {
                GalleryScreen(
                    photos = uiState.galleryPhotos,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedPhotos = uiState.selectedPhotos,
                    showDeleteDialog = uiState.showDeleteSelectedDialog,
                    onPhotoClick = { uri -> viewModel.handleIntent(CameraIntent.SelectPhoto(uri)) },
                    onPhotoLongClick = { uri ->
                        viewModel.handleIntent(CameraIntent.EnterSelectionMode)
                        viewModel.handleIntent(CameraIntent.TogglePhotoSelection(uri))
                    },
                    onToggleSelection = { uri -> viewModel.handleIntent(CameraIntent.TogglePhotoSelection(uri)) },
                    onDeleteSelected = { viewModel.handleIntent(CameraIntent.DeleteSelectedPhotos) },
                    onConfirmDelete = { viewModel.handleIntent(CameraIntent.ConfirmDeleteSelected) },
                    onCancelDelete = { viewModel.handleIntent(CameraIntent.ExitSelectionMode) },
                    onExitSelectionMode = { viewModel.handleIntent(CameraIntent.ExitSelectionMode) },
                    onBack = { viewModel.handleIntent(CameraIntent.CloseGallery) }
                )
            }
            is ScreenState.PhotoViewer -> {
                PhotoViewerScreen(
                    photos = uiState.galleryPhotos,
                    initialIndex = screenState.initialIndex,
                    showDeleteDialog = uiState.pendingDeletePhoto != null,
                    onPhotoChanged = { photo -> viewModel.handleIntent(CameraIntent.ViewPhoto(photo)) },
                    onBack = { viewModel.handleIntent(CameraIntent.NavigateBack) },
                    onDelete = { photo -> viewModel.handleIntent(CameraIntent.DeletePhoto(photo.uri)) },
                    onConfirmDelete = { viewModel.handleIntent(CameraIntent.ConfirmDeletePhoto) },
                    onCancelDelete = { viewModel.handleIntent(CameraIntent.CancelDeletePhoto) }
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
private fun CameraContent(
    viewModel: CameraViewModel,
    lensFacing: LensFacing,
    focusPoint: FocusPoint?,
    latestPhotoUri: android.net.Uri?,
    isCapturing: Boolean,
    isSwitchingCamera: Boolean,
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 相机预览
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            lensFacing = lensFacing,
            focusPoint = focusPoint,
            isCapturing = isCapturing,
            isSwitchingCamera = isSwitchingCamera
        )

        // 顶部返回按钮（考虑状态栏 insets）
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
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

        // 底部控制栏
        CameraControls(
            modifier = Modifier.align(Alignment.BottomCenter),
            latestPhotoUri = latestPhotoUri,
            isCapturing = isCapturing,
            onTakePhoto = onTakePhoto,
            onSwitchCamera = onSwitchCamera,
            onOpenGallery = onOpenGallery
        )
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
            .safeDrawingPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
