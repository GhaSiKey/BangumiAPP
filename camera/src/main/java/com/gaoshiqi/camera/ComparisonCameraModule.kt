package com.gaoshiqi.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaoshiqi.camera.comparison.ComparisonCameraActivity
import com.gaoshiqi.camera.comparison.ui.ComparisonCameraScreen
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonCameraViewModel
import com.gaoshiqi.camera.gallery.GalleryActivity

/**
 * 对比拍照所需的圣地数据
 *
 * @property referenceImageUrl 参考图片 URL（必须）
 * @property pointName 圣地名称（必须）
 * @property subjectName 番剧名称（必须）
 * @property subjectCover 番剧封面 URL，用于拍立得风格合成（可选）
 * @property episode 出现集数（可选）
 * @property lat 纬度（可选，默认 0.0）
 * @property lng 经度（可选，默认 0.0）
 */
data class ComparisonPhotoData(
    val referenceImageUrl: String,
    val pointName: String,
    val subjectName: String,
    val subjectCover: String = "",
    val episode: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0
) {
    init {
        require(referenceImageUrl.isNotBlank()) { "referenceImageUrl cannot be blank" }
        require(pointName.isNotBlank()) { "pointName cannot be blank" }
        require(subjectName.isNotBlank()) { "subjectName cannot be blank" }
    }

    companion object {
        internal const val EXTRA_REFERENCE_IMAGE_URL = "reference_image_url"
        internal const val EXTRA_POINT_NAME = "point_name"
        internal const val EXTRA_SUBJECT_NAME = "subject_name"
        internal const val EXTRA_SUBJECT_COVER = "subject_cover"
        internal const val EXTRA_EPISODE = "episode"
        internal const val EXTRA_LAT = "lat"
        internal const val EXTRA_LNG = "lng"

        /**
         * 从 Intent 中解析数据
         */
        internal fun fromIntent(intent: Intent): ComparisonPhotoData? {
            val referenceImageUrl = intent.getStringExtra(EXTRA_REFERENCE_IMAGE_URL)
                ?: return null
            val pointName = intent.getStringExtra(EXTRA_POINT_NAME) ?: ""
            val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: ""

            return ComparisonPhotoData(
                referenceImageUrl = referenceImageUrl,
                pointName = pointName,
                subjectName = subjectName,
                subjectCover = intent.getStringExtra(EXTRA_SUBJECT_COVER) ?: "",
                episode = intent.getStringExtra(EXTRA_EPISODE),
                lat = intent.getDoubleExtra(EXTRA_LAT, 0.0),
                lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
            )
        }
    }

    /**
     * 将数据写入 Intent
     */
    internal fun toIntent(intent: Intent): Intent = intent.apply {
        putExtra(EXTRA_REFERENCE_IMAGE_URL, referenceImageUrl)
        putExtra(EXTRA_POINT_NAME, pointName)
        putExtra(EXTRA_SUBJECT_NAME, subjectName)
        putExtra(EXTRA_SUBJECT_COVER, subjectCover)
        putExtra(EXTRA_EPISODE, episode)
        putExtra(EXTRA_LAT, lat)
        putExtra(EXTRA_LNG, lng)
    }
}

/**
 * 对比拍照结果
 *
 * @property composedPhotoPath 合成图（拍立得风格）的保存路径
 * @property originalPhotoPath 原图的保存路径（可能为 null）
 */
data class ComparisonPhotoResult(
    val composedPhotoPath: String,
    val originalPhotoPath: String?
)

/**
 * 对比拍照模块对外 API
 *
 * 提供圣地巡礼对比拍照功能，支持：
 * - 相机预览与参考图对比
 * - 拍立得风格图片合成
 * - 同时保存原图和合成图
 *
 * ## 使用方式
 *
 * ### 方式1：直接启动 Activity
 * ```kotlin
 * val data = ComparisonPhotoData(
 *     referenceImageUrl = "https://example.com/image.jpg",
 *     pointName = "木之浦 Village",
 *     subjectName = "跃动青春",
 *     subjectCover = "https://example.com/cover.jpg",
 *     episode = "1",
 *     lat = 35.6762,
 *     lng = 139.6503
 * )
 * ComparisonCameraModule.startCamera(context, data)
 * ```
 *
 * ### 方式2：使用 ActivityResultContract 获取拍照结果
 * ```kotlin
 * val launcher = rememberLauncherForActivityResult(
 *     ComparisonCameraModule.CaptureContract()
 * ) { result ->
 *     result?.let {
 *         // 合成图: it.composedPhotoPath
 *         // 原图: it.originalPhotoPath
 *     }
 * }
 * launcher.launch(data)
 * ```
 *
 * ### 方式3：作为 Composable 组件嵌入
 * ```kotlin
 * ComparisonCameraModule.ComparisonCameraRoute(
 *     data = data,
 *     onPhotoSaved = { composedPath, originalPath -> },
 *     onClose = { }
 * )
 * ```
 */
object ComparisonCameraModule {

    /**
     * 方式1：直接启动对比拍照 Activity
     *
     * @param context 上下文
     * @param data 对比拍照所需的圣地数据
     */
    fun startCamera(context: Context, data: ComparisonPhotoData) {
        ComparisonCameraActivity.start(
            context = context,
            referenceImageUrl = data.referenceImageUrl,
            pointName = data.pointName,
            subjectName = data.subjectName,
            subjectCover = data.subjectCover,
            episode = data.episode,
            lat = data.lat,
            lng = data.lng
        )
    }

    /**
     * 方式2：获取拍照结果的 Contract
     *
     * 使用方法：
     * ```kotlin
     * val launcher = rememberLauncherForActivityResult(
     *     ComparisonCameraModule.CaptureContract()
     * ) { result ->
     *     result?.composedPhotoPath?.let { path ->
     *         // 处理合成图路径
     *     }
     * }
     * launcher.launch(data)
     * ```
     */
    class CaptureContract : ActivityResultContract<ComparisonPhotoData, ComparisonPhotoResult?>() {
        override fun createIntent(context: Context, input: ComparisonPhotoData): Intent {
            return Intent(context, ComparisonCameraActivity::class.java).also {
                input.toIntent(it)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ComparisonPhotoResult? {
            if (resultCode != Activity.RESULT_OK || intent == null) return null

            val composedPath = intent.getStringExtra(EXTRA_COMPOSED_PHOTO_PATH) ?: return null
            val originalPath = intent.getStringExtra(EXTRA_ORIGINAL_PHOTO_PATH)

            return ComparisonPhotoResult(
                composedPhotoPath = composedPath,
                originalPhotoPath = originalPath
            )
        }
    }

    /**
     * 方式3：Composable 组件
     *
     * 使用方法：
     * ```kotlin
     * ComparisonCameraModule.ComparisonCameraRoute(
     *     data = data,
     *     onPhotoSaved = { composedPath, originalPath ->
     *         // 处理保存结果
     *     },
     *     onClose = {
     *         // 关闭相机
     *     }
     * )
     * ```
     *
     * @param data 对比拍照所需的圣地数据
     * @param onPhotoSaved 照片保存成功回调，参数为 (合成图路径, 原图路径)
     * @param onClose 关闭回调
     */
    @Composable
    fun ComparisonCameraRoute(
        data: ComparisonPhotoData,
        onPhotoSaved: (composedPath: String, originalPath: String?) -> Unit = { _, _ -> },
        onClose: () -> Unit
    ) {
        val context = LocalContext.current.applicationContext
        val viewModel: ComparisonCameraViewModel = viewModel(
            factory = ComparisonCameraViewModel.Factory(
                context = context,
                referenceImageUrl = data.referenceImageUrl,
                pointName = data.pointName,
                subjectName = data.subjectName,
                subjectCover = data.subjectCover,
                episode = data.episode,
                lat = data.lat,
                lng = data.lng
            )
        )

        ComparisonCameraScreen(
            viewModel = viewModel,
            onClose = onClose
        )
    }

    // Intent Extra Keys
    const val EXTRA_COMPOSED_PHOTO_PATH = "extra_composed_photo_path"
    const val EXTRA_ORIGINAL_PHOTO_PATH = "extra_original_photo_path"
}

/**
 * 相册模块对外 API
 *
 * 提供照片浏览和管理功能
 *
 * ## 使用方式
 *
 * ```kotlin
 * GalleryModule.openGallery(context)
 * ```
 */
object GalleryModule {

    /**
     * 打开相册 Activity
     *
     * @param context 上下文
     */
    fun openGallery(context: Context) {
        GalleryActivity.start(context)
    }

    /**
     * 方式2：Composable 组件（如需嵌入使用）
     *
     * 注意：目前 GalleryActivity 内部逻辑较复杂，
     * 建议直接使用 openGallery() 启动 Activity
     */
    // @Composable
    // fun GalleryRoute(onClose: () -> Unit) { ... }
}
