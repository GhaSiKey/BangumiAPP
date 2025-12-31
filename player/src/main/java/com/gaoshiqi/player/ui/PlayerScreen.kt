package com.gaoshiqi.player.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaoshiqi.player.ui.theme.PlayerTheme
import com.gaoshiqi.player.viewmodel.PlayerIntent
import com.gaoshiqi.player.viewmodel.PlayerState
import com.gaoshiqi.player.viewmodel.PlayerUiState
import com.gaoshiqi.player.viewmodel.PlayerViewModel

/**
 * 播放器主界面
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 错误提示
    LaunchedEffect(uiState.playerState) {
        if (uiState.playerState is PlayerState.Error) {
            snackbarHostState.showSnackbar(
                (uiState.playerState as PlayerState.Error).message
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 视频播放器区域
            VideoPlayer(
                player = viewModel.getPlayer(),
                modifier = Modifier.fillMaxWidth()
            )

            // 播放控制栏
            PlayerControls(
                playerState = uiState.playerState,
                isPlaying = uiState.isPlaying,
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                onPlayPauseClick = {
                    viewModel.handleIntent(PlayerIntent.TogglePlayPause)
                },
                onSeek = { position ->
                    viewModel.handleIntent(PlayerIntent.SeekTo(position))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // URL输入区域
            UrlInputSection(
                url = uiState.videoUrl,
                onUrlChange = { url ->
                    viewModel.handleIntent(PlayerIntent.SetUrl(url))
                },
                onPlayClick = {
                    keyboardController?.hide()
                    viewModel.handleIntent(PlayerIntent.Play)
                },
                isLoading = uiState.playerState is PlayerState.Loading,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 状态显示
            Spacer(modifier = Modifier.height(16.dp))
            PlayerStateInfo(
                playerState = uiState.playerState,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun UrlInputSection(
    url: String,
    onUrlChange: (String) -> Unit,
    onPlayClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("视频地址") },
            placeholder = { Text("输入视频URL...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onPlayClick() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onPlayClick,
            enabled = !isLoading && url.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "加载中..." else "播放")
        }
    }
}

@Composable
private fun PlayerStateInfo(
    playerState: PlayerState,
    modifier: Modifier = Modifier
) {
    val stateText = when (playerState) {
        is PlayerState.Idle -> "等待输入视频地址"
        is PlayerState.Loading -> "加载中..."
        is PlayerState.Playing -> "播放中"
        is PlayerState.Paused -> "已暂停"
        is PlayerState.Error -> "错误: ${playerState.message}"
    }

    Text(
        text = "状态: $stateText",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * 播放器内容（用于Preview，不依赖ViewModel）
 */
@Composable
fun PlayerScreenContent(
    uiState: PlayerUiState,
    onUrlChange: (String) -> Unit,
    onPlayClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.playerState) {
        if (uiState.playerState is PlayerState.Error) {
            snackbarHostState.showSnackbar(
                (uiState.playerState as PlayerState.Error).message
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 视频播放器占位区域（Preview用）
            VideoPlayerPlaceholder(
                modifier = Modifier.fillMaxWidth()
            )

            // 播放控制栏
            PlayerControls(
                playerState = uiState.playerState,
                isPlaying = uiState.isPlaying,
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                onPlayPauseClick = onPlayPauseClick,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(16.dp))

            // URL输入区域
            UrlInputSection(
                url = uiState.videoUrl,
                onUrlChange = onUrlChange,
                onPlayClick = onPlayClick,
                isLoading = uiState.playerState is PlayerState.Loading,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 状态显示
            Spacer(modifier = Modifier.height(16.dp))
            PlayerStateInfo(
                playerState = uiState.playerState,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ==================== Previews ====================

@Preview(showBackground = true, name = "Idle State")
@Composable
private fun PlayerScreenIdlePreview() {
    PlayerTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(),
            onUrlChange = {},
            onPlayClick = {},
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "With URL Input")
@Composable
private fun PlayerScreenWithUrlPreview() {
    PlayerTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(
                videoUrl = "https://example.com/video.mp4"
            ),
            onUrlChange = {},
            onPlayClick = {},
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "Playing State")
@Composable
private fun PlayerScreenPlayingPreview() {
    PlayerTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(
                playerState = PlayerState.Playing(
                    currentPosition = 65000L,
                    duration = 180000L,
                    bufferedPosition = 120000L
                ),
                videoUrl = "https://example.com/video.mp4",
                isPlaying = true,
                currentPosition = 65000L,
                duration = 180000L
            ),
            onUrlChange = {},
            onPlayClick = {},
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun PlayerScreenLoadingPreview() {
    PlayerTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(
                playerState = PlayerState.Loading,
                videoUrl = "https://example.com/video.mp4"
            ),
            onUrlChange = {},
            onPlayClick = {},
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun PlayerScreenErrorPreview() {
    PlayerTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(
                playerState = PlayerState.Error("无法加载视频，请检查网络连接"),
                videoUrl = "https://example.com/video.mp4"
            ),
            onUrlChange = {},
            onPlayClick = {},
            onPlayPauseClick = {},
            onSeek = {}
        )
    }
}
