package com.gaoshiqi.otakumap.detail.viewmodel

import com.gaoshiqi.otakumap.data.bean.BangumiDetail

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