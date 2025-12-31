package com.gaoshiqi.player.viewmodel

/**
 * 播放器用户意图
 */
sealed class PlayerIntent {
    /** 设置视频URL */
    data class SetUrl(val url: String) : PlayerIntent()

    /** 开始播放 */
    data object Play : PlayerIntent()

    /** 暂停播放 */
    data object Pause : PlayerIntent()

    /** 切换播放/暂停状态 */
    data object TogglePlayPause : PlayerIntent()

    /** 跳转到指定位置 */
    data class SeekTo(val position: Long) : PlayerIntent()

    /** 重试播放 */
    data object Retry : PlayerIntent()

    /** 停止并释放播放器 */
    data object Release : PlayerIntent()
}
