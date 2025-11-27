package com.gaoshiqi.otakumap.detail.viewmodel

/**
 * Created by gaoshiqi
 * on 2025/7/17 17:30
 * email: gaoshiqi@bilibili.com
 */
sealed class CommentsIntent {
    object Refresh : CommentsIntent() // 下拉刷新
    object LoadMore : CommentsIntent() // 上拉加载更多
}