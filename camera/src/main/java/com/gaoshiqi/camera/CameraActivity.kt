package com.gaoshiqi.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaoshiqi.camera.ui.CameraScreen
import com.gaoshiqi.camera.viewmodel.CameraViewModel

class CameraActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CameraActivity::class.java)
            context.startActivity(intent)
        }

        /**
         * 预热相机 Provider，可在应用启动时调用以加速相机打开
         */
        fun warmUp(context: Context) {
            ProcessCameraProvider.getInstance(context.applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 尽早开始初始化 CameraProvider（异步）
        val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: CameraViewModel = viewModel(
                        factory = CameraViewModel.Factory(applicationContext)
                    )

                    CameraScreen(
                        viewModel = viewModel,
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}
