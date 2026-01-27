package com.gaoshiqi.player.viewmodel

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerViewModel(
    private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "ExoPlayer"
        private const val MAX_LOGS = 100
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    // 网络统计相关
    private var totalBytesLoaded: Long = 0L
    private var lastBandwidthEstimate: Long = 0L

    private fun log(tag: PlayerLog.LogTag, message: String) {
        Log.d(TAG, "[${tag.name}] $message")
        _uiState.update { state ->
            val newLogs = (listOf(PlayerLog(tag = tag, message = message)) + state.logs)
                .take(MAX_LOGS)
            state.copy(logs = newLogs)
        }
    }

    private val playerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "STATE_IDLE"
                Player.STATE_BUFFERING -> "STATE_BUFFERING"
                Player.STATE_READY -> "STATE_READY"
                Player.STATE_ENDED -> "STATE_ENDED"
                else -> "UNKNOWN($playbackState)"
            }
            log(PlayerLog.LogTag.PLAYBACK, "onPlaybackStateChanged: $stateName")

            when (playbackState) {
                Player.STATE_IDLE -> {
                    _uiState.update { it.copy(playerState = PlayerState.Idle) }
                }
                Player.STATE_BUFFERING -> {
                    _uiState.update { it.copy(playerState = PlayerState.Loading) }
                }
                Player.STATE_READY -> {
                    val player = exoPlayer ?: return
                    val duration = player.duration.coerceAtLeast(0L)
                    log(PlayerLog.LogTag.MEDIA, "视频时长: ${formatDuration(duration)}")

                    if (player.isPlaying) {
                        _uiState.update {
                            it.copy(
                                playerState = PlayerState.Playing(
                                    currentPosition = player.currentPosition,
                                    duration = duration,
                                    bufferedPosition = player.bufferedPosition
                                ),
                                isPlaying = true,
                                duration = duration
                            )
                        }
                        startProgressUpdate()
                    } else {
                        _uiState.update {
                            it.copy(
                                playerState = PlayerState.Paused(
                                    currentPosition = player.currentPosition,
                                    duration = duration
                                ),
                                isPlaying = false,
                                duration = duration
                            )
                        }
                        stopProgressUpdate()
                    }
                }
                Player.STATE_ENDED -> {
                    log(PlayerLog.LogTag.PLAYBACK, "播放结束")
                    _uiState.update {
                        it.copy(
                            playerState = PlayerState.Paused(
                                currentPosition = it.duration,
                                duration = it.duration
                            ),
                            isPlaying = false,
                            currentPosition = it.duration
                        )
                    }
                    stopProgressUpdate()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            log(PlayerLog.LogTag.PLAYBACK, "onIsPlayingChanged: $isPlaying")

            val player = exoPlayer ?: return
            _uiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    playerState = if (isPlaying) {
                        PlayerState.Playing(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0L),
                            bufferedPosition = player.bufferedPosition
                        )
                    } else {
                        PlayerState.Paused(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0L)
                        )
                    }
                )
            }
            if (isPlaying) {
                startProgressUpdate()
            } else {
                stopProgressUpdate()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val errorCode = error.errorCode
            val errorCodeName = when (errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "网络连接失败"
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "网络连接超时"
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "HTTP状态错误"
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "文件未找到"
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> "清单解析失败"
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "容器格式不支持"
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "解码器初始化失败"
                PlaybackException.ERROR_CODE_DECODING_FAILED -> "解码失败"
                else -> "错误码: $errorCode"
            }
            log(PlayerLog.LogTag.ERROR, "onPlayerError: $errorCodeName - ${error.message}")

            _uiState.update {
                it.copy(
                    playerState = PlayerState.Error(
                        error.localizedMessage ?: "播放出错"
                    ),
                    isPlaying = false
                )
            }
            stopProgressUpdate()
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            log(PlayerLog.LogTag.MEDIA, "onVideoSizeChanged: ${videoSize.width}x${videoSize.height}")
        }

        override fun onTracksChanged(tracks: Tracks) {
            val trackInfo = buildString {
                tracks.groups.forEachIndexed { index, group ->
                    val trackType = when (group.type) {
                        androidx.media3.common.C.TRACK_TYPE_VIDEO -> "视频"
                        androidx.media3.common.C.TRACK_TYPE_AUDIO -> "音频"
                        androidx.media3.common.C.TRACK_TYPE_TEXT -> "字幕"
                        else -> "其他"
                    }
                    append("Track$index: $trackType (${group.length}个) ")
                }
            }
            log(PlayerLog.LogTag.MEDIA, "onTracksChanged: $trackInfo")
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            val reasonName = when (reason) {
                Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> "SOURCE_UPDATE"
                Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> "PLAYLIST_CHANGED"
                else -> "UNKNOWN($reason)"
            }
            log(PlayerLog.LogTag.MEDIA, "onTimelineChanged: $reasonName, windowCount=${timeline.windowCount}")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val reasonName = when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> "AUTO"
                Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> "SEEK"
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> "PLAYLIST_CHANGED"
                Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> "REPEAT"
                else -> "UNKNOWN($reason)"
            }
            log(PlayerLog.LogTag.MEDIA, "onMediaItemTransition: $reasonName, uri=${mediaItem?.localConfiguration?.uri}")
        }

        override fun onRenderedFirstFrame() {
            log(PlayerLog.LogTag.PLAYBACK, "onRenderedFirstFrame: 首帧渲染完成")
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            val reasonName = when (reason) {
                Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> "AUTO_TRANSITION"
                Player.DISCONTINUITY_REASON_SEEK -> "SEEK"
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> "SEEK_ADJUSTMENT"
                Player.DISCONTINUITY_REASON_SKIP -> "SKIP"
                Player.DISCONTINUITY_REASON_REMOVE -> "REMOVE"
                Player.DISCONTINUITY_REASON_INTERNAL -> "INTERNAL"
                else -> "UNKNOWN($reason)"
            }
            log(PlayerLog.LogTag.PLAYBACK, "onPositionDiscontinuity: $reasonName, ${formatDuration(oldPosition.positionMs)} -> ${formatDuration(newPosition.positionMs)}")
        }
    }

    /**
     * AnalyticsListener 用于获取网络统计信息
     */
    private val analyticsListener = object : AnalyticsListener {
        override fun onBandwidthEstimate(
            eventTime: AnalyticsListener.EventTime,
            totalLoadTimeMs: Int,
            totalBytesLoaded: Long,
            bitrateEstimate: Long
        ) {
            this@PlayerViewModel.totalBytesLoaded = totalBytesLoaded
            lastBandwidthEstimate = bitrateEstimate
            log(
                PlayerLog.LogTag.NETWORK,
                "带宽估算: ${formatBitrate(bitrateEstimate)}, 已下载: ${formatBytes(totalBytesLoaded)}"
            )
        }

        override fun onLoadCompleted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            val dataType = when (mediaLoadData.dataType) {
                C.DATA_TYPE_MEDIA -> "媒体"
                C.DATA_TYPE_MANIFEST -> "清单"
                C.DATA_TYPE_DRM -> "DRM"
                C.DATA_TYPE_AD -> "广告"
                else -> "其他"
            }
            val bytesLoaded = loadEventInfo.bytesLoaded
            val loadDurationMs = loadEventInfo.loadDurationMs
            val speed = if (loadDurationMs > 0) {
                (bytesLoaded * 8 * 1000 / loadDurationMs) // bps
            } else 0L

            log(
                PlayerLog.LogTag.NETWORK,
                "加载完成[$dataType]: ${formatBytes(bytesLoaded)}, 耗时${loadDurationMs}ms, 速度${formatBitrate(speed)}"
            )
        }

        override fun onVideoSizeChanged(
            eventTime: AnalyticsListener.EventTime,
            videoSize: VideoSize
        ) {
            _uiState.update { state ->
                state.copy(
                    networkStats = state.networkStats.copy(
                        videoSize = VideoSizeInfo(videoSize.width, videoSize.height)
                    )
                )
            }
        }

        override fun onTracksChanged(
            eventTime: AnalyticsListener.EventTime,
            tracks: Tracks
        ) {
            var videoBitrate = 0L
            var audioBitrate = 0L

            tracks.groups.forEach { group ->
                if (group.isSelected) {
                    for (i in 0 until group.length) {
                        if (group.isTrackSelected(i)) {
                            val format = group.getTrackFormat(i)
                            when (group.type) {
                                C.TRACK_TYPE_VIDEO -> {
                                    videoBitrate = format.bitrate.toLong().coerceAtLeast(0L)
                                }
                                C.TRACK_TYPE_AUDIO -> {
                                    audioBitrate = format.bitrate.toLong().coerceAtLeast(0L)
                                }
                            }
                        }
                    }
                }
            }

            _uiState.update { state ->
                state.copy(
                    networkStats = state.networkStats.copy(
                        videoBitrate = videoBitrate,
                        audioBitrate = audioBitrate
                    )
                )
            }
        }
    }

    fun handleIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.SetUrl -> setVideoUrl(intent.url)
            is PlayerIntent.Play -> play()
            is PlayerIntent.Pause -> pause()
            is PlayerIntent.TogglePlayPause -> togglePlayPause()
            is PlayerIntent.SeekTo -> seekTo(intent.position)
            is PlayerIntent.Retry -> retry()
            is PlayerIntent.Release -> release()
        }
    }

    private fun setVideoUrl(url: String) {
        log(PlayerLog.LogTag.USER_ACTION, "设置URL: ${url.take(50)}...")
        _uiState.update { it.copy(videoUrl = url) }
    }

    private fun play() {
        val url = _uiState.value.videoUrl
        if (url.isBlank()) {
            log(PlayerLog.LogTag.ERROR, "URL为空")
            _uiState.update {
                it.copy(playerState = PlayerState.Error("请输入视频地址"))
            }
            return
        }

        log(PlayerLog.LogTag.USER_ACTION, "开始播放")
        _uiState.update { it.copy(playerState = PlayerState.Loading) }

        if (exoPlayer == null) {
            log(PlayerLog.LogTag.LIFECYCLE, "创建ExoPlayer实例")
            // 重置网络统计
            totalBytesLoaded = 0L
            lastBandwidthEstimate = 0L
            _uiState.update { it.copy(networkStats = NetworkStats()) }

            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
                addAnalyticsListener(analyticsListener)
                log(PlayerLog.LogTag.LIFECYCLE, "添加Player.Listener和AnalyticsListener")
            }
        }

        exoPlayer?.apply {
            log(PlayerLog.LogTag.MEDIA, "创建MediaItem")
            val mediaItem = MediaItem.fromUri(url)

            log(PlayerLog.LogTag.LIFECYCLE, "setMediaItem")
            setMediaItem(mediaItem)

            log(PlayerLog.LogTag.LIFECYCLE, "prepare() - 开始准备媒体源")
            prepare()

            log(PlayerLog.LogTag.LIFECYCLE, "playWhenReady = true")
            playWhenReady = true
        }
    }

    private fun pause() {
        log(PlayerLog.LogTag.USER_ACTION, "暂停播放")
        exoPlayer?.pause()
    }

    private fun togglePlayPause() {
        val player = exoPlayer
        if (player == null) {
            play()
            return
        }
        if (player.isPlaying) {
            log(PlayerLog.LogTag.USER_ACTION, "切换: 播放 -> 暂停")
            player.pause()
        } else {
            log(PlayerLog.LogTag.USER_ACTION, "切换: 暂停 -> 播放")
            player.play()
        }
    }

    private fun seekTo(position: Long) {
        log(PlayerLog.LogTag.USER_ACTION, "Seek到: ${formatDuration(position)}")
        exoPlayer?.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    private fun retry() {
        log(PlayerLog.LogTag.USER_ACTION, "重试播放")
        release()
        play()
    }

    private fun release() {
        log(PlayerLog.LogTag.LIFECYCLE, "释放播放器资源")
        stopProgressUpdate()
        exoPlayer?.apply {
            log(PlayerLog.LogTag.LIFECYCLE, "移除Listeners")
            removeListener(playerListener)
            removeAnalyticsListener(analyticsListener)
            log(PlayerLog.LogTag.LIFECYCLE, "release()")
            release()
        }
        exoPlayer = null
        _uiState.update {
            it.copy(
                playerState = PlayerState.Idle,
                isPlaying = false,
                currentPosition = 0L,
                duration = 0L,
                networkStats = NetworkStats()
            )
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _uiState.update { state ->
                        state.copy(
                            currentPosition = player.currentPosition,
                            bufferedPosition = player.bufferedPosition,
                            networkStats = state.networkStats.copy(
                                bandwidthEstimate = lastBandwidthEstimate,
                                totalBytesLoaded = totalBytesLoaded
                            )
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    override fun onCleared() {
        log(PlayerLog.LogTag.LIFECYCLE, "ViewModel.onCleared()")
        super.onCleared()
        release()
    }

    private fun formatDuration(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 1000 / 60) % 60
        val hours = ms / 1000 / 3600
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun formatBitrate(bps: Long): String {
        if (bps <= 0) return "N/A"
        val mbps = bps / 1_000_000.0
        val kbps = bps / 1_000.0
        return when {
            mbps >= 1 -> String.format("%.2f Mbps", mbps)
            kbps >= 1 -> String.format("%.0f Kbps", kbps)
            else -> "$bps bps"
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val mb = bytes / (1024.0 * 1024.0)
        val kb = bytes / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
