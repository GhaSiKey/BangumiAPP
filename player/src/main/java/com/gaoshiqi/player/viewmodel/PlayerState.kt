package com.gaoshiqi.player.viewmodel

/**
 * 播放器状态
 */
sealed class PlayerState {
    /** 空闲状态，等待输入URL */
    data object Idle : PlayerState()

    /** 加载中 */
    data object Loading : PlayerState()

    /** 播放中 */
    data class Playing(
        val currentPosition: Long = 0L,
        val duration: Long = 0L,
        val bufferedPosition: Long = 0L
    ) : PlayerState()

    /** 暂停 */
    data class Paused(
        val currentPosition: Long = 0L,
        val duration: Long = 0L
    ) : PlayerState()

    /** 错误 */
    data class Error(val message: String) : PlayerState()
}

/**
 * 播放器UI状态（用于Compose）
 */
data class PlayerUiState(
    val playerState: PlayerState = PlayerState.Idle,
    val videoUrl: String = "",
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val logs: List<PlayerLog> = emptyList(),
    // 网络统计
    val networkStats: NetworkStats = NetworkStats()
)

/**
 * 网络统计信息
 */
data class NetworkStats(
    val bandwidthEstimate: Long = 0L,        // 带宽估算 (bps)
    val totalBytesLoaded: Long = 0L,         // 总下载字节数
    val videoSize: VideoSizeInfo = VideoSizeInfo(),  // 视频尺寸
    val videoBitrate: Long = 0L,             // 视频码率
    val audioBitrate: Long = 0L              // 音频码率
) {
    /** 格式化带宽为可读字符串 */
    fun formatBandwidth(): String {
        if (bandwidthEstimate <= 0) return "N/A"
        val mbps = bandwidthEstimate / 1_000_000.0
        val kbps = bandwidthEstimate / 1_000.0
        return when {
            mbps >= 1 -> String.format("%.2f Mbps", mbps)
            kbps >= 1 -> String.format("%.0f Kbps", kbps)
            else -> "$bandwidthEstimate bps"
        }
    }

    /** 格式化下载量 */
    fun formatBytesLoaded(): String {
        if (totalBytesLoaded <= 0) return "0 B"
        val mb = totalBytesLoaded / (1024.0 * 1024.0)
        val kb = totalBytesLoaded / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$totalBytesLoaded B"
        }
    }

    /** 格式化码率 */
    fun formatBitrate(bitrate: Long): String {
        if (bitrate <= 0) return "N/A"
        val mbps = bitrate / 1_000_000.0
        val kbps = bitrate / 1_000.0
        return when {
            mbps >= 1 -> String.format("%.2f Mbps", mbps)
            else -> String.format("%.0f Kbps", kbps)
        }
    }
}

/**
 * 视频尺寸信息
 */
data class VideoSizeInfo(
    val width: Int = 0,
    val height: Int = 0
) {
    fun format(): String {
        if (width == 0 || height == 0) return "N/A"
        val quality = when {
            height >= 2160 -> "4K"
            height >= 1440 -> "2K"
            height >= 1080 -> "1080P"
            height >= 720 -> "720P"
            height >= 480 -> "480P"
            else -> "SD"
        }
        return "${width}x${height} ($quality)"
    }
}

/**
 * 播放器日志
 */
data class PlayerLog(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: LogTag,
    val message: String
) {
    enum class LogTag {
        LIFECYCLE,      // 生命周期
        PLAYBACK,       // 播放状态
        MEDIA,          // 媒体信息
        NETWORK,        // 网络/缓冲
        ERROR,          // 错误
        USER_ACTION     // 用户操作
    }
}
