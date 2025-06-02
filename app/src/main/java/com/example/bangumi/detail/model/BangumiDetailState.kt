package com.example.bangumi.detail.model

import com.example.bangumi.data.model.BangumiDetail

/**
 * Created by gaoshiqi
 * on 2025/6/1 17:20
 * email: gaoshiqi@bilibili.com
 */
sealed class BangumiDetailState {
    object IDLE : BangumiDetailState()
    object LOADING : BangumiDetailState()
    data class SUCCESS(val data: BangumiDetail) : BangumiDetailState()
    data class ERROR(val msg: String) : BangumiDetailState()
}