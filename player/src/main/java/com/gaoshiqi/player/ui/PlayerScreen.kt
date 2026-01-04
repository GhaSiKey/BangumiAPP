package com.gaoshiqi.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gaoshiqi.player.viewmodel.NetworkStats
import com.gaoshiqi.player.viewmodel.PlayerIntent
import com.gaoshiqi.player.viewmodel.PlayerLog
import com.gaoshiqi.player.viewmodel.PlayerState
import com.gaoshiqi.player.viewmodel.PlayerUiState
import com.gaoshiqi.player.viewmodel.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 测试视频源
 */
private data class TestVideoSource(
    val name: String,
    val url: String
)

private val testVideoSources = listOf(
    TestVideoSource("芙莉莲", "https://fe-video-qc.xhscdn.com/athena-creator/1040g0pg3104o5f8u5q5g5pebdah3cnu7o5c94v8?filename=1.mp4"),
    TestVideoSource("间谍过家家", "https://sns-video-hw.xhscdn.com/spectrum/1040g0jg31n7jtmot4u005p2qf72k4m7r92mijqo"),
    TestVideoSource("鬼灭之刃", "https://hn.bfvvs.com/play/b688Ynle/index.m3u8"),
    TestVideoSource("Big Buck Bunny", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
)

/**
 * 播放器主界面
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

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
                .verticalScroll(scrollState)
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

            // 网络统计信息
            NetworkStatsSection(
                networkStats = uiState.networkStats,
                isVisible = uiState.playerState is PlayerState.Playing ||
                        uiState.playerState is PlayerState.Paused ||
                        uiState.playerState is PlayerState.Loading,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            // 测试视频快捷按钮
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "快捷测试",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                testVideoSources.forEach { source ->
                    FilledTonalButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.handleIntent(PlayerIntent.SetUrl(source.url))
                            viewModel.handleIntent(PlayerIntent.Play)
                        }
                    ) {
                        Text(source.name)
                    }
                }
            }

            // 日志展示区域
            Spacer(modifier = Modifier.height(24.dp))
            PlayerLogSection(
                logs = uiState.logs,
                onClearLogs = { viewModel.clearLogs() },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 日志展示区域
 */
@Composable
private fun PlayerLogSection(
    logs: List<PlayerLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    // 新日志时自动滚动到顶部
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "ExoPlayer 日志 (${logs.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClearLogs) {
                Text("清除")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "暂无日志\n点击播放按钮开始",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(logs) { log ->
                        LogItem(log = log, timeFormat = timeFormat)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(
    log: PlayerLog,
    timeFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    val tagColor = when (log.tag) {
        PlayerLog.LogTag.LIFECYCLE -> MaterialTheme.colorScheme.primary
        PlayerLog.LogTag.PLAYBACK -> MaterialTheme.colorScheme.secondary
        PlayerLog.LogTag.MEDIA -> MaterialTheme.colorScheme.tertiary
        PlayerLog.LogTag.NETWORK -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        PlayerLog.LogTag.ERROR -> MaterialTheme.colorScheme.error
        PlayerLog.LogTag.USER_ACTION -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
    }

    val tagName = when (log.tag) {
        PlayerLog.LogTag.LIFECYCLE -> "生命周期"
        PlayerLog.LogTag.PLAYBACK -> "播放"
        PlayerLog.LogTag.MEDIA -> "媒体"
        PlayerLog.LogTag.NETWORK -> "网络"
        PlayerLog.LogTag.ERROR -> "错误"
        PlayerLog.LogTag.USER_ACTION -> "用户"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Top
    ) {
        // 时间戳
        Text(
            text = timeFormat.format(Date(log.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.width(85.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Tag标签
        Box(
            modifier = Modifier
                .background(tagColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = tagName,
                style = MaterialTheme.typography.labelSmall,
                color = tagColor
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 消息内容
        Text(
            text = log.message,
            style = MaterialTheme.typography.bodySmall,
            color = if (log.tag == PlayerLog.LogTag.ERROR) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
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
 * 网络统计信息区域
 */
@Composable
private fun NetworkStatsSection(
    networkStats: NetworkStats,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "网络统计",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左列
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(
                        label = "网速",
                        value = networkStats.formatBandwidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatItem(
                        label = "已下载",
                        value = networkStats.formatBytesLoaded()
                    )
                }

                // 右列
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(
                        label = "分辨率",
                        value = networkStats.videoSize.format()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatItem(
                        label = "视频码率",
                        value = networkStats.formatBitrate(networkStats.videoBitrate)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 播放器内容（用于Preview，不依赖ViewModel）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerScreenContent(
    uiState: PlayerUiState,
    onUrlChange: (String) -> Unit,
    onPlayClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onTestVideoClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

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
                .verticalScroll(scrollState)
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

            // 测试视频快捷按钮
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "快捷测试",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                testVideoSources.forEach { source ->
                    FilledTonalButton(
                        onClick = { onTestVideoClick(source.url) }
                    ) {
                        Text(source.name)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
