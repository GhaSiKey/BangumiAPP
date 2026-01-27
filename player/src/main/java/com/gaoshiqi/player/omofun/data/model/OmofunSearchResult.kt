package com.gaoshiqi.player.omofun.data.model

/**
 * Omofun 搜索结果
 */
data class OmofunSearchResult(
    /** 番剧标题 */
    val title: String,
    /** 封面图片 URL */
    val coverUrl: String?,
    /** 详情页 URL */
    val detailUrl: String,
    /** 年份 */
    val year: String?,
    /** 状态 (如：连载中、已完结) */
    val status: String?
)
