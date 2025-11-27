package com.gaoshiqi.otakumap.data.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by gaoshiqi
 * on 2025/5/31 22:51
 * email: gaoshiqi@bilibili.com
 */
data class BangumiDetail(
    val id: Int,
    val type: Int,
    val name: String,
    @SerializedName("name_cn") val nameCn: String,
    val summary: String,
    val nsfw: Boolean,
    val locked: Boolean,
    val date: String?,
    val platform: String?,
    val images: Images,
    val infobox: List<Map<String, Any>>?,
    val volumes: Int?,
    val eps: Int?,
    @SerializedName("total_episodes") val totalEpisodes: Int?,
    val rating: Rating?,
    val collection: Collection?,
    @SerializedName("meta_tags") val metaTags: List<String>?,
    val tags: List<Tag>?
) {
    data class Images(
        val large: String,
        val common: String,
        val medium: String,
        val small: String,
        val grid: String
    )

    data class Rating(
        val rank: Int,
        val total: Int,
        val count: Map<String, Int>,
        val score: Double
    )

    data class Collection(
        val wish: Int,
        val collect: Int,
        val doing: Int,
        @SerializedName("on_hold") val onHold: Int,
        val dropped: Int
    )

    data class Tag(
        val name: String,
        val count: Int
    )

    // 获取最佳标题显示：优先中文名，没有则用原名
    fun displayTitle(): String {
        return if (nameCn.isNotEmpty()) nameCn else name
    }
}
