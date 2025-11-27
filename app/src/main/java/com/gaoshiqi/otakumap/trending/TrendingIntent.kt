package com.gaoshiqi.otakumap.trending

sealed class TrendingIntent {
    object Refresh : TrendingIntent()   // 下拉刷新
    object LoadMore : TrendingIntent()   // 上拉加载更多
}