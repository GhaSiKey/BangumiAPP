package com.gaoshiqi.camera.comparison.viewmodel

import android.graphics.Bitmap

/**
 * 对比拍照屏幕状态
 */
sealed class ComparisonScreenState {
    /** 相机预览模式 */
    data object Camera : ComparisonScreenState()

    /** 合成图预览模式 */
    data class PhotoPreview(val composedBitmap: Bitmap) : ComparisonScreenState()
}

/**
 * 参考图加载状态
 */
sealed class ReferenceImageState {
    /** 加载中 */
    data object Loading : ReferenceImageState()

    /** 加载成功 */
    data class Success(val bitmap: Bitmap) : ReferenceImageState()

    /** 加载失败 */
    data class Error(val message: String) : ReferenceImageState()
}

/**
 * 拍照状态
 */
sealed class CaptureState {
    /** 空闲状态 */
    data object Idle : CaptureState()

    /** 正在拍照 */
    data object Capturing : CaptureState()

    /** 正在合成图片 */
    data object Composing : CaptureState()

    /** 保存成功 */
    data object Saved : CaptureState()

    /** 发生错误 */
    data class Error(val message: String) : CaptureState()
}

/**
 * 镜头朝向
 */
enum class ComparisonLensFacing {
    BACK,
    FRONT
}

/**
 * 对比拍照 UI 状态
 */
data class ComparisonCameraUiState(
    /** 当前屏幕状态 */
    val screenState: ComparisonScreenState = ComparisonScreenState.Camera,

    /** 参考图加载状态 */
    val referenceState: ReferenceImageState = ReferenceImageState.Loading,

    /** 拍照状态 */
    val captureState: CaptureState = CaptureState.Idle,

    /** 镜头朝向 */
    val lensFacing: ComparisonLensFacing = ComparisonLensFacing.BACK,

    /** 正在切换摄像头 */
    val isSwitchingCamera: Boolean = false,

    /** 参考图信息 */
    val referenceImageUrl: String = "",
    val pointName: String = "",
    val subjectName: String = "",

    /** 拍立得风格合成所需的额外信息 */
    val subjectCover: String = "",
    val episode: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,

    /** 错误信息 */
    val errorMessage: String? = null,

    /** 是否应该关闭页面 */
    val shouldClose: Boolean = false
)
