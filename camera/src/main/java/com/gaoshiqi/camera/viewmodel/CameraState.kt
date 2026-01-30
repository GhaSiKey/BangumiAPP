package com.gaoshiqi.camera.viewmodel

import android.net.Uri

/**
 * 屏幕状态（当前显示哪个屏幕）
 */
sealed class ScreenState {
    /** 相机预览 */
    data object Camera : ScreenState()

    /** 拍照后预览 */
    data class PhotoPreview(val photoUri: Uri) : ScreenState()

    /** 相册浏览 */
    data object Gallery : ScreenState()

    /** 单张照片查看 */
    data class PhotoViewer(val photo: PhotoItem, val initialIndex: Int) : ScreenState()
}

/**
 * 镜头朝向
 */
enum class LensFacing {
    BACK,
    FRONT
}

/**
 * 照片项
 */
data class PhotoItem(
    val uri: String,
    val timestamp: Long,
    val fileName: String
)

/**
 * 聚焦点
 */
data class FocusPoint(
    val x: Float,
    val y: Float
)

/**
 * 相机 UI 状态
 */
data class CameraUiState(
    val screenState: ScreenState = ScreenState.Camera,
    val lensFacing: LensFacing = LensFacing.BACK,
    val isCapturing: Boolean = false,
    val isSwitchingCamera: Boolean = false,
    val latestPhotoUri: Uri? = null,
    val galleryPhotos: List<PhotoItem> = emptyList(),
    val pendingDeletePhoto: PhotoItem? = null,
    val errorMessage: String? = null,
    val shouldClose: Boolean = false,
    val focusPoint: FocusPoint? = null,
    val currentZoomRatio: Float = 1f,
    // 多选模式
    val isSelectionMode: Boolean = false,
    val selectedPhotos: Set<String> = emptySet(),
    val showDeleteSelectedDialog: Boolean = false
)
