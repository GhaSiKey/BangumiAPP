package com.gaoshiqi.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaoshiqi.player.R
import com.gaoshiqi.player.ui.theme.PlayerTheme
import com.gaoshiqi.player.viewmodel.PlayerState

/**
 * 播放控制栏
 */
@Composable
fun PlayerControls(
    playerState: PlayerState,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 进度条
        ProgressSlider(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            enabled = playerState !is PlayerState.Idle && playerState !is PlayerState.Loading
        )

        // 控制按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 播放/暂停按钮
            when (playerState) {
                is PlayerState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressSlider(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember(currentPosition) {
        mutableFloatStateOf(currentPosition.toFloat())
    }
    var isDragging by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = if (isDragging > 0f) sliderPosition else currentPosition.toFloat(),
            onValueChange = { value ->
                isDragging = 1f
                sliderPosition = value
            },
            onValueChangeFinished = {
                onSeek(sliderPosition.toLong())
                isDragging = 0f
            },
            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
            enabled = enabled && duration > 0,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 时间显示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 格式化时间为 mm:ss 或 hh:mm:ss
 */
private fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

// ==================== Previews ====================

@Preview(showBackground = true, name = "Idle State")
@Composable
private fun PlayerControlsIdlePreview() {
    PlayerTheme {
        PlayerControls(
            playerState = PlayerState.Idle,
            isPlaying = false,
            currentPosition = 0L,
            duration = 0L,
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "Playing State")
@Composable
private fun PlayerControlsPlayingPreview() {
    PlayerTheme {
        PlayerControls(
            playerState = PlayerState.Playing(
                currentPosition = 65000L,
                duration = 180000L,
                bufferedPosition = 120000L
            ),
            isPlaying = true,
            currentPosition = 65000L,
            duration = 180000L,
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "Paused State")
@Composable
private fun PlayerControlsPausedPreview() {
    PlayerTheme {
        PlayerControls(
            playerState = PlayerState.Paused(
                currentPosition = 65000L,
                duration = 180000L
            ),
            isPlaying = false,
            currentPosition = 65000L,
            duration = 180000L,
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun PlayerControlsLoadingPreview() {
    PlayerTheme {
        PlayerControls(
            playerState = PlayerState.Loading,
            isPlaying = false,
            currentPosition = 0L,
            duration = 0L,
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}
