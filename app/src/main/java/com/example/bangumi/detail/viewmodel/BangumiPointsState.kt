package com.example.bangumi.detail.viewmodel

import com.example.map.data.LitePoint

/**
 * Created by gaoshiqi
 * on 2025/6/22 21:07
 * email: gaoshiqi@bilibili.com
 */
sealed class BangumiPointsState {
    object LOADING: BangumiPointsState()
    data class SUCCESS(val data: List<LitePoint>): BangumiPointsState()
    data class ERROR(val msg: String): BangumiPointsState()
}