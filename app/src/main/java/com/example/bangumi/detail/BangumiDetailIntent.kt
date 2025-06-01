package com.example.bangumi.detail

/**
 * Created by gaoshiqi
 * on 2025/6/1 17:22
 * email: gaoshiqi@bilibili.com
 */
sealed class BangumiDetailIntent {
    object LoadBangumiDetail : BangumiDetailIntent()
    object Retry: BangumiDetailIntent()
}