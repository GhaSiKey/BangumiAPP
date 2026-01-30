package com.gaoshiqi.camera.viewmodel

/**
 * 相机模块用户意图
 */
sealed class CameraIntent {
    // 相机操作
    data object TakePhoto : CameraIntent()
    data object SwitchCamera : CameraIntent()

    // 照片预览
    data object ConfirmPhoto : CameraIntent()
    data object RetakePhoto : CameraIntent()

    // 相册操作
    data object OpenGallery : CameraIntent()
    data object CloseGallery : CameraIntent()
    data class SelectPhoto(val uri: String) : CameraIntent()
    data class DeletePhoto(val uri: String) : CameraIntent()
    data object ConfirmDeletePhoto : CameraIntent()
    data object CancelDeletePhoto : CameraIntent()

    // 多选操作
    data object EnterSelectionMode : CameraIntent()
    data object ExitSelectionMode : CameraIntent()
    data class TogglePhotoSelection(val uri: String) : CameraIntent()
    data object DeleteSelectedPhotos : CameraIntent()
    data object ConfirmDeleteSelected : CameraIntent()

    // 导航
    data object NavigateBack : CameraIntent()

    // 相机初始化
    data object CameraReady : CameraIntent()
    data class CameraError(val message: String) : CameraIntent()
}
