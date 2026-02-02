package com.gaoshiqi.camera.comparison.viewmodel

/**
 * 对比拍照模块用户意图
 */
sealed class ComparisonCameraIntent {
    /** 拍照 */
    data object TakePhoto : ComparisonCameraIntent()

    /** 切换前后摄像头 */
    data object SwitchCamera : ComparisonCameraIntent()

    /** 确认保存照片 */
    data object ConfirmPhoto : ComparisonCameraIntent()

    /** 重新拍照 */
    data object RetakePhoto : ComparisonCameraIntent()

    /** 返回/关闭 */
    data object NavigateBack : ComparisonCameraIntent()

    /** 相机初始化完成 */
    data object CameraReady : ComparisonCameraIntent()

    /** 相机初始化失败 */
    data class CameraError(val message: String) : ComparisonCameraIntent()

    /** 重试加载参考图 */
    data object RetryLoadReference : ComparisonCameraIntent()
}
