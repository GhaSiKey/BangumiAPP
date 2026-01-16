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
        fun start(context: Context) {
            val intent = Intent(context, PlayerTestActivity::class.java)
            context.startActivity(intent)
        }
    }
}
