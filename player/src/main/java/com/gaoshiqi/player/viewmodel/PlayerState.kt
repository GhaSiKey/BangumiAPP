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
    val bufferedPosition: Long = 0L
)
