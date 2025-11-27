package com.gaoshiqi.otakumap.detail.viewmodel

import com.gaoshiqi.otakumap.data.bean.BangumiCharacter

/**
 * Created by gaoshiqi
 * on 2025/6/9 18:57
 * email: gaoshiqi@bilibili.com
 */
sealed class BangumiCharacterState {
    object LOADING: BangumiCharacterState()
    data class SUCCESS(val data: List<BangumiCharacter>): BangumiCharacterState()
    data class ERROR(val msg: String): BangumiCharacterState()
}