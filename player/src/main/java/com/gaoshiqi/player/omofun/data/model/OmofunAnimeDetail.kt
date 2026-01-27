package com.gaoshiqi.player.omofun.data.model

/**
 * Omofun 番剧详情
 */
data class OmofunAnimeDetail(
    /** 番剧标题 */
    val title: String,
    /** 封面图片 URL */
    val coverUrl: String?,
    /** 番剧简介 */
    val description: String?,
    /** 播放列表 (可能有多个线路) */
    val playlists: List<OmofunPlaylist>
)

/**
 * 播放列表 (一个线路)
 */
data class OmofunPlaylist(
    /** 线路名称 (如：线路一、备用线路) */
    val name: String,
    /** 该线路下的所有集数 */
    val episodes: List<OmofunEpisode>
)
