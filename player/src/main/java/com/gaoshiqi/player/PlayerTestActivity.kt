package com.gaoshiqi.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.gaoshiqi.player.ui.PlayerScreen
import com.gaoshiqi.player.ui.theme.PlayerTheme
import com.gaoshiqi.player.viewmodel.PlayerIntent
import com.gaoshiqi.player.viewmodel.PlayerViewModel

/**
 * 播放器测试页面
 * 用于开发测试视频播放功能
 */
class PlayerTestActivity : ComponentActivity() {

    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            PlayerViewModel.Factory(this)
        )[PlayerViewModel::class.java]

        // 处理外部传入的视频 URL
        intent.getStringExtra(EXTRA_VIDEO_URL)?.let { url ->
            viewModel.handleIntent(PlayerIntent.SetUrl(url))
            viewModel.handleIntent(PlayerIntent.Play)
        }

        setContent {
            PlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 页面不可见时暂停播放
        viewModel.handleIntent(PlayerIntent.Pause)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放播放器资源
        viewModel.handleIntent(PlayerIntent.Release)
    }

    companion object {
        private const val EXTRA_VIDEO_URL = "video_url"

        /**
         * 启动播放器页面
         */
        fun start(context: Context) {
            val intent = Intent(context, PlayerTestActivity::class.java)
            context.startActivity(intent)
        }

        /**
         * 启动播放器并播放指定视频
         * @param videoUrl 视频 URL
         * @param title 视频标题（预留参数，暂未使用）
         */
        fun start(context: Context, videoUrl: String, @Suppress("UNUSED_PARAMETER") title: String? = null) {
            val intent = Intent(context, PlayerTestActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, videoUrl)
            }
            context.startActivity(intent)
        }
    }
}
