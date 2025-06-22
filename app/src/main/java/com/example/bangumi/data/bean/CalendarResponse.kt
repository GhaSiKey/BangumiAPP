package com.example.bangumi.data.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by gaoshiqi
 * on 2025/5/30 10:58
 * email: gaoshiqi@bilibili.com
 */
// 根响应模型
data class CalendarResponse(
    val weekday: Weekday? = null,
    val items: List<SubjectSmall>? = null
)

// 星期模型
data class Weekday(
    val en: String? = null,
    val cn: String? = null,
    val ja: String? = null,
    val id: Int? = null
)

// 条目模型
data class SubjectSmall(
    val id: Int? = null,
    val url: String? = null,
    val type: Int? = null,
    val name: String? = null,
    @SerializedName("name_cn") val nameCn: String? = null,
    val summary: String? = null,
    @SerializedName("air_date") val airDate: String? = null,
    @SerializedName("air_weekday") val airWeekday: Int? = null,
    val images: Images? = null,
    val eps: Int? = null,
    @SerializedName("eps_count") val epsCount: Int? = null,
    val rating: Rating? = null,
    val rank: Int? = null,
    val collection: Collection? = null
)

data class Images(
    val large: String? = null,
    val common: String? = null,
    val medium: String? = null,
    val small: String? = null,
    val grid: String? = null,
)

data class Rating(
    val total: Int? = null,
    val count: Map<String, Int>? = null,
    val score: Double? = null
)

data class Collection(
    val wish: Int? = null,
    val collect: Int? = null,
    val doing: Int? = null,
    @SerializedName("on_hold") val onHold: Int? = null,
    val dropped: Int? = null
)
