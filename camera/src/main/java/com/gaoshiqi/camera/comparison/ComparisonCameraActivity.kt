package com.gaoshiqi.camera.comparison

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaoshiqi.camera.R
import com.gaoshiqi.camera.comparison.ui.ComparisonCameraScreen
import com.gaoshiqi.camera.comparison.viewmodel.CaptureState
import com.gaoshiqi.camera.comparison.viewmodel.ComparisonCameraViewModel

/**
 * 对比拍照 Activity
 * 用于拍摄圣地巡礼对比照片
 */
class ComparisonCameraActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_REFERENCE_IMAGE_URL = "reference_image_url"
        private const val EXTRA_POINT_NAME = "point_name"
        private const val EXTRA_SUBJECT_NAME = "subject_name"
        private const val EXTRA_SUBJECT_COVER = "subject_cover"
        private const val EXTRA_EPISODE = "episode"
        private const val EXTRA_LAT = "lat"
        private const val EXTRA_LNG = "lng"

        /**
         * 启动对比拍照页面
         *
         * @param context 上下文
         * @param referenceImageUrl 参考图片 URL
         * @param pointName 圣地名称
         * @param subjectName 番剧名称
         * @param subjectCover 番剧封面 URL（用于拍立得风格合成）
         * @param episode 出现集数（可选）
         * @param lat 纬度
         * @param lng 经度
         */
        fun start(
            context: Context,
            referenceImageUrl: String,
            pointName: String,
            subjectName: String,
            subjectCover: String,
            episode: String?,
            lat: Double,
            lng: Double
        ) {
            val intent = Intent(context, ComparisonCameraActivity::class.java).apply {
                putExtra(EXTRA_REFERENCE_IMAGE_URL, referenceImageUrl)
                putExtra(EXTRA_POINT_NAME, pointName)
                putExtra(EXTRA_SUBJECT_NAME, subjectName)
                putExtra(EXTRA_SUBJECT_COVER, subjectCover)
                putExtra(EXTRA_EPISODE, episode)
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val referenceImageUrl = intent.getStringExtra(EXTRA_REFERENCE_IMAGE_URL) ?: run {
            Toast.makeText(this, "Missing reference image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val pointName = intent.getStringExtra(EXTRA_POINT_NAME) ?: ""
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: ""
        val subjectCover = intent.getStringExtra(EXTRA_SUBJECT_COVER) ?: ""
        val episode = intent.getStringExtra(EXTRA_EPISODE)
        val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        val lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ComparisonCameraViewModel = viewModel(
                        factory = ComparisonCameraViewModel.Factory(
                            context = applicationContext,
                            referenceImageUrl = referenceImageUrl,
                            pointName = pointName,
                            subjectName = subjectName,
                            subjectCover = subjectCover,
                            episode = episode,
                            lat = lat,
                            lng = lng
                        )
                    )

                    val uiState by viewModel.uiState.collectAsState()

                    // 显示保存成功提示
                    LaunchedEffect(uiState.captureState) {
                        if (uiState.captureState is CaptureState.Saved) {
                            Toast.makeText(
                                this@ComparisonCameraActivity,
                                R.string.comparison_photo_saved,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    ComparisonCameraScreen(
                        viewModel = viewModel,
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}
