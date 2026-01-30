package com.gaoshiqi.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaoshiqi.camera.ui.CameraScreen
import com.gaoshiqi.camera.viewmodel.CameraViewModel

/**
 * Camera 模块对外 API
 *
 * 提供三种使用方式：
 * 1. 直接启动 Activity
 * 2. 使用 ActivityResultContract 获取拍照结果
 * 3. 作为 Composable 组件嵌入
 */
object CameraModule {

    /**
     * 预热相机模块
     * 建议在应用启动时或进入相机前的页面时调用，以加速相机打开
     */
    fun warmUp(context: Context) {
        CameraActivity.warmUp(context)
    }

    /**
     * 方式1：直接启动相机 Activity
     */
    fun startCamera(context: Context) {
        CameraActivity.start(context)
    }

    /**
     * 方式2：获取拍照结果的 Contract
     *
     * 使用方法：
     * ```kotlin
     * val launcher = rememberLauncherForActivityResult(CameraModule.CapturePhotoContract()) { photoPath ->
     *     photoPath?.let { /* 处理照片路径 */ }
     * }
     * launcher.launch(Unit)
     * ```
     */
    class CapturePhotoContract : ActivityResultContract<Unit, String?>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, CameraActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            return if (resultCode == Activity.RESULT_OK) {
                intent?.getStringExtra(EXTRA_PHOTO_PATH)
            } else {
                null
            }
        }
    }

    /**
     * 方式3：Composable 组件
     *
     * 使用方法：
     * ```kotlin
     * CameraModule.CameraRoute(
     *     onPhotoSaved = { path -> /* 处理照片路径 */ },
     *     onClose = { /* 关闭相机 */ }
     * )
     * ```
     */
    @Composable
    fun CameraRoute(
        onPhotoSaved: (String) -> Unit = {},
        onClose: () -> Unit
    ) {
        val viewModel: CameraViewModel = viewModel(
            factory = CameraViewModel.Factory(
                androidx.compose.ui.platform.LocalContext.current.applicationContext
            )
        )

        CameraScreen(
            viewModel = viewModel,
            onClose = onClose
        )
    }

    const val EXTRA_PHOTO_PATH = "extra_photo_path"
}
