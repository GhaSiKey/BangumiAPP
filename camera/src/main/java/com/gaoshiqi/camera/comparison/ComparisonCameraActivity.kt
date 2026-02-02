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

        /**
         * 启动对比拍照页面
         *
         * @param context 上下文
         * @param referenceImageUrl 参考图片 URL
         * @param pointName 圣地名称
         * @param subjectName 番剧名称
         */
        fun start(
            context: Context,
            referenceImageUrl: String,
            pointName: String,
            subjectName: String
        ) {
            val intent = Intent(context, ComparisonCameraActivity::class.java).apply {
                putExtra(EXTRA_REFERENCE_IMAGE_URL, referenceImageUrl)
                putExtra(EXTRA_POINT_NAME, pointName)
                putExtra(EXTRA_SUBJECT_NAME, subjectName)
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
                            subjectName = subjectName
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
