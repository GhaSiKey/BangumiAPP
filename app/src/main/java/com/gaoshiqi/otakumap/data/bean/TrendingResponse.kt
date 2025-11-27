package com.gaoshiqi.otakumap.data.bean

data class TrendingResponse(
    val data: List<TrendingSubjectItem>,
    val total: Int
)

data class TrendingSubjectItem(
    val subject: TrendingSubject,
    val count: Int
)

data class TrendingSubject(
    val id: Int,
    val name: String,
    val nameCN: String,
    val type: Int,
    val info: String,
    val rating: TrendingRate,
    val locked: Boolean,
    val nsfw: Boolean,
    val images: Images
)

data class TrendingRate(
    val rank: Int,
    val count: List<Int>,
    val score: Double,
    val total: Int
)