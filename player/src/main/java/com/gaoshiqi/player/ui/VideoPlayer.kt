package com.gaoshiqi.player.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.gaoshiqi.player.R
import com.gaoshiqi.player.ui.theme.PlayerTheme

/**
 * 视频播放器组件
 * 使用AndroidView包装ExoPlayer的PlayerView
 */
@Composable
fun VideoPlayer(
    player: ExoPlayer?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val playerView = remember {
            PlayerView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = false // 使用自定义控制器
            }
        }

        DisposableEffect(player) {
            playerView.player = player
            onDispose {
                playerView.player = null
            }
        }

        AndroidView(
            factory = { playerView },
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * 视频播放器占位组件（用于Preview）
 */
@Composable
fun VideoPlayerPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_play),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(0.2f)
        )
    }
}

// ==================== Previews ====================

@Preview(showBackground = true, name = "Video Player Placeholder")
@Composable
private fun VideoPlayerPreview() {
    PlayerTheme {
        VideoPlayerPlaceholder()
    }
}
